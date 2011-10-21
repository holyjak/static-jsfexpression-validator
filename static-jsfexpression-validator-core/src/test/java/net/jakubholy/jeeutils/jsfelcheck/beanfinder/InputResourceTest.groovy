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
import org.junit.Test

class InputResourceTest {

    private static final String FILE_PATH = "src/test/resources/test-text-file.txt"

    @Test(expected=IllegalArgumentException)
    public void constructor_should_fail_for_null_stream() throws Exception {
        new InputResource((InputStream) null)
    }

    @Test(expected=IllegalArgumentException)
    public void constructor_should_fail_for_null_file() throws Exception {
        new InputResource((File) null)
    }

    @Test
    public void resource_created_for_stream_should_return_it() throws Exception {
        def stream = new ReaderInputStream(new StringReader("text"))
        def resource = new InputResource(stream)
        assert resource.getStream() == stream
        assert resource.getFileIfAvailable() == null
    }

    @Test(expected=IllegalArgumentException)
    public void resource_creation_for_stream_should_fail_if_stream_closed() throws Exception {
        def stream = new ReaderInputStream(new StringReader("text"))
        stream.close()
        new InputResource(stream)
    }

    @Test
    public void resource_created_for_file_should_return_it() throws Exception {
        def file = new File(FILE_PATH)
        assert new InputResource(file).getFileIfAvailable() == file
    }

    @Test
    public void resource_created_for_file_should_return_it_as_stream_too() throws Exception {
        assert new InputResource(new File(FILE_PATH)).getStream() instanceof NamedInputStream
    }
}
