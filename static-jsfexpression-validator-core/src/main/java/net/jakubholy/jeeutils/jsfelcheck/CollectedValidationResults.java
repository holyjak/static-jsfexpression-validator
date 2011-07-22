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

package net.jakubholy.jeeutils.jsfelcheck;

import java.util.Collection;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.MissingLocalVariableTypeDeclarationException; // SUPPRESS CHECKSTYLE
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ExpressionRejectedByFilterResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ResultsIterable;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;

/**
 * Collected results of JSF EL validation including failures, valid expressions, missing type
 * declaration etc.
 */
public interface CollectedValidationResults {

    /**
     * Local JSF variables defined in the processed files (such as the var of h:dataTable) that couldn't be applied
     * correctly because their type hasn't been declared.
     *
     * Notice that any expression that uses such local variable will fail to validate and thus there will be
     * consecutive failures.
     *
     * @return possibly empty
     *
     * @see AbstractJsfStaticAnalyzer#validateElExpressions(String, java.util.Map, java.util.Map, java.util.Map)
     * @see net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.DataTableVariableResolver
     * #declareTypeFor(String, Class)
     */
    Collection<MissingLocalVariableTypeDeclarationException> getVariablesNeedingTypeDeclaration();

    /**
     * Successfully validated JSF EL expressions.
     * @return possibly empty
     */
    ResultsIterable<SuccessfulValidationResult> goodResults();

    /**
     * Information about JSF EL expressions that failed to validate for some reason.
     * @return possibly empty
     */
    ResultsIterable<FailedValidationResult> failures();

    /**
     * Expressions that were not validated because a filter set on the analyzer has rejected them.
     * @return possibly empty
     * @see AbstractJsfStaticAnalyzer#addElExpressionFilter
     */
    ResultsIterable<ExpressionRejectedByFilterResult> excluded();

    /**
     * Were there any problems during the validation such as expressions that failed to validate or
     * local variables missing type declaration?
     * @return true if there were failures
     */
    boolean hasErrors();

}