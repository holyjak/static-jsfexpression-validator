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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.impl;

import javax.faces.el.MethodBinding;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;

import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.ElBindingFactory;

import com.sun.faces.el.MethodBindingFactory;
import com.sun.faces.el.ValueBindingFactory;

/**
 * Implementation using a legacy Sun-based jsf-impl 1.1.
 */
public class Sun11_legacyElBindingFactoryImpl implements ElBindingFactory { // SUPPRESS CHECKSTYLE

    @SuppressWarnings("rawtypes")
    private static final Class[] NO_PARAMS = new Class[0];

    private final ValueBindingFactory valueBindingFactory = new ValueBindingFactory();
    private final MethodBindingFactory methodBindingFactory = new MethodBindingFactory();

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.ElBindingFactory#createValueBinding(java.lang.String)
     */
    @Override
    public ValueBinding createValueBinding(String ref)
            throws ReferenceSyntaxException {
        return valueBindingFactory.createValueBinding(ref);
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.ElBindingFactory#createMethodBinding(java.lang.String)
     */
    @Override
    public MethodBinding createMethodBinding(String ref) {
        if (ref == null) {
            throw new NullPointerException("The argument ref: String may not be null");
        }

        return methodBindingFactory.createMethodBinding(ref, NO_PARAMS);
    }

}
