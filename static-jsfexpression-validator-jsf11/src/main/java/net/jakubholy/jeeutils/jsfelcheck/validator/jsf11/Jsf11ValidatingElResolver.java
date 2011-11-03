/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11;

import com.sun.faces.el.MethodBindingImpl;
import net.jakubholy.jeeutils.jsfelcheck.validator.AttributeInfo;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.PredefinedVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidationResultHelper;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.BaseEvaluationException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InternalValidatorFailureException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException;
import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.ElBindingFactory;
import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.impl.Sun11_02ElBindingFactoryImpl;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

/**
 * {@inheritDoc}
 *
 * JSF 1.1 implementation.
 */
public class Jsf11ValidatingElResolver implements ValidatingElResolver {

	private static final Logger LOG = Logger.getLogger(Jsf11ValidatingElResolver.class.getName());

	private static final Set<String> METHOD_BINDING_ATTRIBUTES = new HashSet<String>(Arrays.asList(
			"action", "actionListener", "validator", "valueChangeListener"
	));

    private final ElBindingFactory elBindingFactory;
    private final MockingPropertyResolver propertyResolver;
    private final PredefinedVariableResolver variableResolver;
    private final FacesContext mockFacesContext;

    public Jsf11ValidatingElResolver() { // SUPPRESS CHECKSTYLE (javadoc)
        propertyResolver = new MockingPropertyResolver();
        variableResolver = new PredefinedVariableResolver(propertyResolver);

        mockFacesContext = mock(FacesContext.class);
        final Application application = mock(Application.class);
        final ExternalContext externalContext = mock(ExternalContext.class/*, loggingAnswerDecorator*/);

        when(mockFacesContext.getApplication()).thenReturn(application);
        when(mockFacesContext.getExternalContext()).thenReturn(externalContext);

        when(application.getVariableResolver()).thenReturn(new Jsf11VariableResolverAdapter(variableResolver));
        when(application.getPropertyResolver()).thenReturn(new Jsf11PropertyResolverAdapter(propertyResolver));

        elBindingFactory = new Sun11_02ElBindingFactoryImpl(application);

	    // JSF 1.1 MethodBindingImpl relies on application.createValueBinding to
	    // get the bean whose method to access
	    when(application.createValueBinding(anyString())).thenAnswer(new Answer<Object>() {
		    @Override
		    public Object answer(InvocationOnMock invocation) throws Throwable {
			    String elExpression = (String) invocation.getArguments()[0];
			    return elBindingFactory.createValueBinding(elExpression);
		    }
	    });
    }


    /** {@inheritDoc} */
    public void setUnknownVariableResolver(ElVariableResolver unknownVariableResolver) {
        variableResolver.setUnknownVariableResolver(unknownVariableResolver);
    }

	@Override
	/** {@inheritDoc} */
	public ValidationResult validateElExpression(String elExpression, AttributeInfo attributeInfo) {
		if (METHOD_BINDING_ATTRIBUTES.contains(attributeInfo.getAttributeName())) {
			return validateMethodElExpression(elExpression);
		} else {
			return validateValueElExpression(elExpression);
		}
	}

    private ValidationResult validateMethodElExpression(final String elExpression) {
        try {
            // Create binding - throws an exception if no matching method found
            final MethodBinding binding = elBindingFactory.createMethodBinding(elExpression);
	        assertMethodExists(elExpression, binding);
	        return new SuccessfulValidationResult(elExpression, binding);
        } catch (BaseEvaluationException e) {
	        return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (EvaluationException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw ValidationResultHelper.wrapIfNeededAndAddContext(elExpression, e);
        }
    }

	private void assertMethodExists(String elExpression, MethodBinding binding) {
		try {
			tryAssertMethodExists(binding);
		} catch (MethodNotFoundException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new InternalValidatorFailureException(
					"Validation of method existence for '" + elExpression
						+ "' via " + binding + " failed for unexpected reasons: "
						+ e.getMessage()
					, e);
		}
	}

	/**
	 * Assert that the method actually exists.
	 * We cannot use {@link javax.faces.el.MethodBinding#getType(javax.faces.context.FacesContext)}
	 * to verify that because it also checks that the method has the same parameters
	 * as supplied to the {@link com.sun.faces.el.MethodBindingImpl} constructor, which
	 * won't work for us for we always pretend that there are no arguments.
	 * (Because there is no way for us to find out the actual expected arguments.)
	 * Thus this method only checks the target method name and not its arguments.
	 */
	@SuppressWarnings("unchecked")
	private void tryAssertMethodExists(MethodBinding binding) {
		Class<MethodBindingImpl> bindingClass = (Class<MethodBindingImpl>) binding.getClass();
		try {
			final Field methodNameField = bindingClass.getDeclaredField("name");
			methodNameField.setAccessible(true);
			final String methodName = (String) methodNameField.get(binding);

			final Field targetBindingField = bindingClass.getDeclaredField("vb");
			targetBindingField.setAccessible(true);
			final ValueBinding targetBinding = (ValueBinding) targetBindingField.get(binding);

			tryAssertMethodExists(targetBinding, methodName);

		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(
					"Failed to access MethodBindingImpl's private fields", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(
					"Failed to access MethodBindingImpl's private fields", e);
		}
	}

	private void tryAssertMethodExists(ValueBinding targetBinding, String methodName) throws net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException {
		final Object targetObject = getBindingValueOrFail(targetBinding);
		final Class<? extends Object> targetClass = targetObject.getClass();
		Method[] allMethods = targetClass.getMethods();
		for (Method method : allMethods) {
			if (method.getName().equals(methodName)) {
				return;
			}
		}

		throw new net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException(
				"No method " + methodName + " found in " + targetClass
		);
	}

	private Object getBindingValueOrFail(ValueBinding targetBinding) throws EvaluationException, IllegalStateException {
		final Object value = targetBinding.getValue(mockFacesContext);
		if (value == null) {
			throw new IllegalStateException("The value binding " + targetBinding
				+ " returned null as its value");
		}

		return value;

	}

	private ValidationResult validateValueElExpression(final String elExpression) {
        final ValueBinding binding = elBindingFactory.createValueBinding(elExpression);
        try {
            final Object resolvedMockedValue = binding.getValue(mockFacesContext);
            // if (resolvedMockedValue == null ) - do somethin? is it possible at all?
            return new SuccessfulValidationResult(elExpression, resolvedMockedValue);
        } catch (BaseEvaluationException e) {
	        return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (EvaluationException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw ValidationResultHelper.wrapIfNeededAndAddContext(elExpression, e);
        }
    }

    /** {@inheritDoc} */
    public JsfElValidator declareVariable(final String name, final Object value) {
        variableResolver.declareVariable(name, value);
        return this;
    }

    /** {@inheritDoc} */
    public JsfElValidator definePropertyTypeOverride(final String elExpression, final Class<?> newType) {
        propertyResolver.definePropertyTypeOverride(elExpression, newType);
        return this;
    }

    /** {@inheritDoc} */
    public void setIncludeKnownVariablesInException(
            boolean includeKnownVariablesInException) {
        variableResolver.setIncludeKnownVariablesInException(includeKnownVariablesInException);
    }

    /** {@inheritDoc} */
    public void addElExpressionFilter(ElExpressionFilter elExpressionFilter) {
        propertyResolver.addElExpressionFilter(elExpressionFilter);
    }

    public boolean isIncludeKnownVariablesInException() {
        return variableResolver.isIncludeKnownVariablesInException();
    }

}
