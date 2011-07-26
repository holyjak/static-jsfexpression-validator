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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

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
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidationResultHelper;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.BaseEvaluationException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

import org.apache.myfaces.el.unified.FacesELContext;

/** {@inheritDoc}
 *
 * JSF 1.2 implementation based on MyFaces.
 *
 */
public class Jsf12ValidatingElResolver implements ValidatingElResolver {

    private static final Class<?>[] NO_PARAMS = new Class<?>[0];
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

        elContext = new FacesELContext(buildElResolver() , context);
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

    /** {@inheritDoc} */
    public ValidationResult validateValueElExpression(String elExpression) {
        final ValueExpression valueExpression = expressionFactory.createValueExpression(
                elContext, elExpression, Object.class);
        try {
            final Object resolvedMockedValue = valueExpression.getValue(elContext);
            // if (resolvedMockedValue == null ) - do somethin? is it possible at all?
            return new SuccessfulValidationResult(resolvedMockedValue);
        } catch (ELException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (BaseEvaluationException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw ValidationResultHelper.wrapIfNeededAndAddContext(elExpression, e);
        }

    }

    /** {@inheritDoc} */
    public ValidationResult validateMethodElExpression(String elExpression) {
        try {
            final MethodExpression methodExpression = expressionFactory.createMethodExpression(
                    elContext, elExpression, Object.class, NO_PARAMS);
            return new SuccessfulValidationResult(methodExpression);
        } catch (ELException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (BaseEvaluationException e) {
            return ValidationResultHelper.produceFailureResult(elExpression, e);
        } catch (RuntimeException e) {
            throw ValidationResultHelper.wrapIfNeededAndAddContext(elExpression, e);
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