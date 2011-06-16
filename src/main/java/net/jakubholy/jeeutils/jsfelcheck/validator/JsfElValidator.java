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

public interface JsfElValidator {

    /**
     * Validates that JSF EL is valid and returns its value, which is either a predefined
     * variable or a mock of the class expected to be returned by a property.
     * (So the value isn't really interesting as it isn't determined based on real objects,
     * it's only important to verify that it is not null or that it is an instance of
     * the expected type.)
     * @param elExpression (required) Ex.: {@code #{aBean}, #{aBean.aProperty}, #{aBean['key'].property}}
     */
    ValidationResult validateValueElExpression(final String elExpression);

    JsfElValidator declareVariable(final String name,
            final Object value);

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
     * @param mapJsfExpression The expression where to override the guessed type with only names and dots, perhaps plus .*; i.e.
     *  'var.prop['key']' becomes var.prop
     * @param newType
     */
    JsfElValidator definePropertyTypeOverride(final String mapJsfExpression, final Class<?> newType);

    ValidationResult validateMethodElExpression(final String expression);

}