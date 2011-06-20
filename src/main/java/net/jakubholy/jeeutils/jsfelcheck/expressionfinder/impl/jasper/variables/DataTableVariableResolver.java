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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.model.DataModel;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.AttributesValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;


/**
 * Extract JSF EL variable defined in a h:dataTable and resolve its
 * type.
 * <p>
 * It is not possible to derive the type (for element type of
 * collections is not known or only at the compile time) and therefore the
 * type of the dataTable value's elements must be declared up-front
 * via {@link #declareTypeFor(String, Class)}.
 * <p>
 * We know that the name of the variable produced is stored in the
 * attribute 'var' and that its values will be set based on the value
 * of the attribute 'value'. We know that 'value' must be of a particular
 * type (DataModel, Collection, array (?)).
 */
public class DataTableVariableResolver implements TagJsfVariableResolver {

    private static final Logger LOG = Logger.getLogger(DataTableVariableResolver.class.getName());

    private final Map<String, Class<?>> declaredTypes = new Hashtable<String, Class<?>>();

    private Class<?> jstlResultClass;

    /**
     * Ex: org.opentravel.www.OTA._2003._05.RailReservationTypeItinerary$$EnhancerByMockitoWithCGLIB$$1b4c48fc
     * $javax.faces.model.DataModel$$EnhancerByMockitoWithCGLIB$$773ae086
     */
    private Pattern mockitoGeneratedClassRE = Pattern.compile("\\$?(.*?)\\$\\$EnhancerByMockitoWithCGLIB.*");;

    public DataTableVariableResolver() {
        try {
            jstlResultClass = Class.forName("javax.servlet.jsp.jstl.sql.Result");
        } catch (ClassNotFoundException e) { }
    }

    public DataTableVariableResolver declareTypeFor(String jsfExpression,
            Class<?> type) {
        declaredTypes.put(jsfExpression, type);
        return this;
    }

    @Override
    public VariableInfo extractContextVariables(Map<String, String> attributes,
            AttributesValidationResult resolvedJsfExpressions) throws DeclareTypeOfVariableException {

        String iterationVariableName = attributes.get("var");
        String sourceExpression = normalizeExpression(attributes.get("value"));

        // TODO verify value set to avoid NPE

        ValidationResult sourceModel = resolvedJsfExpressions.get("value");
        // TODO verify sourceModel <> null and one of the allowed types

        Class<?> declaredVariableType = declaredTypes.get(sourceExpression);
        declaredVariableType = tryToExtractTypeFromSourceExpression(sourceModel, declaredVariableType);

        if (/*iterationVariableName != null &&*/ declaredVariableType == null) {
            throw new DeclareTypeOfVariableException(iterationVariableName, sourceExpression);
        } else {
            LOG.info("Variable extracted: name=" + iterationVariableName + ", type " + declaredVariableType);
            return new VariableInfo(iterationVariableName, declaredVariableType);
        }

    }

    /**
     * Try to guess the type based on the value of the source EL expression, if possible.
     * It is not possible if source is a Collection but may be possible if it is an
     * array or a simple value (don't ask me why somebody uses dataTable for that but they do).
     * @param sourceModel (optional)
     * @param declaredVariableType (optional)
     * @return the variables type if it can be determined (or the declared type, if set), null otherwise
     */
    private Class<?> tryToExtractTypeFromSourceExpression(
            ValidationResult sourceModel, final Class<?> declaredVariableType) {

        Class<?> result = declaredVariableType;

        if (declaredVariableType == null && sourceModel instanceof SuccessfulValidationResult) {

            Object sourceValue = ((SuccessfulValidationResult) sourceModel).getExpressionResult();

            if (sourceValue != null) {
                //  array, a List, JDBC ResultSet, JSTL ResultSet (perhaps javax.servlet.jsp.jstl.sql.Result?), or any other type of object. (Other objects are represented as one row)
                // See sublcasses of DataModel: http://download.oracle.com/docs/cd/E17802_01/j2ee/javaee/javaserverfaces/2.0/docs/api/javax/faces/model/DataModel.html
                Class<?> sourceValueType = sourceValue.getClass();
                boolean isNonArrayCollection = List.class.isAssignableFrom(sourceValueType)
                    || ResultSet.class.isAssignableFrom(sourceValueType)
                    || DataModel.class.isAssignableFrom(sourceValueType)
                    || (jstlResultClass != null && jstlResultClass.isAssignableFrom(sourceValueType));

                if (sourceValueType.isArray()) {
                    result = sourceValueType.getComponentType();
                } else if (!isNonArrayCollection) {
                    result = stripMockitoSubclass(sourceValueType); // the source is likely a single value thing
                    // Perhaps make configurable whether / for which types to allow this???
                }
            }
        }

        return result;
    }

    /**
     * Avoid mocked classes like $javax.faces.model.DataModel$$EnhancerByMockitoWithCGLIB$$773ae086
     */
    private Class<?> stripMockitoSubclass(Class<?> sourceValueType) {
        Matcher matcher = mockitoGeneratedClassRE.matcher(sourceValueType.getName());
        if (matcher.find()) {
            String mockedClass = matcher.group(1);
            try {
                LOG.info("Found mockito-generated class '"
                        + sourceValueType.getName() + "', replacing with the original class '"
                        + mockedClass + "'");
                return Class.forName(mockedClass);
            } catch (ClassNotFoundException e) {
                LOG.log(Level.SEVERE, "Found mockito-generated class '"
                        + sourceValueType.getName() + "' but failed "
                		+ "to access the original class '" + mockedClass
                		+ "', perhaps a classloader issue?"
                        , e);
                return null;
            }
        }
        return sourceValueType;
    }

    private String normalizeExpression(String expression) {
        if (expression == null) return null;
        return expression.replaceFirst("^\\s*#\\{\\s*", "").replaceAll("\\s*\\}\\s*$", "");
    }

}