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

package net.jakubholy.jeeutils.jsfelcheck.validator.exception;

import net.jakubholy.jeeutils.jsfelcheck.validator.results.JsfExpressionDescriptor;

public class InternalValidatorFailureException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String elExpression;
    private JsfExpressionDescriptor jsfExpressionDescriptor;

    public InternalValidatorFailureException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InternalValidatorFailureException(final Throwable cause) {
        super(cause);
    }

    public InternalValidatorFailureException setExpression(final String elExpression) {
        this.elExpression = elExpression;
        return this;
    }

    @Override
    public String getMessage() {
        String message;
        if (elExpression == null) {
            message = super.getMessage();
        } else {
            message = "Failed for the expression " + elExpression + ": " + super.getMessage();
        }

        if (jsfExpressionDescriptor != null) {
            message += " in file " + jsfExpressionDescriptor.getJspFile()
                + ", tag line " + jsfExpressionDescriptor.getTagLineNumber();
        }

        if (getCause() != null) {
            message += " Cause: " + getCause();
        }

        return message;
    }

    public void setExpressionDescriptor(
            JsfExpressionDescriptor jsfExpressionDescriptor) {
                this.jsfExpressionDescriptor = jsfExpressionDescriptor;
    }

}