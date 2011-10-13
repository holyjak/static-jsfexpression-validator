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
import net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration
import org.junit.Before
import org.junit.Test
import static net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration.forExtraVariables
import static net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration.fromSpringConfigFiles

public class AbstractJsfStaticAnalyzerTest {

    private AbstractJsfStaticAnalyzer analyzer
    RecordingDummyElResolver recordingElValidator

    @Before
    public void setUp() {
        def testAnalyzer = new TestJsfStaticAnalyzerImpl()
        recordingElValidator = testAnalyzer.getResolver()
        analyzer = testAnalyzer;
    }

    @Test
    public void should_accept_local_variable_configuration() throws Exception {
        def config = new LocalVariableConfiguration()
        assert analyzer.withLocalVariablesConfiguration(config) == analyzer
        assert analyzer.getContextVariableRegistry() == config.toRegistry()
    }

    @Test(expected = IllegalArgumentException)
    public void should_deny_null_local_variable_configuration() throws Exception {
        analyzer.withLocalVariablesConfiguration(null)
    }

    @Test(expected = IllegalArgumentException)
    public void should_deny_null_managed_beans_and_variables_configuration() throws Exception {
        analyzer.withManagedBeansAndVariablesConfiguration(null)
    }

    @Test
    public void should_accept_managed_beans_and_variables_configuration() throws Exception {
        def v1 = "123"; def v2 = new Long(42)
        def config = new ManagedBeansAndVariablesConfiguration()
            .withExtraVariable("var1", v1)
            .withExtraVariable("var2", v2)
        assert analyzer.withManagedBeansAndVariablesConfiguration(config) == analyzer
        assert recordingElValidator.extraVariables == ["var1" : v1, "var2": v2]
        // Any way to check that the files will actually be loaded?!
    }

    @Test
    public void should_propagate_extra_variables_instance_to_resolver() throws Exception {
        def value = "someValue"
        analyzer.withManagedBeansAndVariablesConfiguration(forExtraVariables().withExtraVariable("myVariable", value))
        assert recordingElValidator.getExtraVariables() == ["myVariable" : value]
    }

    @Test
    public void should_propagate_type_overrides_to_resolver() throws Exception {
        analyzer.withPropertyTypeOverride("myBean.myProp", Integer)
        assert recordingElValidator.getPropertyTypeOverrides().find {
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
    public void should_support_spring_config_with_path_relative_import() throws Exception {

        assert analyzer.withManagedBeansAndVariablesConfiguration(
            fromSpringConfigFiles("src/test/resources/springConfig-with_import.xml" as File))
                .findSpringManagedBeans().find { it.name == "beanInImportedFile" }
    }

}
