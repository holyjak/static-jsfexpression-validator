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
package net.jakubholy.jeeutils.jsfelcheck.beanfinder.jsf12

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.containsString
import static org.junit.Assert.fail
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.InputResource

public class Jsf12FacesConfigXmlBeanFinderTest {

    private Jsf12FacesConfigXmlBeanFinder beanFinder;

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Before
    void setUp() {
        beanFinder = new Jsf12FacesConfigXmlBeanFinder()
    }

     @Test
     public void should_fail_if_nonexistent_file_supplied() throws Exception {
         thrown.expect(IllegalArgumentException)
         thrown.expectMessage(containsString("/no/such/file"))
         finderForFile("/no/such/file").findDefinedBackingBeans()
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
                 , containsString("Failed to parse")));
         finderForFile("faces-config-invalid_xml.xml").findDefinedBackingBeans()
     }

     @Test(expected=RuntimeException)
     public void should_fail_for_closed_stream() throws Exception {
         def stream = getClass().getResourceAsStream("/faces-config-no_beans.xml")
         stream.close()
         beanFinder.setFacesConfigResources([stream]).findDefinedBackingBeans()
         fail("Should have failed because of a closed stream")
     }

     @Test
     public void should_close_supplied_stream() throws Exception {
         def stream = getClass().getResourceAsStream("/faces-config-no_beans.xml")
         beanFinder.setFacesConfigResources([new InputResource(stream)]).findDefinedBackingBeans()
         try {
            stream.read()
            fail("should have failed because of the stream being closed")
         } catch (IOException e) {
             if (!e.getMessage().contains("Stream closed")) {
                 fail("Unexpected exception: $e")
             }
         }
     }

     @Test
     public void should_return_all_beans_in_the_file() throws Exception {
         assert finderForFile("faces-config-some_beans.xml").findDefinedBackingBeans()
             .collect {it.name}.sort() == ["bean1","bean2"]
     }

     @Test
     public void should_return_also_resource_bundle_variables() throws Exception {
         assert finderForFile("faces-config-resource_bundle_var.xml").findDefinedBackingBeans()
            .collect {it.name} == ["resourceBundleFromFacesXml"]
     }

     def finderForFile(fileName) {
         return beanFinder.setFacesConfigResources([new InputResource(
                 new File("src/test/resources/" + fileName))])
     }

     def finderForStream(fileName) {
         return beanFinder.setFacesConfigResources([new InputResource(
                 getClass().getResourceAsStream("/$fileName"))])
     }



}
