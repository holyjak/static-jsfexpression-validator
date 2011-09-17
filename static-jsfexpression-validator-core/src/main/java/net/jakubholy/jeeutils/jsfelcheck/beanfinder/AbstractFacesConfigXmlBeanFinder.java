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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractFacesConfigXmlBeanFinder implements ManagedBeanFinder {

    private Collection<InputStream> facesConfigStreams = new LinkedList<InputStream>();

    @Override
    public final Collection<ManagedBeanDescriptor> findDefinedBackingBeans() {
        final Collection<ManagedBeanDescriptor> allBeans =
            new LinkedList<ManagedBeanDescriptor>();

        for (InputStream facesConfigStream : getFacesConfigStreams()) {
            allBeans.addAll(
                parseFacesConfig(facesConfigStream));
        }

        return allBeans;
    }

    protected abstract Collection<ManagedBeanDescriptor> parseFacesConfig(InputStream facesConfigStream);

    protected final Collection<InputStream> getFacesConfigStreams() {
        return facesConfigStreams;
    }


    protected final ManagedBeanFinder setFacesConfigFiles(final Collection<File> facesConfigFiles) {
        if (facesConfigFiles == null || facesConfigFiles.isEmpty()) {
            throw new IllegalArgumentException("facesConfigStreams: Collection<File> cannot be null/empty, is: "
                    + facesConfigFiles);
        }

        for (File facesConfigXml : facesConfigFiles) {
            if (!facesConfigXml.canRead()) {
                throw new IllegalArgumentException("The supplied faces-config XML file "
                        + "cannot be opened for reading: " + facesConfigXml);
            }

            try {
                getFacesConfigStreams().add(new NamedInputStream(facesConfigXml));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("Failed to create an input stream for the file "
                		+ facesConfigXml, e);
            }

        }

        return this;
    }

    /**
     * Set input streams to the faces-config XML files to read managed beans from.
     * @param facesConfigStreams (required)
     */
    protected final ManagedBeanFinder setFacesConfigStreams(final Collection<InputStream> facesConfigStreams) {
        if (facesConfigStreams == null) {
            throw new IllegalArgumentException("The facesConfigStreams: Collection<InputStream> can't be null");
        }
        getFacesConfigStreams().addAll(facesConfigStreams);
        return this;
    }
}
