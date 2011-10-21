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

package net.jakubholy.jeeutils.jsfelcheck.validator;

/**
 * Basic info about an attribute that has an EL assigned to it.
 */
public class AttributeInfo {

	private final String attributeName;
	private final Class<?> attributeType;

	/**
	 * @param attributeName (required)
	 * @param attributeType (required) the type of the attribute this expression is assigned to;
	 */
	public AttributeInfo(String attributeName, Class<?> attributeType) {
		this.attributeName = attributeName;
		this.attributeType = attributeType;
	}

	public Class<?> getAttributeType() {
		return attributeType;
	}

	public String getAttributeName() {
		return attributeName;
	}

	@Override
	public String toString() {
		return "Attribute{" +
				"name='" + attributeName + '\'' +
				", type=" + attributeType +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AttributeInfo that = (AttributeInfo) o;

		if (attributeName != null ? !attributeName.equals(that.attributeName) : that.attributeName != null)
			return false;
		if (attributeType != null ? !attributeType.equals(that.attributeType) : that.attributeType != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = attributeName != null ? attributeName.hashCode() : 0;
		result = 31 * result + (attributeType != null ? attributeType.hashCode() : 0);
		return result;
	}
}
