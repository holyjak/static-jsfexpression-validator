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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jakubholy.jeeutils.jsfelcheck.validator.ElVariableResolver;

/**
 * Registry of local JSF EL variables defined in a view page used for resolving
 * them in a context-sensitive way.
 *
 * It delegates the actual resolution to individual tag resolvers respecting the current
 * context (scope) starting from the innermost context and proceeding upwards.
 */
public class ContextVariableRegistry implements ElVariableResolver {

    public static class Error_YouMustDelcareTypeForThisVariable {}

    private static class VariableContex {
        private final long tagId;
        private final VariableInfo variable;

        public VariableContex(long tagId, VariableInfo variableInfo) {
            this.tagId = tagId;
            this.variable = variableInfo;
        }

        public long getTagId() {
            return tagId;
        }

        public VariableInfo getVariable() {
            return variable;
        }

    }

    private final Map<String, TagJsfVariableResolver> resolvers = new Hashtable<String, TagJsfVariableResolver>();

    private final List<VariableContex> contextStack = new LinkedList<VariableContex>();

    public ContextVariableRegistry registerResolverForTag(String tagQName,
            TagJsfVariableResolver resolver) {
        resolvers.put(tagQName, resolver);
        return this;
    }

    @Override
    public Class<?> resolveVariable(String name) {
        for (VariableContex varContext : contextStack) {
            VariableInfo contextVariable = varContext.getVariable();
            if (contextVariable.getVariableName().equals(name)) {
                return contextVariable.getDeclaredVariableType();
            }
        }

        return null;
    }

    public void extractContextVariables(PageNode jspTag,
            AttributesValidationResult resolvedJsfExpressions) throws DeclareTypeOfVariableException {

        TagJsfVariableResolver resolverForTag = resolvers.get(jspTag.getqName());

        if (resolverForTag != null) {
            try {
                VariableInfo variable = resolverForTag.extractContextVariables(
                        jspTag.getAttributes(), resolvedJsfExpressions);
                // if not null => create new context & store it
                storeNewContextVariable(jspTag, variable);
            } catch (DeclareTypeOfVariableException e) {
                // Add a fake context variable to make the error messages clearer
                // (instead of unknown variable)
                storeNewContextVariable(jspTag, new VariableInfo(
                        e.getVariableName(), Error_YouMustDelcareTypeForThisVariable.class));
                throw e;
            }
        }
    }

    private void storeNewContextVariable(PageNode jspTag, VariableInfo variable) {
        if (variable != null) {
            contextStack.add(0, new VariableContex(jspTag.getId(), variable));
        }
    }

    public void discardContextFor(PageNode jspTag) {
        if (!contextStack.isEmpty() && contextStack.get(0).getTagId() == jspTag.getId()) {
            contextStack.remove(0);
        }

    }

}
