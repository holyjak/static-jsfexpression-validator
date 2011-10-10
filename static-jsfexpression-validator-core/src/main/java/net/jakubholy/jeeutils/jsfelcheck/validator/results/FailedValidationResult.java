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

import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InvalidExpressionException;

/**
 * Result for an invalid EL expression.
 */
public class FailedValidationResult extends ValidationResult {

    private final InvalidExpressionException failure;
    private final String elExpression;

    /**
     * Result for the given failure.
     * @param failure (required) details of what was wrong
     */
    public FailedValidationResult(InvalidExpressionException failure) {
        this.failure = failure;
        // Failure can be null in tests but not in reality
        if (failure == null) {
            elExpression = "";
        } else {
            elExpression = failure.getElExpression();
        }
    }

    @Override
    public boolean hasErrors() {
        return true;
    }

    public InvalidExpressionException getFailure() {
        return failure;
    }

    /**
     * The invalid EL expression.
     */
    public String getElExpression() {
        return elExpression;
    }

    @Override
    public String toString() {
        // Note: The EL is already included in the descriptor
        return "FailedValidationResult [failure=" + failure + "; "
            + super.getExpressionDescriptor() + "]";
    }

}
