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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets

import org.junit.Test
import org.junit.Before
import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test.MyActionBean
import org.junit.Ignore
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.jsf20.ExperimentalFaceletsComponentBuilderAndRenderer
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.jsf20.MyFaces21ValidatingFaceletsParser
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener
import groovy.transform.Canonical
import org.junit.BeforeClass
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNode

// TODO Move this pages to static-jsfexpression-validator-jsf20 and make it running there (incl. custom component under resources/)
// see org.apache.myfaces.view.facelets.compiler.NotifyingCompilationManagerTest
class ValidateAllExpressionsInFaceletsTest {

    private static final Set ALL_EL_EXPRESSIONS = new HashSet(allExpectedEls())

    def private static allExpectedEls() {
        def allMapKeys = MyActionBean
            .allMapKeys() - ["valueForCustomTag", "valueForComposite"]
        allMapKeys = allMapKeys.collect { "myActionBean.map.$it" }
        def beanPropsAndMethods = ["doAction"
                , "doActionListening", "doValueChangeListening"
                , "doValidating", "value", "books"
                , "converter", "validator", "actionsInvokedSummary"
                , "paramFromTemplatedPage", "paramFromIncludingPage"
                , "myTagAttribute.map.valueForCustomTag"
                , "cc.attrs.compositeAttributeValueBean.map.valueForComposite"
                , "derivedVar"
            ].collect { "myActionBean.$it" }
        def localVars = ["book.name"]
        def selfmapKeys = MyActionBean.allSelfmapKeys().collect { "myActionBean.selfmap.$it" }
        return allMapKeys + selfmapKeys + beanPropsAndMethods + localVars
    }



    private static JsfElValidatingFaceletsParser parser
    def static collectedCalls

    @BeforeClass
    public static void setUpForAll() {
        collectedCalls = []
        PageNodeListener listener = createListenerLoggingCallsInto(collectedCalls)
        parser = new MyFaces21ValidatingFaceletsParser("src/main/webapp" as File, listener)
    }

    /*@Before
    public void setUp() {
        finder = new ExperimentalFaceletsComponentBuilderAndRenderer(new File("src/main/webapp"), "tests/valid_el")
    }*/

    /**
     * EXPRESSIONS CURRENTLY NOT DETECTED:
     * <pre>
     *    myActionBean.map.trickyExprKey}           - actually found but as: myActionBean.map["trickyExprKey}"]
     *    myActionBean.map.varFromIncludingPage
     *    myActionBean.value
     *    myActionBean.paramFromTemplatedPage
     *    myActionBean.paramFromIncludingPage
     *    myActionBean.myTagAttribute.map.valueForCustomTag
     *    myActionBean.cc.attrs.compositeAttributeValueBean.map.valueForComposite
     *    myActionBean.derivedVar
     * </pre>
     */
    @Ignore("Not integrated yet with the rest of the app")
    @Test
    public void compile_and_listen_for_compiler_events() throws Exception {
        parser.validateExpressionsInView(toUrl("faceletsParsingFullTest.xhtml"), "/faceletsParsingFullTest.xhtml")
        parser.validateExpressionsInView(toUrl("templateTest/pageWithTemplate.xhtml"), "/templateTest/pageWithTemplate.xhtml")
        parser.validateExpressionsInView(toUrl("customTagTest/pageWithCustomTagAndComposite.xhtml"), "/customTagTest/pageWithCustomTagAndComposite.xhtml")

        // BEWARE The test doesn't really work yet, the check needs to be modified
        assert (allExpectedEls() - extractAllElAttributesFromCalls()) == []
    }


    def toUrl(fileName) {
        return new File("src/main/webapp/tests/valid_el/$fileName").toURI().toURL()
    }

    def extractAllElAttributesFromCalls() {
        def nodeEnteredCalls
        assert (nodeEnteredCalls = collectedCalls.findResults { (it.method == "nodeEntered")? it.args[0]: null } )
        // Extract the EL attributes
        def allElAttributes = nodeEnteredCalls.collectMany { it.attributes.values().findAll { it.contains("#{") } }
        return allElAttributes.collectMany { extractAttributeEls(it) }
    }

    def extractAttributeEls(attributeValue) {
        def els = []
        // Split at '#{' then remove the last '}' and all after it
        (attributeValue =~ /#\{(?:.(?!#\{))*/).each { match ->
            els << match.substring(2).replaceFirst(/\}[^}]*?$/,"")
        }
        return els
    }

    // ################################################################################################################

    @Canonical private static class Call {
        def method
        def args

        String toString() {
            _toString().replaceFirst("^.*?Call", "")
        }
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
