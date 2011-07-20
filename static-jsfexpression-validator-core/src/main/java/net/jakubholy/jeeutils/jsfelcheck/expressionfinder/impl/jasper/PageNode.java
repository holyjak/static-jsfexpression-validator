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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper;

import java.util.Map;

/** Represents a tag in a JSP/JSF page, in other words a node of the page. */
public class PageNode {

    private static long counter = 0;

    private final long id = counter++;
    private final String qName;
    private final Map<String, String> attributes;
    private final Class<?> tagHandlerClass;
    private final int lineNumber;

    /**
     * Create new page node describing a custom JSP tag.
     * @param qName (required)
     *  qualified name of the tag, ex.: h:dataTable
     * @param tagHandlerClass (required)
     *  the handler used for the tag, ex.: org.apache.myfaces.taglib.html.HtmlDataTableTag
     * @param lineNumber (required)
     *  the line where the tag starts in the page source code
     * @param attributeMap (required)
     *  map with attributes of the tag: name -> value
     */
    public PageNode(String qName, Class<?> tagHandlerClass, int lineNumber,
            Map<String, String> attributeMap) {
                this.qName = qName;
                this.tagHandlerClass = tagHandlerClass;
                this.lineNumber = lineNumber;
                this.attributes = attributeMap;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getQName() {
        return qName;
    }

    public Class<?> getTagHandlerClass() {
        return tagHandlerClass;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "PageNode [qName=" + qName + ", tagHandlerClass="
            + tagHandlerClass.getName() + ", line=" + lineNumber
            + ", attributes=" + attributes + ",id=" + id + "]";
    }

    public long getId() {
        return id;
    }



}
