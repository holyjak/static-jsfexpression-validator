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

import org.junit.*

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.TagJsfVariableResolver
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.VariableInfo

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.AttributesValidationResult
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNode
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;

public class AbstractJsfStaticAnalyzerTest {

    private final PageNode fakeTag = new PageNode("my:custTag", Object.class, -1, ["var":"myVar", value:"someElHere"])
    private final AttributesValidationResult emptyAttributes = new AttributesValidationResult()

    private AbstractJsfStaticAnalyzer analyzer;

    @Before
    public void setUp() {
        analyzer = new TestJsfStaticAnalyzerImpl();
    }

    @Test
    public void should_add_custom_tag_variable_resolver() {
        TagJsfVariableResolver customResolver = new TagJsfVariableResolver() {
            VariableInfo extractContextVariables(Map<String, String> ignore1, AttributesValidationResult ignore2) {
            return new VariableInfo("myVar", String)
            }
        }

        analyzer.registerTagVariableResolver(fakeTag.getQName(), customResolver)
        assertCustomResolverUsed()
    }

    @Test
    public void should_reregister_dataTableResolver_under_custom_name() throws Exception {

        def attrs = new AttributesValidationResult()
        attrs.add("value", new SuccessfulValidationResult("I'm a value"))

        analyzer.registerDataTableTag(fakeTag.getQName())
        assertCustomResolverUsed(attrs)
    }

    private def assertCustomResolverUsed(attrs = emptyAttributes) {
        analyzer.getContextVariableRegistry().with {
            extractContextVariables(fakeTag, attrs)
            assert resolveVariable("myVar") != null
        }
    }

}
