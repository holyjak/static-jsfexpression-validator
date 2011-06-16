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
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.FacesConfigXmlBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder.ManagedBeanDescriptor;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.SpringContextBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.ContextVariableRegistry;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.DataTableVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.DeclareTypeOfVariableException;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JsfElValidatingPageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.JspCParsingToNodesOnly;
import net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

import org.apache.jasper.compiler.JsfElCheckingVisitor;

/**
 * Perform analysis of (selected) JSF 1.1 JSP files and validate that
 * all EL expressions reference only existing managed beans and their
 * properties/action methods.
 * <p>
 * For local variables, such as the <var>var</var> produced by h:dataTable, you must first declare of
 * what type they are as this cannot be determined based on the code, see {@link JsfElValidator#definePropertyTypeOverride(String, Class)}.
 * <p>
 * If there are some EL variables aside of managed beans in faces-config and the local ones you can
 * declare them to the validator via {@link JsfElValidator#declareVariable(String, Object)}.
 * <p>
 * If there are other tags than h:dataTable that can create local variables, you must create and
 * register an appropriate resolver for them as is done with the dataTable.
 *
 * <h3>Limitations</h3>
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
 * - Perform a separate run using the RegExp EL extractor to verify that the Jasper-based one
 * has found all EL expressions.
 *
 * @author jholy
 *
 */
public class JsfStaticAnalyzer {

    private boolean printCorrectExpressions = false;
    private String jspsToIncludeCommaSeparated = null;

    /**
     * Notion: Variable - the first element in an EL expression; property: any but the first element.
     * Example: #{variable.propert1.property2['some.key']}
     * @param jspDir (required) where to search for JSP pages
     * @param localVariableTypes (required) type definitions for local EL variables such as produced by h:dataTable, see {@link  DataTableVariableResolver#declareTypeFor(String, Class)}
     * @param extraVariables (required) extra variables/managed beans not defined in faces-context, see {@link JsfElValidator#declareVariable(String, Object)}
     * @param propertyTypeOverrides  (required) override the type to use for a property; mostly useful for properties where the proper type cannot be derived
     *  such as a Collection, see {@link JsfElValidator#definePropertyTypeOverride(String, Class)}
     * @throws Exception
     */
    public void validateElExpressions(String jspDir, Map<String, Class<?>> localVariableTypes, Map<String, Class<?>> extraVariables, Map<String, Class<?>> propertyTypeOverrides) throws Exception {

            final long start = System.currentTimeMillis();

            // 1b. Set exclusions and type overrides
            // SKIPPED NOW

            // Context-local variables
            DataTableVariableResolver dataTableResolver = new DataTableVariableResolver();
            for (Entry<String, Class<?>> variable : localVariableTypes.entrySet()) {
                dataTableResolver.declareTypeFor(variable.getKey(), variable.getValue());
            }

            ContextVariableRegistry contextVarRegistry = new ContextVariableRegistry();
            contextVarRegistry.registerResolverForTag("h:dataTable", dataTableResolver);

            ValidatingJsfElResolver elValidator = new ValidatingJsfElResolver(contextVarRegistry);
            elValidator.setIncludeKnownVariablesInException(false);
            // FIXME Undeclared values w/o ComponentTypeOverride lead to st. like InternalValidatorFailureException:
            // Failed for the expression #{requestScope.noFields == 5}: java.lang.IllegalArgumentException:
            // Cannot convert MockObjectOfUnknownType[prop=noFields] of type class
            // no.via.test.jsfunit.validator.MockObjectOfUnknownType to class java.lang.Long.

            // DEFAULT EXTRA VARIABLES
            // TODO Do we want to report all requestScope variables that are used in the pages?
            elValidator.definePropertyTypeOverride("requestScope.*", String.class);

            for (Entry<String, Class<?>> override : propertyTypeOverrides.entrySet()) {
                elValidator.definePropertyTypeOverride(override.getKey(), override.getValue());
            }

            elValidator.declareVariable("requestScope", Collections.EMPTY_MAP);
            elValidator.declareVariable("sessionScope", Collections.EMPTY_MAP);
            elValidator.declareVariable("request", FakeValueFactory.fakeValueOfType(HttpServletRequest.class, "request"));

            for (Entry<String, Class<?>> variable : extraVariables.entrySet()) {
                Object fakedValue = FakeValueFactory.fakeValueOfType(variable.getValue(), variable.getKey());
                elValidator.declareVariable(variable.getKey(), fakedValue);
            }

            registerKnownManagedBeans(elValidator);

            // Listener
            JsfElValidatingPageNodeListener pageNodeValidator = new JsfElValidatingPageNodeListener(
                    elValidator, contextVarRegistry);

            // Run it
            JspCParsingToNodesOnly jspc = createJsfElValidatingJspParser(jspDir, pageNodeValidator);
            jspc.execute();

            System.err.println(">>> LOCAL VARIABLES THAT YOU MUST DECLARE TYPE FOR #########################################");
            for (DeclareTypeOfVariableException untypedVar : pageNodeValidator.getValidationResults().getVariablesNeedingTypeDeclaration()) {
                System.err.println(untypedVar);
            }

            System.err.println("\n>>> FAILED JSF EL EXPRESSIONS #########################################");
            System.err.println("(Set logging to fine for " + ValidatingJsfElResolver.class + " to se failure details and stacktraces)");

            // TODO Log separately undefined variables Later: suppress derived errors w/ them

            int failureCnt = 0;
            for (ValidationResult result : pageNodeValidator.getValidationResults().failures()) {
                System.err.println(result);
                ++failureCnt;
            }

            if (failureCnt > 0) {
                System.err.println("\n>>> TOTAL FAILED EXPRESIONS: " + failureCnt);
            }

            if (printCorrectExpressions) {
                System.out.println("\n>>> CORRECT EXPRESSIONS #########################################");
            }

            int successCnt = 0;
            for (ValidationResult result : pageNodeValidator.getValidationResults().goodResults()) {
                if (printCorrectExpressions) {
                    System.out.println(result);
                }
                ++successCnt;
            }

            final long end = System.currentTimeMillis();
            final long durationS = (end - start) / 1000;

            final long seconds = durationS % 60;
            final long minutes = durationS / 60;


            System.out.println("\n\n>>> TOTAL EXPRESSIONS CHECKED: " + (failureCnt + successCnt)
                    + " (FAILED: " + failureCnt + ") IN " + minutes + "min "
                    + seconds + "s");

            // TODO Verify all JSF ELs found & checked by comparing their number w/ regExp search

            /*
            Collection<File> viewFile = findViewFiles();
            List<ExpressionFailure> failures = validateJsfExpressionsInViews(viewFile);
            reportInvalidExpressions(failures);
            */
        }

        private JspCParsingToNodesOnly createJsfElValidatingJspParser(String jspDir, JsfElValidatingPageNodeListener tagJsfElValidator) {
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
        private void reportInvalidExpressions(List<ExpressionFailure> failures) {
            for (ExpressionFailure expressionFailure : failures) {
                System.out.println("Invalid EL: " + expressionFailure.expression
                        + " in " + expressionFailure.sourceFile
                        + ", problem: " + expressionFailure.problem);
            }
        }*/

        /*
        // Using RegExp
        private List<ExpressionFailure> validateJsfExpressionsInViews(
                Collection<File> viewFile) throws IOException {
            List<ExpressionFailure> failures = new LinkedList<ExpressionFailure>();
            for (File file : viewFile) {
                JsfElFinder expressionFinder = new JsfElFinder(
                        FileUtils.readFileToString(file));

                for (ExpressionInfo elExpression : expressionFinder) {
                    final String expression = elExpression.getExpression();
                    // check valid
                        if (elExpression.getType() == ExpressionInfo.ElType.VALUE) {
                            elValidator.validateValueElExpression(expression, null);
                        } else {
                            elValidator.validateMethodElExpression(expression, null);
                        }
                        //failures.add(new ExpressionFailure(expression, e.getMessage(), file));
                }
            }
            return failures;
        }

        private Collection<File> findViewFilesInFilesystem() {
            ViewFileFinder viewFinder = new FilesystemViewFinder(Collections.singleton(new File("web")));
            Collection<File> viewFile = viewFinder.findViewFiles();

            System.out.println(">>> VIEW FILES " + viewFile.toString().replace(',', '\n'));
            System.out.println("#############################################################\n");
            return viewFile;
        }
        */

        /**
         * Find out what managed beans are defined in faces-context and perhaps
         * elsewhere and declare them to the validator.
         * @param elValidator (required)
         */
        private void registerKnownManagedBeans(JsfElValidator elValidator) {
            Collection<ManagedBeanDescriptor> allDefinedBeans = new LinkedList<ManagedBeanFinder.ManagedBeanDescriptor>();
            {
                ManagedBeanFinder beanFinder = new FacesConfigXmlBeanFinder(
                        new File("web/WEB-INF/faces-config.xml"));
                Collection<ManagedBeanDescriptor> facesConfigBeans = beanFinder.findDefinedBackingBeans();

                Collection<ManagedBeanDescriptor> springBeans = findSpringManagedBeans();

                allDefinedBeans.addAll(facesConfigBeans);
                allDefinedBeans.addAll(springBeans);
            }

            System.out.println(">>> KNOWN BEANS: " + allDefinedBeans);
            System.out.println("#############################################################\n");

            // TODO where is messages defined?
            elValidator.declareVariable("messages", Collections.emptyMap());

            for (ManagedBeanDescriptor beanDescriptor : allDefinedBeans) {
                Object fakeValue = mock(beanDescriptor.getType());
                elValidator.declareVariable(beanDescriptor.getName(), fakeValue);
            }
        }

        private Collection<ManagedBeanDescriptor> findSpringManagedBeans() {
            File springConfig = new File("web/WEB-INF/applicationContext.xml");

            if (!springConfig.canRead()) {
                return Collections.emptyList();
            }

            ManagedBeanFinder beanFinder = new SpringContextBeanFinder(
                    Collections.singleton(springConfig));

            String oldViaDeploymentEnvironment = System.getProperty("via.deploymentEnvironment");
            System.setProperty("via.deploymentEnvironment", "production");

            try {
                return beanFinder.findDefinedBackingBeans();
            } finally {
                if (oldViaDeploymentEnvironment == null) {
                    System.clearProperty("via.deploymentEnvironment");
                } else {
                    System.setProperty("via.deploymentEnvironment", oldViaDeploymentEnvironment);
                }
            }
        }

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

        public static void main(String[] args) throws Exception {

            String jspRoot = null;
            Map<String, Class<?>> componentTypeOverrides = new Hashtable<String, Class<?>>();
            Map<String, Class<?>> extraVariables = new Hashtable<String, Class<?>>();
            Map<String, Class<?>> propertyOverrides = new Hashtable<String, Class<?>>();

            for (int i = 0; i < args.length; i += 2) {
                String argument = args[i];

                if ("--localVariableTypes".equals(argument)) {
                    parseNameToTypeMappings(args[i+1], componentTypeOverrides);
                }

                if ("--propertyOverrides".equals(argument)) {
                    parseNameToTypeMappings(args[i+1], propertyOverrides);
                }

                if ("--extraVariables".equals(argument)) {
                    parseNameToTypeMappings(args[i+1], extraVariables);
                }

                if ("--jspRoot".equals(argument)) {
                    jspRoot = args[i+1];
                }

            }

            if (jspRoot == null) {
                System.err.println("USAGE: java -jar ... <options>; options are:\n"
                		+ " --jspRoot <directory> (required)\n"
                		+ " --localVariableTypes <bean1.property=package.SomeType,bean2.p2.p3=...> (optional) - types of components in colections used as value of h:dataTable\n"
                		+ " --extraVariables <bean1=SomeType1,bean2=AnotherType,...> (optional) - define managed beans not in faces-config\n"
                	    + " --propertyOverrides bean1.property=package.SomeType,..> (optional) - types of objects in collections used for iterating etc.\n");
                System.exit(-1);
            }

            new JsfStaticAnalyzer().validateElExpressions(jspRoot, componentTypeOverrides, extraVariables, propertyOverrides);
        }

        private static void parseNameToTypeMappings(
                String argumentValue, Map<String, Class<?>> parsedMappings) {

            String[] individualMappings = argumentValue.split(",");

            try {
                for (String mapping : individualMappings) {
                    String[] mappingParts = mapping.split("=");
                    String expression = mappingParts[0];
                    Class<?> type = Class.forName(mappingParts[1]);
                    parsedMappings.put(expression, type);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse argument '"
                        + argumentValue + "'; expected format: 'string1=package.Type1,string2=Type2' etc. "
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

}
