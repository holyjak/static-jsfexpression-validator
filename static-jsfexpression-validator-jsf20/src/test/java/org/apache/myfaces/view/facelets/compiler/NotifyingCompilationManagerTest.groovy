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

package org.apache.myfaces.view.facelets.compiler

import groovy.transform.Canonical
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.JsfElValidatingFaceletsParser
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.jsf20.MyFaces21ValidatingFaceletsParser
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNode
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError

class NotifyingCompilationManagerTest {

    @Canonical private static class Call {
        def method
        def args

        String toString() {
            _toString().replaceFirst("^.*?Call", "")
        }
    }

    private static final File TEST_WEBROOT = new File("src/test/resources")

    private static JsfElValidatingFaceletsParser parser
    def static collectedCalls

    @BeforeClass
    public static void setUpForAll() {
        collectedCalls = []
        PageNodeListener listener = createListenerLoggingCallsInto(collectedCalls)
        parser = new MyFaces21ValidatingFaceletsParser(TEST_WEBROOT, listener)
    }

    @Before
    public void setUp() {
        collectedCalls.clear()
    }

    @Test
    public void should_notify_about_file_entered() throws Exception {
        compile("empty.xhtml")
        assert collectedCalls.find { it.equals(new Call("fileEntered", ["/empty.xhtml"]))}
    }

    @Test
    public void should_notify_about_pair_jsf_tag_entered_and_left() throws Exception {
        compile("oneJsfTag.xhtml")
        assert collectedCalls.find { it.method == "nodeEntered" && it.args[0].QName == "h:form" }
        assert collectedCalls.find { it.method == "nodeLeft" && it.args[0].QName == "h:form" }
    }

    @Test
    public void should_notify_about_nonpair_jsf_tag_entered_and_left() throws Exception {
        compile("oneNonpairJsfTag.xhtml")
        assert collectedCalls.find { it.method == "nodeEntered" && it.args[0].QName == "h:outputLabel" }
        assert collectedCalls.find { it.method == "nodeLeft" && it.args[0].QName == "h:outputLabel" }
    }

    @Test
    public void "should not report HTML tags without ELs"() throws Exception {
        compile("el/htmlTagWithoutEls.xhtml")
        assert ! collectedCalls.find { it.method == "nodeEntered" && it.args[0].QName == "TEXT_CONTENT" }
    }

    @Test
    public void "should report all ELs in the body of an HTML tag"() throws Exception {
        compile("el/insideHtmlTagBody.xhtml")
        def textNode = assertTagReported("TEXT_CONTENT", ["content" : "before#{insideHtmlTagBody1}in#{insideHtmlTagBody2}after"])
        def parentNodeLine = 22 // the parent <p> starts here
        assert textNode.lineNumber == parentNodeLine
    }

    @Test
    public void "should report EL in in the body of a JSF tag"() throws Exception {
        compile("el/insideJsfTagBodyAsTextContent.xhtml")
        assertTagReported("TEXT_CONTENT", ["content" : "#{insideJsfTagBodyAsTextContent}"])
    }

    @Test
    public void "should report EL in a HTML tag's attribute"() throws Exception {
        compile("el/inSingleHtmlTagAttribute.xhtml")
        assertTagReported("img", ["src" : "#{inSingleHtmlTagAttribute}"])
    }

    @Test
    public void "should report all ELs in a complex page mixing HTML and JSF tags"() throws Exception {
        compile("el/combinedHtmlAndJsfTags.xhtml")
        assertTagReported("TEXT_CONTENT", ["content" : "#{multiInHtmlTagBeforeJsfTag}"])
        assertTagReported("div", ["class" : "#{nestedBlockAttribute}"])
        assertTagReported("TEXT_CONTENT", ["content" : "#{doubleNestedBlockContent}"])
        assertTagReported("TEXT_CONTENT", ["content" : "#{multiInHtmlTagAfterJsfTag}"])
    }

    // TODO Verify behavior of custom tag and composite and template parsing

    // ###############################################################################################

    def compile(viewName) {
        parser.validateExpressionsInView(toUrl(viewName), "/$viewName")
    }

    def toUrl(fileName) {
        return new File(TEST_WEBROOT, "$fileName").toURI().toURL()
    }

    PageNode assertTagReported(expectedQName, expectedAttributes) {
        def qnamedNodeEntered
        def pageNode
        try {
            assert (qnamedNodeEntered = collectedCalls.findResults {
                def firstArgument = it.args[0]
                (it.method == "nodeEntered" && firstArgument.QName == expectedQName)? firstArgument: null }
            )
            // Verify all the expected attributes were present
            assert (pageNode = qnamedNodeEntered.find { (expectedAttributes - it.attributes).isEmpty() })
        } catch (PowerAssertionError e) {
            throw new PowerAssertionError(e.getMessage()
                    + "\nWHERE expectedQName=$expectedQName, expectedAttributes=$expectedAttributes");
        }
        return pageNode
    }

    private static PageNodeListener createListenerLoggingCallsInto(collectedCalls) {
        def map = [:]

        PageNodeListener.class.methods.each() { method ->
            map."$method.name" = { Object[] args ->
                collectedCalls << new Call(method.name, args)
            }
        }

        return map.asType(PageNodeListener.class)
    }
}
