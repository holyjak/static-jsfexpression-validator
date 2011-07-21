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

package net.jakubholy.jeeutils.jsfelcheck.validator.results;

/**
 * Context information about an EL expression, attached to validation results etc.
 */
public class JsfExpressionDescriptor {

    private final String expression;
    private String jspFile;
    private int tagLineNumber = -1;

    /**
     * Descriptor whose context data is just the expression itself.
     * Further context info may be provided via {@link #setJspFile(String)} and {@link #setTagLineNumber(int)}.
     * @param elExpression (required)
     */
    public JsfExpressionDescriptor(String elExpression) {
        this.expression = elExpression;
    }

    /**
     * Descriptor with tag location information and no EL expression (it's assumed that it isn't needed because it's
     * already communicated in another way).
     * @param lineNumber (required) the line where the tag containing the EL expression in question starts
     * @param jspFile (required) the page source file where the tag is
     */
    public JsfExpressionDescriptor(int lineNumber, String jspFile) {
        this.tagLineNumber = lineNumber;
        this.jspFile = jspFile;
        this.expression = "";
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "expression=" + expression
        + ((jspFile == null)? "" : ", file=" + jspFile)
        + ((tagLineNumber == -1)? "" : ", tagLine=" + tagLineNumber);
    }

    public void setJspFile(String jspFile) {
        this.jspFile = jspFile;
    }

    public void setTagLineNumber(int currentTagLineNumber) {
        this.tagLineNumber  = currentTagLineNumber;
    }

    public String getJspFile() {
        return jspFile;
    }

    public int getTagLineNumber() {
        return tagLineNumber;
    }

}
