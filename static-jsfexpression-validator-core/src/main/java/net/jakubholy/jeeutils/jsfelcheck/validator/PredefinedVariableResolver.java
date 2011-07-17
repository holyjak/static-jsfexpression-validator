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
import java.util.logging.Logger;

import javax.faces.el.EvaluationException;
import javax.servlet.jsp.PageContext;

import net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException;

/**
 * Resolve variables from a pre-defined list initialized via
 * {@link PredefinedVariableResolver#declareVariable(String, Object)}.
 * Throws {@link VariableNotFoundException} if it encounters a variable not present on the list.
 */
public final class PredefinedVariableResolver {

	/**
	 * Be notified when a new variable is encountered, namely when
	 * processing the first segment of a new EL.
	 */
    public static interface NewVariableEncounteredListener {

    	/**
    	 * Notification of the EL variable being validated.
    	 * @param variableName (required)
    	 */
        void handleNewVariableEncountered(String variableName);
    }

    static final String[] IMPLICIT_MAP_OBJECTS = new String[] {
    	"pageScope", "requestScope", "sessionScope", "applicationScope", "param"
        , "paramValues", "header", "headerValues", "cookie", "initParam" };

    private final Logger log = Logger.getLogger(getClass().getName());

    private boolean includeKnownVariablesInException = true;

    private final Map<String, Object> knownVariables = new HashMap<String, Object>();
    private final PredefinedVariableResolver.NewVariableEncounteredListener newVariableEncounteredListener;
    private ElVariableResolver unknownVariableResolver;

    /**
     * New resolver, notifying the given listener, if any.
     * @param newVariableEncounteredListener (optional)
     */
    public PredefinedVariableResolver(
            final PredefinedVariableResolver.NewVariableEncounteredListener newVariableEncounteredListener) {
        this.newVariableEncounteredListener = newVariableEncounteredListener;
        defineImplicitObjects();
    }

    /**
     * Define implicit objects required by the JSP 2.0 specification.
     */
    private void defineImplicitObjects() {
        for (String implicitObjectName : IMPLICIT_MAP_OBJECTS) {
            declareVariable(implicitObjectName, Collections.EMPTY_MAP);
        }

        declareVariable("pageContext", FakeValueFactory.fakeValueOfType(PageContext.class, "pageContext"));
    }

    /**
     * Resolve variable: check that it is valid and return its value based on the pre-set
     * information.
     * @param variableName (required)
     * @return an instance of the variable's type
     * @throws EvaluationException if something fails, e.g. no such variable is known
     *
     * @see #declareVariable(String, Object)
     */
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

    /**
     * Declare a new 'known' variable, supplying its name and value.
     * @param name (required) the name of the EL variable (likely a managed bean)
     * @param value (required) its value
     * @see JsfElValidator#declareVariable(String, Object)
     */
    public void declareVariable(final String name, final Object value) {

        if (value instanceof Class<?>) {
            log.warning("declareVariable('" + name + "', value:" + value + "): Are you sure you wanted to declare "
            		+ "a variable containing a Class and not an instance of the class (using the FakeValueFactory)?!");
        }

        Object currentOverride = knownVariables.get(name);

        if (currentOverride != null) {
            throw new IllegalArgumentException("The variable '"
                    + name + "' is already defined; current value: "
                    + currentOverride + ", new: " + value);
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