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

package net.jakubholy.jeeutils.jsfelcheck.validator.results;

/**
 * Result for a JSF EL expression that was successfully validated.
 */
public class SuccessfulValidationResult extends ValidationResult {

    private final Object expressionResult;

    /**
     * Result for EL expression whose evaluation produced the given value.
     * In the context of the "fake" validating resolver this is usually a "fake value" of the expression's output type.
     * @param expressionResult (optional) result of evaluating the EL
     *
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory
     */
    public SuccessfulValidationResult(Object expressionResult) {
        this.expressionResult = expressionResult;
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    /**
     * @return the value produced by the resolved expression.
     */
    public Object getExpressionResult() {
        return expressionResult;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((expressionResult == null) ? 0 : expressionResult.hashCode());
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
        SuccessfulValidationResult other = (SuccessfulValidationResult) obj;
        if (expressionResult == null) {
            if (other.expressionResult != null)
                return false;
        } else if (!expressionResult.equals(other.expressionResult))
            return false;
        return true;
        // CHECKSTYLE:ON
    }

    @Override
    public String toString() {
    	    return "Correct: " + super.toString()
        		+ " (fake EL result=" + expressionResult + ")";
    }

}
