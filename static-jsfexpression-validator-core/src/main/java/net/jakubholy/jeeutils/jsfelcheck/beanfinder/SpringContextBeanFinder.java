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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Register all Spring Beans defined in Spring applicationContext file(s) as JSF
 * Managed Beans.
 * Uses Spring itself to get the beans.
 */
public class SpringContextBeanFinder implements ManagedBeanFinder {

    private static final Logger LOG = Logger.getLogger(SpringContextBeanFinder.class.getName());

    private Collection<InputResource> springContextFiles;

    /**
     * Finder reading from the supplied Spring applicationContext XML files accessed via streams.
     */
    public static ManagedBeanFinder forStreams(final Collection<InputResource> facesConfigStreams) {
        return new SpringContextBeanFinder().setSpringConfigStreams(facesConfigStreams);
    }

    SpringContextBeanFinder() {}

    private SpringContextBeanFinder setSpringConfigStreams(final Collection<InputResource> springContextFiles) {
        this.springContextFiles = springContextFiles;
        return this;
    }

    /** {@inheritDoc} */
    public Collection<ManagedBeanDescriptor> findDefinedBackingBeans() {
        Collection<ManagedBeanDescriptor> allBeans = new LinkedList<ManagedBeanFinder.ManagedBeanDescriptor>();

        BeanDefinitionRegistry knownBeans = new SimpleBeanDefinitionRegistry();
        XmlBeanDefinitionReader beanParser = new XmlBeanDefinitionReader(knownBeans);
        beanParser.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);

        beanParser.loadBeanDefinitions(toResources(springContextFiles));

        LOG.info("found " + knownBeans.getBeanDefinitionCount() + " definitions in the given Spring config files");

        String[] beanNames = knownBeans.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = knownBeans.getBeanDefinition(beanName);
            String beanClassName = extractBeanType(beanName, beanDefinition);
            Class<?> beanClass = loadBeanClass(beanName, beanClassName);
            allBeans.add(new ManagedBeanDescriptor(beanName, beanClass));
        }

        return allBeans;
    }

    private String extractBeanType(String beanName, BeanDefinition beanDefinition) {
        final String definedType = beanDefinition.getBeanClassName();
        String actualType = definedType;

        // Bypass proxies etc. (perhaps there can be more levels of decorators?)
        // TODO test more
        BeanDefinition decoratedBeanDef;
        while ((decoratedBeanDef = beanDefinition.getOriginatingBeanDefinition()) != null) {
            actualType = decoratedBeanDef.getBeanClassName();
            LOG.info("Found bean " + beanName + " wrapped with a decorator; using the original type "
                    + actualType + "; decorator: " + definedType);
        }
        return actualType;
    }

    private Class<?> loadBeanClass(String beanName, String beanClassName) {
        try {
            return Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load class '"
            		+ beanClassName + "' for bean '" + beanName + "'");
        }
    }

    private Resource[] toResources(Collection<InputResource> resourceFiles) {
        Resource[] locations = new Resource[resourceFiles.size()];

        int index = 0;
        for (InputResource configFile : resourceFiles) {
            if (configFile.getFileIfAvailable() != null) {
                locations[index++] = new FileSystemResource(configFile.getFileIfAvailable());
            } else {
                locations[index++] = new InputStreamResource(configFile.getStream());
            }
        }

        return locations;
    }

}
