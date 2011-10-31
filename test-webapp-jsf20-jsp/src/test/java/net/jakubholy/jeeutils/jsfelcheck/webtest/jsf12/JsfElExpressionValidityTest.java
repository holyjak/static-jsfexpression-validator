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
import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.testbean.MyCollectionBean;
import org.junit.Test;

import java.io.File;

import static net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration.declareLocalVariable;
import static net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration.fromFacesConfigFiles;
import static org.junit.Assert.assertEquals;

/**
 * Check EL expressions that are supposed to be validated successfully.
 */
public class JsfElExpressionValidityTest {

    @Test
    public void verify_all_el_expressions_valid() throws Exception {

        JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();

        jsfStaticAnalyzer.withLocalVariablesConfiguration(
                    declareLocalVariable("myCollectionBean.list", MyCollectionBean.ValueHolder.class)
                    .withCustomDataTableTagAlias("t:dataTable"))
                .withPropertyTypeOverride("myCollectionBean.list.*", MyCollectionBean.ValueHolder.class)
                .withManagedBeansAndVariablesConfiguration(
                        fromFacesConfigFiles(new File("src/main/webapp/WEB-INF/faces-config.xml"))
                        .withExtraVariable("iAmExtraVariable", new Object())
                        .withExtraVariable("myCollectionBean", new MyCollectionBean()))
                ;

        CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions(new File("src/main/webapp//tests/valid_el"));

        assertEquals("There shall be no invalid JSF EL expressions; check System.err/.out for details. FAILURE "
                + results.failures()
                , 0, results.failures().size());

    }

    private JsfStaticAnalyzer createConfiguredAnalyzer() {
        JsfStaticAnalyzer jsfStaticAnalyzer = JsfStaticAnalyzer.forJsp();
        jsfStaticAnalyzer.setPrintCorrectExpressions(false);
        return jsfStaticAnalyzer;
    }

}
