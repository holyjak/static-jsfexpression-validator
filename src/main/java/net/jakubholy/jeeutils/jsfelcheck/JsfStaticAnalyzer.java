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
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.FacesConfigXmlBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder.ManagedBeanDescriptor;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.SpringContextBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.CollectedValidationResultsImpl;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JsfElValidatingPageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JspCParsingToNodesOnly;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.ContextVariableRegistry;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.DataTableVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.DeclareTypeOfVariableException;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.MockObjectOfUnknownType;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ExpressionRejectedByFilterResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JsfElCheckingVisitor;

/**
 * Perform analysis of (selected) JSF 1.1 JSP files and validate that all EL
 * expressions reference only existing managed beans and their properties/action
 * methods.
 * <p>
 * For local variables, such as the <var>var</var> produced by h:dataTable, you
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
 * we use {@link MockObjectOfUnknownType} - if you see it in a failed JSF EL check then you need
 * to declare the type to use.
 *
 * <h3>Limitations</h3>
 *
 * <pre>
 * - JSF 1.1 (switching to another one requires replacing the Sun's ValueBindingFactory and
 * MethodBindingFactory used by the {@link JsfElValidator} by appropriate alternatives).
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
public class JsfStaticAnalyzer {

    public static class ExpressionFailure {

        private final String expression;
        private final String problem;
        private final File sourceFile;

        public ExpressionFailure(String expression, String problem,
                File sourceFile) {
            this.expression = expression;
            this.problem = problem;
            this.sourceFile = sourceFile;
        }

        @Override
        public String toString() {
            return "ExpressionFailure [expression=" + expression + ", problem="
                    + problem + ", sourceFile=" + sourceFile + "]";
        }

    }

    private static final Logger LOG = Logger.getLogger(JsfStaticAnalyzer.class
            .getName());

    private final ValidatingJsfElResolver elValidator = new ValidatingJsfElResolver();

    private boolean printCorrectExpressions = false;
    private String jspsToIncludeCommaSeparated = null;
    private Collection<File> facesConfigFiles = Collections.emptyList();
    private Collection<File> springConfigFiles = Collections.emptyList();
    private boolean suppressOutput = false;


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
     * @param localVariableTypes
     *            (required) type definitions for local EL variables such as
     *            produced by h:dataTable, see
     *            {@link DataTableVariableResolver#declareTypeFor(String, Class)}
     * @param extraVariables
     *            (required) extra variables/managed beans not defined in
     *            faces-context, see
     *            {@link JsfElValidator#declareVariable(String, Object)}
     * @param propertyTypeOverrides
     *            (required) override the type to use for a property; mostly
     *            useful for properties where the proper type cannot be derived
     *            such as a Collection, see
     *            {@link JsfElValidator#definePropertyTypeOverride(String, Class)}
     * @return
     * @throws Exception
     */
    public CollectedValidationResults validateElExpressions(String jspDir,
            Map<String, Class<?>> localVariableTypes,
            Map<String, Class<?>> extraVariables,
            Map<String, Class<?>> propertyTypeOverrides) {

        if (localVariableTypes == null) {
            localVariableTypes = Collections.emptyMap();
        }
        if (extraVariables == null) {
            extraVariables = Collections.emptyMap();
        }
        if (propertyTypeOverrides == null) {
            propertyTypeOverrides = Collections.emptyMap();
        }

        assertJspDirValid(jspDir);

        LOG.info("validateElExpressions: entry for JSP root " + jspDir + ", " + extraVariables.size()
                + " extra variables, " + localVariableTypes.size() + " type-defined local variables, "
                + propertyTypeOverrides.size() + " property type overrides.");

        final long start = System.currentTimeMillis();

        // 1b. Set exclusions and type overrides
        // SKIPPED NOW

        // Context-local variables
        DataTableVariableResolver dataTableResolver = new DataTableVariableResolver();
        for (Entry<String, Class<?>> variable : localVariableTypes.entrySet()) {
            dataTableResolver.declareTypeFor(variable.getKey(),
                    variable.getValue());
        }

        ContextVariableRegistry contextVarRegistry = new ContextVariableRegistry();
        contextVarRegistry.registerResolverForTag("h:dataTable",
                dataTableResolver);

        elValidator.setUnknownVariableResolver(contextVarRegistry);
        elValidator.setIncludeKnownVariablesInException(false);

        for (Entry<String, Class<?>> override : propertyTypeOverrides
                .entrySet()) {
            elValidator.definePropertyTypeOverride(override.getKey(),
                    override.getValue());
        }

        // DEFAULT EXTRA VARIABLES
        elValidator.declareVariable("requestScope", Collections.EMPTY_MAP);
        // return an empty string, advantage: JSF can coalesce it to number/boolean/String as needed,
        // ex: #{requestScope.myCnt == 5}
        elValidator.definePropertyTypeOverride("requestScope.*", String.class);
        elValidator.declareVariable("sessionScope", Collections.EMPTY_MAP);
        elValidator.declareVariable("request", FakeValueFactory
                .fakeValueOfType(HttpServletRequest.class, "request"));

        for (Entry<String, Class<?>> variable : extraVariables.entrySet()) {
            Object fakedValue = FakeValueFactory.fakeValueOfType(
                    variable.getValue(), variable.getKey());
            elValidator.declareVariable(variable.getKey(), fakedValue);
        }

        registerKnownManagedBeans(elValidator);

        // Listener
        JsfElValidatingPageNodeListener pageNodeValidator = new JsfElValidatingPageNodeListener(
                elValidator, contextVarRegistry);

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

        printErr(">>> LOCAL VARIABLES THAT YOU MUST DECLARE TYPE FOR ["
                + results.getVariablesNeedingTypeDeclaration().size()
        		+ "] #########################################");
        for (DeclareTypeOfVariableException untypedVar : results.getVariablesNeedingTypeDeclaration()) {
            System.err.println(untypedVar);
        }

        printErr("\n>>> FAILED JSF EL EXPRESSIONS ["
                + results.failures().size()
        		+ "] #########################################");
        printErr("(Set logging to fine for "
                + ValidatingJsfElResolver.class
                + " to se failure details and stacktraces)");

        // TODO Log separately undefined variables Later: suppress derived
        // errors w/ them

        for (ValidationResult result : results.failures()) {
            System.err.println(result);
        }

        if (results.failures().size() > 0) {
            printErr("\n>>> TOTAL FAILED EXPRESIONS: " + results.failures().size());
        }

        if (results.excluded().size() > 0) {
            Set<ElExpressionFilter> filters = new HashSet<ElExpressionFilter>();
            for (ExpressionRejectedByFilterResult exclusionResult : results.excluded()) {
                filters.add(exclusionResult.getFilter());
            }
            String filtersList = filters.isEmpty()? "" : " by filters: " + filters;
            printErr(">>> TOTAL EXCLUDED EXPRESIONS: " + results.excluded().size() + filtersList);
        }

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

        final long end = System.currentTimeMillis();
        final long durationS = (end - start) / 1000;

        final long seconds = durationS % 60;
        final long minutes = durationS / 60;

        printOut("\n\n>>> TOTAL EXPRESSIONS CHECKED: "
                + (results.failures().size() + results.goodResults().size())
                + " (FAILED: " + results.failures().size()
                + ", IGNORED EXPRESSIONS: " + results.excluded().size()
                + ") IN " + minutes + "min " + seconds + "s");

        // TODO Verify all JSF ELs found & checked by comparing their number w/
        // regExp search

        /*
         * Collection<File> viewFile = findViewFiles(); List<ExpressionFailure>
         * failures = validateJsfExpressionsInViews(viewFile);
         * reportInvalidExpressions(failures);
         */

        return results;
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

    private void printOut(String message) {
        print(System.out, message);
    }

    private void printErr(String message) {
        print(System.err, message);
    }

    private void print(PrintStream out, String message) {
        if (!suppressOutput) {
            out.println(message);
        }
    }

    private JspCParsingToNodesOnly createJsfElValidatingJspParser(
            String jspDir, JsfElValidatingPageNodeListener tagJsfElValidator) {
        JsfElCheckingVisitor.setNodeListener(tagJsfElValidator);

        JspCParsingToNodesOnly jspc = new JspCParsingToNodesOnly();
        jspc.setUriroot(jspDir);
        jspc.setVerbose(1); // 0 = false, 1 = true
        if (jspsToIncludeCommaSeparated != null) {
            jspc.setJspFiles(jspsToIncludeCommaSeparated); // leave unset to
                                                           // process all;
                                                           // comma-separated
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
     * @param elValidator
     *            (required)
     */
    private void registerKnownManagedBeans(JsfElValidator elValidator) {
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
        if (getFacesConfigFiles().isEmpty()) {
            return Collections.emptyList();
        }

        LOG.info("Loading faces-config managed beans from "
                + getFacesConfigFiles());

        ManagedBeanFinder beanFinder = new FacesConfigXmlBeanFinder(
                getFacesConfigFiles());
        Collection<ManagedBeanDescriptor> facesConfigBeans = beanFinder
                .findDefinedBackingBeans();
        return facesConfigBeans;
    }

    private Collection<ManagedBeanDescriptor> findSpringManagedBeans() {
        if (getSpringConfigFiles().isEmpty()) {
            return Collections.emptyList();
        }

        LOG.info("Loading Spring managed beans from " + getSpringConfigFiles());

        ManagedBeanFinder beanFinder = new SpringContextBeanFinder(
                getSpringConfigFiles());
        return beanFinder.findDefinedBackingBeans();
    }

    /**
     * Used to ignore some expressions, i.e. not to validate them.
     * @param elExpressionFilter (required)
     */
    public void addElExpressionFilter(ElExpressionFilter elExpressionFilter) {
        elValidator.addElExpressionFilter(elExpressionFilter);
    }

    public static void main(String[] args) throws Exception {

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
                            + " --localVariableTypes <bean1.property=package.SomeType,bean2.p2.p3=...> (optional) - types of components in colections used as value of h:dataTable\n"
                            + " --extraVariables <bean1=SomeType1,bean2=AnotherType,...> (optional) - define managed beans not in faces-config\n"
                            + " --propertyOverrides bean1.property=package.SomeType,..> (optional) - types of objects in collections used for iterating etc.\n");
            System.exit(-1);
        }

        new JsfStaticAnalyzer().validateElExpressions(jspRoot,
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

    public void setPrintCorrectExpressions(boolean printCorrectExpressions) {
        this.printCorrectExpressions = printCorrectExpressions;
    }

    public boolean isPrintCorrectExpressions() {
        return printCorrectExpressions;
    }

    /**
     * Process only the given files; set to null to process all.
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
     */
    public void setFacesConfigFiles(Collection<File> facesConfigFiles) {
        if (facesConfigFiles == null) {
            this.facesConfigFiles = Collections.emptyList();
        } else {
            this.facesConfigFiles = facesConfigFiles;
        }
    }

    public Collection<File> getFacesConfigFiles() {
        return facesConfigFiles;
    }

    /**
     * The Spring application context XML files to read managed beans from.
     * Default: empty. Set to empty or null not to process any.
     */
    public void setSpringConfigFiles(Collection<File> springConfigFiles) {
        if (springConfigFiles == null) {
            this.springConfigFiles = Collections.emptyList();
        } else {
            this.springConfigFiles = springConfigFiles;
        }
    }

    public Collection<File> getSpringConfigFiles() {
        return springConfigFiles;
    }

    /**
     * True - do not print results to the standard output / error stream. Defaul: false.
     */
    public void setSuppressOutput(boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }

    public boolean isSuppressOutput() {
        return suppressOutput;
    }

}
