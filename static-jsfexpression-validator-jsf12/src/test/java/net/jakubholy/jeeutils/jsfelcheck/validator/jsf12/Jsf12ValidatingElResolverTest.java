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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import net.jakubholy.jeeutils.jsfelcheck.validator.AttributeInfo;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.MethodNotFoundException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.PropertyNotFoundException;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;
import org.junit.Test;

import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolverAbstractTest;

import javax.el.ValueExpression;

import static net.jakubholy.jeeutils.jsfelcheck.validator.jsf12.Jsf12ValidatingElResolver.splitAtLastProperty;
import static org.junit.Assert.*;


public class Jsf12ValidatingElResolverTest extends ValidatingJsfElResolverAbstractTest {

	private static class MyActionBean {
		public String doAction() { return null; }
		public int getValue() { return 0; }
	}

    @Override
    protected ValidatingElResolver setUpResolver() {
        return new Jsf12ValidatingElResolver();
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

	@Test
	public void should_recognize_method_binding_attribute() throws Exception {
		// See e.g. org.apache.myfaces.taglib.html.HtmlCommandButtonTag
		elResolver.declareVariable("myActionBean", new MyActionBean());
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.doAction}", new AttributeInfo("whatever", javax.el.MethodExpression.class)));
	}

	@Test
	public void should_recognize_value_binding_attribute() throws Exception {
		elResolver.declareVariable("myActionBean", new MyActionBean());
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.value}", new AttributeInfo("whateverElse", ValueExpression.class)));
	}

	// TODO Push up
	@Test
	public void should_fail_for_explicit_method_binding_if_no_such_method() throws Exception {
	    elResolver.declareVariable("myActionBean", new Object());
		ValidationResult result = elResolver.validateElExpression(
				"#{myActionBean.noSuchMethodHere}", new AttributeInfo("whatever", javax.el.MethodExpression.class));
		assertFailureWithCause(result, MethodNotFoundException.class);
	}

	/**
	 * Heuristics (needed for Facelets): If we fail to validate EL as a value expression, try whether it isn't a method
	 * expression (must be always in the form "beam[.property*].method")
	 */
	@Test
	public void should_fall_back_to_eval_as_method_expr_if_property_not_found() throws Exception {
		elResolver.declareVariable("myActionBean", new MyActionBean());
		Class<?> unspecifiedExpressionType = String.class;
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.doAction}", new AttributeInfo("aMethodAttribute", unspecifiedExpressionType)));
	}

	@Test
	public void should_not_fall_back_to_eval_as_method_expr_if_attribute_type_is_ValueExpression() throws Exception {
		elResolver.declareVariable("myActionBean", new MyActionBean());
		ValidationResult result = elResolver.validateElExpression(
				"#{myActionBean.doAction}", new AttributeInfo("aMethodAttribute", ValueExpression.class));
		assertFailureWithCause(result, PropertyNotFoundException.class);
	}

	/**
	 * Heuristics: If we fail to validate EL as a value expression, try whether it isn't a method
	 * expression (must be always in the form "beam[.property*].method")
	 */
	@Test
	public void should_not_fall_back_to_eval_as_method_expr_if_variable_not_found() throws Exception {
		Class<?> unspecifiedExpressionType = String.class;
		ValidationResult result = elResolver.validateElExpression(
				"#{unknownValueObject}", new AttributeInfo("anAttribute", unspecifiedExpressionType));
		assertFailureWithCause(result, VariableNotFoundException.class);
	}

	@Test
	public void should_match_method_name_in_EL() throws Exception {
		assertArrayEquals("Simplest"
				, arr("#{bean}", "myMethod"), splitAtLastProperty("#{bean.myMethod}"));

		// Special chars
		assertArrayEquals("methodFollowedBySpaces"
				, arr("#{bean}", "methodFollowedBySpaces"), splitAtLastProperty("#{bean.methodFollowedBySpaces    }"));
		assertArrayEquals("_ and digits"
				, arr("#{bean}", "_myMethod123"), splitAtLastProperty("#{bean._myMethod123}"));
		assertArrayEquals("$ in name"
				, arr("#{bean}", "$my34Method"), splitAtLastProperty("#{bean.$my34Method}"));

		// Complex ELs
		assertArrayEquals("After property"
				, arr("#{bean.property}", "myMethod"), splitAtLastProperty("#{bean.property.myMethod}"));
		assertArrayEquals("After mapped property"
				, arr("#{bean['property']}", "myMethod"), splitAtLastProperty("#{bean['property'].myMethod}"));
		// NOT SUPPORTED YET: assertEquals("As a mapped property", "myMethodAsString", splitAtLastProperty("#{bean['myMethodAsString']}"));

		// Invalid
		try {
			splitAtLastProperty("#{bean.000notAMethod}");
			fail("Should have failed - Name can't start with a digit");
		} catch (IllegalArgumentException e) {}
	}

	private static String[] arr(String... parts) {
		return parts;
	}

}
