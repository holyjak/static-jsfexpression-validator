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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;

public abstract class AbstractValidatingFaceletsParser implements JsfElValidatingFaceletsParser {

	protected final File webappRoot;
	protected final PageNodeListener pageNodeValidator;
	protected Collection<File> taglibs = Collections.emptyList();

	/**
	 * @param webappRoot (required) The folder that contains the webapp's resources folder as per the JSF 2.0 specification
	 * @param pageNodeValidator (required) The actual validator that reacts to tags found and checks them
	 */
	public AbstractValidatingFaceletsParser(File webappRoot, PageNodeListener pageNodeValidator) {
		this.webappRoot = assertNotNull(webappRoot, "webappRoot", File.class);
		this.pageNodeValidator = assertNotNull(pageNodeValidator, "pageNodeValidator", PageNodeListener.class);
	}

	@Override
	public void registerTaglibs(Collection<File> taglibs) {
		this.taglibs = taglibs;
	}
}
