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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Iterator;

import net.jakubholy.jeeutils.jsfelcheck.validator.exception.ExpressionRejectedByFilterException;

import org.junit.Before;
import org.junit.Test;


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

    private MockingPropertyResolver resolver;

    @Before
    public void setUp() {
        resolver = new MockingPropertyResolver();
        resolver.handleNewVariableEncountered("bean");
    }

    @Test
    public void should_keep_current_type_if_no_override() throws Exception {
        assertSame(String.class
                , resolver.determineFinalType("property", String.class));
        assertSame(URL.class
                , resolver.determineFinalType("property", URL.class));
    }

    @Test
    public void should_respect_component_type_override() throws Exception {
        resolver.definePropertyTypeOverride("bean.*", Integer.class);

        assertSame(Integer.class
                , resolver.determineFinalType("mapKey", String.class));
    }

    @Test
    public void should_respect_property_type_override() throws Exception {
        resolver.definePropertyTypeOverride("bean.property", URL.class);

        assertSame(URL.class
                , resolver.determineFinalType("property", Math.class));
    }

    @Test
    public void should_prioritize_property_over_component_type_override() throws Exception {
        resolver.definePropertyTypeOverride("bean.property", URL.class);
        resolver.definePropertyTypeOverride("bean.*", Integer.class);


        assertSame(Integer.class
                , resolver.determineFinalType("nonOverridenProperty", String.class));
        assertSame(URL.class
                , resolver.determineFinalType("property", String.class));
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
        resolver.getValue(new MyVariable(), "acceptedProperty"); // shall pass

        // Reset, try variable.acceptedProperty
        resolver.handleNewVariableEncountered("variable"); // shall pass ok
        try {
            resolver.getValue(new MyVariable(), "deniedProperty");
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

        resolver.handleNewVariableEncountered("dummyVar");

        resolver.addElExpressionFilter(filter1);
        resolver.addElExpressionFilter(filter2);

        resolver.getValue(new MyVariable(), "acceptedProperty");

        assertTrue(filter1.isCalled());
        assertTrue(filter2.isCalled());
    }

}
