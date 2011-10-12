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

import java.util.regex.Pattern;

/**
 * Used to pass on EL validation failures in a JSF implementation independent way.
 */
public class InvalidExpressionException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String expression;

    /**
     * Full constructor.
     * @param expression (required) the EL expression
     * @param message (optional) description of the problem
     * @param cause (required) cause
     */
    public InvalidExpressionException(final String expression, final String message, final Throwable cause) {
        super(message, cause);
        this.expression = expression;
    }

    /**
     * See {@link #InvalidExpressionException(String, String, Throwable)}.
     * @param expression (required)
     * @param message (required)
     */
    public InvalidExpressionException(final String expression, final String message) {
        this(expression, message, null);
    }

    @Override
    public String getMessage() {
        String defaultMessage = (super.getMessage() == null)? "" : super.getMessage();
        String causeInfo = (getCause() == null)? "" : getCause().getClass().getSimpleName()
                + " - " + getCause().getMessage();
        String message = (defaultMessage.contains(expression))
            || causeInfo.contains(expression)? defaultMessage : "Invalid EL expression '"
            + expression + "': " + defaultMessage;
        return withoutMockitoCglibSuffixInClassnames(message + causeInfo);
    }

    /**
     * Remove confusing Mockit CGLIB suffix from faked object values.
     * Example message: Invalid EL expression '#{myObject.noSuchProperty}': PropertyNotFoundException -
     * Property 'noSuchProperty' not found on type your.BeanType$$EnhancerByMockitoWithCGLIB$$c22a782d
     * @param message
     * @return
     */
    private String withoutMockitoCglibSuffixInClassnames(String message) {
        Pattern cglibSuffixPattern = Pattern.compile(Pattern.quote("$$EnhancerByMockitoWithCGLIB$$") + "\\w*");
        return cglibSuffixPattern.matcher(message).replaceAll("");
    }

    @Override
    public String toString() {
        return "InvalidExpressionException [" + getMessage() + "]";
    }

    /**
     * Returns the invalid EL expression.
     */
    public String getElExpression() {
        return expression;
    }
}