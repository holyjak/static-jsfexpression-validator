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

/**
 * Return the Class for the given (likely context-local) JSF EL variable or
 * null if it cannot be resolved (is unknown).
 */
public interface ElVariableResolver {

    /**
     * Return the Class for the given (likely context-local) JSF EL variable.
     * @param name (required) the name of the EL variable, i.e. the first segment of an EL such
     * as 'bean' in 'bean.property1.prop2'
     * @return the type of the EL variable or null if it cannot be resolved (is unknown).
     */
    Class<?> resolveVariable(String name);

}
