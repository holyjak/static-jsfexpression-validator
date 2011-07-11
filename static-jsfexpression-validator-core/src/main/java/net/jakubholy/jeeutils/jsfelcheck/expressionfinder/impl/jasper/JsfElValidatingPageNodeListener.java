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

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.ContextVariableRegistry;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.MissingLocalVariableTypeDeclarationException;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InternalValidatorFailureException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.JsfExpressionDescriptor;

/**
 * The main processing class for the Jasper-based implementation: retrieves information about
 * JSP tags of interest and JSF EL extraction and validation while maintaining the local variable stack.
 */
public class JsfElValidatingPageNodeListener implements PageNodeListener {

    private static final Logger LOG = Logger.getLogger(JsfElValidatingPageNodeListener.class.getName());

    private final ContextVariableRegistry contextVarRegistry;
    private final PageNodeExpressionValidator nodeValidator;
    private final CollectedValidationResultsImpl validationResults = new CollectedValidationResultsImpl();

    private Stack<String> jspFileInclusionStack = new Stack<String>();
    private String jspFile;

    public JsfElValidatingPageNodeListener(JsfElValidator expressionValidator, ContextVariableRegistry contextVarRegistry) {
        this.contextVarRegistry = contextVarRegistry;
        this.nodeValidator = new PageNodeExpressionValidator(expressionValidator);
    }

    //@Override
    public void nodeEntered(PageNode jspTag) {
        LOG.fine("PROCESSING " + jspTag.getqName() + " at " +
                jspTag.getLineNumber() + " id " + jspTag.getId()
                + ", class: " + jspTag.getTagHandlerClass().getName()
                + ", attrs: " + jspTag.getAttributes());

        try {

            AttributesValidationResult resolvedJsfExpressions = nodeValidator.validateJsfExpressions(
                jspTag.getAttributes());

            validationResults.addAllFromTagLineNr(
                jspTag.getLineNumber()
                , resolvedJsfExpressions.getAllResults());

            contextVarRegistry.extractContextVariables(jspTag, resolvedJsfExpressions);
        } catch (MissingLocalVariableTypeDeclarationException e) {
            e.setTagLineNumber(jspTag.getLineNumber());
            e.setJspFile(jspFile);
            validationResults.reportContextVariableNeedingTypeDeclaration(e);
        } catch (InternalValidatorFailureException e) {
            e.setExpressionDescriptor(new JsfExpressionDescriptor(jspTag.getLineNumber(), jspFile));
            throw e;
        }
    }

    //@Override
    public void nodeLeft(PageNode jspTag) {
        LOG.fine("DONE WITH " + jspTag.getId());
        contextVarRegistry.discardContextFor(jspTag);
    }

    //@Override
    public void fileEntered(String jspFile) {
        setCurrentJspFile(jspFile);
        LOG.info(">>> STARTED FOR '" + jspFile + " #############################################");
    }

    public CollectedValidationResultsImpl getValidationResults() {
        return validationResults;
    }

    //@Override
    public void includedFileEntered(String includedFileName) {
        jspFileInclusionStack.push(jspFile);
        setCurrentJspFile(includedFileName);
    }

    //@Override
    public void includedFileLeft(String includedFileName) {
        setCurrentJspFile(jspFileInclusionStack.pop());
    }

    private void setCurrentJspFile(String jspFile) {
        this.jspFile = jspFile;
        this.validationResults.setCurrentJspFile(jspFile);
    }

}
