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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static net.jakubholy.jeeutils.jsfelcheck.validator.jsf12.MethodFakingFunctionMapper.FAKE_METHODS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.*;

public class MethodFakingFunctionMapperTest {

    private MethodFakingFunctionMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new MethodFakingFunctionMapper();
    }

    @Test
    public void should_return_fakeMethod0_for_no_args_call() throws Exception {
        mapper.setCurrentExpression("#{dummy0:myMethod0()}");
        Method actualMethod = mapper.resolveFunction("dummy0", "myMethod0");
        assertSame(FAKE_METHODS[0], actualMethod);
        assertEquals(0, actualMethod.getParameterTypes().length);
    }

    @Test
    public void should_return_fakeMethod1_for_one_args_call() throws Exception {
        mapper.setCurrentExpression("#{pref1:anotherMethod('argument 1')}");
        Method actualMethod = mapper.resolveFunction("pref1", "anotherMethod");
        assertSame(FAKE_METHODS[1], actualMethod);
        assertEquals(1, actualMethod.getParameterTypes().length);
    }

    @Test
    public void should_resolve_all_function_in_expression() throws Exception {
        mapper.setCurrentExpression("#{pref0:firstMethod() + pref1:anotherMethod('argument 1')}");
        Method actualFirstMethod = mapper.resolveFunction("pref0", "firstMethod");
        Method actualSecondMethod = mapper.resolveFunction("pref1", "anotherMethod");
        assertSame(FAKE_METHODS[0], actualFirstMethod);
        assertSame(FAKE_METHODS[1], actualSecondMethod);
    }

    @Test
    public void should_remember_all_functions_encountered_in_the_last_expression() throws Exception {

        // First EL and its functions
        mapper.setCurrentExpression("#{pref0:firstMethod() + pref1:anotherMethod('argument 1')}");
        mapper.resolveFunction("pref0", "firstMethod");
        mapper.resolveFunction("pref1", "anotherMethod");
        assertThat(mapper.getLastExpressionsFunctionQNames(), containsInAnyOrder("pref0:firstMethod", "pref1:anotherMethod"));

        // Second EL and its function
        mapper.setCurrentExpression("#{pref3:yetAnotherMethod()}");
        mapper.resolveFunction("pref3", "yetAnotherMethod");
        assertThat(mapper.getLastExpressionsFunctionQNames(), containsInAnyOrder("pref3:yetAnotherMethod"));

    }

    @Test
    public void should_initially_return_empty_functions_encountered() throws Exception {
        assertThat(mapper.getLastExpressionsFunctionQNames(), Matchers.<String>empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_fail_if_arity_over_5() throws Exception {
        mapper.setCurrentExpression("#{p:tooManyArgsMethod(1,2,3,4,5,6)}");
        mapper.resolveFunction("p", "tooManyArgsMethod");
    }

    @Test
    public void returned_function_shall_return_non_null_value() throws Exception {
        mapper.setCurrentExpression("#{dummy0:myMethod0()}");
        Method actualMethod = mapper.resolveFunction("dummy0", "myMethod0");
        Object fakeMethodResult = actualMethod.invoke(MethodFakingFunctionMapper.class);
        assertEquals("The fake methods should not return null but a generic, easily coercible value"
                , ""
                , fakeMethodResult);
    }

}
