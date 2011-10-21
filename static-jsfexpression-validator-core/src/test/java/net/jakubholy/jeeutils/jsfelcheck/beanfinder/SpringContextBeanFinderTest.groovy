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

package net.jakubholy.jeeutils.jsfelcheck.beanfinder;


import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import static net.jakubholy.jeeutils.jsfelcheck.beanfinder.FileUtils.streamsToResourcesNullSafe

public class SpringContextBeanFinderTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void should_handle_empty_file() throws Exception {
        assert [] == finderForFile("springConfig-empty.xml").findDefinedBackingBeans()
    }

    @Test
    public void should_load_all_defined_beans_from_file() throws Exception {
        assert finderForFile("springConfig-some.xml").findDefinedBackingBeans()
             .collect {it.name}.sort() == ["bean1","bean2","bean3"]
    }

    @Test
    public void should_load_all_defined_beans_from_stream() throws Exception {
        def stream = getClass().getResourceAsStream("/springConfig-some.xml")
        assert finderForStream(stream).findDefinedBackingBeans()
             .collect {it.name}.sort() == ["bean1","bean2","bean3"]
    }

    def finderForFile(String fileName) {
        return finderForStream(new FileInputStream("src/test/resources/" + fileName))
    }

    def finderForStream(InputStream stream) {
        return SpringContextBeanFinder.forStreams(streamsToResourcesNullSafe([stream]))
    }
}
