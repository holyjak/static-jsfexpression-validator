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

import net.jakubholy.jeeutils.jsfelcheck.validator.results.ExpressionRejectedByFilterResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.jsp.PageContext;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test validation of EL expressions, to be extended by every (JSF version-specific) validator implementation.
 *
 * The actual implementation is plugged in by implementing {@link #setUpResolver()}.
 */
public abstract class ValidatingJsfElResolverAbstractTest {

    private static <T> ValueHolder<T> valueHolder(final T value) {
        return new ValueHolder<T>(value);
    }

    protected ValidatingElResolver elResolver; // SUPPRESS CHECKSTYLE
    @Mock private ElVariableResolver mockUnknownVariableResolver;

    /** Provide the resolver to be tested. */
    protected abstract ValidatingElResolver setUpResolver();

    // CHECKSTYLE:OFF (don't want to add JavaDoc to each test method)

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        elResolver = setUpResolver();
        elResolver.setUnknownVariableResolver(mockUnknownVariableResolver);
    }

    @Test
    public void should_accept_hash_marked_jsf_el() throws Exception {
        elResolver.declareVariable("myStringBean", "Hello!");
        assertResultValue("#{myStringBean}", "Hello!");
    }

    @Test
    public void should_return_simple_session_bean() throws Exception {
        final String myBean = "my string bean's value";
        elResolver.declareVariable("myBean", myBean);
        assertEquals(new SuccessfulValidationResult(myBean)
                , elResolver.validateValueElExpression("#{myBean}"));
    }

    @Test
    public void should_not_fail_for_bean_property() throws Exception {
        elResolver.declareVariable("myBean", valueHolder(123456));
        assertExpressionValid("#{myBean.value}");
    }

    @Test
    public void should_not_fail_for_bean_indexed_value() throws Exception {
        elResolver.declareVariable("myArray", new String[1]);
        assertExpressionValid("#{myArray[0]}");
    }

    @Test
    public void should_not_fail_for_bean_keyed_value() throws Exception {
        elResolver.declareVariable("myMap", Collections.EMPTY_MAP);
        assertExpressionValid("#{myMap['my.key']}");
    }

    @Test
    public void should_not_fail_for_property_and_string_bean_value() throws Exception {
        elResolver.declareVariable("valueBean", Collections.EMPTY_MAP);
        elResolver.definePropertyTypeOverride("valueBean.*", ValueHoldingBean.class);
        assertExpressionValid("#{valueBean['key.here'].value}");
    }

    @Test
    public void should_return_property_and_negated_bool_bean_value() throws Exception {
        Map<String, Object> valueBean = new Hashtable<String, Object>();
        valueBean.put("key.here", new ValueHoldingBean(true));
        elResolver.declareVariable("valueBean", valueBean);

        Object result = elResolver.validateValueElExpression("#{!valueBean['key.here'].value}");
        assertEquals(new SuccessfulValidationResult(false), result);
    }

    @Test
    public void should_handle_string_array_property() throws Exception {
        elResolver.declareVariable("myBeanWithStringArrayProperty", new ValueHoldingBean());
        assertExpressionValid("#{myBeanWithStringArrayProperty.stringArray[-1]}");
    }

    @Test
    public void should_fail_for_undefined_variable() throws Exception {
        ValidationResult result = elResolver.validateValueElExpression("#{undefinedBean}");
        assertFailureWithMessageContaining(result, "No variable 'undefinedBean' among the predefined ones");
    }

    @Test
    public void should_fail_for_undefined_getter() throws Exception {
        elResolver.declareVariable("myObject", new Object());
        ValidationResult result = elResolver.validateValueElExpression("#{myObject.noSuchProperty}");
        //assertFailureWithMessageContaining(result, "Property 'noSuchProperty' not found"); //legacy jsf-impl 1.1
        assertFailureWithMessageContaining(result, "Invalid EL expression '#{myObject.noSuchProperty}': "); // jsf-impl 1.1_02b
    }

    @Test
    public void should_fail_for_undefined_map_getter() throws Exception {
        elResolver.declareVariable("myObject", new Object());
        ValidationResult result = elResolver.validateValueElExpression("#{myObject['noSuchKeyedProp']}");
        //assertFailureWithMessageContaining(result, "Property 'noSuchKeyedProp' not found"); // legacy jsf-impl 1.1
        assertFailureWithMessageContaining(result, "Invalid EL expression '#{myObject['noSuchKeyedProp']}': "); // jsf-impl 1.1_02b
    }

    @Test
    public void map_returns_object_by_default() throws Exception {
        elResolver.declareVariable("myMap", Collections.EMPTY_MAP);
        ValidationResult result = elResolver.validateValueElExpression("#{myMap['dummyKey']}");
        assertResultValueType(result, MockObjectOfUnknownType.class);
    }

    @Test
    public void map_variable_returns_specified_type_if_set() throws Exception {
        elResolver.declareVariable("myMap", Collections.EMPTY_MAP);
        elResolver.definePropertyTypeOverride("myMap.*", String.class);
        ValidationResult result = elResolver.validateValueElExpression("#{myMap['dummyKey']}");
        assertResultValueType(result, String.class);
    }

    @Test
    public void map_property_returns_specified_type_if_set() throws Exception {
        elResolver.declareVariable("myVariable", new BeanWithMapAndList());
        elResolver.definePropertyTypeOverride("myVariable.mapProperty.*", Integer.class);
        ValidationResult result = elResolver.validateValueElExpression("#{myVariable.mapProperty['dummyKey']}");
        assertResultValueType(result, Integer.class);
    }

    @Test
    public void should_try_to_resolve_unknown_variables_via_registry() throws Exception {
        elResolver.validateValueElExpression("#{unknownVariable}");
        verify(mockUnknownVariableResolver).resolveVariable("unknownVariable");
    }

    /**
     * Having an indexed property such as bean.property[123] we should return a faked object even though
     * the (fake) collection object doesn't have any such element. I.e. we shouldn't throw OutOfBoundExc.
     * or do a similar thing.
     */
    @Test
    public void should_correctly_resolve_indexed_property_to_declared_fake_value() throws Exception {
        elResolver.declareVariable("myVariable", new BeanWithMapAndList());
        elResolver.definePropertyTypeOverride("myVariable.arrayProperty.*", Integer.class);
        int index = Integer.MAX_VALUE;
        ValidationResult result = elResolver.validateValueElExpression("#{myVariable.arrayProperty[" + index + "]}");
        // if we are here it's already good - the resolver hasn't thrown OutOfBound or similar exception but
        // let's check the return value anyway
        assertThat("The resolution should have succeeded even though no element with the index " + index
               + " exists; actual result: " + result
               , result
               , is(instanceOf(SuccessfulValidationResult.class)));
        assertResultValueType(result, Integer.class);
    }
    @Test
    public void should_correctly_resolve_indexed_property_to_default_fake_value() throws Exception {
        elResolver.declareVariable("myVariable", new BeanWithMapAndList());
        // no override declared => a default value should be used so that no exception occurs
        ValidationResult result = elResolver.validateValueElExpression("#{myVariable.arrayProperty[123]}");
        // if we are here it's already good - the resolver hasn't thrown OutOfBound or similar exception but
        // let's check the return value anyway
        assertThat("The resolution should have succeeded even though no element with the index 123"
                + " exists; actual result: " + result
                , result
                , is(instanceOf(SuccessfulValidationResult.class)));
    }

    @Test
    public void should_evaluate_all_branches_of_conditional_expression() throws Exception {
        String[] expressions =  {
                "#{false and noSuchBean}"
                , "#{if(false) ...}"
                , "#{(true? validBean : noSuchBean).propertyOfFirstBeanOnly}"
                , "#{true? null : noSuchVar}"
        };
        ValidationResult result = elResolver.validateValueElExpression("#{true? null : noSuchVar}");
        assertThat("The resolution should have checked all the expression incl. the invalid one in false branch"
               + "; actual result: " + result
               , result
               , is(instanceOf(FailedValidationResult.class)));
    }

    @Test
    public void should_return_expresseion_rejected_by_filter_result_for_filtered_out_expr() throws Exception {
        elResolver.declareVariable("myVariable", "my value");
        elResolver.addElExpressionFilter(new ElExpressionFilter() {
            public boolean accept(ParsedElExpression expression) {
                return false;
            }});

        ValidationResult result = elResolver.validateValueElExpression("#{myVariable}");
        assertThat(result, is(instanceOf(ExpressionRejectedByFilterResult.class)));
    }

    /**
     * See specification of JSP 2.0, section 2.2.3: Implicit Objects
     * pageContext: PageContext
     * pageScope, requestScope, sessionScope, applicationScope: Map<String, Object>
     * param: Map<String, String>, paramValues: Map<String, String[]>
     * header: Map<String, String>, headerValues: Map<String, String[]>
     * cookie: Map<String, Cookie>
     * initParam: Map<String, String>
     */
    @Test
    public void should_recognize_all_jsp_implicit_objects_as_variables() throws Exception {

        assertResultValueType(elResolver.validateValueElExpression("#{pageContext}"), PageContext.class);

        String[] implicitMapObjects = new String[] {"pageScope", "requestScope", "sessionScope", "applicationScope", "param"
                , "paramValues", "header", "headerValues", "cookie", "initParam" };

        for (String implicitObject : implicitMapObjects) {
            assertResultValueType(elResolver.validateValueElExpression("#{" + implicitObject + "}"), Map.class);
        }
    }

    /**
     * We cannot validate values in request/session/.. map for their set is not known in advance.
     * To avoid false failures we should therefore for any such property return a value, which can
     * coerce into any primitive type likely to be used on a page (typically boolean, int, string).
     *
     * @see #should_recognize_all_jsp_implicit_objects_as_variables()
     */
    @Test
    public void should_be_able_to_coerce_implicit_object_map_property_to_any_primitive_value() throws Exception {

        String[] mapImplicitObjects = new String[] {
                "pageScope", "requestScope", "sessionScope", "applicationScope", "param"
                , "paramValues", "header", "headerValues", "initParam" };

        // For each Map implicit object, any implObj['key'] should generate some default value, which
        // can be coreced to any of the basic primitive types
        for (String implicitObject : mapImplicitObjects) {
            assertBoolResult("#{" + implicitObject + ".generatedValue != 'random'}"); // coerce to String
            assertBoolResult("#{" + implicitObject + ".generatedValue != true}");     // coerce to boolean
            assertBoolResult("#{" + implicitObject + ".generatedValue != 333}");      // coerce to int
        }

        // No exception should have been thrown

    }

    // CHECKSTYLE:ON

    // #################################################################################### HELPER FUNCTIONS ###########

    private void assertResultValueType(ValidationResult result, Class<?> type) {
        assertThat(result, is(instanceOf(SuccessfulValidationResult.class)));
        assertThat(((SuccessfulValidationResult) result).getExpressionResult()
                , is(instanceOf(type)));
    }

    private void assertFailureWithMessageContaining(ValidationResult result, String errorSubstring) {
        assertTrue(result instanceof FailedValidationResult);
        FailedValidationResult failure = (FailedValidationResult) result;
        assertThat(failure.getFailure().getMessage(), is(containsString(errorSubstring)));
    }

    /** Assert that the expression is valid and returns non-null value. */
    protected final void assertExpressionValid(final String elExpression) {
        ValidationResult result = elResolver.validateValueElExpression(elExpression);
        assertThat(result, is(instanceOf(SuccessfulValidationResult.class)));

        assertNotNull("Shall return some Mock"
                , ((SuccessfulValidationResult) result).getExpressionResult());
    }

    private void assertBoolResult(String elExpression) {
        ValidationResult validationResult = elResolver.validateValueElExpression(elExpression);
        assertThat("The expression '" + elExpression + "' should have succeeded to evaluate (to a boolean) but failed."
                , validationResult
                , is(instanceOf(SuccessfulValidationResult.class)));

        SuccessfulValidationResult successfulResult = (SuccessfulValidationResult) validationResult;
        assertThat("The expression '" + elExpression + "' should have yielded a boolean"
                , successfulResult.getExpressionResult()
                , is(instanceOf(boolean.class)));
    }

    protected final <T> void assertResultValue(String elExpression, T expectedValue) {
        ValidationResult validationResult = elResolver.validateValueElExpression(elExpression);
        assertThat("The expression '" + elExpression + "' should have succeeded to evaluate (to " + expectedValue
                + ") but failed."
                , validationResult
                , is(instanceOf(SuccessfulValidationResult.class)));

        SuccessfulValidationResult successfulResult = (SuccessfulValidationResult) validationResult;
        assertEquals("The expression '" + elExpression + "' should have yielded the expecte value"
                , successfulResult.getExpressionResult()
                , expectedValue);
    }

}
