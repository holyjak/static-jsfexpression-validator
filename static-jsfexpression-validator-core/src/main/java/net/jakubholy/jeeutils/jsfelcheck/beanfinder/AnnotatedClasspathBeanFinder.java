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

package net.jakubholy.jeeutils.jsfelcheck.beanfinder;

import com.apple.laf.AquaButtonBorder;
import net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Locate managed beans on the classpath marked by particular annotations
 * (recognized by JSF) such as <code>@Named</code> or <code>@Stateless</code>.
 *
 * <h3>Usage example</h3>
 * <pre>{@code
 * import static net.jakubholy.jeeutils.jsfelcheck.beanfinder.AnnotatedClasspathBeanFinder.*;
 *
 * fromClassesInPackages("your.root.package.with.beans")
 *      .annotatedWith(Named.class, "value")
 *      .config()
 * }</pre>
 *
 * @see AnnotatedClasspathBeanFinder
 */
public class AnnotatedClasspathBeanFinder {

    private final Reflections reflections;
    private Map<String, Class<?>> collectedNamesAndTypes = new HashMap<String, Class<?>>();
	private final ManagedBeansAndVariablesConfiguration config;

	/**
	 * Search for annotated beans in the given packages and subpackages.
	 *
	 * @param packageNames (required) usually the root package(s) of your application - used to limit the scope of
	 *                     search to something manageable for performance reasons. Ex.: "net.jakubholy".
	 * @return this
	 */
    public static AnnotatedClasspathBeanFinder fromClassesInPackages(String... packageNames) {
        return new AnnotatedClasspathBeanFinder(packageNames);
    }

    private AnnotatedClasspathBeanFinder(String... packageNames) {
        this(null, packageNames);
    }

	/**
	 * For internal use, you should call
	 * {@link ManagedBeansAndVariablesConfiguration#fromClassesInPackages(String...)}.
	 */
	public AnnotatedClasspathBeanFinder(ManagedBeansAndVariablesConfiguration config, String... packageNames) {
		this.config = config;
		reflections = new Reflections(packageNames);
	}

	/* *
	 * Search for classes with the given annotation. If the annotation has the method '<code>value()</code>' and it returns a
	 * non-empty string then it is assumed to be the name of the bean otherwise the class name with the first letter
	 * lowercased is taken as the name.
	 * @param annotationType (required) the annotation, such as @Named or @Stateless
	 * @return this
	 * /
	 *
	 * DEPRECATED: It is all to easy to forget to supply the beanNameAttribute
    private AnnotatedClasspathBeanFinder annotatedWith(Class<? extends java.lang.annotation.Annotation> annotationType) {
        return annotatedWith(annotationType, "value");
    }*/

	/**
	 * Search for classes with the given annotation. If the annotation has the method '<code>&lt;beanNameAttribute&gt;()</code>'
	 * and it returns a non-empty string then it is assumed to be the name of the bean otherwise the class name with the first letter
	 * lower-cased is taken as the name.
	 * @param annotationType (required) the annotation, such as @Named or @Stateless
	 * @param beanNameAttribute (required) a name of a method of the annotation that provides the name of the bean if any was set explicitly;
	 *                          This method is often called <code>value</code>, sometimes <code>name</code>.
	 * @return this
	 */
    public AnnotatedClasspathBeanFinder annotatedWith(Class<? extends java.lang.annotation.Annotation> annotationType, String beanNameAttribute) {
        if (beanNameAttribute == null) {
	        throw new IllegalArgumentException("beanNameAttribute is required (try 'value' if unsure, check the JavaDoc of the annotation to be sure)");
        }
	    final Set<Class<?>> beanClasses = reflections.getTypesAnnotatedWith(annotationType);
        Map<String, Class<?>> beans = addNames(beanClasses, annotationType, beanNameAttribute);
        collectedNamesAndTypes.putAll(beans);
        return this;
    }

	/**
	 * Return the beans found.
	 * @return Map of bean names to bean classes; might be empty but never null
	 */
    public Map<String, Class<?>> detect() {
        return collectedNamesAndTypes;
    }

	/**
	 * Return the configuration object associated with this finder for configuring it further or
	 * passing it to the JSF analyzer. Usually the last method you call.
	 */
	public ManagedBeansAndVariablesConfiguration config() {
		config.setAnnotatedBeanFinder(this);
		return config;
	}

	/**
	 * Extract the name of each bean and return map bean name -&gt; bean class.
	 */
    protected static Map<String, Class<?>> addNames(Iterable<Class<?>> beanClasses, Class<? extends java.lang.annotation.Annotation> annotationType, String beanNameAttribute) {
        Map<String, Class<?>> namesAndTypes = new HashMap<String, Class<?>>();

        for(Class<?> beanClass: beanClasses) {
            final String beanName = extractBeanName(beanClass, annotationType, beanNameAttribute);
            namesAndTypes.put(beanName, beanClass);
        }

        return namesAndTypes;
    }

    protected static String extractBeanName(Class<?> target, Class<? extends java.lang.annotation.Annotation> annotationType, String nameField) {

        Annotation annotationInstance = target.getAnnotation(annotationType);

        String explicitName = null;
        if (nameField != null) {
	        try {
		        explicitName = (String) annotationType.getMethod(nameField).invoke(annotationInstance);
	        } catch (Exception e) {
		        // We expect all annotations to have 'value()' by default but we might be wrong so don't throw an
		        // exception in such a case, only fail for an explicitly provided name field method
		        if (!nameField.equals("value")) {
		            throw new IllegalStateException("The annotation " + annotationInstance + " of type "
		            + annotationType + " doesn't have the expected name-providing method called '"
		            + nameField + "'; target class: " + target, e);
		        }
	        }
        }

        if (explicitName != null && explicitName.length() > 0) return explicitName;

        String targetName = target.getSimpleName();
        String defaultName = targetName.substring(0, 1).toLowerCase() + targetName.substring(1);
        return defaultName;
    }
}
