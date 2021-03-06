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
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Simple JSF managed bean that can be used to test
 * all the possible method expressions.
 *
 * TODO Make sure that each property is used EXACTLY ONCE so that we can test each occurence
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

	public static class MyConverter implements Converter {

		static final Collection<String> actionsInvoked = new LinkedList<String>();

		@Override
		public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
			registerActionInvoked("asObject", component);
			return "(object " + value + ")";
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
			registerActionInvoked("asString", component);
			return "(string " + value + ")";
		}

		private void registerActionInvoked(String actionName, UIComponent source) {
			actionsInvoked.add(actionName + "(for=" + source.getId() + ")");
		}
	}

	private static class SymetricMap extends Hashtable<String, String> {
		public SymetricMap(Collection<String> values) {
			for (String value : values) {
				put(value, value);
			}
		}
	}

	private static class SelfMap extends Hashtable<String, MyActionBean> {
		public SelfMap(Collection<String> keys, MyActionBean value) {
			for (String key : keys) {
				put(key, value);
			}
		}
	}

	private static Map<String, String> map = new SymetricMap(Arrays.asList(
			"valueInTemplatedPage", "outputValue", "inputValue", "attributeValue", "fParamValue"
			, "iAmRendered"
			, "palTarget", "palValue", "valueForCustomTag", "valueForComposite"
			, "compositeChildren"
			, "varFromIncludingPage"
			, "trickyExprKey}", "trickyExprTwo"
	));

	private static Set<String> selfmapKeys = new HashSet<String>(Arrays.asList(
			"myTagAttribute", "compositeAttributeValueBean"));
	private static Map<String,MyActionBean> selfmap;

	private MyConverter converter = new MyConverter();
	private UIComponent binded;
	private final Collection<String> actionsInvoked = new LinkedList<String>();

	/** Used in a test only */
	public static Set<String> allMapKeys() {
		return map.keySet();
	}

	/** Used in a test only */
	public static Set<String> allSelfmapKeys() {
		return selfmapKeys;
	}

	/** To be invoked via {@code action="#{myActionBean.doAction}"}. */
	public String doAction() {
		actionsInvoked.add("doAction()");
		return null;
	}

	/** To be invoked via {@code actionListener="#{myActionBean.doActionListening}"}. */
	public void doActionListening(ActionEvent e) {
		actionsInvoked.add("doActionListening(for=" + e.getComponent().getId() + ")");
	}

	/** To be invoked via {@code valueChangeListener="#{myActionBean.doValueChangeListening}"}. */
	public void doValueChangeListening(ValueChangeEvent e) {
		actionsInvoked.add("doValueChangeListening(for=" + e.getComponent().getId() + ")");
	}

	/** To be invoked via {@code validator="#{myActionBean.doValidating}"}. */
	public void doValidating(FacesContext ctx, UIComponent ui, Object something) {
		actionsInvoked.add("doValidating(for=" + ui.getId() + ")");
	}

	public UIComponent getComponentBinding() {
		return binded;
	}

	/** To be invoked via {@code binding="#{myActionBean.componentBinding}"}. */
	public void setComponentBinding(UIComponent binded) {
		this.binded = binded;
	}

	@Override
	public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
		actionsInvoked.add("processAction(" + actionEvent + ")");
	}

	public String getValue() {
		return "{I'm EL!}";
	}

	public Map<String, String> getMap() {
		return map;
	}

	public Map<String, MyActionBean> getSelfmap() {
		if (selfmap == null) {
			selfmap = new SelfMap(selfmapKeys, this);
		}
		return selfmap;
	}


	public Book[] getBooks() {
		return new Book[] {new Book("Hitchiker's Guide to the Galaxy")};
	}

	public String getActionsInvokedSummary() {
		String summary = "";
		if (!actionsInvoked.isEmpty()) {
			summary += actionsInvoked.toString();
		}
		if (!MyConverter.actionsInvoked.isEmpty()) {
			summary += " Converter:" + MyConverter.actionsInvoked;
			MyConverter.actionsInvoked.clear();
		}
		return summary;
	}

	public Converter getConverter() {
		return converter;
	}

	public Validator getValidator() {
		return new Validator() {
			@Override
			public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
				registerActionInvoked("Validator.validate", component);
			}
		};
	}

	// ########################################################################

	void registerActionInvoked(String actionDescription) {
		actionsInvoked.add(actionDescription);
	}

	private void registerActionInvoked(String actionName, UIComponent source) {
		actionsInvoked.add(actionName + "(for=" + source.getId() + ")");
	}
}
