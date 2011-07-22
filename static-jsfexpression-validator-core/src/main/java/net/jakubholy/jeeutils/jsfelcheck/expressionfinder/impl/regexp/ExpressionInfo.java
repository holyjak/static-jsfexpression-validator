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

/**
 * Represents EL expression found in a page source.
 */
public class ExpressionInfo {

    /**
     * What is the EL expected to return - either a method or an object (type).
     */
	public static enum ElType {
		VALUE, METHOD;
	}

	private final ElType type;
	private final String expression;

	ExpressionInfo(final String expression, final ElType type) {
		this.expression = expression;
		this.type = type;
	}

	/** @return Expression's type. */
	public ElType getType() {
		return type;
	}

	/** @return The EL expression itself. */
	public String getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
	    // CHECKSTYLE:OFF
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionInfo other = (ExpressionInfo) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (type != other.type)
			return false;
		return true;
        // CHECKSTYLE:ON
	}

}