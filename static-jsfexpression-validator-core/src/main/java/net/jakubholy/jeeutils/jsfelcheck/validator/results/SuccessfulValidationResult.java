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
    private final String elExpression;

    /**
     * Result for EL expression whose evaluation produced the given value.
     * In the context of the "fake" validating resolver this is usually a "fake value" of the expression's output type.
     * @param elExpression (required) the expression that was succesfully evaluated
     * @param expressionResult (optional) result of evaluating the EL (optional because the result can be null)
     *
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory
     */
    public SuccessfulValidationResult(String elExpression, Object expressionResult) {
        this.elExpression = elExpression;
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

    /**
     * The validated EL expression.
     */
    public String getElExpression() {
        return elExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuccessfulValidationResult that = (SuccessfulValidationResult) o;

        if (elExpression != null ? !elExpression.equals(that.elExpression) : that.elExpression != null) return false;
        if (expressionResult != null ? !expressionResult.equals(that.expressionResult) : that.expressionResult != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = expressionResult != null ? expressionResult.hashCode() : 0;
        result = 31 * result + (elExpression != null ? elExpression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
    	    return "Correct expression '" + elExpression
        		+ "' with mocked EL result=" + expressionResult + "";
    }

}
