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

import java.io.File;
import java.util.Collection;

import net.jakubholy.jeeutils.jsfelcheck.AbstractJsfStaticAnalyzer;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.jsf11.Jsf11FacesConfigXmlBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;

/**
 * Implementation based on JSF 1.1.
 */
public class Jsf11StaticAnalyzer extends AbstractJsfStaticAnalyzer {

    public static void main(String[] args) throws Exception {
        AbstractJsfStaticAnalyzer.main(new Jsf11StaticAnalyzer(), args);
    }

    @Override
    protected ValidatingElResolver createValidatingElResolver() {
        return new Jsf11ValidatingElResolver();
    }

    @Override
    protected ManagedBeanFinder createManagedBeanFinder(
            Collection<File> facesConfigFiles) {
        return new Jsf11FacesConfigXmlBeanFinder(facesConfigFiles);
    }

}
