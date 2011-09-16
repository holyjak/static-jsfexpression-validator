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
package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12

import org.junit.Test
import org.junit.Beforeimport org.junit.Ruleimport org.junit.rules.ExpectedException
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;
public class Jsf12FacesConfigXmlBeanFinderTest {

    private Jsf12FacesConfigXmlBeanFinder beanFinder;

    @Rule ExpectedException thrown = ExpectedException.none();

    @Before
    void setUp() {
        beanFinder = new Jsf12FacesConfigXmlBeanFinder()
    }

     @Test
     public void should_fail_if_nonexistent_file_supplied() throws Exception {
         finderForFile("/no/such/file").findDefinedBackingBeans()
         fail("Should have failed!");
     }

     @Test
     public void should_accept_file_with_no_beans() throws Exception {
         assert finderForFile("faces-config-no_beans.xml").findDefinedBackingBeans().isEmpty()
     }

     @Test
     public void should_throw_descriptive_error_when_invalid_xml() throws Exception {
         thrown.expect(RuntimeException.class);
         thrown.expectMessage(allOf(
                 containsString("faces-config-invalid_xml.xml")
                 , containsString("XML parsing failed")));
         finderForFile("faces-config-invalid_xml.xml").findDefinedBackingBeans()
     }

     @Test
     public void should_be_able_to_parse_config_file_in_jar() throws Exception {
         fail("TBD")
     }

     @Test
     public void should_fail_for_invalid_url() throws Exception {
         fail("TBD")
     }

     @Test
     public void should_return_all_beans_in_the_file() throws Exception {
         fail("TBD")
     }

     @Test
     public void should_return_also_resource_bundle_variables() throws Exception {
         fail("TBD")
     }

     def finderForFile(fileName) {
         beanFinder.setFacesConfigFiles([new File(fileName)])
         return beanFinder
     }



}
