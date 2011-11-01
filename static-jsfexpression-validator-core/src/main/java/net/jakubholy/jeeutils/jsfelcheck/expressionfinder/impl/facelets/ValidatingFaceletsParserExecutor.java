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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;

/**
 * Executes a validatingFaceletsParser for every view file found.
 * Finds and validates EL expressions in Facelets XHTML pages.
 */
public class ValidatingFaceletsParserExecutor {

	private static final Logger LOG = Logger.getLogger(ValidatingFaceletsParserExecutor.class.getName());

	private final File viewFilesRoot;
	private final File webappRoot;

	private final JsfElValidatingFaceletsParser validatingParser;

	/**
	 * @param viewFilesRoot (required) Where to search for .xhtml files to validate
	 * @param webappRoot (required) Parent directory of WEB-INF - to search for tag files
	 * @param validatingParser (required) The validating facelets parser
	 */
	public ValidatingFaceletsParserExecutor(File viewFilesRoot, File webappRoot, JsfElValidatingFaceletsParser validatingParser) {
		this.validatingParser = validatingParser;
		this.viewFilesRoot = assertNotNull(viewFilesRoot, "viewFilesRoot", File.class);
		this.webappRoot = assertNotNull(webappRoot, "webappRoot", File.class);
	}

	public void execute() {
		validatingParser.registerTaglibs(findTaglibFiles());
		
		Collection<File> allViews = findViewFiles();
		for (File view : allViews) {
			try {
				validatingParser.validateExpressionsInView(view.toURI().toURL(), toRootRelativePath(view));
			} catch (IOException e) {
				// Highly unlikely but let's not ignore it anyway
				throw new RuntimeException("Failed to access the view file " + view.getAbsolutePath(), e);
			}
		}
	}

	private String toRootRelativePath(File view) {
		return view.getAbsolutePath().substring(
				viewFilesRoot.getAbsolutePath().length()
		);
	}

	@SuppressWarnings("unchecked")
	private Collection<File> findViewFiles() {
        return FileUtils.listFiles(viewFilesRoot, new String[] {"xhtml"}, true);
    }

	@SuppressWarnings("unchecked")
	private Collection<File> findTaglibFiles() {
		Collection taglibs = FileUtils.listFiles(new File(webappRoot, "WEB-INF"), new String[]{"taglib.xml"}, true);
		LOG.info("Local taglibs found under webroot/WEB-INF: " + taglibs);
		return taglibs;
    }
}
