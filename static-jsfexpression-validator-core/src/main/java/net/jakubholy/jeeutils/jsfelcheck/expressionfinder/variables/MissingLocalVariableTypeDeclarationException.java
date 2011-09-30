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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.variables;

/**
 * Thrown when we detect declaration of a local variable (typically inside an h:dataTable tag)
 * but its expected type hasn't been defined beforehand. (More exactly the type of elements
 * contained in the source EL expression, yielding a collection with elements the variable will reference).
 */
public class MissingLocalVariableTypeDeclarationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String variableName;
    private final String sourceExpression;
    private int tagLineNumber;
    private String jspFile;

    /**
     * @param variableName (required)
     * @param sourceExpression (required) the EL used to feed the local variable
     */
    public MissingLocalVariableTypeDeclarationException(String variableName, String sourceExpression) {
        this.variableName = variableName;
        this.sourceExpression = sourceExpression;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setJspFile(String jspFile) {
        this.jspFile = jspFile;
    }

    public String getJspFile() {
        return jspFile;
    }

    public String getSourceExpression() {
        return sourceExpression;
    }

    public void setTagLineNumber(int tagLineNumber) {
        this.tagLineNumber = tagLineNumber;
    }

    public int getTagLineNumber() {
        return tagLineNumber;
    }

    @Override
    public String toString() {
        return "Declare type of elementes in the collection EL '" + sourceExpression
        	+ "', assigned to the local variable '" + variableName
            + "' (file " + jspFile + ", tag line " + tagLineNumber + ")";
    }

}
