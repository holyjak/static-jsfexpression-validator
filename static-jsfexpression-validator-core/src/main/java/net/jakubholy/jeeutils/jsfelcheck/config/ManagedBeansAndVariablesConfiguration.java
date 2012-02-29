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

package net.jakubholy.jeeutils.jsfelcheck.config;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.AnnotatedClasspathBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.InputResource;
import net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import static net.jakubholy.jeeutils.jsfelcheck.beanfinder.FileUtils.filesToResourcesNullSafe;
import static net.jakubholy.jeeutils.jsfelcheck.beanfinder.FileUtils.streamsToResourcesNullSafe;
import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;

/**
 * Configuration of where should be definitions of known managed beans loaded from
 * and optionally of global (as opposed to tag-local) variables that the validator cannot detect
 * itself.
 * (Basically a managed bean is also just a variable.)
 * <p>
 * You will typically want to static import the static methods to make configurations more readable.
 * </p>
 * <p/>
 * <h3>Usage example</h3>
 * <pre>{@code
 * import static net.jakubholy.jeeutils.jsfelcheck.config.ManagedBeansAndVariablesConfiguration.*;
 * 
 * fromFacesConfigFiles(new File("/path/to/faces-config.xml"))
 *      .andFromSpringConfigFiles(new File("/path/to/applicationContext.xml"))
 *      .andFromClassesInPackages("your.root.package.with.beans").annotatedWith(Named.class, "value").config()
 *      .withExtraVariable("name", MyType.class)
 *      .withExtraVariable("another", String.class)
 * }</pre>
 *
 * @see AnnotatedClasspathBeanFinder
 */
public class ManagedBeansAndVariablesConfiguration {

	private AnnotatedClasspathBeanFinder annotatedBeanFinder = null;

	private enum Type {SPRING, FACES}

    private Collection<InputStream> facesConfigStreams = Collections.emptyList();
    private Collection<InputStream> springConfigStreams = Collections.emptyList();

	private final Map<Type, Collection<InputResource>> resourcesMap;

	{
		resourcesMap = new Hashtable<Type, Collection<InputResource>>();
		Collection<InputResource> empty = Collections.emptyList();
		for (Type resourceType : Type.values()) {
			resourcesMap.put(resourceType, empty);
		}
	}

    private final Map<String, Object> extraVariables = new Hashtable<String, Object>();

	/** For internal use only; call some of the static methods instead. */
	public ManagedBeansAndVariablesConfiguration() {}

    /**
     * New configuration set to read managed beans from the given faces-config.xml files. (Default: none.)
     * Set to empty or null not to process any.
     * @param facesConfigFiles (optional) faces-config files to read managed beans from; may be empty or null
     */
    public static ManagedBeansAndVariablesConfiguration fromFacesConfigFiles(File... facesConfigFiles) {
        return new ManagedBeansAndVariablesConfiguration().andFromFacesConfigFiles(toListNullSafe(facesConfigFiles));
    }

    /**
     * New configuration set to read managed beans from the given faces-config.xml files. (Default: none.)
     * Set to empty or null not to process any.
     * @param facesConfigFiles (optional) faces-config files to read managed beans from; may be empty or null
     */
    public static ManagedBeansAndVariablesConfiguration fromFacesConfigFiles(Collection<File> facesConfigFiles) {
        return new ManagedBeansAndVariablesConfiguration().andFromFacesConfigFiles(facesConfigFiles);
    }

    /**
     * New configuration set to read managed beans from the given faces-config.xml files. (Default: none.)
     * Set to empty or null not to process any.
     * @param facesConfigStreams (optional) faces-config file streams to read managed beans from; may be empty or null
     */
    public static ManagedBeansAndVariablesConfiguration fromFacesConfigStreams(Collection<InputStream> facesConfigStreams) {
        return new ManagedBeansAndVariablesConfiguration().andFromFacesConfigStreams(facesConfigStreams);
    }

    /**
     * New configuration set to read managed beans from the given Spring application context XML files.
     * (Default: none.)
     * Set to empty or null not to process any.
     * @param springConfigFiles (optional) Spring applicationContext files to read managed beans from; may be empty or null
     */
    public static ManagedBeansAndVariablesConfiguration fromSpringConfigFiles(File... springConfigFiles) {
        return new ManagedBeansAndVariablesConfiguration().andFromSpringConfigFiles(toListNullSafe(springConfigFiles));
    }

    /**
     * New configuration set to read managed beans from the given Spring application context XML files.
     * (Default: none.)
     * Set to empty or null not to process any.
     * @param springConfigFiles (optional) Spring applicationContext files to read managed beans from; may be empty or null
     */
    public static ManagedBeansAndVariablesConfiguration fromSpringConfigFiles(Collection<File> springConfigFiles) {
        return new ManagedBeansAndVariablesConfiguration().andFromSpringConfigFiles(springConfigFiles);
    }

    /**
     * New configuration set to read managed beans from the given Spring application context XML files.
     * (Default: none.)
     * Set to empty or null not to process any.
     * @param springConfigStreams (optional) Spring applicationContext file streams to read managed beans from; may be empty or null
     */
    public static ManagedBeansAndVariablesConfiguration fromSpringConfigStreams(Collection<InputStream> springConfigStreams) {
        return new ManagedBeansAndVariablesConfiguration().andFromSpringConfigStreams(springConfigStreams);
    }

    /**
     * Create new, empty configuration - used if you only want to define extra variables.
     * @see #withExtraVariable(String, Class)
     */
    public static ManagedBeansAndVariablesConfiguration forExtraVariables() {
        return new ManagedBeansAndVariablesConfiguration();
    }

    // #########################################################################################################

    // -------------------------------------------------------------------------------------------------- Faces

    /**
     * Non-static version of {@link #fromFacesConfigFiles(java.util.Collection)}.
     */
    public ManagedBeansAndVariablesConfiguration andFromFacesConfigFiles(Collection<File> facesConfigFiles) {
        return withResourcesFor(Type.FACES, filesToResourcesNullSafe(facesConfigFiles));
    }
    /**
     * Non-static version of {@link #fromFacesConfigFiles(java.io.File...)}.
     */
    public ManagedBeansAndVariablesConfiguration andFromFacesConfigFiles(File... facesConfigFiles) {
        return andFromFacesConfigFiles(toListNullSafe(facesConfigFiles));
    }

    /**
     * Non-static version of {@link #fromFacesConfigStreams(java.util.Collection)}}.
     */
    public ManagedBeansAndVariablesConfiguration andFromFacesConfigStreams(Collection<InputStream> facesConfigStreams) {
        return withResourcesFor(Type.FACES, streamsToResourcesNullSafe(facesConfigStreams));
    }

    /** internal use only */
    public Collection<InputResource> getFacesConfigStreams() {
        return Collections.unmodifiableCollection(resourcesMap.get(Type.FACES));
    }

    // -------------------------------------------------------------------------------------------------- Spring

    /**
     * Non-static version of {@link #fromSpringConfigFiles(java.util.Collection)}.
     */
    public ManagedBeansAndVariablesConfiguration andFromSpringConfigFiles(File... springConfigFiles) {
        return andFromSpringConfigFiles(toListNullSafe(springConfigFiles));
    }

    /**
     * Non-static version of {@link #fromSpringConfigFiles(java.io.File...)}.
     */
    public ManagedBeansAndVariablesConfiguration andFromSpringConfigFiles(Collection<File> springConfigFiles) {
        return withResourcesFor(Type.SPRING, filesToResourcesNullSafe(springConfigFiles));
    }

    /**
     * Non-static version of {@link #fromSpringConfigStreams(java.util.Collection)}.
     */
    public ManagedBeansAndVariablesConfiguration andFromSpringConfigStreams(Collection<InputStream> springConfigStreams) {
        return withResourcesFor(Type.SPRING, streamsToResourcesNullSafe(springConfigStreams));
    }

    /**
     * internal use only
     */
    public Collection<InputResource> getSpringConfigStreams() {
        return Collections.unmodifiableCollection(resourcesMap.get(Type.SPRING));
    }

	// ------------------------------------------------------------------------------------------------- Annotated

	/**
	 * Search for annotated beans in the given packages and subpackages.
	 * Used to automatically detect managed beans that are not in faces-config.xml but are instead
	 * marked with an annotation such as {@link javax.faces.bean.ManagedBean}.
	 *
	 * <h3>Example</h3>
	 * <code><pre>{@code
	 *  ManagedBeansAndVariablesConfiguration
	 *      .fromClassesInPackages("your.package.with.annotated.managed.beans")
	 *      .annotatedWith(ManagedBean.class, "value")    // the annotation and method holding optionally the bean name
	 *      .config()
	 * }</pre></code>
	 *
	 * @param packageNames (required) usually the root package(s) of your application - used to limit the scope of
	 *                     search to something manageable for performance reasons. Ex.: "net.jakubholy".
	 * @return this
	 */
	public static AnnotatedClasspathBeanFinder fromClassesInPackages(String... packageNames) {
		return new ManagedBeansAndVariablesConfiguration().andFromClassesInPackages(packageNames);
	}

	/**
	 * Non-static version of {@link #fromClassesInPackages(String...)}.
	 */
	public AnnotatedClasspathBeanFinder andFromClassesInPackages(String... packageNames) {
		return new AnnotatedClasspathBeanFinder(this, packageNames);
	}


    // ------------------------------------------------------------------------------------------------- Extra variables


    /**
     * Register a EL variable and its value so that when it is encountered in an EL expression, it will be possible to
     * resolve it.
     * Normally the {@link net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException}
     * is thrown when an undeclared/unknown variable in encountered.
     * <p>You most likely actually want to use {@link #withExtraVariable(String, Class)} as passing an actual
     * value has rarely any benefits.
     * </p>
     * You use this typically to declare managed beans and their value.
     * The purpose of this method is to make it possible to declare variables of types that whose value we
     * currently cannot fake.
     *
     * @param name  (required) the name of the EL variable (i.e. the first identifier in any EL expression:
     *              var.prop1.prop2)
     * @param value (required) the value to be returned for the variable, used in further evaluation. WARNING: It should
     *              be an actual instance, not a Class!
     * @return this
     * @see #withExtraVariable(String, Class)
     */
    public ManagedBeansAndVariablesConfiguration withExtraVariable(final String name, final Object value) {
        assertNotNull(name, "name", String.class);
        assertNotNull(value, "value", Object.class);
        extraVariables.put(name, value);
        return this;
    }

    /**
     * Register a EL variable and its value so that when it is encountered in an EL expression, it will be possible to
     * resolve it.
     * Normally the {@link net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException}
     * is thrown when an undeclared/unknown variable in encountered.
     * You use this typically to declare managed beans and their class.
     * <p>
     * For the puropose of validation a fake value of the type is created using
     * {@link FakeValueFactory#fakeValueOfType(Class, Object)}.
     * </p>
     *
     * @param name      (required) the name of the EL variable (i.e. the first identifier in any EL expression:
     *                  var.prop1.prop2)
     * @param valueType (required) the value to be returned for the variable, used in further evaluation.
     * @return this
     * @see #withExtraVariable(String, Object)
     */
    public ManagedBeansAndVariablesConfiguration withExtraVariable(final String name, final Class valueType) {
        assertNotNull(name, "name", String.class);
        assertNotNull(valueType, "value", Object.class);
        Object fakeValue = FakeValueFactory.fakeValueOfType(valueType, name);
        extraVariables.put(name, fakeValue);
        return this;
    }

    /**
     * internal use only
     */
    public Map<String, Object> getExtraVariables() {
        return Collections.unmodifiableMap(extraVariables);
    }

	/**
	 * internal use only
	 */
	public Map<String, Object> getAnnotatedBeansFound() {
		// TODO Convert classes to instances
		Map<String, Class<?>> annotatedBeans = Collections.emptyMap();
		if (annotatedBeanFinder != null) {
			annotatedBeans = annotatedBeanFinder.detect();
		}
		return fakeValues(annotatedBeans);
	}

	private Map<String, Object> fakeValues(Map<String, Class<?>> namedBeanTypes) {
		Map<String, Object> namedBeanFakes = new Hashtable<String, Object>();
		for (Map.Entry<String, Class<?>> namedBean : namedBeanTypes.entrySet()) {
			Object fakeValue = FakeValueFactory.fakeValueOfType(namedBean.getValue(), namedBean.getKey());
			namedBeanFakes.put(namedBean.getKey(), fakeValue);
		}
		return namedBeanFakes;
	}


	// ------------------------------------------------------------------------------------------------- Private


	private ManagedBeansAndVariablesConfiguration withResourcesFor(Type type, Collection<InputResource> resources) {
		Collection<InputResource> nullSafeResources;
		if (resources == null || resources.isEmpty()) {
			nullSafeResources = Collections.emptyList();
		} else {
			nullSafeResources = resources;
		}

		this.resourcesMap.put(type, nullSafeResources);

		return this;
	}

    private static Collection<File> toListNullSafe(File[] springConfigFiles) {
        return (springConfigFiles == null)? null : Arrays.asList(springConfigFiles);
    }

	/**
	 * ***internal use only***
	 * Callback for {@link AnnotatedClasspathBeanFinder}
	 */
	public void setAnnotatedBeanFinder(AnnotatedClasspathBeanFinder annotatedBeanFinder) {
		this.annotatedBeanFinder = annotatedBeanFinder;
	}
}
