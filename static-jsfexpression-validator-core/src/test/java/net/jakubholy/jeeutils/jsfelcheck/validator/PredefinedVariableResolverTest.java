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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import net.jakubholy.jeeutils.jsfelcheck.validator.PredefinedVariableResolver.NewVariableEncounteredListener;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PredefinedVariableResolverTest {

    private PredefinedVariableResolver resolver;
    @Mock private NewVariableEncounteredListener listener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        resolver = new PredefinedVariableResolver(listener);
    }

    @Test(expected = VariableNotFoundException.class)
    public void should_throw_exception_for_unknown_variable() throws Exception {
        resolver.resolveVariable("unknown");
    }

    @Test
    public void should_return_predefined_variable() throws Exception {
        resolver.declareVariable("var1", "Var1_Value");
        resolver.declareVariable("bean2", 222); // SUPPRESS CHECKSTYLE

        assertEquals("Var1_Value", resolver.resolveVariable("var1"));
        assertEquals(222, resolver.resolveVariable("bean2"));// SUPPRESS CHECKSTYLE
    }

    @Test
    public void should_fall_back_to_UnknownVariableResolver_if_variable_unknown() throws Exception {

        resolver.setUnknownVariableResolver(new ElVariableResolver() {
            public Class<?> resolveVariable(String name) {
                return List.class;
            }
        });

        Object result = resolver.resolveVariable("unknown");

        assertNotNull("Should be resolved by the UnknownVariableResolver", result);
        assertThat(result, is(instanceOf(List.class)));
    }

    @Test
    public void should_invoke_its_listener_when_variable_known() throws Exception {
        resolver.declareVariable("newVariableName", "value");
        resolver.resolveVariable("newVariableName");
        verify(listener).handleNewVariableEncountered("newVariableName");
    }

    @Test
    public void should_invoke_its_listener_when_variable_resolved_by_its_unknownVariableResolver() throws Exception {
        resolver.setUnknownVariableResolver(new ElVariableResolver() {
            public Class<?> resolveVariable(String name) {
                return List.class;
            }
        });
        resolver.resolveVariable("variableDelegatedToUVR");
        verify(listener).handleNewVariableEncountered("variableDelegatedToUVR");
    }

    @Test
    public void should_not_invoke_its_listener_when_unknown_variable_encountered() throws Exception {
        try {
            resolver.resolveVariable("unknownVariable");
        } catch (VariableNotFoundException e) { /* expected*/ } // SUPPRESS CHECKSTYLE
        verifyZeroInteractions(listener);
    }

}
