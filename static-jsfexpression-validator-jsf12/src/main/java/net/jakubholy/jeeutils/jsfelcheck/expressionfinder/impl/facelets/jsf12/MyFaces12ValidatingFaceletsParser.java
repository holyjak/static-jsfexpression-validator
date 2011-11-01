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

package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.jsf12;

import com.sun.facelets.compiler.Compiler;
import com.sun.facelets.compiler.JsfelcheckSAXCompiler;
import com.sun.facelets.compiler.TagLibraryConfig;
import com.sun.facelets.tag.TagLibrary;
import com.sun.facelets.tag.jsf.core.CoreLibrary;
import com.sun.facelets.tag.jsf.html.HtmlLibrary;
import com.sun.facelets.tag.jstl.core.JstlCoreLibrary;
import com.sun.facelets.tag.jstl.fn.JstlFnLibrary;
import com.sun.facelets.tag.ui.UILibrary;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.AbstractValidatingFaceletsParser;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.StandaloneExternalContext;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.PageNodeListener;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.context.ReleaseableExternalContext;
import org.apache.myfaces.context.servlet.FacesContextImpl;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * {@inheritDoc}
 *
 * Implementation for JSF 1.2 based on MyFaces 1.2.10.
 */
public class MyFaces12ValidatingFaceletsParser extends AbstractValidatingFaceletsParser {

	private static class StandaloneReleaseableExternalContext extends StandaloneExternalContext implements ReleaseableExternalContext {

		public StandaloneReleaseableExternalContext(ResourceLocator resourceLocator, Map<String, String> initParams) {
			super(resourceLocator, initParams);
		}

		@Override
		public void release() {
			// NOOP
		}
	}

	private final ExternalContext externalContext;
	private Compiler compiler;

	/**
	 * @param webappRoot (required) The folder that contains the webapp's resources folder as per the JSF 2.0 specification
	 * @param pageNodeValidator (required) The actual validator that reacts to tags found and checks them
	 */
	public MyFaces12ValidatingFaceletsParser(File webappRoot, PageNodeListener pageNodeValidator) {
		super(webappRoot, pageNodeValidator);
		this.externalContext = initializeFacesContext();
	}

	@Override
	public void validateExpressionsInView(URL xhtmlUrl, String shortName) throws IOException {
		getCompiler().compile(xhtmlUrl, shortName);
	}

	/**
	 * Initialize FacesContext for use by the compiler (so that it is aware of available tag libraries etc.)
	 */
	private ExternalContext initializeFacesContext() {

		// We need a "real" external context to be able to f.ex. locate resources such as facelet composites
		// + enable the Development mode so that location info is attached to tags when compiling a view page
		// Locator to find e.g. custom compositest in webroot/resource/<custom composite library name>
		StandaloneExternalContext.ResourceLocator resourceLocator =
				new StandaloneExternalContext.SingleFolderResourceLocator(webappRoot);
		StandaloneReleaseableExternalContext externalContext = new StandaloneReleaseableExternalContext(resourceLocator, null);

		// Initialize FacesContext; the constructor also sets FacesContext.getCurrentInstance()
		// It's important to have a real implementation mostly to hold a real application, view root etc.
		// It must be correctly configured because f.ex. the parser uses the configured ExpressionFactory
		// to create method/value expressions for EL found
		// BEWARE: Requires factories to be set already
		FacesContext facesContext = new StartupFacesContextImpl(externalContext, externalContext, true);

		// Note: Application creation requires FacesContext.currentInstance
		// Configures default factories, component types (so that Application can instantiate them when compiling) etc.
		new FacesConfigurator(externalContext).configure();

		return externalContext;
	}

	private Compiler createCompiler() {
		Compiler compiler = new JsfelcheckSAXCompiler(pageNodeValidator);

		// BEGIN Copied from ..myfaces...FaceletViewDeclarationLanguage.createCompiler() w/ little inlining
		compiler.addTagLibrary(new CoreLibrary());
		compiler.addTagLibrary(new HtmlLibrary());
		compiler.addTagLibrary(new UILibrary());
		compiler.addTagLibrary(new JstlCoreLibrary());
		compiler.addTagLibrary(new JstlFnLibrary());
		//compiler.addTagLibrary(new CompositeLibrary());
		//compiler.addTagLibrary(new CompositeResourceLibrary());

		//RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
		//compiler.setFaceletsProcessingConfigurations(runtimeConfig.getFaceletProcessingConfigurations());
		// END Copied

		registerLocalTaglibs(compiler);

		return compiler;
	}

	private void registerLocalTaglibs(Compiler compiler) {
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

	private Compiler getCompiler() {
		if (compiler == null) {
			compiler = createCompiler();
		}
		return compiler;
	}
}
