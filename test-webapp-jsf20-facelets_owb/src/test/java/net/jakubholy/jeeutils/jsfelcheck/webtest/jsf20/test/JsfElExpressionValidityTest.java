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

package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test;

import net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer;
import net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration;
import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.MyActionBean;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Named;
import java.awt.print.Book;
import java.io.File;

import static net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration.*;
import static net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration.*;
import static org.junit.Assert.assertEquals;

/**
 * Check EL expressions that are supposed to be  validated successfully.
 */
public class JsfElExpressionValidityTest {

	/**
	 * Test JSF with Facelets - pages under webapp/tests.
	 */
    @Test
    public void verify_all_el_expressions_valid() throws Exception {

        JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();

        jsfStaticAnalyzer
		        /*.withLocalVariablesConfiguration(
		            declareLocalVariable("shop.books", Book.class)
				        //.withCustomDataTableTagAlias("t:dataTable"))
				        )*/
                .withManagedBeansAndVariablesConfiguration(
		                fromClassesInPackages("net.jakubholy").annotatedWith(Named.class, "value").config()
		                .withExtraVariable("myActionBean", MyActionBean.class))
                ;

	    final File webRoot = new File("src/main/webapp");
	    final File testPages = new File(webRoot, "tests");
	    CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions(webRoot, testPages);

        assertEquals("There shall be no invalid JSF EL expressions; check System.err/.out for details. FAILURE "
                + results.failures()
                , 0, results.failures().size());

    }

	/**
	 * Test pages under webapp/tests-inclusions/ covering templates,
	 * custom tags and components included from other pages.
	 * <p>
	 *     Currently this is not supported for 1) included pages are not parsed and
	 *     2) if parsed as top-level pages, the input parameters won't be recognized
	 *     unless manually declared as local variables).
	 *  </p>
	 */
	@Test
	public void verify_el_in_included_pages() throws Exception {
		Assume.assumeTrue(Boolean.getBoolean("jsfelcheck.runFailingTests"));

		JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();

		jsfStaticAnalyzer
		/*.withLocalVariablesConfiguration(
									  declareLocalVariable("shop.books", Book.class)
										  //.withCustomDataTableTagAlias("t:dataTable"))
										  )*/
				  .withManagedBeansAndVariablesConfiguration(
						  fromClassesInPackages("net.jakubholy").annotatedWith(Named.class, "value").config()
								  .withExtraVariable("myActionBean", MyActionBean.class))
		  ;

		  final File webRoot = new File("src/main/webapp");
		  final File inclusionsPages = new File(webRoot, "tests-inclusions");
		  CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions(webRoot, inclusionsPages);

		  assertEquals("There shall be no invalid JSF EL expressions; check System.err/.out for details. FAILURE "
				  + results.failures()
				  , 0, results.failures().size());

	  }

	  private JsfStaticAnalyzer createConfiguredAnalyzer() {
		  JsfStaticAnalyzer jsfStaticAnalyzer = JsfStaticAnalyzer.forFacelets();
		  jsfStaticAnalyzer.setPrintCorrectExpressions(false);
		  return jsfStaticAnalyzer;
	  }

  }
