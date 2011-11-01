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

package org.apache.myfaces.view.facelets.compiler;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNode;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNodeListener;
import org.apache.myfaces.view.facelets.el.ELText;

import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * This subclass has a {@link PageNodeListener} and notifies it when a tag is enetered/left,
 * thus integrating Facelets with the JSF EL validator.
 *
 * TODO Ignore a RemoveUnit and anything within it? (Plus see todos in the body.)
 */
public class NotifyingCompilationManager extends JsfelcheckCompilationManager {

	/**
	 * A PageNode that knows whether the P.N.Listener has been notified about it.
	 */
	private static class PublishablePageNode extends PageNode {

		private boolean publishedToListener = false;

		public PublishablePageNode(Tag tag, Map<String, String> attributeMap) {
			super(tag.getQName(), FaceletHandler.class, tag.getLocation().getLine(), attributeMap);
		}

		public boolean isPublishedToListener() {
			return publishedToListener;
		}

		public void setPublishedToListener(boolean publishedToListener) {
			this.publishedToListener = publishedToListener;
		}
	}

	private static final Logger LOG = Logger.getLogger(NotifyingCompilationManager.class.getName());

	private final PageNodeListener tagListener;
	private final Stack<PublishablePageNode> nodeStack = new Stack<PublishablePageNode>();
	private final StringBuilder currentText = new StringBuilder();

	public NotifyingCompilationManager(String alias, Compiler compiler, FaceletsProcessingInstructions instructions, PageNodeListener tagListener) {
		super(alias, compiler, instructions);
		this.tagListener = tagListener;
	}

	@Override
	public void pushTag(Tag orig) {
		final CompilationUnit previousUnit = currentUnit();
		super.pushTag(orig);

		final CompilationUnit currentUnit = currentUnit();

		// The parent transforms the tag's attribute, namespace etc. so it's better to use the transformed one
		final Tag tag = (currentUnit instanceof TagUnit)? ((TagUnit) currentUnit).getTag() : orig;

		if (previousUnit instanceof TextUnit && !(currentUnit instanceof TextUnit)) {
			// A text unit has been ended => check it for ELs.
			// Use org.apache.myfaces.view.facelets.el.ELText.parse(factory, ctx, string) with a custom
			// factory that rememberes all the expressions created
			// or use parse(string) + ELText.write(Writer out, ELContext ctx) - likely not, it calls
			// ValueExpression.getValue(ctx) [which would trigger validation but not under our direct control]

			// TODO Extract ELs
			// ELText.parse(rememeberingFakeFactory, fakeCtx, currentText.toString());
			ELText elText = ELText.parse(currentText.toString());
			if (elText != null && !elText.isLiteral()) {
				LOG.warning("pushTag: EL in text preceding " + orig + " encountered" +
						", parsing currently not supported: " + elText);
			}

			// TODO We should push text units too and do this at pop or nested JSF tag start
			// to be able to provide more precise location of every EL (think of page full of
			// html and ELs but no JSF tags - would become one huge currentText
			// We may have single (<img src="#{EL}" />) and pair (<p>..#{el}..</p>) tags
			// TODO Do this also in writeText as pushTag may be never called for text-only page (unlikely, yeh)

			currentText.setLength(0);
		}

		if (currentUnit instanceof TextUnit) {
			warnIfNonHtmlTagProcessedAsText(orig);
			currentText.append(orig + extractAttributes(orig).toString());
		} else if (!(currentUnit instanceof TagUnit)) {
			// Could be e.g. a RemoveUnit (TBD: ignore it and all itscontent)
			LOG.info("pushTag: Neither tag nor text encountered - " + orig + ", type: "
					+ currentUnit.getClass().getSimpleName());
		}


		// Note: Tag handler class for Facelets is meaningless - it hasn't
		// JavaBean properties for its attributes and the expression
		// type (Value x Method binding) can't be determined from it
		// => The validator must know what kind of expression to expect
		// based on tag and attribtue names and knowledge of Facelets;
		// For non-standard tags we just have to try both
		PublishablePageNode pageNode = new PublishablePageNode(tag, extractAttributes(tag));
		if (!ignoreNode(currentUnit)) {
			pageNode.setPublishedToListener(true);
			tagListener.nodeEntered(pageNode);
			LOG.finest("pushTag: notified listener and pushed " + pageNode);    // TODO remove log
		}

		nodeStack.push(pageNode);

	}

	private void warnIfNonHtmlTagProcessedAsText(Tag orig) {
		// Warn if it's a tag with namespace => likely it should be a real tag but
		// its taglib hasn't been registered
		String namespace = super.determineQName(orig)[0];
		boolean isInXhtmlNamespace = "http://www.w3.org/1999/xhtml".equals(namespace);
		boolean tagWithExplicitNamespace = ! orig.getQName().equals(orig.getLocalName());
		if (tagWithExplicitNamespace && !isInXhtmlNamespace) {
			LOG.warning("pushTag: No tag library can handle the tag " + orig.getQName() + " (resolved namespace: "
				+ namespace + ") at " + orig.getLocation()
				+ ", processing it as text. Haven't you forgotten to register its taglib with us?");
		}
	}

	private Map<String, String> extractAttributes(Tag tag) {
		Map<String, String> attributeMap = new Hashtable<String, String>();

		TagAttribute[] attrs = tag.getAttributes().getAll();
		for (TagAttribute attr : attrs) {
			attributeMap.put(attr.getLocalName(), attr.getValue());
		}
		return attributeMap;
	}

	@Override
	public void writeText(String value) {
		super.writeText(value);
		currentText.append(value);
	}

	@Override
	public void popTag() {
		final CompilationUnit currentUnitBeforePop = currentUnit();
		super.popTag();

		PublishablePageNode poppedTag = nodeStack.pop();

		if (poppedTag.isPublishedToListener()) {
			tagListener.nodeLeft(poppedTag);
			LOG.finest("popTag: notified listener and popped " + poppedTag);
		}
	}

	private boolean ignoreNode(CompilationUnit currentUnit) {
		return ! (currentUnit instanceof TagUnit);
	}
}
