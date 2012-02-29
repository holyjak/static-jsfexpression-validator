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

import com.sun.faces.el.impl.AndOperator;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.InputResource;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.jsf11.Jsf11FacesConfigXmlBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.JsfElValidatingFaceletsParser;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.Jsf11ValidatingElResolver;

import java.io.File;
import java.util.Collection;


/**
 * <p></p><strong>JavaDoc copied from {@link net.jakubholy.jeeutils.jsfelcheck.AbstractJsfStaticAnalyzer}</strong>; version for JSF 1.1.</p>
 *
 * The validator analyses JSF pages implemented with Facelets or JSP and validates that all EL
 * expressions reference only existing managed beans and their properties/action
 * methods. See the example below and
 * <a href="https://github.com/jakubholynet/static-jsfexpression-validator/blob/master/README.md">examples in the test webapps</a>.
 * Of course you will need one of the JSF version specific subclasses, namely
 * <code>net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer</code>.
 *
 * <h3>Usage example</h3>
 * <p>
 *     You create the analyzer, tell it where it can find your managed beans and run it on your webapp:
 * </p>
 *
 * <code><pre>{@code
 * JsfStaticAnalyzer jsfStaticAnalyzer = JsfStaticAnalyzer.forFacelets();
 * jsfStaticAnalyzer.withManagedBeansAndVariablesConfiguration(
 *      ManagedBeansAndVariablesConfiguration
 *      .fromClassesInPackages("net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.annotated")
 *      .annotatedWith(Named.class, "value")
 *      .config());
 *
 * File webappRoot = new File("src/main/webapp");
 *
 * CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions(
 *      webappRoot,
 *      new File(webappRoot, "tests/annotated"));
 *
 * assertEquals("There shall be no invalid JSF EL expressions; check System.err.out for details. FAILURE " + results.failures()
 *      , 0, results.failures().size());
 * }</pre></code>
 *
 * See the test-webapp-jsf* projects that are part of this project to see examples of usage with different
 * versions o JSF and different configurations. You can <a href="https://github.com/jakubholynet/static-jsfexpression-validator">
 *     see them at GitHub</a> or <a href="http://repo1.maven.org/maven2/net/jakubholy/jeeutils/jsfelcheck/">
 *     download from Maven Central</a>.
 *
 * <h3>Configuration</h3>
 * <p>
 *     You usually need to configure the validator, f.ex. tell it where to find your managed beans, inform it about
 *     the types of JSF local variables and properties that it cannot detect automatically etc.
 *     You might prefer to just look at the examples and start using the validator over reading this
 *     documentation and only come back to it if you find out that you need to know more.
 * </p>
 * <p>
 *     See the <code>with*</code> methods - if they take an object then you usually can create it via a static
 *     method on the object's class - it works best with static imports.
 * </p>
 *
 * <h4>Managed Beans and Other (Top-Level) Variables</h4>
 * <p>
 *     You always have to inform the validator where to find your managed beans - in faces-config, Spring
 *     application configuration XML, or as annotated beans on the classpath.
 * </p>
 * <p>
 * If there are some EL variables aside of managed beans and the local variables you can declare them to the
 * validator via {@link net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration#withExtraVariable(String, Object)}.
 * </p>
 *
 * <h4>Local Variables</h4>
 * <p>
 * For local variables, such as the <code>var</code> produced by <code>h:dataTable</code>, you
 * must declare of what type they are as this cannot be determined based
 * on the code, see
 * {@link net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration#withLocalVariable(String, Class)}.
 * </p>
 * <p>
 * If there are other tags than h:dataTable for JSP or ui:repeat for Facelets that can create local variables then you
 * must create and register an appropriate "resolver" (a class that can extract the local variable name and type from
 * the tag info) for them as is done with the
 * dataTable - see {@link net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration#withCustomDataTableTagAlias} and
 * {@link net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration#withResolverForVariableProducingTag}.
 * </p><p>
 *  See {@link net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.DataTableVariableResolver} for an example.
 * </p>
 *
 * <h4>Declaration of Types of Elements in Collections and Maps</h4>
 * <p>
 *     You can also help the validator by telling it what is the type of elements in collection/maps
 *     via {@link #withPropertyTypeOverride(String, Class)}. (This is necessary even for maps/collections using
 *     generics as the Java compiler removes the type information; you can alternatively try to play with this
 *     <a href="http://theholyjava.wordpress.com/2012/02/07/using-java-compiler-tree-api-to-extract-generics-types/">
 *         experimental generics type extractor</a> based on the Java Compiler API.)
 * </p>
 *
 * <h3>How it works</h3>
 * We use "fake value resolversIn" for a real JSF resolver; those resolversIn do not retrieve
 * variables and property values from the context as JSF normally does but instead produce
 * a new fake value of the expected type using Mockito - thus we can check that expressions are
 * valid. When the type of a variable/property cannot be determined (which is often the case for
 * Collections, which can contain nay Object) and isn't defined via property override etc. then
 * we use {@link net.jakubholy.jeeutils.jsfelcheck.validator.MockObjectOfUnknownType} -
 * if you see it in a failed JSF EL check then you need
 * to declare the type to use.
 *
 * <h3>Limitations</h3>
 *
 * @author jakubholy.net
 *
 * @see #validateElExpressions(java.io.File, java.io.File)
 * @see #withManagedBeansAndVariablesConfiguration(net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration)
 * @see CollectedValidationResults#failures()
 *
 */
public class JsfStaticAnalyzer extends AbstractJsfStaticAnalyzer<JsfStaticAnalyzer> {

	public static JsfStaticAnalyzer forJsp() {
		return new JsfStaticAnalyzer(ViewType.JSP);
	}

	/** @deprecated use the static factory method {@link #forJsp()}  */
	public JsfStaticAnalyzer() {
		this(ViewType.JSP);
	}

	JsfStaticAnalyzer(ViewType viewType) {
		super(ViewType.JSP);
		LOG.info("Created JSF 1.1 JsfStaticAnalyzer");
	}

	public static void main(String[] args) throws Exception { // SUPPRESS CHECKSTYLE (no JavaDoc)
        AbstractJsfStaticAnalyzer.main(new JsfStaticAnalyzer(), args);
    }

    @Override
    protected ValidatingElResolver createValidatingElResolver() {
        if (!new AndOperator().toString().startsWith("HACKED BY JSFELCHECK ")) {
            handleUnhackedElImplementationLoaded("jsf-impl");
        }
        return new Jsf11ValidatingElResolver();
    }

	@Override
	protected JsfElValidatingFaceletsParser createValidatingFaceletsParser(File webappRoot, PageNodeListener pageNodeValidator) {
		throw new UnsupportedOperationException("Sorry, we haven't implemented support for Facelets in the JSF 1.1 "
				+ "validator, consider trying it with the JSF 1.2 version (should be mostly backwards-compatible),");
	}

	@Override
	protected ManagedBeanFinder createManagedBeanFinder(Collection<InputResource> facesConfigFilesToRead) {
        return Jsf11FacesConfigXmlBeanFinder.forResources(facesConfigFilesToRead);
    }

}
