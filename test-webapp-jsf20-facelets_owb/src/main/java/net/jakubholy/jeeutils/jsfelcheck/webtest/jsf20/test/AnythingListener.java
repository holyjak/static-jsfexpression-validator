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

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

/**
 * For use with the {@code <f:*listener type="<this class>" />} tag
 * or (perhaps) with {@code <f:*listener binding=#{instanceOfThisClassBean}" />}.
 */
public class AnythingListener implements ValueChangeListener, ActionListener {

	private static MyActionBean getRequestActionBean() {
		// There must be a better way - injection or ValueExpression ?
		FacesContext context = FacesContext.getCurrentInstance();
		return (MyActionBean) context.getApplication().evaluateExpressionGet(
				context, "#{myActionBean}", MyActionBean.class);
	}

	@Override
	public void processAction(ActionEvent e) throws AbortProcessingException {
		getRequestActionBean().registerActionInvoked("AnythingListener.doActionListening(for="
				+ e.getComponent().getId() + ")");
	}

	@Override
	public void processValueChange(ValueChangeEvent e) throws AbortProcessingException {
		getRequestActionBean().registerActionInvoked("AnythingListener.doValueChangeListening(for="
				+ e.getComponent().getId() + ")");
	}
}
