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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.MissingLocalVariableTypeDeclarationException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.JsfExpressionDescriptor;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.MultipleValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

/**
 * Collected results of validation of one of more pages.
 */
public class CollectedValidationResultsImpl extends MultipleValidationResults implements CollectedValidationResults {

    private int currentTagLineNumber = -1;
    private final List<MissingLocalVariableTypeDeclarationException> variablesNeedingTypeDeclaration =
        new LinkedList<MissingLocalVariableTypeDeclarationException>();
    private String currentJspFile;

    @Override
    protected void postAddSingleResult(ValidationResult singleResult) {
        JsfExpressionDescriptor descriptor = singleResult.getExpressionDescriptor();
        if (descriptor == null) {
            descriptor = new JsfExpressionDescriptor(null);
            singleResult.setExpressionDescriptor(descriptor);
        }
        descriptor.setJspFile(currentJspFile);
        descriptor.setTagLineNumber(currentTagLineNumber);
    }

    public void addAllFromTagLineNr(int currentTagLineNumber,
            Collection<ValidationResult> allResults) {
        this.currentTagLineNumber = currentTagLineNumber;
        super.addAll(allResults);

    }

    public void reportContextVariableNeedingTypeDeclaration(
            MissingLocalVariableTypeDeclarationException e) {
        getVariablesNeedingTypeDeclaration().add(e);
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.CollectedValidationResults#getVariablesNeedingTypeDeclaration()
     */
    //@Override
    public Collection<MissingLocalVariableTypeDeclarationException> getVariablesNeedingTypeDeclaration() {
        return variablesNeedingTypeDeclaration;
    }

    public void setCurrentJspFile(String jspFile) {
        this.currentJspFile = jspFile;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + super.getResultsSummary()
            + ", local variables needing type declaration: " + variablesNeedingTypeDeclaration.size() + "]";
    }

}