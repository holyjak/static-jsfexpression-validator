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
import java.util.Iterator;
import java.util.LinkedList;

public class ParsedElExpression implements Iterable<String> {

    private final Collection<String> expressionParts = new LinkedList<String>();

    @Override
    public Iterator<String> iterator() {
        return expressionParts.iterator();
    }

    public int size() {
        return expressionParts.size();
    }

    public void setVariable(String variable) {
        expressionParts.clear();
        expressionParts.add(variable);
    }

    public void addProperty(String property) {
        expressionParts.add(property);
    }

    @Override
    public String toString() {
        StringBuilder expression = new StringBuilder();

        for (String part : expressionParts) {
            if (expression.length() > 0) {
                expression.append('.');
            }
            expression.append(part);
        }

        return expression.toString();
    }

}
