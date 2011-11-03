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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import net.jakubholy.jeeutils.jsfelcheck.validator.AttributeInfo;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidationResultHelper;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.BaseEvaluationException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InvalidExpressionException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;
import org.apache.myfaces.el.unified.FacesELContext;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.*;

/** {@inheritDoc}
 *
 * JSF 1.2 implementation based on MyFaces.
 *
 */
public class Jsf12ValidatingElResolver implements ValidatingElResolver {

	private static final Logger LOG = Logger.getLogger(Jsf12ValidatingElResolver.class.getName());

    private static final Class<?>[] NO_PARAMS = new Class<?>[0];

	/**
	 * Matches the last property in an EL expression
	 * (i.e. a valid java identifier preceeded by '.' and followed by [optional space and] the closing '}'.
	 */
	public static final Pattern RE_LAST_EL_PROPERTY = Pattern.compile("\\.(?!\\d)((?:\\p{L}|[0-9_$])+)\\s*\\}");

	private final MethodFakingFunctionMapper functionMapper = new MethodFakingFunctionMapper();
    private ValidatingFakeValueResolver validatingResolver;
    private ExpressionFactory expressionFactory;
    private FacesContext context;
    private ELContext elContext;

    /** New, configured resolver. */
    public Jsf12ValidatingElResolver() {
        expressionFactory = new org.apache.el.ExpressionFactoryImpl();

        final Map<String, Object> emptyMap = Collections.emptyMap();
        ExternalContext externalContextMock = mock(ExternalContext.class);
        when(externalContextMock.getApplicationMap()).thenReturn(emptyMap);
        when(externalContextMock.getRequestMap()).thenReturn(emptyMap);
        when(externalContextMock.getSessionMap()).thenReturn(emptyMap);

        context = mock(FacesContext.class);
        when(context.getExternalContext()).thenReturn(externalContextMock);

        FacesELContext facesELContext = new FacesELContext(buildElResolver() , context);
        facesELContext.setFunctionMapper(functionMapper);
        elContext = facesELContext;
        //when(context.getELContext()).thenReturn(elContext); // possible but actually not required by the implem.
    }

    /**
     * Taken from MyFaces' ResolverBuilderForFaces.
     */
    private ELResolver buildElResolver() {
        CompositeELResolver elResolver = new CompositeELResolver();

        // replaces ImplicitObjectResolver, ScopedAttributeResolver, ManagedBeanResolver
        validatingResolver = new ValidatingFakeValueResolver(elResolver);

        elResolver.add(validatingResolver);
        elResolver.add(new ResourceBundleELResolver());
        elResolver.add(new MapELResolver());
        elResolver.add(new ListELResolver());
        elResolver.add(new ArrayELResolver());
        elResolver.add(new BeanELResolver());

        return elResolver;
    }

	@Override
	/** {@inheritDoc} */
	public ValidationResult validateElExpression(String elExpression, AttributeInfo attributeInfo) {

		boolean attributeTypedAsMethod = MethodExpression.class.isAssignableFrom(attributeInfo.getAttributeType());
		boolean attributeTypedAsValue = ValueExpression.class.isAssignableFrom(attributeInfo.getAttributeType());

		if (attributeTypedAsMethod) {
			return validateMethodElExpression(elExpression, true);
		} else if (attributeTypedAsValue) {
			return validateValueElExpression(elExpression);
		} else {
			/* EXPRESSION TYPE NOT SPECIFIED (=> FACELETS), TRY BOTH */
			ValidationResult validationResult = validateValueElExpression(elExpression);

			if (isPropertyNotFoundFailure(validationResult)) {
				return tryValidateAsMethodAfterValueFailed(elExpression, validationResult);
			} else {
				return validationResult;
			}
		}
	}

	private boolean isPropertyNotFoundFailure(ValidationResult validationResult) {
		if (validationResult instanceof FailedValidationResult) {
			InvalidExpressionException failure = ((FailedValidationResult) validationResult).getFailure();
			return (failure.getCause() != null) && (failure.getCause() instanceof net.jakubholy.jeeutils.jsfelcheck.validator.exception.PropertyNotFoundException);
		} else {
			return false;
		}
	}

	/** Return either {@link #validateMethodElExpression} if successful or the original result. */
	private ValidationResult tryValidateAsMethodAfterValueFailed(String elExpression, ValidationResult validationResult) {
		String methodValidationFailure = null;
		try {
			ValidationResult methodResult = validateMethodElExpression(elExpression, false);
			if (!methodResult.hasErrors()) {
				return methodResult;
			}
			methodValidationFailure = methodResult.toString();
		} catch (RuntimeException e) {}

		LOG.info("tryValidateAsMethodAfterValueFailed: '" + elExpression + "' is neither valid ValueExpression nor "
			+ "MethodExpression. Method validation failure: " + methodValidationFailure);

		return validationResult;
	}

	private ValidationResult validateValueElExpression(String elExpression) {
        functionMapper.setCurrentExpression(elExpression);
        final ValueExpression valueExpression = expressionFactory.createValueExpression(
                elContext, elExpression, Object.class);
        try {
            final Object resolvedMockedValue = valueExpression.getValue(elContext);
            // if (resolvedMockedValue == null ) - do somethin? is it possible at all?
            return new SuccessfulValidationResult(elExpression, resolvedMockedValue)
                    .withFunctionsInExpression(functionMapper.getLastExpressionsFunctionQNames());
        } catch (ELException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (BaseEvaluationException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw ValidationResultHelper.wrapIfNeededAndAddContext(elExpression, e);
        }

    }

    private ValidationResult validateMethodElExpression(String elExpression, boolean ignoreAssertFailure) {
        functionMapper.setCurrentExpression(elExpression);      // most likely absolutely unnecessary
        try {
            final MethodExpression methodExpression = expressionFactory.createMethodExpression(
                    elContext, elExpression, Object.class, NO_PARAMS);
	        assertMethodExists(elExpression, ignoreAssertFailure);
            return new SuccessfulValidationResult(elExpression, methodExpression)
                    .withFunctionsInExpression(functionMapper.getLastExpressionsFunctionQNames());
        } catch (ELException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (BaseEvaluationException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw ValidationResultHelper.wrapIfNeededAndAddContext(elExpression, e);
        }

    }

	/**
	 * Assert that the method actually exists.
	 * We cannot use {@link MethodExpression#getMethodInfo(javax.el.ELContext)}
	 * to verify the method because it also checks that the method has the same parameters
	 * as supplied to the MethodExpression's constructor constructor, which
	 * won't work for us for we always pretend that there are no arguments.
	 * (Because, in most cases, there is no way for us to find out the actual expected arguments.)
	 * Thus this method only checks the target method name and not its arguments.
	 */
	private void assertMethodExists(String methodExpression, boolean ignoreAssertFailure)
			throws PropertyNotFoundException, MethodNotFoundException, ELException {
		try {
			tryAssertMethodExists(methodExpression);
		} catch (net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException e) {
			throw new net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException(e);
		} catch (RuntimeException e) {
			if (ignoreAssertFailure) {
				LOG.log(Level.WARNING, "assertMethodExists: Method validation for " + methodExpression
					+ " failed but we assume that it is rather due to a bug in our experimental, hacked verification "
					+ " code. Please open an issue at "
					+ "https://github.com/jakubholynet/static-jsfexpression-validator/issues Failure: " + e);
			} else {
				throw new net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException(e);
			}
		} // other exceptions just propagated

	}

	/**
	 * Take an expression like #{bean.property.method},
	 * get the value and thus class of #{bean.property}
	 * and verify that the class has a public method named 'method'.
	 * @param elExpression (required)
	 */
	void tryAssertMethodExists(String elExpression) {

		String[] targetObjectElAndMethod = splitAtLastProperty(elExpression);
		String targetObjectEl = targetObjectElAndMethod[0];
		String methodName = targetObjectElAndMethod[1];

		ValueExpression targetObjectValueExpr = expressionFactory.createValueExpression(
				elContext, targetObjectEl, Object.class);
		final Class<? extends Object> targetClass = targetObjectValueExpr.getValue(elContext).getClass();

		final Method[] methods = targetClass.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				return;
			}
		}

		throw new net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException(
				"No method '" + methodName + "' found in the target object's " + targetClass);

	}

	protected static String[] splitAtLastProperty(String elExpression) {
		final Matcher matcher = RE_LAST_EL_PROPERTY.matcher(elExpression);
		if (matcher.find()) {
			final int matchGroup = 1;
			final int propertyNameWithDotStart = matcher.start(matchGroup) - 1;

			String lastProperty = matcher.group(matchGroup);
			String elWithoutLastProperty = elExpression.substring(0, propertyNameWithDotStart) + "}";
			return new String[] {elWithoutLastProperty, lastProperty};
		} else {
			throw new IllegalArgumentException("Couldn't find a trailing property name in the EL '" + elExpression
					+ "' using " + matcher);
		}
	}

	/** {@inheritDoc} */
    public JsfElValidator declareVariable(String name, Object value) {
        validatingResolver.getVariableResolver().declareVariable(name, value);
        return this;
    }

    /** {@inheritDoc} */
    public JsfElValidator definePropertyTypeOverride(String mapJsfExpression,
            Class<?> newType) {
        validatingResolver.getPropertyResolver().definePropertyTypeOverride(mapJsfExpression, newType);
        return this;
    }

    /** {@inheritDoc} */
    public void setUnknownVariableResolver(
            ElVariableResolver unknownVariableResolver) {
        validatingResolver.getVariableResolver().setUnknownVariableResolver(unknownVariableResolver);
    }

    /** {@inheritDoc} */
    public void setIncludeKnownVariablesInException(
            boolean includeKnownVariablesInException) {
        validatingResolver.getVariableResolver().setIncludeKnownVariablesInException(includeKnownVariablesInException);
    }

    /** {@inheritDoc} */
    public void addElExpressionFilter(ElExpressionFilter elExpressionFilter) {
        validatingResolver.getPropertyResolver().addElExpressionFilter(elExpressionFilter);
    }

}
