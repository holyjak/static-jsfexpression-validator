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

package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.autodiscovery;

import net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer;
import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.MyActionBean;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;

import javax.inject.Named;
import java.awt.print.Book;
import java.io.File;
import java.util.Collection;
import java.util.Set;

import static net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration.declareLocalVariable;
import static net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration.forExtraVariables;
import static org.junit.Assert.assertEquals;

/**
 * Verify that the JSF EL validator can auto-detect annotated managed beans used in pages.
 */
public class JsfAnnotatedBeanAutodetectionTest {

	@Ignore("until autodiscovery fully implemented")
	@Test
	public void allEjExpressionWithAnnotatedBeansShouldPass() throws Exception {

	    JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();

		Collection autodetectedBeans = detectAnnotatedBeans();
		jsfStaticAnalyzer.withManagedBeansAndVariablesConfiguration(
				forExtraVariables().withExtraVariable("temporary", Object.class));

		File webappRoot = new File("src/main/webapp");
        CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions(
		        webappRoot,
		        new File(webappRoot, "tests/autodiscovery"));

        assertEquals("There shall be no invalid JSF EL expressions; check System.err/.out for details. FAILURE "
		        + results.failures()
		        , 0, results.failures().size());
	}

	private Collection detectAnnotatedBeans() {
		// TODO Use http://code.google.com/p/reflections/
		Reflections reflections = new Reflections("net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.autodiscovery");
		Set<Class<?>> beanClasses = reflections.getTypesAnnotatedWith(Named.class);
		return null;  //To change body of created methods use File | Settings | File Templates.
	}

	private JsfStaticAnalyzer createConfiguredAnalyzer() {
        JsfStaticAnalyzer jsfStaticAnalyzer = JsfStaticAnalyzer.forFacelets();
        jsfStaticAnalyzer.setPrintCorrectExpressions(false);
        return jsfStaticAnalyzer;
    }

}
