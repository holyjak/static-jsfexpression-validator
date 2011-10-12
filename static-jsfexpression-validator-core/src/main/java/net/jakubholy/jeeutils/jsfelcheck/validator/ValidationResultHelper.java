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

import net.jakubholy.jeeutils.jsfelcheck.validator.exception.ExpressionRejectedByFilterException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InternalValidatorFailureException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InvalidExpressionException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ExpressionRejectedByFilterResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper for handling validation failures.
 */
public final class ValidationResultHelper {

    private static final Logger LOG = Logger.getLogger(ValidationResultHelper.class.getName());

    private ValidationResultHelper() { }

    /**
     * Check what type of exception it is and if necessary then wrap it with an InternalValidatorFailureException
     * while also adding some context, namely the problematic EL expression.
     * @param elExpression (required) the EL that failed to validate
     * @param e (required) the validation failure
     * @return as shown
     */
    public static InternalValidatorFailureException wrapIfNeededAndAddContext(final String elExpression,
            RuntimeException e) {
        if (e instanceof InternalValidatorFailureException) {
            InternalValidatorFailureException internalFailure = ((InternalValidatorFailureException) e);
            internalFailure.setExpression(elExpression);
            return internalFailure;
        } else {
            return new InternalValidatorFailureException(e).setExpression(elExpression);
        }
    }

    /**
     * Produce ValidationResult subclass corresponding to the particular evaluation failure.
     * Ex.: {@link ExpressionRejectedByFilterException}, {@link FailedValidationResult}.
     * @param elExpression (required) the EL whose validation failed
     * @param evaluationException (required) the failure
     * @return suitable result
     */
    public static ValidationResult produceFailureResult(final String elExpression,
            Exception evaluationException) {
        LOG.log(Level.FINE, "Resolution failed", evaluationException);
        Throwable unwrappedCause = (evaluationException.getCause() == null)? evaluationException
        		: evaluationException.getCause();

        if (unwrappedCause instanceof ExpressionRejectedByFilterException) {
            return new ExpressionRejectedByFilterResult((ExpressionRejectedByFilterException) unwrappedCause);
        } else {
            return new FailedValidationResult(
                new InvalidExpressionException(elExpression, null, unwrappedCause));
        }
    }

}
