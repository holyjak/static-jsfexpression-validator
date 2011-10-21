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

package net.jakubholy.jeeutils.jsfelcheck.util

import org.junit.Test

class BeanPropertyUtilsTest {

    private static class Parent {
        public String getStringValue() {};
        public void setStringValue(String v) {};
    }

    private static class Child extends Parent {
        public Integer getIntegerValue() {};
        public void setIntegerValue(Integer v) {};

        public void setOnlySettableCharValue(char v) {};
    }

    @Test
    public void should_find_property_declared_in_the_class() throws Exception {
        assert Integer == BeanPropertyUtils.forType(Child).getPropertyTypeOf("integerValue")
    }

    @Test
    public void should_find_property_declared_in_parent_class() throws Exception {
        assert String == BeanPropertyUtils.forType(Child).getPropertyTypeOf("stringValue")
    }

    @Test
    public void should_find_property_even_if_it_only_has_a_setter_and_no_getter() throws Exception {
        assert char.class == BeanPropertyUtils.forType(Child).getPropertyTypeOf("onlySettableCharValue")
    }
}
