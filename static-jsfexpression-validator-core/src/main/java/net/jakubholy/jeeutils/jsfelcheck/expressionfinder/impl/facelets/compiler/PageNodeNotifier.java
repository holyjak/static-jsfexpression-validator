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
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.TextNode;

import java.util.Stack;
import java.util.logging.Logger;

import static net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.compiler.CompilationUnitType.TAG;
import static net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.compiler.CompilationUnitType.TEXT;

/**
 * Reacts to parsing events like node/text encountered and notifies its
 * {@link PageNodeListener} if appropriate.
 */
public class PageNodeNotifier {

	private static final Logger LOG = Logger.getLogger(PageNodeNotifier.class.getName());

	private final PageNodeListener tagListener;
	private final Stack<PublishablePageNode> nodeStack = new Stack<PublishablePageNode>();

	public PageNodeNotifier(PageNodeListener tagListener) {
		this.tagListener = tagListener;
	}

	/**
	 * Event: A new HTML or JSF tag encountered.
	 */
	public void handleTagPushed(TagRepresentation tag, CompilationUnitType unitType, Class<?> currentUnitType) {
		if (TEXT.equals(unitType)) {
			warnIfNonHtmlTagProcessedAsText(tag);
		} else if (!TAG.equals(unitType)) {
			// Could be e.g. a RemoveUnit (TBD: ignore it and all its content)
			LOG.info("pushTag: Neither tag nor text encountered - " + tag + ", type: " + currentUnitType.getSimpleName());
		}

		// Note: Tag handler class for Facelets is meaningless - it hasn't
		// JavaBean properties for its attributes and the expression
		// type (Value x Method binding) can't be determined from it
		// => The validator must know what kind of expression to expect
		// based on tag and attribtue names and knowledge of Facelets;
		// For non-standard tags we just have to try both
		PublishablePageNode pageNode = new PublishablePageNode(tag, tag.attributes());
		if (shouldPublishNode(pageNode, unitType)) {
			pageNode.setPublishedToListener(true);
			tagListener.nodeEntered(pageNode);
			LOG.finest("pushTag: notified listener and pushed " + pageNode);
		}

		nodeStack.push(pageNode);
	}

	/**
	 * Event: Text within a tag body encountered.
	 */
	public void handleTextContent(String value) {
		String valueTrimmed = value.trim();

		if (valueTrimmed.length() > 0 && containsJsfEl(valueTrimmed)) {
			// It's unlikely to have the stack empty but let's be safe...
			int line = nodeStack.empty()? 1 : nodeStack.peek().getLineNumber();
			PageNode textNode = new TextNode(line, valueTrimmed);
			tagListener.nodeEntered(textNode);
			tagListener.nodeLeft(textNode);
		}
	}

	/**
	 * Event: The closing HTML or JSF tag encountered (fired immediately for non-pair tags).
	 */
	public void handleTagPopped() {
		PublishablePageNode poppedTag = nodeStack.pop();

		if (poppedTag.isPublishedToListener()) {
			tagListener.nodeLeft(poppedTag);
			LOG.finest("popTag: notified listener and popped " + poppedTag);
		}
	}

	/**
	 * Warn if it's a tag with namespace => likely it should be a real tag but
	 * its taglib hasn't been properly registered with the compiler.
	 */
	private static void warnIfNonHtmlTagProcessedAsText(TagRepresentation tag) {
		boolean isInXhtmlNamespace = "http://www.w3.org/1999/xhtml".equals(tag.getNamespaceUrl());
		boolean tagWithExplicitNamespace = ! tag.getQName().equals(tag.getLocalName());
		if (tagWithExplicitNamespace && !isInXhtmlNamespace) {
			LOG.warning("pushTag: No tag library can handle the tag " + tag.getQName() + " (resolved namespace: "
					+ tag.getNamespaceUrl() + ") at " + tag.getLocation()
					+ ", processing it as text. Haven't you forgotten to register its taglib with us?");
		}
	}

	/**
	 * Is there a deferred (= JSF) EL expression in the text?
	 */
	private static boolean containsJsfEl(String text) {
		return text.indexOf("#{") > -1;
	}

	/**
	 * Is the current tag/PageNode of interest to the JSF EL Validator and shall we thus notify our listener?
	 */
	private static boolean shouldPublishNode(PublishablePageNode pageNode, CompilationUnitType currentUnit) {
		if (TAG.equals(currentUnit)) {
			return true;
		} else if (TEXT.equals(currentUnit)) {
			String attrValues = pageNode.getAttributes().values().toString();
			if (containsJsfEl(attrValues)) {
				return true;
			}
		}
		return false;
	}
}
