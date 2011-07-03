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
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver.PropertyTypeResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.ExpressionRejectedByFilterException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.faces.el.PropertyResolverImpl;


public class MockingPropertyResolverTest {

    private static class MyVariable {
        public String getAcceptedProperty() { return ""; };
        public String getDeniedProperty() { return ""; };
    }

    private static class AcceptingFiler implements ElExpressionFilter {

        private boolean called = false;

        @Override
        public boolean accept(ParsedElExpression expression) {
            called = true;
            return true;
        }

        public boolean isCalled() {
            return called;
        }

    }

    public static class ConstantPropertyTypeResolverImpl implements PropertyTypeResolver {

        private final Class<?> type;

        public ConstantPropertyTypeResolverImpl(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType(Object target, Object property) {
            return type;
        }
    }

    /** Name of the managed bean variable "encountered" during the current resolution process. */
    private static final String CURRENT_VARIABLE = "bean";
    private MockingPropertyResolver resolver;
    private MyVariable currentVariable;

    @Before
    public void setUp() {
        currentVariable = new MyVariable();
        resolver = new MockingPropertyResolver();
        resolver.handleNewVariableEncountered(CURRENT_VARIABLE);
    }

    @Test
    public void should_keep_current_type_if_no_override() throws Exception {
        assertSame(String.class
                , resolver.determineFinalTypeOfCurrentExpressionAnd("property", String.class));
        assertSame(URL.class
                , resolver.determineFinalTypeOfCurrentExpressionAnd("property", URL.class));
    }

    @Test
    public void should_respect_component_type_override() throws Exception {
        resolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Integer.class);

        assertSame(Integer.class
                , resolver.determineFinalTypeOfCurrentExpressionAnd("mapKey", String.class));
    }

    @Test
    public void should_respect_property_type_override() throws Exception {
        resolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".property", URL.class);

        assertSame(URL.class
                , resolver.determineFinalTypeOfCurrentExpressionAnd("property", Math.class));
    }

    @Test
    public void should_prioritize_property_over_component_type_override() throws Exception {
        resolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".property", URL.class);
        resolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Integer.class);


        assertSame(Integer.class
                , resolver.determineFinalTypeOfCurrentExpressionAnd("nonOverridenProperty", String.class));
        assertSame(URL.class
                , resolver.determineFinalTypeOfCurrentExpressionAnd("property", String.class));
    }


    @Test
    public void should_apply_filter_on_variable() throws Exception {
        resolver.addElExpressionFilter(new ElExpressionFilter(){

            @Override
            public boolean accept(ParsedElExpression expr) {
                if (expr.size() == 1 && expr.iterator().next().equals("deniedVariable")) {
                    return false;
                }
                return true;
            }

        });

        resolver.handleNewVariableEncountered("okVariable"); // shall pass ok

        try {
            resolver.handleNewVariableEncountered("deniedVariable");
            fail("Should have been denied by the filter");
        } catch (ExpressionRejectedByFilterException e) {
            assertEquals("deniedVariable", e.getExpression());
        }
    }

    @Test
    public void should_apply_filter_on_property() throws Exception {
        resolver.setTypeResolver(new ConstantPropertyTypeResolverImpl(String.class));
        resolver.addElExpressionFilter(new ElExpressionFilter(){

            @Override
            public boolean accept(ParsedElExpression expr) {
                Iterator<String> exprIter = expr.iterator();
                if (expr.size() == 2
                        && exprIter.next().equals("variable")
                        && exprIter.next().equals("deniedProperty")) {
                    return false;
                }
                return true;
            }

        });

        // Try variable.acceptedProperty
        resolver.handleNewVariableEncountered("variable"); // shall pass ok
        resolver.getValue(currentVariable, "acceptedProperty"); // shall pass

        // Reset, try variable.acceptedProperty
        resolver.handleNewVariableEncountered("variable"); // shall pass ok
        try {
            resolver.getValue(currentVariable, "deniedProperty");
            fail("Should have been denied by the filter");
        } catch (ExpressionRejectedByFilterException e) {
            assertEquals("variable.deniedProperty", e.getExpression());
        }
    }

    @Test
    public void should_apply_all_filters_for_new_variable() throws Exception {
        AcceptingFiler filter1 = new AcceptingFiler();
        AcceptingFiler filter2 = new AcceptingFiler();

        resolver.addElExpressionFilter(filter1);
        resolver.addElExpressionFilter(filter2);

        resolver.handleNewVariableEncountered("dummyVar");

        assertTrue(filter1.isCalled());
        assertTrue(filter2.isCalled());
    }

    @Test
    public void should_apply_all_filters_for_property() throws Exception {
        AcceptingFiler filter1 = new AcceptingFiler();
        AcceptingFiler filter2 = new AcceptingFiler();

        resolver.setTypeResolver(new ConstantPropertyTypeResolverImpl(String.class));
        resolver.handleNewVariableEncountered("dummyVar");

        resolver.addElExpressionFilter(filter1);
        resolver.addElExpressionFilter(filter2);

        resolver.getValue(currentVariable, "acceptedProperty");

        assertTrue(filter1.isCalled());
        assertTrue(filter2.isCalled());
    }

    @Ignore("Only testable with a real property type resolver")
    @Test(expected=PropertyNotFoundException.class)
    public void should_throw_exception_for_unknown_property() throws Exception {
        resolver.setTypeResolver(new ConstantPropertyTypeResolverImpl(null));
        resolver.getValue(currentVariable, "unknownProperty");
    }

    @Test
    public void should_return_MockObjectOfUnknownType_for_existing_mapped_property_without_declard_component_type() throws Exception {
        resolver.setTypeResolver(new ConstantPropertyTypeResolverImpl(Object.class));
        assertThat(resolver.getValue(Collections.EMPTY_MAP, "some.key")
                , is(instanceOf(MockObjectOfUnknownType.class)));
    }

    @Test
    public void should_return_MockObjectOfUnknownType_for_existing_list_property_without_declard_component_type() throws Exception {
        assertThat(resolver.getValue(Collections.EMPTY_LIST, 123)
                , is(instanceOf(MockObjectOfUnknownType.class)));
    }

    @Test
    public void should_return_MockObjectOfUnknownType_for_existing_array_property_without_declard_component_type() throws Exception {
        assertThat(resolver.getValue(new Object[0], 456)
                , is(instanceOf(MockObjectOfUnknownType.class)));
    }

    @Test
    public void should_return_string_for_existing_string_array_property() throws Exception {
        assertThat(resolver.getValue(new String[0], 33)
                , is(instanceOf(String.class)));
    }

    @Test
    public void should_return_declared_component_type_for_existing_mapped_property() throws Exception {
        resolver.setTypeResolver(new ConstantPropertyTypeResolverImpl(Object.class));
        resolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Runnable.class);
        assertThat(resolver.getValue(Collections.EMPTY_MAP, "some.key")
                , is(instanceOf(Runnable.class)));
    }

    @Test
    public void should_return_declared_component_type_for_existing_list_property() throws Exception {
        resolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Cloneable.class);
        assertThat(resolver.getValue(Collections.EMPTY_LIST, 123)
                , is(instanceOf(Cloneable.class)));
    }

    @Test
    public void should_return_declared_component_type_for_existing_array_property() throws Exception {
        resolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Float.class);
        // Note: We return an int for any number as it can be coerced into any other number
        // i.e. is compatible with it
        assertThat(resolver.getValue(new Object[0], 456)
                , is(instanceOf(Number.class)));
    }

    @Test
    public void should_allow_any_index_for_indexed_array_property() throws Exception {
        // int property
        assertNotNull("Should return a fake value even though no such index in the target"
                , resolver.getValue(new Object[0], Integer.MAX_VALUE));
        // Object property
        assertNotNull("Should return a fake value even though no such index in the target"
                , resolver.getValue(new Object[0], new Integer(12345)));
    }

    @Test
    public void should_allow_any_index_for_indexed_collection_property() throws Exception {
        // int property
        assertNotNull("Should return a fake value even though no such index in the target"
                , resolver.getValue(Collections.EMPTY_LIST, Integer.MAX_VALUE));
        // Object property
        assertNotNull("Should return a fake value even though no such index in the target"
                , resolver.getValue(Collections.EMPTY_LIST, new Integer(12345)));
    }

}
