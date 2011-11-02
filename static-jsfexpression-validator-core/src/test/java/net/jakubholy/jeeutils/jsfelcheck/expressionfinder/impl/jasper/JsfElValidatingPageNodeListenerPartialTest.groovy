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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper

import org.junit.Before
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.ContextVariableRegistry
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Test

import static org.mockito.Mockito.*
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult
import net.jakubholy.jeeutils.jsfelcheck.validator.AttributeInfo
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNode

/**
 * BEWARE This test verifies currently only a very small part of the class
 */
class JsfElValidatingPageNodeListenerPartialTest {

    private static class FakeTagHandler {
        String firstAttribute
        Integer another
    }

    private JsfElValidatingPageNodeListener listener
    @Mock private JsfElValidator expressionValidator
    @Mock private ContextVariableRegistry contextVarRegistry

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this)
        listener = new JsfElValidatingPageNodeListener(expressionValidator, contextVarRegistry)

        when(expressionValidator.validateElExpression(anyString(), any(Class))).thenReturn(
                new SuccessfulValidationResult("#{faked value}", "Preset:OK")
        )
    }

    @Test
    public void nodeEntered_should_validate_all_expressions() throws Exception {
        def attributes = ["firstAttribute" : "#{expr1}", "another" : "#{expr2}"]
        def node = new PageNode("gName", FakeTagHandler, 333, attributes)


        when(expressionValidator.validateElExpression(anyString(), any(AttributeInfo))).thenReturn(
                new SuccessfulValidationResult("#{faked value}", "Preset:OK"));


        listener.nodeEntered(node)

        assert listener.getValidationResults().iterator().size() == 2

        verify(expressionValidator).validateElExpression("#{expr1}", new AttributeInfo("firstAttribute", String))
        verify(expressionValidator).validateElExpression("#{expr2}", new AttributeInfo("another", Integer))
        verifyNoMoreInteractions(expressionValidator)
    }
}
