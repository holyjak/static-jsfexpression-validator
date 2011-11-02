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

import java.util.Map;

/**
 * Representation of a Facelets tag that wraps
 * a MyFaces implementation specific Tag instance,
 * exposing only what we need.
 */
public interface TagRepresentation {

	/** Ex: 'h:dataTable' or (if in default ns) 'div' */
	String getQName();
	String getLocalName();
	/** Resolved namespace having the complete ns URL instead of a ns prefix */
	String getNamespaceUrl();
	Object getLocation(); // or String getLocationDescription()?
	int getLocationLine();
	Map<String, String> attributes();
	
}
