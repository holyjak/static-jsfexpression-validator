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

import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.ExpressionRejectedByFilterException;

/**
 * Information about an expression that hasn't been validated because one of the filters rejected it.
 *
 * @see ElExpressionFilter
 */
public class ExpressionRejectedByFilterResult extends ValidationResult {

    private final ExpressionRejectedByFilterException details;
    private final ElExpressionFilter filter;

    /** New result without any details. */
    public ExpressionRejectedByFilterResult() {
        this(null);
    }

    /**
     * Result with the given details.
     * @param details (optional) Details of where and why
     */
    public ExpressionRejectedByFilterResult(ExpressionRejectedByFilterException details) {
        this.details = details;
        if (details == null) {
            filter = null;
        } else {
            filter = details.getFilter();
        }
    }

    @Override
    public boolean hasErrors() {
        return true;
    }

    @Override
    public String toString() {
        return "ExpressionRejectedByFilterResult [details=" + details + "; "
            + super.getExpressionDescriptor() + "]";
    }

    public ElExpressionFilter getFilter() {
        return filter;
    }

}
