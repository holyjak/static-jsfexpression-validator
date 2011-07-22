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

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.MissingLocalVariableTypeDeclarationException; // SUPPRESS CHECKSTYLE
import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ExpressionRejectedByFilterResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

/**
 * Report validation results (print them to sysout/syserr).
 */
public class ResultsReporter {

    private boolean suppressOutput = false;
    private boolean printCorrectExpressions = false;

    /**
     * Print as standard output.
     * @param message (required)
     */
    public void printOut(String message) {
        print(System.out, message);
    }

    /**
     * Print as error, i.e. something important.
     * @param message (required)
     */
    public void printErr(String message) {
        print(System.err, message);
    }

    private void print(PrintStream out, String message) {
        if (!suppressOutput) {
            out.println(message);
        }
    }

    public void setSuppressOutput(boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }

    public boolean isSuppressOutput() {
        return suppressOutput;
    }

    /**
     * Print results of JSF EL validation to the standard output/error.
     * @param results (required)
     */
    public void printValidationResults(CollectedValidationResults results) {
        printLocalVarsMissingTypeDeclaration(results);
        printFailuresHeader(results);

        // TODO Log separately undefined variables Later: suppress derived
        // errors w/ them

        printFailures(results);
        printExpressionsFilteredOut(results);
        printValidExpressionsIfAllowed(results);
    }

    private void printValidExpressionsIfAllowed(
            CollectedValidationResults results) {
        if (printCorrectExpressions) {
            printOut("\n>>> CORRECT EXPRESSIONS ["
                    + results.goodResults().size()
            		+ "] #########################################");
        }

        for (ValidationResult result : results
                .goodResults()) {
            if (printCorrectExpressions) {
                System.out.println(result);
            }
        }
    }

    private void printExpressionsFilteredOut(CollectedValidationResults results) {
        if (results.excluded().size() > 0) {
            Set<ElExpressionFilter> filters = new HashSet<ElExpressionFilter>();
            for (ExpressionRejectedByFilterResult exclusionResult : results.excluded()) {
                filters.add(exclusionResult.getFilter());
            }
            String filtersList = filters.isEmpty()? "" : " by filters: " + filters;
            printErr(">>> TOTAL EXCLUDED EXPRESIONS: " + results.excluded().size() + filtersList);
        }
    }

    private void printLocalVarsMissingTypeDeclaration(
            CollectedValidationResults results) {
        if (results.getVariablesNeedingTypeDeclaration().size() > 0) {
            printErr(">>> LOCAL VARIABLES THAT YOU MUST DECLARE TYPE FOR ["
                    + results.getVariablesNeedingTypeDeclaration().size()
            		+ "] #########################################\n"
            		+ "(You must declare type of local variables, usually defined by h:dataTable, by specifying "
            		+ "the type of elements in the source collection denoted by its EL, or example:\n"
            	    + "localVariableTypes.put(value h:dataTable's source attribute, element type's class)");
            for (MissingLocalVariableTypeDeclarationException untypedVar : results.getVariablesNeedingTypeDeclaration()) {  // SUPPRESS CHECKSTYLE
                printErr(untypedVar.toString());
            }
        }
    }

    private void printFailuresHeader(CollectedValidationResults results) {
        printErr("\n>>> FAILED JSF EL EXPRESSIONS ["
                + results.failures().size()
        		+ "] #########################################");
        printErr("(Set logging to fine for the correspodning "
                + ValidatingElResolver.class   // FIXME incorrect class in some cases
                + "subclass to se failure details and stacktraces)");
    }

    private void printFailures(CollectedValidationResults results) {
        for (ValidationResult result : results.failures()) {
            printErr(result.toString());
        }

        if (results.failures().size() > 0) {
            printErr("\n>>> TOTAL FAILED EXPRESIONS: " + results.failures().size());
        }
    }

    /**
     * Should the correctly validated EL expressions be also printed, in addition to the invalid ones?
     * Default: false.
     * @param printCorrectExpressions (required)
     */
    public void setPrintCorrectExpressions(boolean printCorrectExpressions) {
        this.printCorrectExpressions = printCorrectExpressions;
    }

    public boolean isPrintCorrectExpressions() {
        return printCorrectExpressions;
    }

}