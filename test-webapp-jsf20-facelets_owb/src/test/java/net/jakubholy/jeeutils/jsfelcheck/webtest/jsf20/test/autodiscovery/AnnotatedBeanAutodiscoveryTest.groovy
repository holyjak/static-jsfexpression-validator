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

package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.autodiscovery

import org.junit.Test
import org.junit.Before
import org.reflections.Reflections
import javax.inject.Named
import javax.enterprise.inject.Default
import com.sun.tools.javac.util.Name
import java.lang.annotation.Annotation
import javax.faces.bean.ManagedBean
import java.sql.Ref

@Named
private class NamedWithDefaultName {}

@ManagedBean(name="custom_ManagedBeanWithName_Name")
private class ManagedBeanWithName {}

private class AnnotatedBeansDetector {

    private final Reflections reflections;
    Map<String, Class<?>> collectedNamesAndTypes = new HashMap<String, Class<?>>();

    public static AnnotatedBeansDetector fromClassesInPackages(String... packageNames) {
        return new AnnotatedBeansDetector(packageNames);
    }

    private AnnotatedBeansDetector(String... packageNames) {
        reflections = new Reflections(packageNames);
    }

    public Reflections getReflections() {
        return reflections;
    }

    public AnnotatedBeansDetector annotatedWith(Class<? extends java.lang.annotation.Annotation> annotationType) {
        return annotatedWith(annotationType, "value");
    }

    public AnnotatedBeansDetector annotatedWith(Class<? extends java.lang.annotation.Annotation> annotationType, String beanNameAttribute) {
        final Set<Class<?>> beanClasses = reflections.getTypesAnnotatedWith(Named.class);
        Map<String, Class<?>> beans = addNames(beanClasses, annotationType, beanNameAttribute);
        collectedNamesAndTypes.putAll(beans);
        return this;
    }

    public Map<String, Class<?>> detect() {
        return collectedNamesAndTypes;
    }

    private static Map<String, Class<?>> addNames(Set<Class<?>> beanClasses, Class<? extends java.lang.annotation.Annotation> annotationType, String beanNameAttribute) {
        Map<String, Class<?>> namesAndTypes = new HashMap<String, Class<?>>();

        for(beanClass in beanClasses) {
            final String beanName = extractBeanName(beanClass, annotationType, beanNameAttribute);
            namesAndTypes.put(beanName, beanClass);
        }

        return namesAndTypes;
    }


    protected static String extractBeanName(Class<?> target, Class<? extends java.lang.annotation.Annotation> annotationType, String nameField) {

        Annotation annotationInstance = target.getAnnotation(annotationType);

        String explicitName = null;
        if ("value".equals(nameField)) {
            // Direct access is quicker than reflection and the usage of value is frequent
            explicitName = annotationInstance.value();
        } else if (nameField != null) {
            explicitName = (String) annotationType.getMethod(nameField).invoke(annotationInstance);
        }

        if (explicitName != null && explicitName.length() > 0) return explicitName;

        String targetName = target.getSimpleName();
        String defaultName = targetName.substring(0, 1).toLowerCase() + targetName.substring(1);
        return defaultName;
    }
}

public class AnnotatedBeanAutodiscoveryTest {

	private Map<String, Class<?>> beanClasses;
    private AnnotatedBeansDetector beanDetector;

	@Before
	public void setUp() {
		// See http://code.google.com/p/reflections/
		//Reflections reflections = new Reflections("net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.annotated");
		//beanClasses = reflections.getTypesAnnotatedWith(Named.class);
        //beanDetector =
        beanClasses = AnnotatedBeansDetector.fromClassesInPackages("net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.annotated").
                annotatedWith(Named).detect();
	}

	@Test
	public void "should find all annotated classes"() throws Exception {
	    assert beanClasses.values() as Set == [JavaDiNamedBean, NamedWithDefaultName] as Set
	}

    @Test
    public void "should extract annotated bean name"() throws Exception {
        assert beanClasses.values().collectEntries { [extractBeanName(it, Named, "value"), it] }.any { k, v -> k == "javaDiNamedBean" && v == JavaDiNamedBean}
    }

    @Test
    public void "should derive default bean name if no explicit one"() throws Exception {
        assert "namedWithDefaultName" == extractBeanName(NamedWithDefaultName, Named, null)
    }

    @Test
    public void "should extract bean name from the given annotation field"() throws Exception {
        assert "custom_ManagedBeanWithName_Name" == extractBeanName(ManagedBeanWithName, ManagedBean, "name")
    }

    public static String extractBeanName(Class<?> target, Class<? extends java.lang.annotation.Annotation> annotationType, String nameField) {

        Annotation annotationInstance = target.getAnnotation(annotationType);

        String explicitName = null;
        if ("value".equals(nameField)) {
            // Direct access is quicker than reflection and the usage of value is frequent
            explicitName = annotationInstance.value();
        } else if (nameField != null) {
            explicitName = (String) annotationType.getMethod(nameField).invoke(annotationInstance);
        }

        if (explicitName != null && explicitName.length() > 0) return explicitName;

        String targetName = target.getSimpleName();
        String defaultName = targetName.substring(0, 1).toLowerCase() + targetName.substring(1);
        return defaultName;
    }
}
