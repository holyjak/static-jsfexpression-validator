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

/**
 * Resolution information for a JSF EL variable - its name and declared type.
 */
public class VariableInfo {

    private final String variableName;
    private final Class<?> declaredVariableType;

    public VariableInfo(String variableName,
            Class<?> declaredVariableType) {
        if (variableName == null) {
            throw new IllegalArgumentException("variableName: String may not be null");
        }
        if (declaredVariableType == null) {
            throw new IllegalArgumentException("declaredVariableType: Class may not be null");
        }
        this.variableName = variableName;
        this.declaredVariableType = declaredVariableType;
    }

    public String getVariableName() {
        return variableName;
    }

    public Class<?> getDeclaredVariableType() {
        return declaredVariableType;
    }

    @Override
    public String toString() {
        return "VariableInfo [variableName=" + variableName
                + ", declaredVariableType=" + declaredVariableType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((declaredVariableType == null) ? 0 : declaredVariableType
                        .hashCode());
        result = prime * result
                + ((variableName == null) ? 0 : variableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VariableInfo other = (VariableInfo) obj;
        if (declaredVariableType == null) {
            if (other.declaredVariableType != null)
                return false;
        } else if (!declaredVariableType.equals(other.declaredVariableType))
            return false;
        if (variableName == null) {
            if (other.variableName != null)
                return false;
        } else if (!variableName.equals(other.variableName))
            return false;
        return true;
    }

}
