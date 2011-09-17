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

package net.jakubholy.jeeutils.jsfelcheck.beanfinder.jsf11;

import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.ManagedBeanBean;
import com.sun.faces.config.rules.FacesConfigRuleSet;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.AbstractFacesConfigXmlBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;

import javax.faces.FacesException;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Find managed bean defined in (a) faces-config file(s).
 * Uses the available JSF implementation to parse the file(s).
 */
public class Jsf11FacesConfigXmlBeanFinder extends AbstractFacesConfigXmlBeanFinder {

    private static final String[][] DTD_INFO = { { "/com/sun/faces/web-facesconfig_1_0.dtd", "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN" } // SUPPRESS CHECKSTYLE
    , { "/com/sun/faces/web-facesconfig_1_1.dtd", "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN" } };

    private static final Logger LOG = Logger.getLogger(Jsf11FacesConfigXmlBeanFinder.class.getName());


    /**
     * Finder reading from the supplied faces-config files.
     */
    public static ManagedBeanFinder forFiles(final Collection<File> facesConfigFiles) {
        return new Jsf11FacesConfigXmlBeanFinder().setFacesConfigFiles(facesConfigFiles);
    }

    /**
     * Finder reading from the supplied faces-config files.
     */
    public static ManagedBeanFinder forStreams(final Collection<InputStream> facesConfigStreams) {
        return new Jsf11FacesConfigXmlBeanFinder().setFacesConfigStreams(facesConfigStreams);
    }

    Jsf11FacesConfigXmlBeanFinder() {}

    @Override
    protected Collection<ManagedBeanDescriptor> parseFacesConfig(InputStream facesConfigStream) {
        Digester digester = digester(false);
        FacesConfigBean facesConfig = new FacesConfigBean();

        parse(digester, facesConfigStream, facesConfig);

        return toManagedBeanDescriptors(facesConfig.getManagedBeans());
    }

    /*private String getManagedBeanClassName(String configFilePath, Node managedBeanNode) {
       String xpath = "./managed-bean-class/text()";
       // String xpath = "./managed-bean/managed-bean-name/text()";
       return ParserUtils.querySingle(managedBeanNode, xpath, configFilePath).trim();
    }*/

    private Collection<ManagedBeanDescriptor> toManagedBeanDescriptors(
            ManagedBeanBean[] beansFound) {

        Collection<ManagedBeanDescriptor> beanDescriptors = new LinkedList<ManagedBeanDescriptor>();

        for (ManagedBeanBean managedBean : beansFound) {
            String beanName = managedBean.getManagedBeanName();
            Class<?> beanClass = loadBeanClass(beanName, managedBean.getManagedBeanClass());
            beanDescriptors.add(new ManagedBeanDescriptor(beanName, beanClass));
        }

        return beanDescriptors;
    }

    private Class<?> loadBeanClass(String beanName, String beanClassName) {
        try {
            return Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load class '"
                    + beanClassName + "' for bean '" + beanName + "'");
        }
    }

    protected Digester digester(boolean validateXml) {
      Digester digester = new Digester();

      digester.setNamespaceAware(false);
      digester.setUseContextClassLoader(true);
      digester.setValidating(validateXml);

      digester.addRuleSet(new FacesConfigRuleSet(false, false, true));

      for (int i = 0; i < DTD_INFO.length; i++) {
        URL url = getClass().getResource(DTD_INFO[i][0]);
        if (url != null) {
          digester.register(DTD_INFO[i][1], url.toString());
        } else {
          throw new RuntimeException("NO_DTD_FOUND_ERROR: " + DTD_INFO[i][1] + "," + DTD_INFO[i][0]);
        }

      }

      digester.push(new FacesConfigBean());

      return digester;
    }


    protected void parse(Digester digester, InputStream stream, FacesConfigBean fcb) {

      InputSource source = null;
      try {
        source = new InputSource(stream);
        source.setByteStream(stream);
        digester.clear();
        digester.push(fcb);
        digester.parse(source);
        stream.close();
        stream = null;
      } catch (Exception e) {
        String message = "Can't parse configuration file from " + stream;
        LOG.log(Level.SEVERE, message, e);
        throw new FacesException(message, e);
      } finally {
        if (stream != null) {
          try {
            stream.close();
          } catch (Exception e) { } // SUPPRESS CHECKSTYLE
        stream = null;
        }
      }
    }

}
