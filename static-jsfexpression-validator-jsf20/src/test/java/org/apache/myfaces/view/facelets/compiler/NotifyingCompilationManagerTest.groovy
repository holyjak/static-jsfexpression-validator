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

import org.junit.Before
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNodeListener
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNode
import org.junit.Test
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.JsfElValidatingFaceletsParser
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.jsf20.MyFaces21ValidatingFaceletsParser
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.Canonical
import org.junit.BeforeClass

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
        /* The following works when a method called from Groovy but not from Java:
        listener = {} as PageNodeListener
        listener.metaClass.invokeMethod = { name, args ->
            collectedCalls << new Call(name, args)
            println "invokeMethod: $name($args)"
        }
        */

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
    public void should_notify_about_file_entered2() throws Exception {
        compile("oneJsfTag.xhtml")
        assert collectedCalls.find { it.method == "nodeEntered" && it.args[0].QName == "h:form" }
        assert collectedCalls.find { it.method == "nodeLeft" && it.args[0].QName == "h:form" }
        println "All calls: $collectedCalls"
    }

    // ###############################################################################################

    def compile(viewName) {
        parser.validateExpressionsInView(toUrl(viewName), "/$viewName")
    }

    def toUrl(fileName) {
        return new File(TEST_WEBROOT, "$fileName").toURI().toURL()
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
