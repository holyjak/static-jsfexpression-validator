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
public final class JsfElMatcher {

	private static final String METHOD_BINDING_ATTRIBUTE_RE = "(?:action(?:Listener)?)";
	private static final String ASSIGNMENT_DECLARATION_RE = "\\s*=\\s*(?:'|\")\\s*";
	private static final String EXPRESSION_RE = "(#\\{.*?\\})";

	private static final Pattern EL_PATTERN = Pattern.compile(
			"(" + METHOD_BINDING_ATTRIBUTE_RE + ASSIGNMENT_DECLARATION_RE + ")?" + EXPRESSION_RE);

	/**
	 * Creates a matcher for the given text.
	 * @param source (required) the text (usually JSP page source code) to check for EL expressions
	 * @return matcher containing the EL expressions found
	 */
	public static JsfElMatcher forText(final String source) {
		return new JsfElMatcher(EL_PATTERN.matcher(source));
	}

	private final Matcher matcher;

	private JsfElMatcher(final Matcher matcher) {
		this.matcher = matcher;
	}

	/**
	 * Returns the next expression.
	 * @return the next expression found or null if no more expressions
	 */
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
