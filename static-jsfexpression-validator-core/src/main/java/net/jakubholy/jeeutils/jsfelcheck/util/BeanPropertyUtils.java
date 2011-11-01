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

package net.jakubholy.jeeutils.jsfelcheck.util;

import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.util.logging.Logger;

/**
 * Utility to determine property type from its name and class.
 */
public class BeanPropertyUtils {

	private static class No_Class_Specified {}

	private static final Logger LOG = Logger.getLogger(BeanPropertyUtils.class.getName());

	private final Class<?> type;

	/**
	 * New instance to work on properties of the given class.
	 *
	 * @param type (required)
	 * @return
	 */
	public static BeanPropertyUtils forType(Class<?> type) {
		return new BeanPropertyUtils(type);
	}

	private BeanPropertyUtils(Class<?> type) {
		this.type = (type == null)? No_Class_Specified.class : type;
	}

	/**
	 * Find the type of the given property.
	 * @param propertyName (required)
	 * @return the type of the property or null if not found
	 */
	public Class<?> getPropertyTypeOf(String propertyName) {
		PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(type);
		for (PropertyDescriptor descriptor : descriptors) {
			if (descriptor.getName().equals(propertyName)) {
				return descriptor.getPropertyType();
			}
		}

		LOG.fine("No property '" + propertyName + "' found on the class " + type.getName());

		return null;
	}

}
