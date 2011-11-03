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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding;

import javax.faces.el.MethodBinding;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;

/**
 * Create JSF EL bindings using a suitable underlying implementation.
 */
public interface ElBindingFactory {

    /**
     * Create a value binding.
     * @param ref (required) ex.: #{bean.property}
     * @return the binding, never null
     * @throws ReferenceSyntaxException if the expression syntax is incorrect (not {..})
     */
    ValueBinding createValueBinding(String ref) throws ReferenceSyntaxException;

    /**
     * Create a method binding.
     * <p>
     *     BEWARE: The binding won't trully correspond to the correct method because
     *     we don't pass in the correct number and types of arguments.
     *     (We don't know what the arguments are - only the tag handler that
     *     uses the method binding attribute knows that.)
     * </p>
     *
     * @param ref (required) ex.: #{bean.actionMethod}
     * @return the binding, never null
     */
    MethodBinding createMethodBinding(String ref);

}