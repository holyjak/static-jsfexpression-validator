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

package net.jakubholy.jeeutils.jsfelcheck.beanfinder

import org.apache.tools.ant.util.ReaderInputStream
import org.junit.Before
import org.junit.Test
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder.ManagedBeanDescriptor;

class AbstractFacesConfigXmlBeanFinderTest {

    private static class TestAbstractFacesConfigXmlBeanFinderImpl extends AbstractFacesConfigXmlBeanFinder {

        @Override
        protected Collection<ManagedBeanDescriptor> parseFacesConfig(InputStream facesConfigStream) {
            return [new ManagedBeanDescriptor("bean4" + facesConfigStream.getText(), String)]
        }

    }

    private TestAbstractFacesConfigXmlBeanFinderImpl finder

    @Before
    public void setUp() {
        finder = new TestAbstractFacesConfigXmlBeanFinderImpl()
    }

    @Test
    public void should_parse_each_config_file() throws Exception {
        finder.setFacesConfigResources([resource("firstContent"), resource("secondContent")])
        assert finder.findDefinedBackingBeans().collect {it.name} == ["bean4firstContent", "bean4secondContent"]
    }

    @Test
    public void should_remember_input_resources() throws Exception {
        def resources = [resource("firstContent"), resource("secondContent")]
        assert finder.setFacesConfigResources(resources).getFacesConfigResources() == resources
    }

    // --------------------------------------------------------------------------------------------------

    def resource(string) {
        return new InputResource(new ReaderInputStream(new StringReader(string)))
    }
}
