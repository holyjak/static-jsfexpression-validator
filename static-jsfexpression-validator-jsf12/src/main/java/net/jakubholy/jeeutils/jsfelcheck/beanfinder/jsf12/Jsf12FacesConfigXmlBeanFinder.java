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

package net.jakubholy.jeeutils.jsfelcheck.beanfinder.jsf12;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.AbstractFacesConfigXmlBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.InputResource;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import org.apache.myfaces.config.FacesConfigUnmarshaller;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.Application;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.ManagedBean;
import org.apache.myfaces.config.impl.digester.elements.ResourceBundle;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Find managed bean defined in (a) faces-config file(s).
 * Uses the available JSF implementation to parse the file(s).
 */
public class Jsf12FacesConfigXmlBeanFinder extends AbstractFacesConfigXmlBeanFinder {

    private final ExternalContext externalContext = Mockito.mock(ExternalContext.class);

    /**
     * Finder reading from the supplied faces-config files.
     */
    public static ManagedBeanFinder forResources(final Collection<InputResource> facesConfigResources) {
        return new Jsf12FacesConfigXmlBeanFinder().setFacesConfigResources(facesConfigResources);
    }

    Jsf12FacesConfigXmlBeanFinder() {}

    @Override
    protected Collection<ManagedBeanDescriptor> parseFacesConfig(InputStream stream) {
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
