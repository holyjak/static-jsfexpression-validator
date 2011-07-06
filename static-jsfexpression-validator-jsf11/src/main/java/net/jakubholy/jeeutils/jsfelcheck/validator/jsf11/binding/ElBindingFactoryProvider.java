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

import java.util.logging.Logger;

import javax.faces.application.Application;

import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.impl.Sun11_02ElBindingFactoryImpl;


public class ElBindingFactoryProvider {

    static final String LEGACY_IMPL_CLASS = "net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.impl.Sun11_legacyElBindingFactoryImpl";
    private static final Logger LOG = Logger.getLogger(ElBindingFactoryProvider.class.getName());

    public static ElBindingFactory getFactory(Application application) {

        try {
            Class.forName("com.sun.faces.el.ValueBindingFactory");
            LOG.info("Instantiating JSF EL Binding factory for the legacy jsf-impl 1.1 ...");
            // Note: This class is pre-compiled in src/main/resources/net/jakubholy/jeeutils/jsfelcheck/validator/binding/impl/Sun11_legacyElBindingFactoryImpl.class
            // for we wouldn't be able to compile it w/o the jsf impl. it uses but it isn't anywhere in Maven
            return instantiate(LEGACY_IMPL_CLASS, "legacy Sun-based v1.1");
        } catch (ClassNotFoundException e) {}

        try {
            Class.forName("com.sun.faces.el.MixedELValueBinding");
            LOG.info("Instantiating JSF EL Binding factory for the published jsf-impl 1.1_02 ...");
            return new Sun11_02ElBindingFactoryImpl(application);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No supported implementation of JSF found (jsf-impl 1.1 (legacy) and 1.1_02");
        }
    }

    private static ElBindingFactory instantiate(String implType, String jsfImlpVersion)  {
        Exception failure = null;
        try {
            @SuppressWarnings("unchecked")
            Class<ElBindingFactory> impl = (Class<ElBindingFactory>) Class.forName(implType);
            return impl.newInstance();
        } catch (ClassNotFoundException e) {
            failure = e;
        } catch (InstantiationException e) {
            failure = e;
        } catch (IllegalAccessException e) {
            failure = e;
        }

        throw new IllegalStateException("Failed to load adapter class '"
                + implType + "' for the detected jsf-impl version " + jsfImlpVersion
                , failure);
    }

}
