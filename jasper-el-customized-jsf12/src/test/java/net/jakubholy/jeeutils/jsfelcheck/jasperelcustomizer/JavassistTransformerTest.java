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

package net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer;

import org.apache.el.parser.AstOr;
import org.junit.Test;

import static org.junit.Assert.fail;

public class JavassistTransformerTest {

    @Test(expected = UnsupportedOperationException.class)
    public void should_fail() throws Exception {
        new AstOr(0).getValue(null);
        System.out.println("DONE " + getClass());
    }

    @Test
    public void todo_implement_proper_tests() {
        fail("Implement proper tests!");
    }


}
