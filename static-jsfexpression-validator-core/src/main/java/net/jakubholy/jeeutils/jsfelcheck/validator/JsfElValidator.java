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

import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

/**
 * A "fake" resolver of JSF EL expression which only checks the validity of the expressions
 * using a custom Variable and Property resolver without requiring real values for the
 * variables being referenced in the expressions.
 *
 * The variable resolver resolversIn against a pre-defined list of known variables, the property resolver doesn't actually
 * invoke any getter but just returns a Mock of the appropriate type (so that evaluation of the expression can
 * proceed, which wouldn't be possible if we used real objects and they returned null).
 *
 * @see #declareVariable(String, Object)
 * @see #definePropertyTypeOverride(String, Class)
 *
 */
public interface JsfElValidator {

    /**
     * Validates that JSF EL is a valid value or method binding expression and returns the
     * validation result.
     * Notice that value bindings are expected to provide a value while method bindings are
     * expected to reference a method that can be called. (Used for action handlers, action listeners.)
     * <p>
     *     For a value binding expression the result contains its value, which is either a predefined
     * variable or a mock of the class expected to be returned by a property.
     * (So the value isn't really interesting as it isn't determined based on real objects,
     * it's only important to verify that it is not null or that it is an instance of
     * the expected type.)
     * </p>
     *  
     *
     * @param elExpression (required) Ex.: {@code #{aBean}, #{aBean.aProperty}, #{aBean['key'].property}, #{b.method}
     * @param attributeInfo (required) the name of the attribute and the typ (MethodBinding or ValueBinding for JSF1.2+)
     * @return results of the validation, typically
     * {@link net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult} or
     * {@link net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult}, but may be other
     */
	ValidationResult validateElExpression(final String elExpression, AttributeInfo attributeInfo);

    /**
     * Register a EL variable and its value so that when it encountered in an EL expression, it will be possible to
     * resolve it.
     * Normally the {@link net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException} is thrown when an undeclared/unknown variable in encountered.
     * You use this typically to declare managed beans and their value, which is, for the purpose of EL validation,
     * usually produced by {@link FakeValueFactory#fakeValueOfType(Class, Object)}.
     *
     * @param name (required) the name of the EL variable (i.e. the first identifier in any EL expression:
     * var.prop1.prop2)
     * @param value (required) the value to be returned for the variable, used in further evaluation. WARNING: It should
     * be an actual instance, not a Class!
     * @return this
     */
    JsfElValidator declareVariable(final String name, final Object value);

    /**
     * Specify the type of a 'property' in a JSF EL expression, usually a component of a collection etc.
     * If you have #{myBean.myMap['anyKey'].whatever} you may declare the type returned from the myMap
     * by specifying override for 'myBean.myMap.*' to be e.g. WhateverType.class.
     * <p>
     * There are two types of overrides:
     * (1) property overrides: pass in the complete property, ex: bean.property1.property2
     * (2) collection component type overrides: for all sub-properties of a variable/property
     * (unless there is also a property override for it), used for arrays etc. Ex: bean.mapProperty.* =>
     * bean.mapProperty['someKey'] and bean.mapProperty.anotherProperty will be both affected by the override
     *
     * @param mapJsfExpression The expression where to override the guessed type with only names and dots,
     * perhaps plus .*; i.e. 'var.prop['key']' becomes var.prop
     * @param newType the type to use for the property
     * @return this
     */
    JsfElValidator definePropertyTypeOverride(final String mapJsfExpression, final Class<?> newType);
}