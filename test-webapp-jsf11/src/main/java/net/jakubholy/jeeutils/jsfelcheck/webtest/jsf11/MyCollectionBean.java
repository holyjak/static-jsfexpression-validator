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

package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf11;

import java.util.Arrays;
import java.util.Collection;

public class MyCollectionBean {

    public static class ValueHolder {
        private final Object value;

        public ValueHolder(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    private static ValueHolder val(Object value) {
        return new ValueHolder(value);
    }

    public Collection getList() {
        return Arrays.asList(val("item1"), val("item2"), val("item2"));
    }

    public String getGreeting() {
        return "Hi from " + getClass().getName() + "!";
    }

	public String actionMethod() {
		return null;
	}
}
