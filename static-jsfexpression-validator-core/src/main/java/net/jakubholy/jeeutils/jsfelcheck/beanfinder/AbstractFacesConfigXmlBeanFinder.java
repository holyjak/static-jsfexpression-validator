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

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;

/**
 * Parent class of the Faces bean finder implementations specific
 * to individual JSF versions, provides the common functionality.
 */
public abstract class AbstractFacesConfigXmlBeanFinder implements ManagedBeanFinder {

    private Collection<InputResource> facesConfigStreams = new LinkedList<InputResource>();

    @Override
    public final Collection<ManagedBeanDescriptor> findDefinedBackingBeans() {
        final Collection<ManagedBeanDescriptor> allBeans =
            new LinkedList<ManagedBeanDescriptor>();

        for (InputResource facesConfigResource : getFacesConfigResources()) {
            allBeans.addAll(
                parseFacesConfig(facesConfigResource.getStream()));
        }

        return allBeans;
    }

    protected abstract Collection<ManagedBeanDescriptor> parseFacesConfig(InputStream facesConfigStream);

    protected final Collection<InputResource> getFacesConfigResources() {
        return facesConfigStreams;
    }

    /**
     * Set input streams to the faces-config XML files to read managed beans from.
     * @param facesConfigResources (required)
     */
    protected final ManagedBeanFinder setFacesConfigResources(final Collection<InputResource> facesConfigResources) {
	    assertNotNull(facesConfigResources, "facesConfigResources", Collection.class);
        getFacesConfigResources().addAll(facesConfigResources);
        return this;
    }
}
