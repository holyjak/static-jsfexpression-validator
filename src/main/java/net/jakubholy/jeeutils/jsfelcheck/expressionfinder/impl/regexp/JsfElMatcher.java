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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * RegExp matcher for JSF EL expression attributes.
 */
public class JsfElMatcher {

	private static final String methodBindingAttributeRE = "(?:action(?:Listener)?)";
	private static final String assignmentDeclarationRE = "\\s*=\\s*(?:'|\")\\s*";
	private static final String expressionRE = "(#\\{.*?\\})";

	private static final Pattern elPattern = Pattern.compile(
			"(" + methodBindingAttributeRE + assignmentDeclarationRE + ")?" + expressionRE);

	public static JsfElMatcher forText(final String source) {
		return new JsfElMatcher(elPattern.matcher(source));
	}

	private final Matcher matcher;

	private JsfElMatcher(final Matcher matcher) {
		this.matcher = matcher;
	}

	public ExpressionInfo findNext() {
		if (matcher.find()) {
			String expression = matcher.group(2);
			boolean valueBinding = matcher.group(1) == null;
			ExpressionInfo.ElType type = valueBinding? ExpressionInfo.ElType.VALUE : ExpressionInfo.ElType.METHOD;
			return new ExpressionInfo(expression, type);
		}
		return null;
	}


}
