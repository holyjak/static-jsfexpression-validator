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

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;

/**
 * Create 'fake' instances of classes (or interfaces). Being fake mean that they can be just
 * mocks not really supporting the behavior of the class.
 */
public class FakeValueFactory {

	protected FakeValueFactory() { }

	/**
	 * A fake instance of the given class/interface cannot be produced for some reason.
	 * (E.g. it is a final class and we tried to mock it, ... .)
	 */
    public static final class UnableToCreateFakeValueException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private UnableToCreateFakeValueException(String arg0, Throwable arg1) {
            super(arg0, arg1);
        }

    }

    private static final List<Class<?>> NUMBER_CLASSES = Arrays.asList(new Class<?>[] {
            int.class, long.class, double.class, float.class
            , Integer.class, Long.class, Double.class, Float.class
    });

    private static Object createPropertMock(final Object property, final Class<?> type) {
        // TBD Mock MockObjectOfUnknownType or just instantiate it?
        try {
            return Mockito.mock(type, "Mocked_class_" + type.getName() + "_for_property_" + property);
        } catch (MockitoException e) {
            throw new UnableToCreateFakeValueException("Failed to create a mock for the property " + property
                    + " with guessed type " + type.getName()
                    + ": " + e.getMessage()
                    , e);
        }
    }

    private static boolean isNumber(Class<?> type) {
        if (Modifier.isFinal(type.getModifiers())) {
            return NUMBER_CLASSES.contains(type);
        } else {
            return false;
        }
    }

    /**
     * Create a (possibly fake) value of the given class/interface.
     * @param type (required) a non-final class or an interface
     * @param propertyToFake (required) for logging and naming
     * @return the (fake) instance
     * @throws UnableToCreateFakeValueException see the message/cause
     */
    public static Object fakeValueOfType(final Class<?> type, final Object propertyToFake)
    	throws UnableToCreateFakeValueException {
                if (type == null) {
                    // We are perhaps in a Map => let's return a default value
                    return new MockObjectOfUnknownType(propertyToFake);
    //                throw new IllegalStateException("Can't determine the type of the property, target="
    //                        + target + ",property=" + property);
                } else if (type.isArray()) {
                    return Array.newInstance(type.getComponentType(), 0);
                } else if (type == String.class) {
                    return "";      // JSF can coerce "" to number etc. as it needs
                } else if (type == Boolean.class || type == boolean.class) {
                    return Boolean.TRUE;    // can't mock the final class Boolean
                } else if (isNumber(type)) {
                    return Integer.MIN_VALUE;    // can't mock the final classes such as int or Integer
                } else if (type == Object.class) {
                    // Likely we couldn't determine the correct type; this usually doesn't matter for the
                    // last part of an expression but is a problem in the middle of it
                    return new MockObjectOfUnknownType(propertyToFake);
                } else {
                    return createPropertMock(propertyToFake, type);
                }
            }

}