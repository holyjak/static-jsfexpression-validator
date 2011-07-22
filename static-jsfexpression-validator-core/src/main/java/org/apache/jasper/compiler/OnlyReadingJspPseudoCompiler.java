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

import java.io.FileNotFoundException;

import org.apache.jasper.JasperException;

// CHECKSTYLE:OFF (copied from Jasper's source code)
/**
 * A simple Compiler subclass that overrides {@link #generateJava()} to invoke
 * the Visitor of our choice.
 */
public class OnlyReadingJspPseudoCompiler extends Compiler {

    /* We're never compiling .java to .class. */
    @Override
    protected void generateClass(String[] smap) throws FileNotFoundException,
            JasperException, Exception {
        throw new UnsupportedOperationException();
    }

    /*
     * Copied from {@link Compiler#generateJava()} and adjusted
     */
    @Override
    protected String[] generateJava() throws Exception {

        // Setup page info area
        pageInfo = new PageInfo(new BeanRepository(ctxt.getClassLoader(),
                errDispatcher), ctxt.getJspFile());

        // Skipped processing of jsp-property-group in web.xml for the current page

        if (ctxt.isTagFile()) {
            try {
                double libraryVersion = Double.parseDouble(ctxt.getTagInfo()
                        .getTagLibrary().getRequiredVersion());
                if (libraryVersion < 2.0) {
                    pageInfo.setIsELIgnored("true", null, errDispatcher, true);
                }
                if (libraryVersion < 2.1) { // SUPPRESS CHECKSTYLE
                    pageInfo.setDeferredSyntaxAllowedAsLiteral("true", null,
                            errDispatcher, true);
                }
            } catch (NumberFormatException ex) {
                errDispatcher.jspError(ex);
            }
        }

        ctxt.checkOutputDir();

        try {
            // Parse the file
            ParserController parserCtl = new ParserController(ctxt, this);

            // Pass 1 - the directives
            Node.Nodes directives =
                parserCtl.parseDirectives(ctxt.getJspFile());
            Validator.validateDirectives(this, directives);

            // Pass 2 - the whole translation unit
            pageNodes = parserCtl.parse(ctxt.getJspFile());

            /*
             * The code above has been copied from Compiler#generateJava() with some
             * omissions and with using our own Visitor.
             * The code that used to follow was just deleted.
             */

            // JH - my own code starts here
            // Validate and process attributes - don't re-validate the
            // directives we validated in pass 1
            pageNodes.visit(JsfElCheckingVisitor.forFile(ctxt.getJspFile()));

        } finally {}

        return null;
    }

    /**
     * The parent's implementation, in our case, checks whether the target file
     * exists and returns true if it doesn't. However it is expensive so
     * we skip it by returning true directly.
     *
     * @see org.apache.jasper.JspCompilationContext#getServletJavaFileName()
     */
    @Override
    public boolean isOutDated(boolean checkClass) {
        return true;
    }

}
// CHECKSTYLE:ON
