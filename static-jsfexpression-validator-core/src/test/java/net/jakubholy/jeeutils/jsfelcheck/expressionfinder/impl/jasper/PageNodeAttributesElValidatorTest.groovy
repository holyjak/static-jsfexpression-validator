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

import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;


import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;


import net.jakubholy.jeeutils.jsfelcheck.validator.AttributeInfo;

public class PageNodeAttributesElValidatorTest {

    private static final class MyMethodExpression {}
    private static final class MyValueExpression {}

    private static class FakeTagHandler {
        MyValueExpression valueAttribute
        MyMethodExpression methodAttribute
    }

    private static class MemorizingElValidatorStub implements JsfElValidator {

        def validationParams = []

        JsfElValidator declareVariable(String name, Object value) { return null; }
        JsfElValidator definePropertyTypeOverride(String mapJsfExpression, Class<?> newType) {
            return null
        }

        ValidationResult validateElExpression(String elExpression, AttributeInfo attributeInfo) {
            validationParams << [ "el" : elExpression, "attribute" : attributeInfo]
            return new SuccessfulValidationResult(elExpression, null)
        }
    }

    private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

    @SuppressWarnings("serial")
    private static class Attributes extends Hashtable<String, String> {

        public static Attributes with(String attributeName, String value) {
            Attributes attributes = new Attributes();
            attributes.put(attributeName, value);
            return attributes;
        }

        public Attributes and(String attributeName, String value) {
            put(attributeName, value);
            return this;
        }

    }

    private MemorizingElValidatorStub expressionValidator;
    private PageNodeAttributesElValidator nodeValidator;

    @Before
    public void setUp() throws Exception {
        expressionValidator = new MemorizingElValidatorStub()
        nodeValidator = new PageNodeAttributesElValidator(expressionValidator);
    }

    @Test
    public void should_return_empty_map_if_no_jsf_expression_attributes() throws Exception {

        AttributesValidationResult resolvedExpressions = nodeValidator.validateJsfExpressions(FakeTagHandler, EMPTY_ATTRIBUTES);
        assertNotNull(resolvedExpressions);
    }

    @Test
    public void should_report_all_expressions() throws Exception {
        Attributes attributes = Attributes
            .with("el1", "#{valid1}")
            .and("el2", "#{another}");

        AttributesValidationResult results = nodeValidator.validateJsfExpressions(FakeTagHandler, attributes);

        assert results.jsfExpressionsFound() && results.getAllResultsMap().
            collectEntries {attr,result -> [attr, result.expressionDescriptor.expression]} == [
                "el1" : "#{valid1}", "el2" : "#{another}"]
    }

    @Test
    public void should_ignore_attributes_without_jsfexpressions() throws Exception {
        AttributesValidationResult results = nodeValidator.validateJsfExpressions(FakeTagHandler,
                Attributes.with("id", "justATextValue"));
        assertFalse(results.jsfExpressionsFound());
    }
    
    @Test
    public void should_pass_attribute_info_with_type_determined_from_tag_handler_to_validator() throws Exception {

        Attributes attributes = Attributes
            .with("valueAttribute", "#{myValueEl}")
            .and("methodAttribute", "#{myMethodEl}");

        nodeValidator.validateJsfExpressions(FakeTagHandler, attributes);

        assert expressionValidator.validationParams == [
                ["el" : "#{myValueEl}", "attribute" : new AttributeInfo("valueAttribute", MyValueExpression)]
                , ["el" : "#{myMethodEl}", "attribute" : new AttributeInfo("methodAttribute", MyMethodExpression)]
        ]
    }

    /**
     * JSF EL up to 1.1 were marked with #{..}, since the introduction of UEL in 1.2 they use
     * #{} for on-requested evaluated expressions (i.e. JSF expr.)
     * and ${..} for immediatelly evaluated ones (i.e. normal JSTL expr.)
     */
    @Test
    public void should_recognize_hash_marked_ie_deferrenced_expressions() throws Exception {
        assertTrue("Should recognize normal JSF expression (deferrenced), i.e. #{}"
                , nodeValidator.containsElExpression("garbage before... #{jsf11_expression} ...and after"));
    }

    /**
     * JSF EL up to 1.1 were marked with #{..}, since the introduction of UEL in 1.2 they use
     * #{} for on-requested evaluated expressions (i.e. JSF expr.)
     * and ${..} for immediatelly evaluated ones (i.e. normal JSTL expr.)
     */
    @Test
    public void should_reject_dollar_marked_ie_immediate_expressions() throws Exception {
        assertFalse("Should reject immediately evaluated UEL expressions, i.e. ${}"
                , nodeValidator.containsElExpression("garbage before... \${jsf12+_expression} ...and after"));
    }

    /**
     * When determining the type of an attribute and the attribute is not present in the tag handler
     * (which is normal for Facelets that don't require get/set pair for its attributes) then return
     * String.
     */
    @Test
    public void should_default_to_String_if_attribute_not_in_tag_handler() throws Exception {
        Attributes attributes = Attributes.with("noSuchAttributeOnHandler", "#{el}");

        nodeValidator.validateJsfExpressions(Object, attributes);

        def validateElExpressionParams = expressionValidator.validationParams[0]
        assert validateElExpressionParams."attribute".attributeType == String
    }


}
