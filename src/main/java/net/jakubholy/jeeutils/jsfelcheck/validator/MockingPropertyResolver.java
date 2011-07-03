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

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

import net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory.UnableToCreateFakeValueException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.ExpressionRejectedByFilterException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InternalValidatorFailureException;


import com.sun.faces.el.PropertyResolverImpl;

/**
 * Automatically return a Mockito mock for any property that is valid, otherwise throw a {@link PropertyNotFoundException}.
 *
 * The mocked type is determined automatically but may be forced via {@link MockingPropertyResolver#definePropertyTypeOverride(String, Class)},
 * which is useful e.g. for Maps that return just Objects.
 */
public final class MockingPropertyResolver extends PropertyResolver implements PredefinedVariableResolver.NewVariableEncounteredListener {

    private final Logger log = Logger.getLogger(getClass().getName());
    private PropertyResolver realResolver = new PropertyResolverImpl();
    private ParsedElExpression currentExpression = new ParsedElExpression();
    private Map<String, Class<?>> typeOverrides = new Hashtable<String, Class<?>>();
    private final Collection<ElExpressionFilter> filters = new LinkedList<ElExpressionFilter>();

    /**
     * Define what type to produce for a JSF EL expression.
     * There are two types of overrides:
     * (1) property overrides: pass in the complete property, ex: bean.property1.property2
     * (2) collection component type overrides: for all sub-properties of a variable/property
     * (unless there is also a property override for it), used for arrays etc. Ex: bean.mapProperty.* =>
     * bean.mapProperty['someKey'] and bean.mapProperty.anotherProperty will be both affected by the override
     * @param elExpression The expression where to override the guessed type with only names and dots; i.e.
     *  'var.prop becomes var.prop
     * @param newType (required)
     */
    public void definePropertyTypeOverride(final String elExpression, final Class<?> newType) {
        if (newType == null) {
            throw new IllegalArgumentException("The overriding type for property '" + elExpression
            		+ "' must not be null.");
        }
        Class<?> currentOverride = typeOverrides.get(elExpression);
        if (currentOverride != null) {
            throw new IllegalArgumentException("The property override for '"
                    + elExpression + "' is already defined; current: " +
                    currentOverride + ", new: " + newType);
        }
        typeOverrides.put(elExpression, newType);
    }

    private void appendCurrentPropertyToExpression(final String property) {
        currentExpression.addProperty(property);
        applyFilters(currentExpression);
    }

    @Override
    public Class<?> getType(Object target, Object property)
            throws EvaluationException, PropertyNotFoundException {
        // Avoid OutOfBoundException for fake 0-length arrays we created
        if (target.getClass().isArray()) {
            return target.getClass().getComponentType();
        }
        return realResolver.getType(target, property);
    }

    @Override
    public Class<?> getType(Object target, int index) throws EvaluationException,
            PropertyNotFoundException {

        // Would normally throw an exception for empty arrays/list not having the given index
        if (target.getClass().isArray()) {
            return target.getClass().getComponentType();
        } else if (target instanceof Collection<?>) {
            return this.determineFinalTypeOfCurrentExpressionAnd(index, null);
        }

        return realResolver.getType(target, index);
    }

    @Override
    public Object getValue(Object target, Object property)
            throws EvaluationException, PropertyNotFoundException {
        //return realResolver.getValue(arg0, arg1);
        return getValue(target, property, getType(target, property));
    }

    @Override
    public Object getValue(Object target, int property)
            throws EvaluationException, PropertyNotFoundException {
        return getValue(target, property, getType(target, property));
    }

    /**
     * Note: In the case of a Map target the property is the key, ex.: 'my.key'.
     */
    @SuppressWarnings("rawtypes")
    private Object getValue(final Object target, final Object property, final Class originalType)
            throws EvaluationException, PropertyNotFoundException {

        final Class type = determineFinalTypeOfCurrentExpressionAnd(property, originalType);
        // Append property only after type has been determined !!!!
        appendCurrentPropertyToExpression(property.toString());
        return fakePropertValue(target, property, type);
    }

    private Object fakePropertValue(final Object target, final Object property, final Class<?> type) {
        if (type == null) {
            realResolver.getValue(target, property); // no exception => valid but unset
        }

        try {
            return FakeValueFactory.fakeValueOfType(type, property);
        } catch (UnableToCreateFakeValueException e) {
            throw new InternalValidatorFailureException("Failed to fake value for the property "
                    + property + " of the expression " + currentExpression
                    , e);
        }
    }

    /**
     * Determine the type to use using overrides and perhaps a default value.
     * @param property
     * @param originalType (optional)
     * @return
     */
    Class<?> determineFinalTypeOfCurrentExpressionAnd(final Object property,
            final Class<?> originalType) {

        //final boolean undefinedType = Object.class == originalType || originalType == null;
        Class<?> override = getTypeOverride(property);
        Class<?> type;
        if (override != null) {
            type = override;
            log.fine("getValue(prop=" + property + ",currentExpr.=" + currentExpression + "): overriding "
                    + originalType + " with " + type + " as requested");
        } else {
            type = originalType;
        }

        if (type == null) {
            // Null is common for collections such as List where component type can't be determined
            type = MockObjectOfUnknownType.class;
        }

        return type;
    }

    private Class<?> getTypeOverride(Object property) {
        if (currentExpression == null) return null; // should not happen?!

        // Check property override first, it has higher priority
        Class<?> propertyOverride = typeOverrides.get(currentExpression + "." + property);
        if (propertyOverride != null) {
            return propertyOverride;
        }

        Class<?> componentOverride = typeOverrides.get(currentExpression + ".*");
        if (componentOverride != null) {
            return componentOverride;
        }

        return null;
    }

    @Override
    public boolean isReadOnly(Object arg0, Object arg1)
            throws EvaluationException, PropertyNotFoundException {
        return realResolver.isReadOnly(arg0, arg1);
    }

    @Override
    public boolean isReadOnly(Object arg0, int arg1)
            throws EvaluationException, PropertyNotFoundException {
        return realResolver.isReadOnly(arg0, arg1);
    }

    @Override
    public void setValue(Object arg0, Object arg1, Object arg2)
            throws EvaluationException, PropertyNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Object arg0, int arg1, Object arg2)
            throws EvaluationException, PropertyNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleNewVariableEncountered(final String variableName) {
        currentExpression.setVariable(variableName);
        applyFilters(currentExpression);
    }

    private void applyFilters(ParsedElExpression currentExpression) throws ExpressionRejectedByFilterException {
        if (!filters.isEmpty()) {
            for (ElExpressionFilter filter : filters) {
                if (!filter.accept(currentExpression)) {
                    throw new ExpressionRejectedByFilterException(currentExpression.toString(), filter);
                }
            }
        }
    }

    /**
     * Throw {@link ExpressionRejectedByFilterException} for any expression not accepted by the supplied filter.
     * @param elExpressionFilter (required)
     */
    public void addElExpressionFilter(ElExpressionFilter elExpressionFilter) {
        filters.add(elExpressionFilter);
    }

    public void clearElExpressionFilters() {
        filters.clear();
    }

}