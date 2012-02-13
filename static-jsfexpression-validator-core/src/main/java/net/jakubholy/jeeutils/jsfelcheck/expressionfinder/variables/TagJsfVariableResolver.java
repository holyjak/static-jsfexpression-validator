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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables;

import java.util.Map;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.AttributesValidationResult;

/**
 * Extract local JSF EL variables defined in a tag such as h:dataTable
 * so that they are available when checking expressions in the nested tags.
 */
public interface TagJsfVariableResolver {

    /**
     * Extract variables from the tag's attributes.
     * @param tagAttributes (required) the attributes of the tag in the form of name to value map
     * @param resolvedJsfExpressions (required) if any of the tag attributes contain JSF EL then the results
     *                               of validating the EL are here (in the case of h:dataTable the source
     *                               <code>value</code> attribute could be something like <code>#{bean.array}</code>
     *                               in which case you need the value to learn the component type)
     * @return info about local variable extracted from the tag (only 1 supported) or null, if no l.v. defined there
     * @throws MissingLocalVariableTypeDeclarationException the tag defines a local variable but its type has not
     * been declared in advance (notice it cannot be derived automatically e.g. if it's an element of a Collection)
     */
    VariableInfo extractContextVariables(Map<String, String> tagAttributes,
            AttributesValidationResult resolvedJsfExpressions) throws MissingLocalVariableTypeDeclarationException;
}
