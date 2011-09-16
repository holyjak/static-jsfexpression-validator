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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;

import org.apache.myfaces.config.FacesConfigUnmarshaller;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.ManagedBean;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

/**
 * Find managed bean defined in (a) faces-config file(s).
 * Uses the available JSF implementation to parse the file(s).
 */
public class Jsf12FacesConfigXmlBeanFinder implements ManagedBeanFinder {

    private final ExternalContext externalContext = Mockito.mock(ExternalContext.class);

    private Collection<File> facesConfigFiles;

    /**
     * Finder reading from the given files.
     * @param facesConfigFiles (required) may be empty
     */
    public Jsf12FacesConfigXmlBeanFinder(final Collection<File> facesConfigFiles) {
        setFacesConfigFiles(facesConfigFiles);
    }


    Jsf12FacesConfigXmlBeanFinder() { /* for testing only */ }

    private void setFacesConfigFiles(final Collection<File> facesConfigFiles) {
        // TODO remove this duplication with Jsf11FacesConfigXmlBeanFinder
        if (facesConfigFiles == null || facesConfigFiles.isEmpty()) {
            throw new IllegalArgumentException("facesConfigFiles: Collection<File> cannot be null/empty, is: "
                    + facesConfigFiles);
        }

        for (File file : facesConfigFiles) {
            if (!file.canRead()) {
                throw new IllegalArgumentException("The supplied faces-config XML file "
                        + "cannot be opened for reading: " + file);
            }
        }

        this.facesConfigFiles = new LinkedList<File>(facesConfigFiles);
    }

    /* TODO Test with:
     * - file w/ no beans
     * - file w/ invalid xml
     * - URL to non-exist. file, URL into a JAR, ...
     * - Invalid URL
     *
     * TODO Handle faces-config defined vars as resource-bundle (any more?)
     */
    @Override
    public Collection<ManagedBeanDescriptor> findDefinedBackingBeans() {
        // TODO remove this duplication with Jsf11FacesConfigXmlBeanFinder
        final Collection<ManagedBeanDescriptor> allBeans =
            new LinkedList<ManagedBeanDescriptor>();

        for (File facesConfigXml : facesConfigFiles) {
            try {
                allBeans.addAll(
                        parseFacesConfig(facesConfigXml.toURI().toURL()));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Failed to convert the file "
                		+ facesConfigXml + " to URL", e);
            }
        }

        return allBeans;
    }

    private Collection<ManagedBeanDescriptor> parseFacesConfig(URL configFileUrl) {
        try {
            InputStream stream = configFileUrl.openStream();
            if (stream == null) {
                throw new FacesException(
                        "Failed to open stream on the faces config file " + configFileUrl);
            }
            FacesConfig facesConfig = getUnmarshaller().getFacesConfig(stream, configFileUrl.toString());
            stream.close();

            return toManagedBeanDescriptors(facesConfig.getManagedBeans());
        } catch (IOException e) {
            throw new RuntimeException("Failed to access the faces-config.xml", e);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to parse the faces-config.xml", e);
        }
    }

    private Collection<ManagedBeanDescriptor> toManagedBeanDescriptors(
            Collection<ManagedBean> managedBeans) {
        Collection<ManagedBeanDescriptor> descriptors = new LinkedList<ManagedBeanFinder.ManagedBeanDescriptor>();

        for (ManagedBean managedBean : managedBeans) {
            descriptors.add(new ManagedBeanDescriptor(
                    managedBean.getManagedBeanName()
                    , managedBean.getManagedBeanClass()));
        }

        return descriptors;
    }

    private FacesConfigUnmarshaller<FacesConfig> getUnmarshaller() {
        return  new DigesterFacesConfigUnmarshallerImpl(externalContext); // req. for Resolver config.
    }

}
