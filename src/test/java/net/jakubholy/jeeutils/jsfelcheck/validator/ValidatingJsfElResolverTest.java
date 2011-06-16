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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ValidatingJsfElResolverTest {

    private static <T> ValueHolder<T> valueHolder(final T value) {
        return new ValueHolder<T>(value);
    }

    private ValidatingJsfElResolver elResolver;
    @Mock private ElVariableResolver mockUnknownVariableResolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        elResolver = new ValidatingJsfElResolver(mockUnknownVariableResolver);
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
        elResolver.definePropertyTypeOverride("valueBean", ValueHoldingBean.class);
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

    private void assertExpressionValid(final String elExpression) {
        assertNotNull("Shall return some Mock"
                , elResolver.validateValueElExpression(elExpression));
    }

    @Test
    public void should_fail_for_undefined_variable() throws Exception {
        ValidationResult result = elResolver.validateValueElExpression("#{undefinedBean}");
        assertFailureWithMessageContaining(result, "No variable 'undefinedBean' among the predefined ones");
    }

    private void assertFailureWithMessageContaining(ValidationResult result, String errorSubstring) {
        assertTrue(result instanceof FailedValidationResult);
        FailedValidationResult failure = (FailedValidationResult) result;
        assertThat(failure.getFailure().getMessage(), is(containsString(errorSubstring)));
    }

    @Test
    public void should_fail_for_undefined_getter() throws Exception {
        elResolver.declareVariable("myObject", new Object());
        ValidationResult result = elResolver.validateValueElExpression("#{myObject.noSuchProperty}");
        assertFailureWithMessageContaining(result, "Property 'noSuchProperty' not found");
    }

    @Test
    public void should_fail_for_undefined_map_getter() throws Exception {
        elResolver.declareVariable("myObject", new Object());
        ValidationResult result = elResolver.validateValueElExpression("#{myObject['noSuchKeyedProp']}");
        assertFailureWithMessageContaining(result, "Property 'noSuchKeyedProp' not found");
    }

    @Test
    public void map_returns_object_by_default() throws Exception {
        elResolver.declareVariable("myMap", Collections.EMPTY_MAP);
        ValidationResult result = elResolver.validateValueElExpression("#{myMap['dummyKey']}");
        assertResultValueType(result, MockObjectOfUnknownType.class);
    }

    private void assertResultValueType(ValidationResult result, Class<?> type) {
        assertThat(result, is(instanceOf(SuccessfulValidationResult.class)));
        assertThat( ((SuccessfulValidationResult) result).getExpressionResult()
                , is(instanceOf(type)));
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
        elResolver.declareVariable("myVariable", new BeanWithMap());
        elResolver.definePropertyTypeOverride("myVariable.mapProperty.*", Integer.class);
        ValidationResult result = elResolver.validateValueElExpression("#{myVariable.mapProperty['dummyKey']}");
        assertResultValueType(result, Integer.class);
    }

    @Test
    public void should_try_to_resolve_unknown_variables_via_registry() throws Exception {
        elResolver.validateValueElExpression("#{unknownVariable}");
        verify(mockUnknownVariableResolver).resolveVariable("unknownVariable");
    }

}
