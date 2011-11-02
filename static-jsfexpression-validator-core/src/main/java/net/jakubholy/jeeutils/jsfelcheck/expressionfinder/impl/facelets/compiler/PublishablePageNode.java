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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.compiler;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNode;

import java.util.Map;

/**
 * A PageNode that knows whether the P.N.Listener has been notified about it.
 * (Currently used for Facelets only.)
 */
public class PublishablePageNode extends PageNode {

	private static final class FakeFaceletHandler {}

	private boolean publishedToListener = false;

	public PublishablePageNode(TagRepresentation tag, Map<String, String> attributeMap) {
		super(tag.getQName(), FakeFaceletHandler.class, tag.getLocationLine(), attributeMap);
	}

	public boolean isPublishedToListener() {
		return publishedToListener;
	}

	public void setPublishedToListener(boolean publishedToListener) {
		this.publishedToListener = publishedToListener;
	}
}
