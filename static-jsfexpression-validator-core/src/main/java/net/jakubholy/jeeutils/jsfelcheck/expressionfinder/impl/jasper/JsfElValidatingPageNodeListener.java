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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper;

import java.util.Stack;
import java.util.logging.Logger;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.ContextVariableRegistry;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables.MissingLocalVariableTypeDeclarationException; // SUPPRESS CHECKSTYLE
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InternalValidatorFailureException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.JsfExpressionDescriptor;

/**
 * The main processing class for the Jasper-based implementation: retrieves information about
 * JSP tags of interest and triggers JSF EL extraction and validation while maintaining the local variable stack.
 */
public class JsfElValidatingPageNodeListener implements PageNodeListener {

    private static final Logger LOG = Logger.getLogger(JsfElValidatingPageNodeListener.class.getName());

    private final ContextVariableRegistry contextVarRegistry;
    private final PageNodeExpressionValidator nodeValidator;
    private final CollectedValidationResultsImpl validationResults = new CollectedValidationResultsImpl();

    private Stack<String> jspFileInclusionStack = new Stack<String>();
    private String currentJspFile;

    /**
     * New listener using the given validator and resolving local variables via the given registry.
     * @param expressionValidator (required)
     * @param contextVarRegistry (required)
     */
    public JsfElValidatingPageNodeListener(
            JsfElValidator expressionValidator, ContextVariableRegistry contextVarRegistry) {
        this.contextVarRegistry = contextVarRegistry;
        this.nodeValidator = new PageNodeExpressionValidator(expressionValidator);
    }

    /** {@inheritDoc} */
    public void nodeEntered(PageNode jspTag) {
        LOG.fine("PROCESSING " + jspTag.getQName() + " at "
                + jspTag.getLineNumber() + " id " + jspTag.getId()
                + ", class: " + jspTag.getTagHandlerClass().getName()
                + ", attrs: " + jspTag.getAttributes());

        try {

            AttributesValidationResult resolvedJsfExpressions = nodeValidator.validateJsfExpressions(
                jspTag.getTagHandlerClass(), jspTag.getAttributes());

            validationResults.addAllFromTagLineNr(
                jspTag.getLineNumber()
                , resolvedJsfExpressions.getAllResults());

            contextVarRegistry.extractContextVariables(jspTag, resolvedJsfExpressions);
        } catch (MissingLocalVariableTypeDeclarationException e) {
            e.setTagLineNumber(jspTag.getLineNumber());
            e.setJspFile(currentJspFile);
            validationResults.reportContextVariableNeedingTypeDeclaration(e);
        } catch (InternalValidatorFailureException e) {
            e.setExpressionDescriptor(new JsfExpressionDescriptor(jspTag.getLineNumber(), currentJspFile));
            throw e;
        }
    }

    /** {@inheritDoc} */
    public void nodeLeft(PageNode jspTag) {
        LOG.fine("DONE WITH " + jspTag.getId());
        contextVarRegistry.discardContextFor(jspTag);
    }

    /** {@inheritDoc} */
    public void fileEntered(String newJspFile) {
        setCurrentJspFile(newJspFile);
        LOG.info(">>> STARTED FOR '" + newJspFile + " #############################################");
    }

    public CollectedValidationResultsImpl getValidationResults() {
        return validationResults;
    }

    /** {@inheritDoc} */
    public void includedFileEntered(String includedFileName) {
        jspFileInclusionStack.push(currentJspFile);
        setCurrentJspFile(includedFileName);
    }

    /** {@inheritDoc} */
    public void includedFileLeft(String includedFileName) {
        setCurrentJspFile(jspFileInclusionStack.pop());
    }

    private void setCurrentJspFile(String currentJspFile) {
        this.currentJspFile = currentJspFile;
        this.validationResults.setCurrentJspFile(currentJspFile);
    }

}
