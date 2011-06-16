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

package net.jakubholy.jeeutils.jsfelcheck.validator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.Logger;


import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InternalValidatorFailureException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InvalidExpressionException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;


import com.sun.faces.el.MethodBindingFactory;
import com.sun.faces.el.ValueBindingFactory;

/**
 * A "fake" resolver of JSF EL expression which only checks the validity of the expressions
 * using a custom Variable and Property resolver without requiring real values for the
 * variables being referenced in the expressions.
 *
 * The variable resolver resolvers against a pre-defined list of known variables, the property resolver doesn't actually
 * invoke any getter but just returns a Mock of the appropriate type (so that evaluation of the expression can
 * proceed, which wouldn't be possible if we used real objects and they returned null).
 *
 * @see #declareVariable(String, Object)
 * @see #definePropertyTypeOverride(String, Class)
 *
 */
public class ValidatingJsfElResolver implements JsfElValidator {

    private static final Logger LOG = Logger.getLogger(ValidatingJsfElResolver.class.getName());

    private MockingPropertyResolver propertyResolver;
    private PredefinedVariableResolver variableResolver;
    private FacesContext mockFacesContext;

    public ValidatingJsfElResolver(ElVariableResolver unknownVariableResolver) {
        propertyResolver = new MockingPropertyResolver();
        variableResolver = new PredefinedVariableResolver(propertyResolver, unknownVariableResolver);

        mockFacesContext = mock(FacesContext.class);
        final Application application = mock(Application.class);
        final ExternalContext externalContext = mock(ExternalContext.class/*, loggingAnswerDecorator*/);

        when(mockFacesContext.getApplication()).thenReturn(application);
        when(mockFacesContext.getExternalContext()).thenReturn(externalContext);

        when(application.getVariableResolver()).thenReturn(variableResolver);
        when(application.getPropertyResolver()).thenReturn(propertyResolver);
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator#validateMethodElExpression(java.lang.String)
     */
    @Override
    public ValidationResult validateMethodElExpression(final String elExpression) {
        try {
            // Create binding - throws an exception if no matching method found
            final MethodBinding binding = new MethodBindingFactory().createMethodBinding(elExpression, new Class[0]);
            return new SuccessfulValidationResult(binding);
        } catch (EvaluationException e) {
            return produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw wrapIfNeededAndAddContext(elExpression, e);
        }
    }

    private ValidationResult produceFailureResult(final String elExpression,
            EvaluationException e) {
        LOG.log(Level.FINE, "Resolution failed", e);
        return new FailedValidationResult(
                new InvalidExpressionException(elExpression, null, e.getCause()));
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator#validateValueElExpression(java.lang.String)
     */
    @Override
    public ValidationResult validateValueElExpression(final String elExpression) {
        final ValueBinding binding = new ValueBindingFactory().createValueBinding(elExpression);
        try {
            final Object resolvedMockedValue = binding.getValue(mockFacesContext);
            // if (resolvedMockedValue == null ) - do somethin? is it possible at all?
            return new SuccessfulValidationResult(resolvedMockedValue);
        } catch (EvaluationException e) {
            return produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw wrapIfNeededAndAddContext(elExpression, e);
        }
    }

    private InternalValidatorFailureException wrapIfNeededAndAddContext(final String elExpression,
            RuntimeException e) {
        if (e instanceof InternalValidatorFailureException) {
            InternalValidatorFailureException internalFailure = ((InternalValidatorFailureException) e);
            internalFailure.setExpression(elExpression);
            return internalFailure;
        } else {
            return new InternalValidatorFailureException(e).setExpression(elExpression);
        }
    }


    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator#declareVariable(java.lang.String, java.lang.Object)
     */
    @Override
    public JsfElValidator declareVariable(final String name, final Object value) {
        variableResolver.declareVariable(name, value);
        return this;
    }

    @Override
    public JsfElValidator definePropertyTypeOverride(final String elExpression, final Class<?> newType) {
        propertyResolver.definePropertyTypeOverride(elExpression, newType);
        return this;
    }


    /** List known variables in VariableNotFoundException? */
    public void setIncludeKnownVariablesInException(
            boolean includeKnownVariablesInException) {
        variableResolver.setIncludeKnownVariablesInException(includeKnownVariablesInException);
    }

    public boolean isIncludeKnownVariablesInException() {
        return variableResolver.isIncludeKnownVariablesInException();
    }

}
