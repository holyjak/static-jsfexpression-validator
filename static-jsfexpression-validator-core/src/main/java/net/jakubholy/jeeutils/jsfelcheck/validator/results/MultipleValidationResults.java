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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Collection of validation results whose {@link #hasErrors()} is true if any of the contained results has it true.
 */
public class MultipleValidationResults extends ValidationResult implements Iterable<ValidationResult> {

    private final Collection<ValidationResult> results = new LinkedList<ValidationResult>();
    private final Collection<SuccessfulValidationResult> goodResults =
        new LinkedList<SuccessfulValidationResult>();
    private final Collection<FailedValidationResult> failures =
        new LinkedList<FailedValidationResult>();
    private final Collection<ExpressionRejectedByFilterResult> exclusions =
        new LinkedList<ExpressionRejectedByFilterResult>();

    private boolean errors = false;

    @Override
    public boolean hasErrors() {
        return errors;
    }

    /**
     * Add all results from the given collection to this one.
     * @param multipleResults (required)
     */
    public void add(MultipleValidationResults multipleResults) {
        addAll(multipleResults.results);
    }

    /**
     * Add a single result to this collection.
     * @param singleResult (required)
     */
    public void add(ValidationResult singleResult) {
        addSingleResult(singleResult);
    }

    private void addSingleResult(ValidationResult singleResult) {
        errors |= singleResult.hasErrors();
        results.add(singleResult);
        if (singleResult instanceof SuccessfulValidationResult) {
            goodResults.add((SuccessfulValidationResult) singleResult);
        } else if (singleResult instanceof ExpressionRejectedByFilterResult) {
            exclusions.add((ExpressionRejectedByFilterResult) singleResult);
        } else {
            failures.add((FailedValidationResult) singleResult);
        }
        postAddSingleResult(singleResult);
    }

    /**
     * Subclass hook - called after a single result has been added to this.
     * @param singleResult (required)
     */
    protected void postAddSingleResult(ValidationResult singleResult) { }

    /**
     * Add all the results in the collection to this one.
     * @param allResults (required)
     */
    public void addAll(Collection<ValidationResult> allResults) {
        for (ValidationResult singleResult : allResults) {
            this.add(singleResult);
        }
    }

    /** {@inheritDoc} */
    public Iterator<ValidationResult> iterator() {
        return results.iterator();
    }

    /** {@inheritDoc} */
    public ResultsIterable<SuccessfulValidationResult> goodResults() {
        return new ResultsIterable<SuccessfulValidationResult>(goodResults);
    }

    /** {@inheritDoc} */
    public ResultsIterable<FailedValidationResult> failures() {
        return new ResultsIterable<FailedValidationResult>(failures);
    }

    /** {@inheritDoc} */
    public ResultsIterable<ExpressionRejectedByFilterResult> excluded() {
        return new ResultsIterable<ExpressionRejectedByFilterResult>(exclusions);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getResultsSummary() + "]";
    }

    protected final String getResultsSummary() {
        return "failures=" + failures.size()
                		+ ",valid expressions=" + goodResults.size()
                		+ ",ignored expressions=" + exclusions.size();
    }

}
