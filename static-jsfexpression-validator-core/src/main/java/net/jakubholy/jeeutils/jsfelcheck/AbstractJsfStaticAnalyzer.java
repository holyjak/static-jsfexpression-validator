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

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.FileUtils;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder.ManagedBeanDescriptor;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.SpringContextBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.CollectedValidationResultsImpl;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JsfElValidatingPageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JspCParsingToNodesOnly;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.ContextVariableRegistry;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.DataTableVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;

import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JsfElCheckingVisitor;

/**
 * Perform analysis of (selected) JSF 1.1 JSP files and validate that all EL
 * expressions reference only existing managed beans and their properties/action
 * methods.
 * <p>
 * For local variables, such as the <code>var</code> produced by h:dataTable, you
 * must first declare of what type they are as this cannot be determined based
 * on the code, see
 * {@link DataTableVariableResolver#declareTypeFor(String, Class)}.
 * <p>
 * If there are some EL variables aside of managed beans in faces-config (and
 * perhaps Spring config) and the local variables you can declare them to the
 * validator via {@link JsfElValidator#declareVariable(String, Object)}.
 * <p>
 * If there are other tags than h:dataTable that can create local variables, you
 * must create and register an appropriate resolver for them as is done with the
 * dataTable.
 *
 * <h3>How it works</h3>
 * We use "fake value resolvers" for a real JSF resolver; those resolvers do not retrieve
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
 * - JSF 2.0 doesn't have a native support yet, reuses JSF 1.2 implementation.
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
 * <h3>TO DO</h3>
 * - Perform a separate run using the RegExp EL extractor to verify that the
 * Jasper-based one has found all EL expressions.
 *
 * @author jholy
 *
 */
public abstract class AbstractJsfStaticAnalyzer {

    private static final Logger LOG = Logger.getLogger(AbstractJsfStaticAnalyzer.class.getName());

    private final ValidatingElResolver elValidator;
    private final ResultsReporter resultsReporter = new ResultsReporter();

    private String jspsToIncludeCommaSeparated = null;
    private Collection<InputStream> facesConfigFiles = Collections.emptyList();
    private Collection<InputStream> springConfigFiles = Collections.emptyList();

    /** New, unconfigured analyzer. */
    public AbstractJsfStaticAnalyzer() {
        elValidator = createValidatingElResolver();
    }

    /** Create the JSF-implementation specific valiator to use. */
    protected abstract ValidatingElResolver createValidatingElResolver();


    /**
     * Check expressions in all JSP files under the jspDir and print the failed (or all) ones
     * to System out.
     * <p>
     * Notion: Variable - the first element in an EL expression; property: any
     * but the first element. Example:
     * #{variable.propert1.property2['some.key']}
     *
     * @param jspDir
     *            (required) where to search for JSP pages
     * @param localVariableTypesParam
     *            (required) type definitions for local EL variables such as
     *            produced by h:dataTable, see
     *            {@link DataTableVariableResolver#declareTypeFor(String, Class)}
     * @param extraVariablesParam
     *            (required) extra variables/managed beans not defined in
     *            faces-context, see
     *            {@link JsfElValidator#declareVariable(String, Object)}
     * @param propertyTypeOverridesParam
     *            (required) override the type to use for a property; mostly
     *            useful for properties where the proper type cannot be derived
     *            such as a Collection, see
     *            {@link JsfElValidator#definePropertyTypeOverride(String, Class)}
     * @return results of the validation (never null)
     * @throws Exception
     */
    public CollectedValidationResults validateElExpressions(String jspDir,
            Map<String, Class<?>> localVariableTypesParam,
            Map<String, Class<?>> extraVariablesParam,
            Map<String, Class<?>> propertyTypeOverridesParam) {

        final Map<String, Class<?>> localVariableTypes = assignMapOrEmpty(localVariableTypesParam);
        final Map<String, Class<?>> extraVariables = assignMapOrEmpty(extraVariablesParam);
        final Map<String, Class<?>> propertyTypeOverrides = assignMapOrEmpty(propertyTypeOverridesParam);

        assertJspDirValid(jspDir);

        LOG.info("validateElExpressions: entry for JSP root " + jspDir + ", " + extraVariables.size()
                + " extra variables, " + localVariableTypes.size() + " type-defined local variables, "
                + propertyTypeOverrides.size() + " property type overrides.");

        final long start = System.currentTimeMillis();

        JsfElValidatingPageNodeListener pageNodeValidator = initializeValidationSubsystem(
                localVariableTypes, extraVariables, propertyTypeOverrides);

        applyConfigurationFromSystemProperties();

        // Run it
        JspCParsingToNodesOnly jspc = createJsfElValidatingJspParser(jspDir,
                pageNodeValidator);
        try {
            jspc.execute();
        } catch (JasperException e) {
            throw new RuntimeException("Jasper failed to parse your JSP files", e);
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

    private void applyConfigurationFromSystemProperties() {
        setPrintCorrectExpressions(
                Boolean.getBoolean("jsfelcheck.printCorrectExpressions") || isPrintCorrectExpressions());
        setSuppressOutput(
                Boolean.getBoolean("jsfelcheck.suppressOutput") || isSuppressOutput());
    }

    private JsfElValidatingPageNodeListener initializeValidationSubsystem(
            final Map<String, Class<?>> localVariableTypes,
            final Map<String, Class<?>> extraVariables,
            final Map<String, Class<?>> propertyTypeOverrides) {
        final ContextVariableRegistry contextVarRegistry = initializeContextVariableRegistry(localVariableTypes);

        elValidator.setUnknownVariableResolver(
                contextVarRegistry);
        elValidator.setIncludeKnownVariablesInException(false);

        setPropertyTypeOverrides(propertyTypeOverrides);

        // DEFAULT EXTRA VARIABLES
        declareImplicitVariables();
        declareExtraVariables(extraVariables);

        discoverAndRegisterDefinedManagedBeans(elValidator);

        // Listener
        JsfElValidatingPageNodeListener pageNodeValidator = new JsfElValidatingPageNodeListener(
                elValidator, contextVarRegistry);
        return pageNodeValidator;
    }

    private void declareExtraVariables(
            final Map<String, Class<?>> extraVariables) {
        for (Entry<String, Class<?>> variable : extraVariables.entrySet()) {
            Object fakedValue = FakeValueFactory.fakeValueOfType(
                    variable.getValue(), variable.getKey());
            elValidator.declareVariable(variable.getKey(), fakedValue);
        }
    }

    private void declareImplicitVariables() {
        elValidator.declareVariable("request", FakeValueFactory
                .fakeValueOfType(HttpServletRequest.class, "request"));
    }

    private void setPropertyTypeOverrides(
            final Map<String, Class<?>> propertyTypeOverrides) {
        for (Entry<String, Class<?>> override : propertyTypeOverrides
                .entrySet()) {
            elValidator.definePropertyTypeOverride(override.getKey(),
                    override.getValue());
        }
    }

    private ContextVariableRegistry initializeContextVariableRegistry(
            final Map<String, Class<?>> localVariableTypes) {
        // Context-local variables
        DataTableVariableResolver dataTableResolver = initializeDataResolver(localVariableTypes);

        ContextVariableRegistry contextVarRegistry = new ContextVariableRegistry();
        contextVarRegistry.registerResolverForTag("h:dataTable", dataTableResolver);
        return contextVarRegistry;
    }

    private DataTableVariableResolver initializeDataResolver(
            final Map<String, Class<?>> localVariableTypes) {
        DataTableVariableResolver dataTableResolver = new DataTableVariableResolver();
        for (Entry<String, Class<?>> variable : localVariableTypes.entrySet()) {
            dataTableResolver.declareTypeFor(variable.getKey(),
                    variable.getValue());
        }
        return dataTableResolver;
    }

    private Map<String, Class<?>> assignMapOrEmpty(
            Map<String, Class<?>> parameter) {
        Map<String, Class<?>> localVariableTypes = parameter;
        if (localVariableTypes == null) {
            localVariableTypes = Collections.emptyMap();
        }
        return localVariableTypes;
    }

    private void assertJspDirValid(String jspDir) throws IllegalArgumentException {
        if (jspDir == null) {
            throw new IllegalArgumentException("jspDir (path of the directory with JSP files) may not be null");
        }

        File jspDirFile = new File(jspDir);
        if (!jspDirFile.isDirectory()) {
            throw new IllegalArgumentException("jspDir (path of the directory with JSP files) is not a directory! "
                    + "Path: " + jspDir + " (absolute: " + jspDirFile.getAbsolutePath() + ")");
        } else if (!jspDirFile.canRead()) {
            throw new IllegalArgumentException("jspDir (path of the directory with JSP files) is not readable! "
                    + "Path: " + jspDir + " (absolute: " + jspDirFile.getAbsolutePath() + ")");
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
        if (facesConfigFiles.isEmpty()) {
            return Collections.emptyList();
        }

        LOG.info("Loading faces-config managed beans from " + facesConfigFiles);

        ManagedBeanFinder beanFinder = createManagedBeanFinder(facesConfigFiles);
        Collection<ManagedBeanDescriptor> facesConfigBeans = beanFinder
                .findDefinedBackingBeans();
        return facesConfigBeans;
    }

    protected abstract ManagedBeanFinder createManagedBeanFinder(
            Collection<InputStream> facesConfigFilesToRead);

    private Collection<ManagedBeanDescriptor> findSpringManagedBeans() {
        if (springConfigFiles.isEmpty()) {
            return Collections.emptyList();
        }

        LOG.info("Loading Spring managed beans from " + springConfigFiles);

        ManagedBeanFinder beanFinder = SpringContextBeanFinder.forStreams(springConfigFiles);
        return beanFinder.findDefinedBackingBeans();
    }

    /**
     * Used to ignore some expressions, i.e. not to validate them.
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

        analyzer.validateElExpressions(jspRoot,
                componentTypeOverrides, extraVariables, propertyOverrides);
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
     * The faces-config.xml files to read managed beans from. Default: empty.
     * Set to empty or null not to process any.
     * @param facesConfigFiles (required) faces-config files to read managed beans from; may be empty
     */
    public void setFacesConfigFiles(Collection<File> facesConfigFiles) {
        if (facesConfigFiles == null || facesConfigFiles.isEmpty()) {
            this.facesConfigFiles = Collections.emptyList();
        } else {
            this.facesConfigFiles = FileUtils.filesToStream(facesConfigFiles);
        }
    }

    /**
     * The faces-config.xml files to read managed beans from. Default: empty.
     * Set to empty or null not to process any.
     * @param facesConfigStreams (required) faces-config files to read managed beans from; may be empty
     */
    public void setFacesConfigStreams(Collection<InputStream> facesConfigStreams) {
        if (facesConfigStreams == null || facesConfigStreams.isEmpty()) {
            this.facesConfigFiles = Collections.emptyList();
        } else {
            this.facesConfigFiles = facesConfigStreams;
        }
    }

    /**
     * The Spring application context XML files to read managed beans from.
     * Default: empty. Set to empty or null not to process any.
     * @param springConfigFiles (required) Spring applicationContext files to read managed beans from; may be empty
     */
    public void setSpringConfigFiles(Collection<File> springConfigFiles) {
        if (springConfigFiles == null || springConfigFiles.isEmpty()) {
            this.springConfigFiles = Collections.emptyList();
        } else {
            this.springConfigFiles = FileUtils.filesToStream(springConfigFiles);
        }
    }

    /**
     * The Spring application context XML files to read managed beans from.
     * Default: empty. Set to empty or null not to process any.
     * @param springConfigStream (required) Spring applicationContext files to read managed beans from; may be empty
     */
    public void setSpringConfigStreams(Collection<InputStream> springConfigStream) {
        if (springConfigStream == null || springConfigStream.isEmpty()) {
            this.springConfigFiles = Collections.emptyList();
        } else {
            this.springConfigFiles = springConfigStream;
        }
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

}
