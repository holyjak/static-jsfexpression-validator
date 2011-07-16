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

import java.util.Iterator;


/**
 * Find JSF EL expressions in text using regular expressions and
 * iterate over the results.
 */
public class JsfElFinder implements Iterable<ExpressionInfo> {

	private static class ExpressionInfoIterator implements Iterator<ExpressionInfo> {

		private final JsfElMatcher expressionMatcher;
		private ExpressionInfo lastFound;

		public ExpressionInfoIterator(final JsfElMatcher expressionMatcher) {
			this.expressionMatcher = expressionMatcher;
		}

		//@Override
		public boolean hasNext() {
			lastFound = expressionMatcher.findNext();
			return lastFound != null;
		}

		//@Override
		public ExpressionInfo next() {
			return lastFound;
		}

		//@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private final String sourceText;

	/**
	 * Finder for EL expressions in the given (JSP page) source code.
	 * @param sourceText (required)
	 */
	public JsfElFinder(final String sourceText) {
		this.sourceText = sourceText;
	}

	/**
	 * Search for EL expressions in the source.
	 * @return Iterator over the expressions found
	 */
	public Iterator<ExpressionInfo> iterator() {
		return new ExpressionInfoIterator(
				JsfElMatcher.forText(sourceText));
	}



}
