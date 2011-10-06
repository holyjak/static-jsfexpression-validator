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

package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12;

import net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Verify that all expressions found are invalid.
 */
public class JsfElInvalidExpressionsTest {

    @Test
    public void verify_all_el_expressions_fail() throws Exception {

        JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();
        CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions("src/main/webapp//tests/failing_el");

        assertEquals("All the expressions found should have failed to validate! Expected to fail:"
                + results.goodResults()
                , 0
                , results.goodResults().size());

    }

    private JsfStaticAnalyzer createConfiguredAnalyzer() {
        JsfStaticAnalyzer jsfStaticAnalyzer = new JsfStaticAnalyzer();
        jsfStaticAnalyzer.setPrintCorrectExpressions(false);
        jsfStaticAnalyzer.setFacesConfigFiles(Collections.singleton(new File(
                "src/main/webapp/WEB-INF/faces-config.xml")));
        return jsfStaticAnalyzer;
    }

}
