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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.regexp;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JsfElMatcherTest {

	private static final ExpressionInfo.ElType EXP_VALUE = ExpressionInfo.ElType.VALUE;
	private static final ExpressionInfo.ElType EXP_METHOD = ExpressionInfo.ElType.METHOD;

	@Test
	public void should_find_all_value_bindings() {
		String expressions = " space #{simpleBean}"
			+ "#{bean.property}	"
			+ "#{ spacedBean } "
			+ "#{bean['key'].property} EOF";

		JsfElMatcher matcher = JsfElMatcher.forText(expressions);

		assertEquals(new ExpressionInfo("#{simpleBean}", EXP_VALUE), matcher.findNext());
		assertEquals(new ExpressionInfo("#{bean.property}", EXP_VALUE), matcher.findNext());
		assertEquals(new ExpressionInfo("#{ spacedBean }", EXP_VALUE), matcher.findNext());
		assertEquals(new ExpressionInfo("#{bean['key'].property}", EXP_VALUE), matcher.findNext());
		assertEquals(null, matcher.findNext());
	}

	@Test
	public void should_find_simple_method_binding() {
		String expressions = "action='#{bean.method}'"
			+ "action = \" #{ bean2.method2 } \""
			+ "actionListener	=	\"	#{	b3.m3	}	\""
			+ "actionListener= '#{objekt.action}'";

		JsfElMatcher matcher = JsfElMatcher.forText(expressions);

		assertEquals(new ExpressionInfo("#{bean.method}", EXP_METHOD), matcher.findNext());
		assertEquals(new ExpressionInfo("#{ bean2.method2 }", EXP_METHOD), matcher.findNext());
		assertEquals(new ExpressionInfo("#{	b3.m3	}", EXP_METHOD), matcher.findNext());
		assertEquals(new ExpressionInfo("#{objekt.action}", EXP_METHOD), matcher.findNext());
		assertEquals(null, matcher.findNext());
	}

}
