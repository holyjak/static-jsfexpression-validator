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

package net.jakubholy.jeeutils.jsfelcheck

import net.jakubholy.jeeutils.jsfelcheck.TestJsfStaticAnalyzerImpl.RecordingDummyElResolver
import net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration
import org.junit.Before
import org.junit.Test

public class AbstractJsfStaticAnalyzerTest {

    private AbstractJsfStaticAnalyzer analyzer
    RecordingDummyElResolver recordingResolver

    @Before
    public void setUp() {
        def testAnalyzer = new TestJsfStaticAnalyzerImpl()
        recordingResolver = testAnalyzer.getResolver()
        analyzer = testAnalyzer;
    }

    @Test
    public void should_accept_local_variable_configuration() throws Exception {
        def config = new LocalVariableConfiguration()
        analyzer.withLocalVariablesConfiguration(config)
        assert analyzer.getContextVariableRegistry() == config.toRegistry()
    }

    @Test
    public void should_propagate_type_overrides_to_resolver() throws Exception {
        analyzer.withPropertyTypeOverride("myBean.myProp", Integer)
        assert recordingResolver.getPropertyTypeOverrides().find {
            it.key == "myBean.myProp" && it.value == Integer.class
        }
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_type_override_without_name() throws Exception {
        analyzer.withPropertyTypeOverride(null, Integer)
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_type_override_without_value() throws Exception {
        analyzer.withPropertyTypeOverride("name", null)
    }

    @Test
    public void should_propagate_extra_variables_to_resolver() throws Exception {
        analyzer.withExtraVariable("myVariable", Math)
        assert recordingResolver.getExtraVariables() == ["myVariable" : Math]
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_extra_variable_without_name() throws Exception {
        analyzer.withExtraVariable(null, String)
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_extra_variable_without_value() throws Exception {
        analyzer.withExtraVariable("myVariable", null)
    }

}
