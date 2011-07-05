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

package org.apache.jasper.compiler;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNode;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNodeListener;

import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.Node.CustomTag;
import org.apache.jasper.compiler.Node.IncludeDirective;
import org.apache.jasper.compiler.Node.Visitor;
import org.xml.sax.Attributes;

/**
 * Process tree structure of a JSP tags of a page represented by Node objects
 * and delegate any handling to a {@link PageNodeListener}, if set.
 */
public class JsfElCheckingVisitor extends Visitor {

    public static class NullPageNodeListener implements PageNodeListener {

        public void nodeEntered(PageNode currentCustomTag) {}

        public void nodeLeft(PageNode currentCustomTag) {}

        public void fileEntered(String jspFile) {}

        public void includedFileEntered(String includedFileName) {
        }

        public void includedFileLeft(String includedFileName) {
        }

    }

    private static PageNodeListener nodeListener = new NullPageNodeListener();

    public static JsfElCheckingVisitor forFile(final String jspFile) {
        return new JsfElCheckingVisitor(jspFile);
    }

    public static void setNodeListener(PageNodeListener nodeListener) {
        JsfElCheckingVisitor.nodeListener = nodeListener;
    }

    private JsfElCheckingVisitor(final String jspFile) {
        nodeListener.fileEntered(jspFile);
    }

    /* @Override
    public void visit(IncludeDirective n) throws JasperException {
        logEntry(n, toString(n.getAttributes()));
        // NOTE: We could store somewhere info that this file is included from this node =>
        // when being processed, it could lookup the parent file's context variables
        // However we'd need to ensure that we first parse the including, parent file.
        super.visit(n);
    }*/

    @Override
    public void visit(IncludeDirective n) throws JasperException {
        String includedFileName = n.getAttributeValue("file");
        nodeListener.includedFileEntered(includedFileName);
        super.visit(n);
        nodeListener.includedFileLeft(includedFileName);
    }

    @Override
    public void visit(CustomTag n) throws JasperException {
        Map<String, String> attributeMap = asMap(n.getAttributes());

        final PageNode currentCustomTag = new PageNode(
                n.getQName(), n.getTagHandlerClass(), n.getStart().getLineNumber(), attributeMap);

        nodeListener.nodeEntered(currentCustomTag);
        super.visit(n);
        nodeListener.nodeLeft(currentCustomTag);
    }

    private Map<String, String> asMap(Attributes attributes) {
        if (attributes == null || attributes.getLength() == 0) Collections.emptyMap();
        Map<String, String> attributeMap = new Hashtable<String, String>();

        for (int i = 0; i < attributes.getLength(); i++) {
            attributeMap.put(attributes.getQName(i), attributes.getValue(i));
        }

        return attributeMap;
    }

}
