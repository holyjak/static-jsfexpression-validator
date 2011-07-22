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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import net.jakubholy.jeeutils.jsfelcheck.validator.PredefinedVariableResolver;

/**
 * Adapt {@link PredefinedVariableResolver} to behave as JSF 1.1 {@link VariableResolver}.
 */
public final class Jsf11VariableResolverAdapter extends VariableResolver {

    private final PredefinedVariableResolver resolver;

    /**
     * Adapter delegating to the given resolver.
     * @param resolver (required)
     */
    public Jsf11VariableResolverAdapter(PredefinedVariableResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("The resolver: PredefinedVariableResolver may not be null");
        }
        this.resolver = resolver;
    }

    @Override
    public Object resolveVariable(FacesContext fc, String variableName)
            throws EvaluationException {
        return resolver.resolveVariable(variableName);
    }

}