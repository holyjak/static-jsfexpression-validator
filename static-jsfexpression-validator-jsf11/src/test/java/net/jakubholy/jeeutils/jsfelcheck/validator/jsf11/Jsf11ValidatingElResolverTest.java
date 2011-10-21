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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import net.jakubholy.jeeutils.jsfelcheck.validator.AttributeInfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolverAbstractTest;

import static org.junit.Assert.fail;


public class Jsf11ValidatingElResolverTest extends ValidatingJsfElResolverAbstractTest {

	private static class MyActionBean {
		public String doAction() { return null; }
		public void doActionListening(ActionEvent e) {}
		public void doValueChangeListening(ValueChangeEvent e) {}
		public void doValidating(FacesContext ctx, UIInput ui) {}
		public int getValue() { return 0; }
	}

    @Override
    protected ValidatingElResolver setUpResolver() {
        return new Jsf11ValidatingElResolver();
    }

    @Test(expected = ReferenceSyntaxException.class)
    public void should_reject_non_el_literal() throws Exception {
        elResolver.validateElExpression("this is a literal string, not an EL expression", new AttributeInfo("<dummy>", Object.class));
    }

	@Test
	public void should_recognize_action_attributes_as_method_binding() throws Exception {
		elResolver.declareVariable("myActionBean", new MyActionBean());
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.doAction}", new AttributeInfo("action", String.class)));
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.doActionListening}", new AttributeInfo("actionListener", String.class)));
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.doValueChangeListening}", new AttributeInfo("valueChangeListener", String.class)));
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.doValidating}", new AttributeInfo("validator", String.class)));
	}

	@Test
	public void should_recognize_other_attributes_as_value_binding() throws Exception {
		elResolver.declareVariable("myActionBean", new MyActionBean());
		assertResultValid(elResolver.validateElExpression(
				"#{myActionBean.value}", new AttributeInfo("whateverOther", String.class)));
	}

}
