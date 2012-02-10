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

package net.jakubholy.jeeutils.jsfelcheck.beanfinder

import static net.jakubholy.jeeutils.jsfelcheck.beanfinder.AnnotatedClasspathBeanFinder.*

import org.junit.Before
import org.junit.Test
import javax.faces.bean.ManagedBean
import java.lang.annotation.Annotation
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.annotated.ValuedBeanAnnotation
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.annotated.ValuedWithDefaultName
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.annotated.ManagedBeanWithExplicitName
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.annotated.ManagedBeanWithDefaultName
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.annotated.ValuedWithExplicitName


class AnnotatedClasspathBeanFinderTest {


    private Map<String, Class<?>> beanClasses;
    private AnnotatedClasspathBeanFinder beanDetector;

    @Before
    public void setUp() {
        beanClasses = AnnotatedClasspathBeanFinder.
                fromClassesInPackages("net.jakubholy.jeeutils.jsfelcheck.beanfinder.annotated").
                annotatedWith(ValuedBeanAnnotation, "value").
                annotatedWith(ManagedBean, "name")
                .detect();
    }

    @Test
    public void "should find all annotated classes with proper names"() throws Exception {
        assert beanClasses.values() as Set ==
                [ManagedBeanWithDefaultName, ManagedBeanWithExplicitName, ValuedWithDefaultName, ValuedWithExplicitName] as Set
        assert beanClasses.keySet() ==
                ["managedBeanWithDefaultName"
                        , "custom_ManagedBeanWithExplicitName_name"
                        , "valuedWithDefaultName"
                        , "custom_ValuedWithExplicitName_name"] as Set
        assert beanClasses == [
            "managedBeanWithDefaultName":   ManagedBeanWithDefaultName
            , "custom_ManagedBeanWithExplicitName_name":    ManagedBeanWithExplicitName
            , "valuedWithDefaultName": ValuedWithDefaultName
            , "custom_ValuedWithExplicitName_name": ValuedWithExplicitName
        ]
    }

    @Test
    public void "should derive default bean name if no explicit one"() throws Exception {
        assert "valuedWithDefaultName" == extractBeanName(ValuedWithDefaultName, ValuedBeanAnnotation, null)
    }

    @Test
    public void "should extract bean name from the default 'value' annotation field"() throws Exception {
        assert "custom_ValuedWithExplicitName_name" == extractBeanName(ValuedWithExplicitName, ValuedBeanAnnotation, "value")
    }

    @Test
    public void "should extract bean name from a custom annotation field"() throws Exception {
        assert "custom_ManagedBeanWithExplicitName_name" == extractBeanName(ManagedBeanWithExplicitName, ManagedBean, "name")
    }

    @Test
    public void "should add bean name to its class"() throws Exception {
        assert addNames([ManagedBeanWithExplicitName], ManagedBean, "name") == ["custom_ManagedBeanWithExplicitName_name": ManagedBeanWithExplicitName]
        assert addNames([ValuedWithExplicitName], ValuedBeanAnnotation, "value") == ["custom_ValuedWithExplicitName_name": ValuedWithExplicitName]
    }
}
