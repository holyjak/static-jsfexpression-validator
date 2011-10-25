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

package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf20.test;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;

/**
 * Simple JSF managed bean that can be used to test
 * all the possible method expressions.
 */
public class MyActionBean implements ActionListener {

	public static class Book {

		private final String name;

		public Book(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private UIComponent binded;

	public String doAction() {
		return null;
	}

	public void doActionListening(ActionEvent e) {
	}

	public void doValueChangeListening(ValueChangeEvent e) {
	}

	public void doValidating(FacesContext ctx, UIInput ui) {
	}

	public String getValue() {
		return "{I'm EL!}";
	}

	public UIComponent getComponentBinding() {
		return binded;
	}

	public void setComponentBinding(UIComponent binded) {
		this.binded = binded;
	}

	@Override
	public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
	}

	public Book[] getBooks() {
		return new Book[] {new Book("Hitchiker's Guide to the Galaxy")};
	}
}
