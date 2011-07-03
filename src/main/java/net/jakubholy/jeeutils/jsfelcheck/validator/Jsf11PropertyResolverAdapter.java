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

package net.jakubholy.jeeutils.jsfelcheck.validator;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

import net.jakubholy.jeeutils.jsfelcheck.validator.FakeValueFactory.UnableToCreateFakeValueException;
import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver.PropertyTypeResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.ExpressionRejectedByFilterException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InternalValidatorFailureException;


import com.sun.faces.el.PropertyResolverImpl;

/**
 * Automatically return a Mockito mock for any property that is valid, otherwise throw a {@link PropertyNotFoundException}.
 *
 * The mocked type is determined automatically but may be forced via {@link Jsf11PropertyResolverAdapter#definePropertyTypeOverride(String, Class)},
 * which is useful e.g. for Maps that return just Objects.
 */
public final class Jsf11PropertyResolverAdapter extends PropertyResolver {

    public static class Jsf11PropertyTypeResolverImpl implements PropertyTypeResolver {

        private PropertyResolver realResolver = new PropertyResolverImpl();

        @Override
        public Class<?> getType(Object target, Object property) {

            if (property instanceof Integer) {
                Class<?> type = realResolver.getType(target, ((Integer) property).intValue());
                if (type != null) {
                    return type;
                }
            }

            return realResolver.getType(target, property);
        }
    }

    private final MockingPropertyResolver resolver;
    private final PropertyResolver realResolver = new PropertyResolverImpl();

    public Jsf11PropertyResolverAdapter(MockingPropertyResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("The resolver is required");
        }
        this.resolver = resolver;
        resolver.setTypeResolver(new Jsf11PropertyTypeResolverImpl());
    }

    @Override
    public Class<?> getType(Object target, Object property)
            throws EvaluationException, PropertyNotFoundException {
        return resolver.getTypeInternal(target, property);
    }

    @Override
    public Class<?> getType(Object target, int index) throws EvaluationException,
            PropertyNotFoundException {
        return resolver.getTypeInternal(target, index);
    }

    @Override
    public Object getValue(Object target, Object property)
            throws EvaluationException, PropertyNotFoundException {
        //return realResolver.getValue(arg0, arg1);
        return resolver.getValue(target, property, getType(target, property));
    }

    @Override
    public Object getValue(Object target, int property)
            throws EvaluationException, PropertyNotFoundException {
        return resolver.getValue(target, property, getType(target, property));
    }

    @Override
    public boolean isReadOnly(Object arg0, Object arg1)
            throws EvaluationException, PropertyNotFoundException {
        return realResolver.isReadOnly(arg0, arg1);
    }

    @Override
    public boolean isReadOnly(Object arg0, int arg1)
            throws EvaluationException, PropertyNotFoundException {
        return realResolver.isReadOnly(arg0, arg1);
    }

    @Override
    public void setValue(Object arg0, Object arg1, Object arg2)
            throws EvaluationException, PropertyNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Object arg0, int arg1, Object arg2)
            throws EvaluationException, PropertyNotFoundException {
        throw new UnsupportedOperationException();
    }

}