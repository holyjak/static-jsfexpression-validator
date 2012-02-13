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

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.InputResource;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder.ManagedBeanDescriptor;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.SpringContextBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration;
import net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.JsfElValidatingFaceletsParser;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.ValidatingFaceletsParserExecutor;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.CollectedValidationResultsImpl;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JsfElValidatingPageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JspCParsingToNodesOnly;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.ContextVariableRegistry;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JsfElCheckingVisitor;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Perform analysis of JSF pages implemented with Facelets or JSP and validate that all EL
 * expressions reference only existing managed beans and their properties/action
 * methods.
 * <p>
 * For local variables, such as the <code>var</code> produced by h:dataTable, you
 * must first declare of what type they are as this cannot be determined based
 * on the code, see
 * {@link LocalVariableConfiguration#withLocalVariable(String, Class)}.
 * <p>
 * If there are some EL variables aside of managed beans in faces-config (and
 * perhaps Spring config) and the local variables you can declare them to the
 * validator via {@link ManagedBeansAndVariablesConfiguration#withExtraVariable(String, Object)}.
 * <p>
 * If there are other tags than h:dataTable for JSP or ui:repeat for Facelets that can create local variables then you
 * must create and register an appropriate "resolver" (a class that can extract the local variable name and type from
 * the tag info) for them as is done with the
 * dataTable - see {@link LocalVariableConfiguration#withCustomDataTableTagAlias} and
 * {@link LocalVariableConfiguration#withResolverForVariableProducingTag}.
 * See {@link net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.DataTableVariableResolver} for an example.
 * <p>
 *     You can also help the resolver by telling it what is the type of elements in collection/maps
 *     via {@link #withPropertyTypeOverride(String, Class)}. (This is necessary even for maps/collections using
 *     generics as the Java compiler removes the type information; you can alternatively try to play with this
 *     <a href="http://theholyjava.wordpress.com/2012/02/07/using-java-compiler-tree-api-to-extract-generics-types/">
 *         experimental generics type extractor</a> based on Java Compiler API.)
 * </p>
 *
 * <h3>Usage example</h3>
 * <p>
 *     You create the analyzer, tell it where it can find your managed beans and run it on your webapp:
 * </p>
 *
 * <code><pre>{@code
 * JsfStaticAnalyzer jsfStaticAnalyzer = JsfStaticAnalyzer.forFacelets();
 * jsfStaticAnalyzer.withManagedBeansAndVariablesConfiguration(
		ManagedBeansAndVariablesConfiguration
		.fromClassesInPackages("net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.annotated")
		.annotatedWith(Named.class, "value")
		.config());


 * File webappRoot = new File("src/main/webapp");
 * CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions(
		webappRoot,
		new File(webappRoot, "tests/annotated"));
 *
 * assertEquals("There shall be no invalid JSF EL expressions; check System.err/.out for details. FAILURE " + results.failures()
 *      , 0, results.failures().size());
 * }</pre></code>
 *
 * See the test-webapp-jsf* projects that are part of this project to see examples of usage with different
 * versions o JSF and different configurations. You can <a href="https://github.com/jakubholynet/static-jsfexpression-validator">
 *     see them at GitHub</a> or <a href="http://repo1.maven.org/maven2/net/jakubholy/jeeutils/jsfelcheck/">
 *     download from Maven Central</a>.
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
 * <pre>
 * - Function in expressions do not cause failures but are not checked for validity (e.g. fn:contains(s1,s2)).
 * - Currently it's assumed that a tag can declare only 1 local variable (var in dataTable).
 * - Included files (statically or dynamically) are processed without taking their inclusion
 * into account, i.e. variables defined in the including page aren't available when checking them.
 * We have though all the information for taking inclusions into account available, it would just
 * require more work (filter the included files out not to be processed as top-level files and
 * when an inclusion tag is encountered, process the included file passing the current context on;
 * see {@link org.apache.jasper.compiler.JsfElCheckingVisitor} and
 * org.apache.jasper.compiler.Node.Visitor.visit(IncludeAction)
 * , org.apache.jasper.compiler.Node.Visitor.visit(IncludeDirective)).
 * </pre>
 *
 * @author jakubholy.net
 *
 */
public abstract class AbstractJsfStaticAnalyzer<T extends AbstractJsfStaticAnalyzer> {

    protected static final Logger LOG = Logger.getLogger(AbstractJsfStaticAnalyzer.class.getName());

	protected static enum ViewType {JSP, FACELETS };

    private final T self;
	private final ViewType viewType;

    private final ValidatingElResolver elValidator;
    private final ResultsReporter resultsReporter = new ResultsReporter();

    private String jspsToIncludeCommaSeparated = null;

    private LocalVariableConfiguration localVariableConfiguration = new LocalVariableConfiguration();
    private ManagedBeansAndVariablesConfiguration managedBeansConfiguration = new ManagedBeansAndVariablesConfiguration();

    /** New, unconfigured analyzer.
     * @param viewType*/
    public AbstractJsfStaticAnalyzer(ViewType viewType) {
        elValidator = createValidatingElResolver();
        if (elValidator == null) {
            throw new IllegalStateException("Implementation returned null elValidator: ValidatingElResolver");
        }
        self = (T) this;
	    this.viewType = assertNotNull(viewType, "viewType", ViewType.class);
    }

    /** Create the JSF-implementation specific valiator to use. */
    protected abstract ValidatingElResolver createValidatingElResolver();

	/**
     * Check expressions in all JSP files under the viewFilesRoot and print the failed (or all) ones
     * to System out.
     * <p>
	 *     This is as calling {@link #validateElExpressions(java.io.File, java.io.File)} with both arguments same.
     * </p>
     *
     * @param viewFilesRoot
     *            (required) where to search for JSP pages
     * @return results of the validation (never null)
     * @throws Exception
     */
    public CollectedValidationResults validateElExpressions(File viewFilesRoot) {
		return validateElExpressions(viewFilesRoot, viewFilesRoot);
	}

    /**
     * Check expressions in all JSP files under the viewFilesRoot and print the failed (or all) ones
     * to System out.
     * <p>
     * Notion: Variable - the first element in an EL expression; property: any
     * but the first element. Example:
     * #{variable.propert1.property2['some.key']}
     *
     * @param webappRoot
     *            (required) the root directory of the web application (containing WEB-INF) - used e.g. to search for local tag libraries
     * @param viewFilesRoot
     *            (required) where to search for JSP pages
     * @return results of the validation (never null)
     * @throws Exception
     */
    public CollectedValidationResults validateElExpressions(File webappRoot, File viewFilesRoot) {

        assertJspDirValid(webappRoot, "webappRoot (webapp root directory, containing WEB-INF)");
	    //assertJspDirValid(new File(webappRoot, "WEB-INF)"), "WEB-INF (under the provided webappRoot directory)");
	    assertJspDirValid(viewFilesRoot, "viewFilesRoot (path of the directory with JSP files)");

        LOG.info("validateElExpressions: entry for JSP root " + viewFilesRoot);

        final long start = System.currentTimeMillis();

        JsfElValidatingPageNodeListener pageNodeValidator = initializeValidationSubsystem();

        applyConfigurationFromSystemProperties();

	    if (viewType.equals(ViewType.JSP)) {
			// Run it
			JspCParsingToNodesOnly jspc = createJsfElValidatingJspParser(viewFilesRoot.getPath(),
					pageNodeValidator);
			try {
				jspc.execute();
			} catch (JasperException e) {
				throw new RuntimeException("Jasper failed to parse your JSP files", e);
			}
	    } else {
		    JsfElValidatingFaceletsParser faceletsParser = createValidatingFaceletsParser(webappRoot, pageNodeValidator);
		    new ValidatingFaceletsParserExecutor(viewFilesRoot, webappRoot, faceletsParser).execute();
	    }

        // Handle results
        CollectedValidationResultsImpl results = pageNodeValidator.getValidationResults();

        resultsReporter.printValidationResults(results);

        final long end = System.currentTimeMillis();
        // CHECKSTYLE:OFF ignore magic numbers warning
        final long durationS = (end - start) / 1000;
        final long seconds = durationS % 60;
        final long minutes = durationS / 60;
        // CHECKSTYLE:ON

        resultsReporter.printOut("\n\n>>> TOTAL EXPRESSIONS CHECKED: "
                + (results.failures().size() + results.goodResults().size())
                + " (FAILED: " + results.failures().size()
                + ", IGNORED EXPRESSIONS: " + results.excluded().size()
                + ") IN " + minutes + "min " + seconds + "s");

        return results;
    }

	abstract protected JsfElValidatingFaceletsParser createValidatingFaceletsParser(File webappRoot, PageNodeListener pageNodeValidator);

    private void applyConfigurationFromSystemProperties() {
        setPrintCorrectExpressions(
                Boolean.getBoolean("jsfelcheck.printCorrectExpressions") || isPrintCorrectExpressions());
        setSuppressOutput(
                Boolean.getBoolean("jsfelcheck.suppressOutput") || isSuppressOutput());
    }

	/**
	 * Initialize the validation subsystem after having processed all the configuration
	 * supplied by the user based on it.
	 */
    private JsfElValidatingPageNodeListener initializeValidationSubsystem() {

	    // Declare default local variable resolvers (h:dataTable declared in the localVar.Config)
	    // ui:repeat is sufficiently similar to h:dataTable (in: value, out: var) to be reusable:
	    localVariableConfiguration.withCustomDataTableTagAlias("ui:repeat");
        ContextVariableRegistry contextVariableRegistry = localVariableConfiguration.toRegistry();
        
        elValidator.setUnknownVariableResolver(contextVariableRegistry);
        elValidator.setIncludeKnownVariablesInException(false);

        // DEFAULT EXTRA VARIABLES
        declareImplicitVariables();

        discoverAndRegisterDefinedManagedBeans(elValidator);

        // Listener
        JsfElValidatingPageNodeListener pageNodeValidator = new JsfElValidatingPageNodeListener(
                elValidator, contextVariableRegistry);
        return pageNodeValidator;
    }

    private void declareImplicitVariables() {
        elValidator.declareVariable("request", FakeValueFactory
                .fakeValueOfType(HttpServletRequest.class, "request"));
    }

    private void assertJspDirValid(File directory, String dirDescription) throws IllegalArgumentException {
        if (directory == null) {
            throw new IllegalArgumentException(dirDescription + " may not be null");
        }

	    final String pathMsg = " Path: " + directory + " (absolute: " + directory.getAbsolutePath() + ")";
        if (!directory.exists()) {
	        throw new IllegalArgumentException(dirDescription + " does not exist!" + pathMsg);
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException(dirDescription + " is not a directory!" + pathMsg);
        } else if (!directory.canRead()) {
            throw new IllegalArgumentException(dirDescription + " is not readable!" + pathMsg);
        }
    }

    private JspCParsingToNodesOnly createJsfElValidatingJspParser(
            String jspDir, JsfElValidatingPageNodeListener tagJsfElValidator) {
        JsfElCheckingVisitor.setNodeListener(tagJsfElValidator);

        JspCParsingToNodesOnly jspc = new JspCParsingToNodesOnly();
        jspc.setUriroot(jspDir);
        jspc.setVerbose(1); // 0 = false, 1 = true
        if (jspsToIncludeCommaSeparated != null) {
            jspc.setJspFiles(jspsToIncludeCommaSeparated); // leave unset to process all; comma-separated
        }
        return jspc;
    }

    /*
     * private void reportInvalidExpressions(List<ExpressionFailure> failures) {
     * for (ExpressionFailure expressionFailure : failures) {
     * System.out.println("Invalid EL: " + expressionFailure.expression + " in "
     * + expressionFailure.sourceFile + ", problem: " +
     * expressionFailure.problem); } }
     */

    /*
     * // Using RegExp private List<ExpressionFailure>
     * validateJsfExpressionsInViews( Collection<File> viewFile) throws
     * IOException { List<ExpressionFailure> failures = new
     * LinkedList<ExpressionFailure>(); for (File file : viewFile) { JsfElFinder
     * expressionFinder = new JsfElFinder( FileUtils.readFileToString(file));
     *
     * for (ExpressionInfo elExpression : expressionFinder) { final String
     * expression = elExpression.getExpression(); // check valid if
     * (elExpression.getType() == ExpressionInfo.ElType.VALUE) {
     * elValidator.validateValueElExpression(expression, null); } else {
     * elValidator.validateMethodElExpression(expression, null); }
     * //failures.add(new ExpressionFailure(expression, e.getMessage(), file));
     * } } return failures; }
     *
     * private Collection<File> findViewFilesInFilesystem() { ViewFileFinder
     * viewFinder = new FilesystemViewFinder(Collections.singleton(new
     * File("web"))); Collection<File> viewFile = viewFinder.findViewFiles();
     *
     * System.out.println(">>> VIEW FILES " + viewFile.toString().replace(',',
     * '\n')); System.out.println(
     * "#############################################################\n");
     * return viewFile; }
     */

    /**
     * Find out what managed beans are defined in faces-context and perhaps
     * elsewhere and declare them to the validator.
     *
     * @param elValidator (required)
     */
    private void discoverAndRegisterDefinedManagedBeans(JsfElValidator elValidator) {    // SUPPRESS CHECKSTYLE (param hides field)
        Collection<ManagedBeanDescriptor> allDefinedBeans = new LinkedList<ManagedBeanFinder.ManagedBeanDescriptor>();

        allDefinedBeans.addAll(findFacesManagedBeans());
        int facesBeans = allDefinedBeans.size();
        allDefinedBeans.addAll(findSpringManagedBeans());
        int springBeans = allDefinedBeans.size() - facesBeans;

        System.out.println(">>> KNOWN BEANS [total: " + allDefinedBeans.size()
                + ", faces-config: " + facesBeans + ", Spring: " + springBeans
                + "]: " + allDefinedBeans);
        System.out
                .println("#############################################################\n");

        for (ManagedBeanDescriptor beanDescriptor : allDefinedBeans) {
            Object fakeValue = mock(beanDescriptor.getType());
            elValidator.declareVariable(beanDescriptor.getName(), fakeValue);
        }
    }

    private Collection<ManagedBeanDescriptor> findFacesManagedBeans() {
        Collection<InputResource> configStreams = managedBeansConfiguration.getFacesConfigStreams();
        if (configStreams.isEmpty()) {
            return Collections.emptyList();
        }

        LOG.info("Loading faces-config managed beans from " + configStreams);

        ManagedBeanFinder beanFinder = createManagedBeanFinder(configStreams);
        Collection<ManagedBeanDescriptor> facesConfigBeans = beanFinder
                .findDefinedBackingBeans();
        return facesConfigBeans;
    }

    protected abstract ManagedBeanFinder createManagedBeanFinder(
            Collection<InputResource> facesConfigFilesToRead);

    Collection<ManagedBeanDescriptor> findSpringManagedBeans() {
        Collection<InputResource> configStreams = managedBeansConfiguration.getSpringConfigStreams();
        if (configStreams.isEmpty()) {
            return Collections.emptyList();
        }

        LOG.info("Loading Spring managed beans from " + configStreams);

        ManagedBeanFinder beanFinder = SpringContextBeanFinder.forStreams(configStreams);
        return beanFinder.findDefinedBackingBeans();
    }

    /**
     * Used to ignore some expressions, i.e. not to validate them.
     * <p>
     * 		Note that you can use it quite creatively f.ex. to build statistics about what EL and managed beans your app really uses (w/o any filtering).
     * </p>
     * @param elExpressionFilter (required)
     */
    public void addElExpressionFilter(ElExpressionFilter elExpressionFilter) {
        elValidator.addElExpressionFilter(elExpressionFilter);
    }

    protected static void main(AbstractJsfStaticAnalyzer analyzer, String[] args) throws Exception {

        String jspRoot = null;
        Map<String, Class<?>> componentTypeOverrides = new Hashtable<String, Class<?>>();
        Map<String, Class<?>> extraVariables = new Hashtable<String, Class<?>>();
        Map<String, Class<?>> propertyOverrides = new Hashtable<String, Class<?>>();

        for (int i = 0; i < args.length; i += 2) {
            String argument = args[i];

            if ("--localVariableTypes".equals(argument)) {
                parseNameToTypeMappings(args[i + 1], componentTypeOverrides);
            }

            if ("--propertyOverrides".equals(argument)) {
                parseNameToTypeMappings(args[i + 1], propertyOverrides);
            }

            if ("--extraVariables".equals(argument)) {
                parseNameToTypeMappings(args[i + 1], extraVariables);
            }

            if ("--jspRoot".equals(argument)) {
                jspRoot = args[i + 1];
            }

        }

        if (jspRoot == null) {
            System.err
                    .println("USAGE: java -jar ... <options>; options are:\n"
                            + " --jspRoot <directory> (required)\n"
                            + " --localVariableTypes <bean1.property=package.SomeType,bean2.p2.p3=...> (optional) - "
                            + "types of components in colections used as value of h:dataTable\n"
                            + " --extraVariables <bean1=SomeType1,bean2=AnotherType,...> (optional) - define managed "
                            + "beans not in faces-config\n"
                            + " --propertyOverrides bean1.property=package.SomeType,..> (optional) - types of objects "
                            + "in collections used for iterating etc.\n");
            System.exit(-1);
        }

        analyzer.validateElExpressions(new File(jspRoot));
    }

    private static void parseNameToTypeMappings(String argumentValue,
            Map<String, Class<?>> parsedMappings) {

        String[] individualMappings = argumentValue.split(",");

        try {
            for (String mapping : individualMappings) {
                String[] mappingParts = mapping.split("=");
                String expression = mappingParts[0];
                Class<?> type = Class.forName(mappingParts[1]);
                parsedMappings.put(expression, type);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse argument '"
                            + argumentValue
                            + "'; expected format: 'string1=package.Type1,string2=Type2' etc. "
                            + " Problem: " + e);
        }

    }

    /**
     * Normally successfully validated expressions are not printed but by setting this to true
     * you can force them to be printed.
     * <p>
     * It can be also set by setting the system property
     * {@code jsfelcheck.printCorrectExpressions} to true.
     *
     * @param printCorrectExpressions (required)
     */
    public void setPrintCorrectExpressions(boolean printCorrectExpressions) {
        resultsReporter.setPrintCorrectExpressions(printCorrectExpressions);
    }

    public boolean isPrintCorrectExpressions() {
        return resultsReporter.isPrintCorrectExpressions();
    }

    /**
     * Process only the given files; set to null to process all.
     * @param jspsToIncludeCommaSeparated (optional) comma-separated list of path to files to process,
     * relative to the jspDir (they shouldn't start with a '/'). Null to reset, i.e. to process all files under jspDir.
     */
    public void setJspsToIncludeCommaSeparated(
            String jspsToIncludeCommaSeparated) {
        this.jspsToIncludeCommaSeparated = jspsToIncludeCommaSeparated;
    }

    public String getJspsToIncludeCommaSeparated() {
        return jspsToIncludeCommaSeparated;
    }

    /**
     * True - do not print results to the standard output / error stream. Default: false.
     * <p>
     * It can be also set by setting the system property
     * {@code jsfelcheck.suppressOutput} to true.
     *
     * @param suppressOutput (required)
     */
    public void setSuppressOutput(boolean suppressOutput) {
        resultsReporter.setSuppressOutput(suppressOutput);
    }

    public boolean isSuppressOutput() {
        return resultsReporter.isSuppressOutput();
    }

    /** For testing only */
    ContextVariableRegistry getContextVariableRegistry() {
        return localVariableConfiguration.toRegistry();
    }

    /**
     * Declare local variables (defined by tags such as h:dataTable) and resolvers for non-default
     * tags that declare local variables.
     * @param configuration (required)
     * @return this
     */
    public T withLocalVariablesConfiguration(LocalVariableConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration: LocalVariableConfiguration may not be null");
        }
        this.localVariableConfiguration = configuration;
        return self;
    }

    /**
     * Configure where should be definitions of known managed beans loaded from
     * and optionally global (as opposed to tag-local) variables that the validator cannot detect itself.
     * (Basically a managed bean is also just a variable.)
     * @param configuration (required)
     * @return this
     */
    public T withManagedBeansAndVariablesConfiguration(ManagedBeansAndVariablesConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration: LocalVariableConfiguration may not be null");
        }
        this.managedBeansConfiguration = configuration;

        for (Map.Entry<String, Object> extraVariable : configuration.getExtraVariables().entrySet()) {
            elValidator.declareVariable(extraVariable.getKey(), extraVariable.getValue());
        }

	    for (Map.Entry<String, Object> annotatedBean : configuration.getAnnotatedBeansFound().entrySet()) {
		    elValidator.declareVariable(annotatedBean.getKey(), annotatedBean.getValue());
	    }

        return self;
    }

    /**
     * Specify the type of a 'property' in a JSF EL expression, usually a component of a collection etc.
     * If you have #{myBean.myMap['anyKey'].whatever} you may declare the type returned from the myMap
     * by specifying override for 'myBean.myMap.*' to be e.g. WhateverType.class.
     * <p>
     * There are two types of overrides:
     * (1) property overrides: pass in the complete property, ex: bean.property1.property2
     * (2) collection component type overrides: for all sub-properties of a variable/property
     * (unless there is also a property override for it), used for arrays etc. Ex: bean.mapProperty.* =>
     * bean.mapProperty['someKey'] and bean.mapProperty.anotherProperty will be both affected by the override
     *
     * @param mapJsfExpression (required) The expression where to override the guessed type with only names and dots,
     * perhaps plus .*; i.e. 'var.prop['key']' becomes var.prop
     * @param newType (required) the type to use for the property
     * @return this
     */
    public T withPropertyTypeOverride(final String mapJsfExpression, final Class<?> newType) {
        assertNotNull(mapJsfExpression, "mapJsfExpression", String.class);
        assertNotNull(newType, "newType", Class.class);
        elValidator.definePropertyTypeOverride(mapJsfExpression, newType);
        return self;
    }

    /**
     * Inform the user that other than our modified EL implementation has been loaded, unless forced not to fail.
     * @param dependencyName (required) the name of the maven dependency
     * @throws IllegalStateException
     */
    protected final void handleUnhackedElImplementationLoaded(String dependencyName) throws IllegalStateException {
        String ignoreFailureProperty = "jsfelcheck.ignoreUnhackedElDependency";
        if (! Boolean.getBoolean(ignoreFailureProperty)) {
            throw new IllegalStateException("You are not using the hacked version of " + dependencyName
                + " and thus not all"
                + "expressions can be checked due to short-circuit evaluation of and/or/?:. Please fix your"
                + " classpath or set the system property '" + ignoreFailureProperty + "' to true if it's "
                + "OK for you that we can't guratantee validation of everything.");
        }
    }
}
