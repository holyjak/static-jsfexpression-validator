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

/**
 * Get informed about nodes in a JSP pages being processed by the
 * {@link org.apache.jasper.compiler.JsfElCheckingVisitor}.
 */
public interface PageNodeListener {

    /**
     * Processing of a new node (tag) has started.
     * @param currentCustomTag (required) the new tag
     */
    void nodeEntered(PageNode currentCustomTag);

    /**
     * Processing of a node (tag) has finished.
     * @param currentCustomTag (required) the tag being done
     */
    void nodeLeft(PageNode currentCustomTag);

    /**
     * Processing of a new page source file has started.
     * @param jspFile (required) the jspRoot-relative path of the file
     */
    void fileEntered(String jspFile);

    /**
     * Processing of a new static include of another page source file has started.
     * @param includedFileName (required) the jspRoot-relative path of the included file
     */
    void includedFileEntered(String includedFileName);

    /**
     * Processing of an include has finished (i.e. we are back in the original file)
     * @param includedFileName (required) the file being left
     */
    void includedFileLeft(String includedFileName);

}
