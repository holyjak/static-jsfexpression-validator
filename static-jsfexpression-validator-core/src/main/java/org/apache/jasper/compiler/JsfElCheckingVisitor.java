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
 * <p>
 *     This class is instantied by the OnlyReadingJspPseudoCompiler
 *     and serves as an adapter to our tag JSF EL checking code to
 *     shield it from a direct dependency on Japser.
 * </p>
 *
 */
public final class JsfElCheckingVisitor extends Visitor {

    /**
     * Listen to nodes (tags) being found in the [JSP] source file.
     */
    private static class NullPageNodeListener implements PageNodeListener {

        /**
         * Start tag encountered.
         * @param currentCustomTag (required) info about the tag
         */
        public void nodeEntered(PageNode currentCustomTag) { }

        /**
         * The closing tag (if any) encountered.
         * @param currentCustomTag (required) info about the tag
         */
        public void nodeLeft(PageNode currentCustomTag) { }

        /**
         * Processing of a JSP file has started.
         * @param jspFile (required) the name of the file
         */
        public void fileEntered(String jspFile) { }

        /**
         * Static JSP include encountered, the included file is going to be processed.
         * @param includedFileName (required) the name of the included file
         */
        public void includedFileEntered(String includedFileName) {
        }

        /**
         * Inverse of {@link #includedFileEntered(String)}, called when done with it.
         * @param includedFileName (required) the name of the file
         */
        public void includedFileLeft(String includedFileName) {
        }

    }

    private static PageNodeListener nodeListener = new NullPageNodeListener();

    /**
     * Create a new visitor for the given JSP file.
     * @param jspFile (required) the name of the file that will be processed by this visitor
     * @return the visitor
     */
    public static JsfElCheckingVisitor forFile(final String jspFile) {
        return new JsfElCheckingVisitor(jspFile);
    }

    /**
     * Set the listeners that should be notified of nodes (tags) as they are found and processed.
     * <p>
     *     This is *static* because of Japser API limitations - we can only instruct it what
     *     Compiler class it should use but it instantiates it itself. The
     *     OnlyReadingJspPseudoCompiler in turn instantiates the JsfElCheckingVisitor and
     *     therefore there is no way how to pass arguments from the JsfElValidator to the visitor
     *     instance so we just set it on the visitor's class.
     *     (Alternatively we could use a factory that would locate an existing instance
     *     on demand but still we will need to use either a static singleton or the new
     *     version of thereof called Spring.)
     * </p>
     * @param nodeListener the listener
     */
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

	    // n.getTagHandlerClass() => map attribute name to the type (method|value) of the target field
	    // n.getTagHandlerClass().getDeclaredMethod("setValue", javax.el.ValueExpression.class)

        final PageNode currentCustomTag = new PageNode(
                n.getQName(), n.getTagHandlerClass(), n.getStart().getLineNumber(), attributeMap);

        nodeListener.nodeEntered(currentCustomTag);
        super.visit(n);
        nodeListener.nodeLeft(currentCustomTag);
    }

    private Map<String, String> asMap(Attributes attributes) {
        if (attributes == null || attributes.getLength() == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> attributeMap = new Hashtable<String, String>();

        for (int i = 0; i < attributes.getLength(); i++) {
            attributeMap.put(attributes.getQName(i), attributes.getValue(i));
        }

        return attributeMap;
    }

}
