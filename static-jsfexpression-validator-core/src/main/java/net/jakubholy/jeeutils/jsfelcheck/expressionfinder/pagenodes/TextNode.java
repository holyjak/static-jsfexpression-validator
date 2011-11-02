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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.Map;

/**
 * Represents not a true tag but a text content within the body of a tag.
 * Ex.: <code>{@code <p>text content inside a tag</p>}</code>
 */
public class TextNode extends PageNode {

	public static final String QNAME = "TEXT_CONTENT";

	public TextNode(int lineNumber, String textContent) {
		super(QNAME, Text.class, lineNumber, Collections.singletonMap("content", textContent));
	}
}
