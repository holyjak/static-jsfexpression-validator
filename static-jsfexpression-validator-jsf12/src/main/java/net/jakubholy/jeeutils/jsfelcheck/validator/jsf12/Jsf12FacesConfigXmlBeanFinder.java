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

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;

import javax.faces.context.ExternalContext;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;

import org.apache.myfaces.config.FacesConfigUnmarshaller;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.Application;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.ManagedBean;
import org.apache.myfaces.config.impl.digester.elements.ResourceBundle;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

/**
 * Find managed bean defined in (a) faces-config file(s).
 * Uses the available JSF implementation to parse the file(s).
 */
public class Jsf12FacesConfigXmlBeanFinder implements ManagedBeanFinder {

    private final ExternalContext externalContext = Mockito.mock(ExternalContext.class);

    private Collection<InputStream> facesConfigStreams = new LinkedList<InputStream>();

    /**
     * Finder reading from the given files.
     * @param facesConfigFiles (required) may be empty
     */
    public Jsf12FacesConfigXmlBeanFinder(final Collection<File> facesConfigFiles) {
        setFacesConfigFiles(facesConfigFiles);
    }


    Jsf12FacesConfigXmlBeanFinder() { /* for testing only */ }

    public Jsf12FacesConfigXmlBeanFinder setFacesConfigFiles(final Collection<File> facesConfigFiles) {
        // TODO remove this duplication with Jsf11FacesConfigXmlBeanFinder
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
                this.facesConfigStreams.add(new NamedInputStream(facesConfigXml));
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
    public Jsf12FacesConfigXmlBeanFinder setFacesConfigStreams(final Collection<InputStream> facesConfigStreams) {
        if (facesConfigStreams == null) {
            throw new IllegalArgumentException("The facesConfigStreams: Collection<InputStream> can't be null");
        }
        this.facesConfigStreams.addAll(facesConfigStreams);
        return this;
    }

    @Override
    public Collection<ManagedBeanDescriptor> findDefinedBackingBeans() {
        // TODO remove this duplication with Jsf11FacesConfigXmlBeanFinder
        final Collection<ManagedBeanDescriptor> allBeans =
            new LinkedList<ManagedBeanDescriptor>();

        for (InputStream facesConfigStream : facesConfigStreams) {
            allBeans.addAll(
                parseFacesConfig(facesConfigStream));
        }

        return allBeans;
    }

    private Collection<ManagedBeanDescriptor> parseFacesConfig(InputStream stream) {
        try {
            FacesConfig facesConfig = getUnmarshaller().getFacesConfig(stream, stream.toString());
            stream.close();

            final Collection<ManagedBeanDescriptor> managedBeans = toManagedBeanDescriptors(facesConfig.getManagedBeans());

            extractResourceBundleVarsInto(managedBeans, facesConfig);

            return managedBeans;
        } catch (IOException e) {
            throw new RuntimeException("Failed to access the faces-config xml from " + stream, e);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to parse the faces-config xml from " + stream, e);
        }
    }

    private void extractResourceBundleVarsInto(Collection<ManagedBeanDescriptor> managedBeans, FacesConfig facesConfig) {
        for (Application application : facesConfig.getApplications()) {
            for (ResourceBundle resourceBundle : application.getResourceBundle()) {
                managedBeans.add(new ManagedBeanDescriptor(
                        resourceBundle.getVar()
                        , java.util.ResourceBundle.class
                ));
            }
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
