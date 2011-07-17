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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.JsfExpressionDescriptor;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

/**
 * Helper for extracting JSF EL from a tag and validating the EL via a validator.
 */
public class PageNodeExpressionValidator {

    private final JsfElValidator expressionValidator;

    /**
     * New node helper using the given EL validator.
     * @param expressionValidator (required)
     */
    public PageNodeExpressionValidator(JsfElValidator expressionValidator) {
        this.expressionValidator = expressionValidator;
    }

    private Map<String, String> extractJsfExpressions(Map<String, String> attributes) {
        Map<String, String> jsfExpressions = new Hashtable<String, String>();
        for (Entry<String, String> atribute : attributes.entrySet()) {
            if( atribute.getValue().contains("#{")) {
                jsfExpressions.put(atribute.getKey(), atribute.getValue());
            }
        }
        return jsfExpressions;
    }

    private boolean isMethodBinding(String attribute) {
        return attribute.equals("action")
            || attribute.equals("actionListener")
            || attribute.equals("valueChangeListener"); // e.g. h:selectOneMenu
    }

    /**
     * Validate all JSF EL expressions in the tag's attributes and return
     * the resolved values of those expressions, do nothing of no expressions.
     * @param attributes (required) attributes of the tag
     * @return attribute name -> EL expression evaluation result or empty
     */
    public AttributesValidationResult validateJsfExpressions(Map<String, String> attributes) {

        Map<String, String> jsfExpressions = extractJsfExpressions(attributes);
        AttributesValidationResult resolvedExpressions = new AttributesValidationResult();

        for (Entry<String, String> jsfElAttribute : jsfExpressions.entrySet()) {
            String attributeName = jsfElAttribute.getKey();
            String elExpression = jsfElAttribute.getValue();

            ValidationResult result;

            if (isMethodBinding(attributeName)) {
                result = expressionValidator.validateMethodElExpression(elExpression);
            } else {
                result = expressionValidator.validateValueElExpression(elExpression);
            }

            result.setExpressionDescriptor(new JsfExpressionDescriptor(elExpression));

            resolvedExpressions.add(attributeName, result);

        }

        return resolvedExpressions;

    }

}