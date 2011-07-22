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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.PredefinedVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidationResultHelper;
import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.ElBindingFactory;
import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.ElBindingFactoryProvider;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

/**
 * {@inheritDoc}
 *
 * JSF 1.1 implementation.
 */
public class Jsf11ValidatingElResolver implements ValidatingElResolver {

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

        elBindingFactory = ElBindingFactoryProvider.getFactory(application);
    }


    /** {@inheritDoc} */
    public void setUnknownVariableResolver(ElVariableResolver unknownVariableResolver) {
        variableResolver.setUnknownVariableResolver(unknownVariableResolver);
    }

    /** {@inheritDoc} */
    public ValidationResult validateMethodElExpression(final String elExpression) {
        try {
            // Create binding - throws an exception if no matching method found
            final MethodBinding binding = elBindingFactory.createMethodBinding(elExpression);
            return new SuccessfulValidationResult(binding);
        } catch (EvaluationException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw ValidationResultHelper.wrapIfNeededAndAddContext(elExpression, e);
        }
    }

    /** {@inheritDoc} */
    public ValidationResult validateValueElExpression(final String elExpression) {
        final ValueBinding binding = elBindingFactory.createValueBinding(elExpression);
        try {
            final Object resolvedMockedValue = binding.getValue(mockFacesContext);
            // if (resolvedMockedValue == null ) - do somethin? is it possible at all?
            return new SuccessfulValidationResult(resolvedMockedValue);
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
