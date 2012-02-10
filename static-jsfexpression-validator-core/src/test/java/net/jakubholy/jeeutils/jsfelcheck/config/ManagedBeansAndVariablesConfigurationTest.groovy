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

import org.junit.Before
import org.junit.Test
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.InputResource

class ManagedBeansAndVariablesConfigurationTest {

    private static final String RESOURCE_DIR = "src/test/resources";

    private ManagedBeansAndVariablesConfiguration config;

    @Before
    public void setUp() {
        config = new ManagedBeansAndVariablesConfiguration();
    }

    // ------------------------------------------------------------ FACES

    @Test
    public void andFromFacesConfigFiles_should_store_them() throws Exception {
        Collection<File> facesConfigFiles = [new File("$RESOURCE_DIR/test-text-file.txt")];
        assert config.andFromFacesConfigFiles(facesConfigFiles).getFacesConfigStreams().size() == 1
        def stream = firstStream(config.getFacesConfigStreams())
        assert stream.getText() == "from text file"
        assert config.getSpringConfigStreams().isEmpty()
    }

    @Test
    public void static_fromFacesConfigFiles_should_store_them_too() throws Exception {
        Collection<File> facesConfigFiles = [new File("$RESOURCE_DIR/test-text-file.txt")];
        def stream = firstStream(ManagedBeansAndVariablesConfiguration.fromFacesConfigFiles(facesConfigFiles)
            .getFacesConfigStreams())
        assert stream.getText() == "from text file"
    }

    @Test
    public void andFromFacesConfigStreams_should_store_them() throws Exception {
        def stream = new StringBufferInputStream("1234");
        Collection<InputStream> facesConfigStreams = [stream];
        assert config.andFromFacesConfigStreams(facesConfigStreams)
            .getFacesConfigStreams().toArray() == asResourceArray(stream)
        assert config.getSpringConfigStreams().isEmpty()
    }

    @Test
    public void static_fromFacesConfigStreams_should_store_them_too() throws Exception {
        def stream = new StringBufferInputStream("1234");
        Collection<InputStream> facesConfigStreams = [stream];
        assert ManagedBeansAndVariablesConfiguration.fromFacesConfigStreams(facesConfigStreams)
            .getFacesConfigStreams().toArray() == asResourceArray(stream)
    }

    @Test
    public void andFromFacesConfigFiles_should_replace_null_with_empty_collection() throws Exception {
        assert config.andFromFacesConfigFiles(null).getFacesConfigStreams().empty
    }

    @Test
    public void andFromFacesConfigStreams_should_replace_null_with_empty_collection() throws Exception {
        assert config.andFromFacesConfigStreams(null).getFacesConfigStreams().empty
    }

    // ------------------------------------------------------------ SPRING

    @Test
    public void andFromSpringConfigFiles_should_store_them() throws Exception {
        Collection<File> springConfigFiles = [new File("$RESOURCE_DIR/test-text-file.txt")];
        assert config.andFromSpringConfigFiles(springConfigFiles).getSpringConfigStreams().size() == 1
        def stream = firstStream(config.getSpringConfigStreams())
        assert stream.getText() == "from text file"
        assert config.getFacesConfigStreams().isEmpty()
    }

    @Test
    public void static_fromSpringConfigFiles_should_store_them_too() throws Exception {
        Collection<File> springConfigFiles = [new File("$RESOURCE_DIR/test-text-file.txt")];
        def stream = firstStream(ManagedBeansAndVariablesConfiguration.fromSpringConfigFiles(springConfigFiles)
            .getSpringConfigStreams())
        assert stream.getText() == "from text file"
    }

    @Test
    public void andFromSpringConfigStreams_should_store_them() throws Exception {
        def stream = new StringBufferInputStream("1234");
        Collection<InputStream> springConfigStreams = [stream];
        assert config.andFromSpringConfigStreams(springConfigStreams)
            .getSpringConfigStreams().toArray() == asResourceArray(stream)
        assert config.getFacesConfigStreams().isEmpty()
    }

    @Test
    public void static_fromSpringConfigStreams_should_store_them_too() throws Exception {
        def stream = new StringBufferInputStream("1234");
        Collection<InputStream> springConfigStreams = [stream];
        assert ManagedBeansAndVariablesConfiguration.fromSpringConfigStreams(springConfigStreams)
            .getSpringConfigStreams().toArray() == asResourceArray(stream)
    }

    @Test
    public void andFromSpringConfigFiles_should_replace_null_with_empty_collection() throws Exception {
        assert config.andFromSpringConfigFiles(null).getSpringConfigStreams().empty
    }

    @Test
    public void andFromSpringConfigStreams_should_replace_null_with_empty_collection() throws Exception {
        assert config.andFromSpringConfigStreams(null).getSpringConfigStreams().empty
    }

    // ------------------------------------------------------------ VARIABLES

    @Test
    public void withExtraVariable_for_class_should_convert_it_to_andstance_and_store_it() throws Exception {
        assert config
            .withExtraVariable("myVar", String)
            .withExtraVariable("another", Integer)
            .getExtraVariables().collectEntries { k, v -> [(k): v.getClass()]} == ["myVar": String, "another" : Integer]
    }

    @Test
    public void withExtraVariable_for_object_should_store_it() throws Exception {
        def v1 = "123"; def v2 = new Integer(42);
        assert config
            .withExtraVariable("myVar", v1)
            .withExtraVariable("another", v2)
            .getExtraVariables() == ["myVar": v1, "another" : v2]
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_extra_variable_without_name() throws Exception {
        config.withExtraVariable(null, String)
    }

    @Test(expected=IllegalArgumentException)
    public void should_fail_for_extra_variable_without_value() throws Exception {
        config.withExtraVariable("myVariable", null)
    }

    @Test
    public void static_forExtraVariables_returns_empty_instance() throws Exception {
        assert ManagedBeansAndVariablesConfiguration.forExtraVariables() instanceof ManagedBeansAndVariablesConfiguration
    }

    // ------------------------------------------------------------ ANNOTATED BEANS AUTO-DETECTION


    @Test
    public void static_fromAnnotatedBeans_should_store_them() throws Exception {
        Collection<File> springConfigFiles = [new File("$RESOURCE_DIR/test-text-file.txt")];
        def stream = firstStream(ManagedBeansAndVariablesConfiguration.fromSpringConfigFiles(springConfigFiles)
                .getSpringConfigStreams())
        assert stream.getText() == "from text file"
    }


    // ----------------------------------------------------------------------------------------
    def asResourceArray(stream) {
        return [new InputResource(stream)].toArray()
    }

    def firstStream(configResources) {
        return configResources.iterator().next().stream
    }
}
