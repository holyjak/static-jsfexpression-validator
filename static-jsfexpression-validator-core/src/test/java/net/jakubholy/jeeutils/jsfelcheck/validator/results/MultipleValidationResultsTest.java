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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class MultipleValidationResultsTest {

    private MultipleValidationResults multipleValidationResults;

    @Before
    public void setUp() throws Exception {
        multipleValidationResults = new MultipleValidationResults();
    }

    @Test
    public void should_not_have_errors_when_empty() {
        assertFalse(multipleValidationResults.hasErrors());
    }

    @Test
    public void should_have_error_when_one_failed_result() {
        multipleValidationResults.add(new FailedValidationResult(null));
        assertTrue(multipleValidationResults.hasErrors());
    }

    @Test
    public void adding_ok_result_should_not_reset_errors() {
        multipleValidationResults.add(new FailedValidationResult(null));
        multipleValidationResults.add(new SuccessfulValidationResult(null));
        assertTrue(multipleValidationResults.hasErrors());
    }

    @Test
    public void should_have_error_when_one_ok_result() {
        multipleValidationResults.add(new SuccessfulValidationResult(null));
        assertFalse(multipleValidationResults.hasErrors());
    }

    @Test
    public void testAddMultipleValidationResults() {
        ValidationResult originalResult1 = new SuccessfulValidationResult(1);

        ValidationResult mergedResult1 = new SuccessfulValidationResult(2);
        ValidationResult mergedResult2 = new FailedValidationResult(null);

        MultipleValidationResults resultsToMerge = new MultipleValidationResults();
        resultsToMerge.add(mergedResult1);
        resultsToMerge.add(mergedResult2);

        ValidationResult originalResult2 = new SuccessfulValidationResult(1);

        multipleValidationResults.add(originalResult1);
        multipleValidationResults.add(resultsToMerge);
        multipleValidationResults.add(originalResult2);

        for (ValidationResult result : multipleValidationResults) {
            if (result.equals(resultsToMerge)) {
                fail("The results of resultsToMerge should be merged in, not the multiresult object itself");
            }
        }

        ValidationResult[] expected = new ValidationResult[] {
                originalResult1, mergedResult1, mergedResult2, originalResult2 };
        int cnt = 0;
        for (ValidationResult result : multipleValidationResults) {
            assertEquals("Mismatch at index " + cnt
                    , expected[cnt++], result);
        }

    }

    @Test
    public void should_return_reports_in_order_of_addition() {
        ValidationResult[] input = new ValidationResult[]{
                new SuccessfulValidationResult(null)
                , new SuccessfulValidationResult(1)
                , new FailedValidationResult(null)
                , new FailedValidationResult(null)
        };

        for (ValidationResult result : input) {
            multipleValidationResults.add(result);
        }

        assertThat(multipleValidationResults, contains(input));
    }

    @Test
    public void should_have_errors_after_merging_failed_results() throws Exception {
        MultipleValidationResults resultsToMerge = new MultipleValidationResults();
        resultsToMerge.add(new FailedValidationResult(null));

        multipleValidationResults.add(resultsToMerge);

        assertTrue(multipleValidationResults.hasErrors());
    }

    @Test
    public void should_keep_no_errors_after_merging_good_results() throws Exception {
        MultipleValidationResults resultsToMerge = new MultipleValidationResults();
        resultsToMerge.add(new SuccessfulValidationResult(null));

        multipleValidationResults.add(resultsToMerge);

        assertFalse(multipleValidationResults.hasErrors());
    }

    @Test
    public void should_keep_errors_after_merging_good_results() throws Exception {
        multipleValidationResults.add(new FailedValidationResult(null));

        MultipleValidationResults resultsToMerge = new MultipleValidationResults();
        resultsToMerge.add(new SuccessfulValidationResult(null));

        multipleValidationResults.add(resultsToMerge);

        assertTrue(multipleValidationResults.hasErrors());
    }

    @Test
    public void should_return_iterator_over_good_results_only() throws Exception {
        SuccessfulValidationResult good1 = new SuccessfulValidationResult(null);
        SuccessfulValidationResult good2 = new SuccessfulValidationResult("hi!");

        ValidationResult[] input = new ValidationResult[]{
                new FailedValidationResult(null)
                , good1
                , new FailedValidationResult(null)
        };

        for (ValidationResult result : input) {
            multipleValidationResults.add(result);
        }

        MultipleValidationResults resultsToMerge = new MultipleValidationResults();
        resultsToMerge.add(good2);

        multipleValidationResults.add(resultsToMerge);

        LinkedList<SuccessfulValidationResult> expected = new LinkedList<SuccessfulValidationResult>();
        expected.add(good1);
        expected.add(good2);

        LinkedList<SuccessfulValidationResult> actual = new LinkedList<SuccessfulValidationResult>();
        for (SuccessfulValidationResult goodResult : multipleValidationResults.goodResults()) {
            actual.add(goodResult);
        }

        assertEquals(expected, actual);

    }

}
