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

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver.PropertyTypeResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.PredefinedVariableResolver;

public class ValidatingFakeValueResolver extends ELResolver implements PropertyTypeResolver {

    private final CompositeELResolver allResolver;
    private ELContext currentContext;
    MockingPropertyResolver propertyResolver;
    PredefinedVariableResolver variableResolver;

    public ValidatingFakeValueResolver(CompositeELResolver allResolver) {
        this.allResolver = allResolver;

        this.propertyResolver = new MockingPropertyResolver();
        propertyResolver.setTypeResolver(this);

        this.variableResolver = new PredefinedVariableResolver(propertyResolver);
    }

    public void setValue(final ELContext context, final Object base, final Object property, final Object value)
        throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
    }

    public boolean isReadOnly(final ELContext context, final Object base, final Object property)
        throws NullPointerException, PropertyNotFoundException, ELException {
        return false;
    }

    public Object getValue(final ELContext context, final Object base, final Object property)
        throws NullPointerException, PropertyNotFoundException, ELException {

        currentContext = context;

        Object result;
        if (base == null) {
            if (!(property instanceof String)) {
                return null;
            }
            result = resolveVariable((String) property);
        } else {
            result = resolveProperty(base, property);
        }

        if (result != null) {
            // may be this isn't necessary as likely already was set during the resolution
            context.setPropertyResolved(true);
        }

        return result;

    }

    /**
     * @param base
     * @param property - usually a String (property name, map key) or Integer (arrays etc.)
     * @return
     */
    private Object resolveProperty(Object base, Object property) {
        return propertyResolver.getValue(base, property);
    }

    private Object resolveVariable(String variable) {
        return variableResolver.resolveVariable(variable);
    }

    public Class<?> getType(final ELContext context, final Object base, final Object property)
        throws NullPointerException, PropertyNotFoundException, ELException {
        return null;
    }

    public Iterator getFeatureDescriptors(final ELContext context, final Object base) {

        if (base != null) return null;

        final ArrayList<FeatureDescriptor> descriptors = new ArrayList<FeatureDescriptor>();

        Map<String, Object> declaredVariables = variableResolver.getDeclaredVariables();
        for (Entry<String, Object> variable : declaredVariables.entrySet()) {
            descriptors.add(makeDescriptor(variable.getKey(), variable.getValue().getClass()));
        }

        return descriptors.iterator();
    }

    private FeatureDescriptor makeDescriptor(final String beanName, final Class<?> managedBeanType) {
        final FeatureDescriptor fd = new FeatureDescriptor();
        fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, Boolean.TRUE);
        fd.setValue(ELResolver.TYPE, managedBeanType);
        fd.setName(beanName);
        fd.setDisplayName(beanName);
        //fd.setShortDescription();
        fd.setExpert(false);
        fd.setHidden(false);
        fd.setPreferred(true);
        return fd;
    }

    public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
        if (base != null) return null;
        return Object.class;
    }

    //@Override
    public Class<?> getType(Object target, Object property) {
        return allResolver.getType(currentContext, target, property);
    }

    MockingPropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    PredefinedVariableResolver getVariableResolver() {
        return variableResolver;
    }

}