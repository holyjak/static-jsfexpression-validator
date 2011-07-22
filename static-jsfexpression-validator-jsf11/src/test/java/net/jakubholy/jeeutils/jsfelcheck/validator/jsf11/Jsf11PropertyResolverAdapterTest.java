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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import javax.faces.el.PropertyNotFoundException;

import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.MockObjectOfUnknownType;
import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ParsedElExpression;

import org.junit.Before;
import org.junit.Test;


public class Jsf11PropertyResolverAdapterTest {

    private static class MyVariable {
        public String getAcceptedProperty() { return ""; };
        public String getDeniedProperty() { return ""; };
    }

    private static class AcceptingFiler implements ElExpressionFilter {

        private boolean called = false;

        public boolean accept(ParsedElExpression expression) {
            called = true;
            return true;
        }

        public boolean isCalled() {
            return called;
        }

    }

    /** Name of the managed bean variable "encountered" during the current resolution process. */
    private static final String CURRENT_VARIABLE = "bean";
    private Jsf11PropertyResolverAdapter resolver;
    private MyVariable currentVariable;
    private MockingPropertyResolver trueResolver;

    @Before
    public void setUp() {
        currentVariable = new MyVariable();
        trueResolver = new MockingPropertyResolver();
        resolver = new Jsf11PropertyResolverAdapter(trueResolver);
        trueResolver.handleNewVariableEncountered(CURRENT_VARIABLE);
    }

    @Test(expected = PropertyNotFoundException.class)
    public void should_throw_exception_for_unknown_property() throws Exception {
        resolver.getValue(currentVariable, "unknownProperty");
    }

    @Test
    public void should_return_MockObjectOfUnknownType_for_existing_mapped_property_without_declard_component_type()
        throws Exception {
        assertThat(resolver.getValue(Collections.EMPTY_MAP, "unknownProperty")
                , is(instanceOf(MockObjectOfUnknownType.class)));
    }

    @Test
    public void should_return_MockObjectOfUnknownType_for_existing_list_property_without_declard_component_type()
        throws Exception {
        assertThat(resolver.getValue(Collections.EMPTY_LIST, randomIndex())
                , is(instanceOf(MockObjectOfUnknownType.class)));
    }

    @Test
    public void should_return_MockObjectOfUnknownType_for_existing_array_property_without_declard_component_type()
        throws Exception {
        assertThat(resolver.getValue(new Object[0], randomIndex())
                , is(instanceOf(MockObjectOfUnknownType.class)));
    }

    @Test
    public void should_return_string_for_existing_string_array_property() throws Exception {
        assertThat(resolver.getValue(new String[0], randomIndex())
                , is(instanceOf(String.class)));
    }

    @Test
    public void should_return_declared_component_type_for_existing_mapped_property() throws Exception {
        trueResolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Runnable.class);
        assertThat(resolver.getValue(Collections.EMPTY_MAP, "unknownProperty")
                , is(instanceOf(Runnable.class)));
    }

    @Test
    public void should_return_declared_component_type_for_existing_list_property() throws Exception {
        trueResolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Cloneable.class);
        assertThat(resolver.getValue(Collections.EMPTY_LIST, randomIndex())
                , is(instanceOf(Cloneable.class)));
    }

    @Test
    public void should_return_declared_component_type_for_existing_array_property() throws Exception {
        trueResolver.definePropertyTypeOverride(CURRENT_VARIABLE + ".*", Float.class);
        // Note: We return an int for any number as it can be coerced into any other number
        // i.e. is compatible with it
        assertThat(resolver.getValue(new Object[0], randomIndex())
                , is(instanceOf(Number.class)));
    }

    private int randomIndex() {
        final int maxIndex = 1000;
        return (int) Math.random() * maxIndex;
    }

}
