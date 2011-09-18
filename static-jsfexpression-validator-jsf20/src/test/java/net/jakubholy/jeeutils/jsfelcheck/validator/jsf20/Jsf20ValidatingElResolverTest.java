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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf20;

import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolverAbstractTest;
import net.jakubholy.jeeutils.jsfelcheck.validator.jsf12.Jsf12ValidatingElResolver;
import org.junit.Test;


public class Jsf20ValidatingElResolverTest extends ValidatingJsfElResolverAbstractTest {

    @Override
    protected ValidatingElResolver setUpResolver() {
        return new Jsf12ValidatingElResolver(); // just reusing 1.2 for the time being ...
    }

    /**
     * JSF EL up to 1.1 were marked with #{..}, since the introduction of UEL in 1.2 they use
     * ${..} (used for JSP EL only before that).
     */
    @Test
    public void should_recognize_both_jsf_and_uel_expression_markers() throws Exception {
        elResolver.declareVariable("myStringBean", "Hello!");

        assertResultValue("#{myStringBean}", "Hello!");
        assertResultValue("${myStringBean}", "Hello!");

        // This particular implementation doesn't fail when we pass in a literal string, which is little strange
        assertResultValue("this is a literal string, not an EL expression"
                , "this is a literal string, not an EL expression");
    }

    @Test
    public void should_fake_method_of_any_arguments_and_arity() throws Exception {
        assertExpressionValid("#{fake:fakeFunction0()}");
        assertExpressionValid("#{fake:fakeFunction1(123) == true}");           // coerce to boolean
        assertExpressionValid("#{fake:fakeFunction2('string', 456) == 987}");   // coerce to int
        elResolver.declareVariable("myArray", new String[0]);
        assertExpressionValid("#{fake:fakeFunction3(myArray, 'myString')}");
        // more complex, composed expression:
        assertExpressionValid("#{f:parentFunc(fake:oneParamFunc(), another:anotherParamFunc(456))}");
        //assertExpressionValid("#{(fake:fakeFunction0() + another:anotherFunc(456)) > 0}");
        // `- now fails, can't coerce "" -> int (NumberFormatException)
    }

}
