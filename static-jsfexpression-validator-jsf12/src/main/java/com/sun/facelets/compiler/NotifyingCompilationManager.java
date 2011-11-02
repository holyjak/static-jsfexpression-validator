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

package com.sun.facelets.compiler;

import com.sun.facelets.tag.Tag;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.compiler.CompilationUnitType;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.compiler.PageNodeNotifier;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.compiler.TagRepresentation;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener;

import static net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.compiler.CompilationUnitType.*;

/**
 * This subclass has a {@link net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener} and notifies it when a tag is enetered/left,
 * thus integrating Facelets with the JSF EL validator.
 */
public class NotifyingCompilationManager extends JsfelcheckCompilationManager {

	private final PageNodeNotifier nodeNotifier;

	public NotifyingCompilationManager(String alias, Compiler compiler, PageNodeListener tagListener) {
		super(alias, compiler);
		nodeNotifier = new PageNodeNotifier(tagListener);
	}

	@Override
	public void pushTag(Tag orig) {
		super.pushTag(orig);

		final CompilationUnit currentUnit = currentUnit();
		final Class<?> currentUnitType = currentUnit.getClass();

		// The parent transforms the tag's attribute, namespace etc. so it's better to use the transformed one
		final Tag tagTransformed = (currentUnit instanceof TagUnit)? ((TagUnit) currentUnit).getTag() : orig;
		final TagRepresentation tag = new MyFaces12TagRepresentation(tagTransformed, determineQName(tagTransformed)[0]);

		final CompilationUnitType unitType;
		if (currentUnit instanceof TagUnit) {
			unitType = TAG;
		} else if (currentUnit instanceof TextUnit) {
			unitType = TEXT;
		} else {
			unitType = OTHER;
		}

		nodeNotifier.handleTagPushed(tag, unitType, currentUnitType);

	}

	@Override
	public void writeText(String value) {
		super.writeText(value);
		nodeNotifier.handleTextContent(value);
	}

	@Override
	public void popTag() {
		super.popTag();
		nodeNotifier.handleTagPopped();
	}
}
