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

package net.jakubholy.jeeutils.jsfelcheck.config

import org.junit.Test

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.DataTableVariableResolver
import org.junit.Before
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.TagJsfVariableResolver
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.VariableInfo
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.AttributesValidationResult


class LocalVariableConfigurationTest {

    private class MemorisingDataTableResolver extends DataTableVariableResolver {

        @Override
        DataTableVariableResolver declareTypeFor(String jsfExpression, Class<?> type) {
            LocalVariableConfigurationTest.this.variables.put(jsfExpression, type)
            return super.declareTypeFor(jsfExpression, type)
        }
        
    }

    private Map<String, Class<?>> variables
    private LocalVariableConfiguration config
    private DataTableVariableResolver resolver

    @Before
    public void setUp() {
        variables = [:]
        resolver = new MemorisingDataTableResolver()
        config = new LocalVariableConfiguration(resolver)
    }

    @Test
    public void toRegistry_on_empty_configuration_returns_non_null() throws Exception {
        assert null != new LocalVariableConfiguration().toRegistry()
    }

    @Test
    public void registers_data_table_resolver_with_the_context() throws Exception {
        assert resolversIn(config) == ["h:dataTable" : resolver]
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_null_custom_datatable_tag_alias() throws Exception {
        config.withCustomDataTableTagAlias(null)
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_empty_custom_datatable_tag_alias() throws Exception {
        config.withCustomDataTableTagAlias("")
    }

    @Test
    public void should_register_resolver_for_custom_datatable_tag_alias() throws Exception {
        assert resolversIn(config.withCustomDataTableTagAlias("my:dataTableAlias")) ==
                ["h:dataTable" : resolver, "my:dataTableAlias" : resolver]
    }

    @Test
    public void should_register_custom_resolver_for_tag() throws Exception {
        def customResolver = new TagJsfVariableResolver() {
            VariableInfo extractContextVariables(Map<String, String> tagAttributes, AttributesValidationResult resolvedJsfExpressions) {
                return null
            }
        };
        assert resolversIn(config.withResolverForVariableProducingTag("my:customTag", customResolver))
            .subMap(["my:customTag"]) == ["my:customTag" : customResolver]
    }

    @Test
    def void should_propagate_variable_declaration_to_data_table_resolver() {
        config.withLocalVariable("myElExpr", String)
            .withLocalVariable("myExpr2", Integer)
        assert variables == ["myElExpr" : String, "myExpr2" : Integer]
    }

    // #####################################################################################

    def resolversIn(config) {
        return config.toRegistry().getRegisteredResolvers()
    }
}
