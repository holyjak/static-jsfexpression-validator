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

package net.jakubholy.jeeutils.jsfelcheck.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException;

/**
 * Resolve variables from a pre-defined list initialized via {@link PredefinedVariableResolver#declareVariable(String, Object)}.
 * Throws {@link VariableNotFoundException} if it encounters a variable not present on the list.
 */
public final class PredefinedVariableResolver {

    private boolean includeKnownVariablesInException = true;

    public static interface NewVariableEncounteredListener {
        public void handleNewVariableEncountered(String variableName);
    }

    private final Map<String, Object> knownVariables = new HashMap<String, Object>();
    private final PredefinedVariableResolver.NewVariableEncounteredListener newVariableEncounteredListener;
    private ElVariableResolver unknownVariableResolver;

    public PredefinedVariableResolver(
            final PredefinedVariableResolver.NewVariableEncounteredListener newVariableEncounteredListener) {

        this.newVariableEncounteredListener = newVariableEncounteredListener;
    }

    public Object resolveVariable(String variableName)
            throws EvaluationException {

        final Object resolvedValue = tryResolveVariable(variableName);

        if (resolvedValue == null) {
            throw new VariableNotFoundException("No variable '" + variableName + "' among the predefined ones"
                    + (isIncludeKnownVariablesInException()? ": " + knownVariables.keySet() : "."));
        }

        if (newVariableEncounteredListener != null) {
            newVariableEncounteredListener.handleNewVariableEncountered(variableName);
        }

        return resolvedValue;
    }

    private Object tryResolveVariable(String variableName)
    throws EvaluationException {

        if (knownVariables.containsKey(variableName)) {
            return knownVariables.get(variableName);
        }

        Class<?> contextLocalVarType = (unknownVariableResolver == null)?
                null : unknownVariableResolver.resolveVariable(variableName);
        if (contextLocalVarType != null) {
            return FakeValueFactory.fakeValueOfType(contextLocalVarType, variableName);
        }

        return null;
    }

    public void declareVariable(final String name, final Object value) {
        Object currentOverride = knownVariables.get(name);
        if (currentOverride != null) {
            throw new IllegalArgumentException("The variable '"
                    + name + "' is already defined; current value: " +
                    currentOverride + ", new: " + value);
        }
        knownVariables.put(name, value);
    }

    public Map<String, Object> getDeclaredVariables() {
        return Collections.unmodifiableMap(knownVariables);
    }

    public void setIncludeKnownVariablesInException(
            boolean includeKnownVariablesInException) {
        this.includeKnownVariablesInException = includeKnownVariablesInException;
    }

    public boolean isIncludeKnownVariablesInException() {
        return includeKnownVariablesInException;
    }

    public void setUnknownVariableResolver(ElVariableResolver unknownVariableResolver) {
        this.unknownVariableResolver = unknownVariableResolver;
    }

    public ElVariableResolver getUnknownVariableResolver() {
        return unknownVariableResolver;
    }

}