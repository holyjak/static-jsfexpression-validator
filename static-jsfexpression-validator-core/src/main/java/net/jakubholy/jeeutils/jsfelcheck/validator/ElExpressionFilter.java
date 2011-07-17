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
 *  Can be added to the validator to include/exclude selected JSF EL expressions.
 */
public interface ElExpressionFilter {

	/**
	 * Should this EL expression be accepted for further processing, i.e. for validation?
	 * @param expression (required) representation of the EL expression found in a source view file
	 * @return true if the EL expression should be validated, false if it should not be validated
	 * (it will be added to a list of excluded expressions that can be checked later)
	 *
	 * @see net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults#excluded()
	 */
    boolean accept(ParsedElExpression expression);

}
