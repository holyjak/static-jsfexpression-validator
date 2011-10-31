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

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert;
import org.apache.commons.io.FileUtils;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;
import org.apache.myfaces.view.facelets.compiler.*;
import org.apache.myfaces.view.facelets.compiler.Compiler;
import org.apache.myfaces.view.facelets.tag.TagLibrary;
import org.apache.myfaces.view.facelets.tag.composite.CompositeLibrary;
import org.apache.myfaces.view.facelets.tag.composite.CompositeResourceLibrary;
import org.apache.myfaces.view.facelets.tag.jsf.core.CoreLibrary;
import org.apache.myfaces.view.facelets.tag.jsf.html.HtmlLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.core.JstlCoreLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.fn.JstlFnLibrary;
import org.apache.myfaces.view.facelets.tag.ui.UILibrary;

import javax.faces.application.ProjectStage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;

/**
 * Finds and validates EL expressions in Facelets XHTML pages.
 */
public class JsfElValidatingFaceletsParser {

	private static final Logger LOG = Logger.getLogger(JsfElValidatingFaceletsParser.class.getName());

	private final PageNodeListener pageNodeValidator;
	private final ExternalContext externalContext;
	private final File viewFilesRoot;
	private final File webappRoot;

	/**
	 * @param viewFilesRoot (required) Where to search for .xhtml files to validate
	 * @param webappRoot (required) The folder that contains the webapp's resources folder as per the JSF 2.0 specification
	 * @param pageNodeValidator (required) The actual validator that reacts to tags found and checks them
	 */
	public JsfElValidatingFaceletsParser(File viewFilesRoot, File webappRoot, PageNodeListener pageNodeValidator) {
		this.viewFilesRoot = assertNotNull(viewFilesRoot, "viewFilesRoot", File.class);
		this.webappRoot = assertNotNull(webappRoot, "webappRoot", File.class);
		this.pageNodeValidator = assertNotNull(pageNodeValidator, "pageNodeValidator", PageNodeListener.class);
		this.externalContext = initializeFacesContext(viewFilesRoot);
	}

	/**
	 * Initialize FacesContext for use by the compiler (so that it is aware of available tag libraries etc.)
	 */
	private ExternalContext initializeFacesContext(File viewFilesRoot) {

		// We need a "real" external context to be able to f.ex. locate resources such as facelet composites
		// + enable the Development mode so that location info is attached to tags when compiling a view page
		Map<String, String> initParams = Collections.singletonMap(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Development.name());
		// Locator to find e.g. custom compositest in webroot/resource/<custom composite library name>
		StandaloneExternalContext.ResourceLocator resourceLocator =
				new StandaloneExternalContext.SingleFolderResourceLocator(webappRoot);
		ExternalContext externalContext = new StandaloneExternalContext(resourceLocator, initParams);

		// Initialize FacesContext; the constructor also sets FacesContext.getCurrentInstance()
		// It's important to have a real implementation mostly to hold a real application, view root etc.
		// It must be correctly configured because f.ex. the parser uses the configured ExpressionFactory
		// to create method/value expressions for EL found
		FacesContext facesContext = new StartupFacesContextImpl(externalContext, null, null, true);

		// Note: Application creation requires FacesContext.currentInstance
		// Configures default factories, component types (so that Application can instantiate them when compiling) etc.
		new FacesConfigurator(externalContext).configure();

		return externalContext;
	}

	public void execute() {
		Collection<File> allViews = findViewFiles();
		for (File view : allViews) {
			Compiler compiler = createCompiler();
			try {
				compiler.compile(view.toURI().toURL(), toRootRelativePath(view));
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

	private Compiler createCompiler() {
		Compiler compiler = new JsfelcheckSAXCompiler(pageNodeValidator);

		// BEGIN Copied from ..myfaces...FaceletViewDeclarationLanguage.createCompiler() w/ little inlining
        compiler.addTagLibrary(new CoreLibrary());
        compiler.addTagLibrary(new HtmlLibrary());
        compiler.addTagLibrary(new UILibrary());
        compiler.addTagLibrary(new JstlCoreLibrary());
        compiler.addTagLibrary(new JstlFnLibrary());
        compiler.addTagLibrary(new CompositeLibrary());
        compiler.addTagLibrary(new CompositeResourceLibrary());

		RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
		compiler.setFaceletsProcessingConfigurations(
				runtimeConfig.getFaceletProcessingConfigurations());
		// END Copied

		registerLocalTaglibs(compiler);

		return compiler;
	}

	private void registerLocalTaglibs(Compiler compiler) {
		Collection<File> taglibs = findTaglibFiles();
		LOG.info("Local taglibs found under webroot/WEB-INF: " + taglibs);
		for (File taglib : taglibs) {
			try {
				URL taglibUrl = taglib.toURI().toURL();
				TagLibrary tagLib = TagLibraryConfig.create(taglibUrl);
				compiler.addTagLibrary(tagLib);
			} catch (IOException e) {
				throw new RuntimeException("Taglib parsing failed", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<File> findViewFiles() {
        return FileUtils.listFiles(viewFilesRoot, new String[] {"xhtml"}, true);
    }

	@SuppressWarnings("unchecked")
	private Collection<File> findTaglibFiles() {
        return FileUtils.listFiles(new File(webappRoot, "WEB-INF"), new String[] {"taglib.xml"}, true);
    }
}
