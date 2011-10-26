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

import org.apache.el.ExpressionFactoryImpl;
import org.apache.myfaces.config.FacesConfigurator;
import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.context.servlet.StartupFacesContextImpl;
import org.apache.myfaces.view.facelets.Facelet;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.compiler.*;
import org.apache.myfaces.view.facelets.compiler.Compiler;
import org.apache.myfaces.view.facelets.impl.DefaultFaceletFactory;
import org.apache.myfaces.view.facelets.tag.composite.CompositeLibrary;
import org.apache.myfaces.view.facelets.tag.composite.CompositeResourceLibrary;
import org.apache.myfaces.view.facelets.tag.jsf.core.CoreLibrary;
import org.apache.myfaces.view.facelets.tag.jsf.html.HtmlLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.core.JstlCoreLibrary;
import org.apache.myfaces.view.facelets.tag.jstl.fn.JstlFnLibrary;
import org.apache.myfaces.view.facelets.tag.ui.UILibrary;

import javax.el.Expression;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.ProjectStage;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKitFactory;
import javax.faces.view.Location;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.ResourceResolver;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
 
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO
 * - try with UEL: INFO: MyFaces Unified EL support disabled - Likely conflict between Jetty's and our EL impl.
 *  (JSF cant's srr tomcat-jasper-el's javax.el.ValueReference), I tried Jetty 8 that impl. JSP 2.2 but failed to run it
 * - try with JSF 1.2, 2.0 pages - is it compatible with them?
 *
 * FIXME
 * - EL in f:attribute and f:setPropertyActionListener not verified - perhaps these f: tags are processed differently?
 * - handle verification of in-text ELs such as {@code <h:column>#{bean.value}</h:column>} - turned into a
 *  org.apache.myfaces.view.facelets.compiler.UIInstructions (with instr=TextInstruction) - not sure how to get its
 *  ELText :-(
 * - Shall we check ui:param used to pass values to templates via ui:insert, ui:composition? And composite:attribute?
 */
public class ExperimentalFaceletsElFinder {

	private static final Logger LOG = Logger.getLogger(ExperimentalFaceletsElFinder.class.getName());
	private ExternalContext externalContextMock;
	private long compilationDoneMs;
	private long componentTreeConstructionDoneMs;
	private final File viewsRootFolder;

	private abstract static class FacesContextHack extends FacesContext {

		public static void setFacesContextSingleton(FacesContext ctx) {
			FacesContext.setCurrentInstance(ctx);
		}
	}

	//private FaceletContext mockFaceletContext;
	private FacesContext facesContext;

	public static void main(String[] a) throws Exception {
		long start = System.currentTimeMillis();
		ExperimentalFaceletsElFinder finder = new ExperimentalFaceletsElFinder(new File(
				"test-webapp-jsf20-facelets_owb/src/main/webapp/tests/valid_el"));

		//String view = "/faceletsParsingFullTest.xhtml";
		String view = "/templateTest/customTag.xhtml";
		finder.verifyExpressionsIn(view);

		// Simple page time: DONE IN 1.6s (parsing: 1.4s, component tree: 0.2s)
		System.out.format("\n##### DONE IN %2.1fs (parsing: %2.1fs, component tree: %2.1fs)"
				, (System.currentTimeMillis() - start)/1000.0
				, (finder.compilationDoneMs - start)/1000.0
				, (finder.componentTreeConstructionDoneMs - finder.compilationDoneMs)/1000.0
		);
	}

	public ExperimentalFaceletsElFinder(File viewsRoot) {
		this.viewsRootFolder = viewsRoot;
		// Copied from Jsf12ValidatingElResolver constructor
		final Map<String, Object> emptyMap = Collections.emptyMap();
		externalContextMock = mock(ExternalContext.class);
        when(externalContextMock.getApplicationMap()).thenReturn(new Hashtable<String, Object>());
        when(externalContextMock.getRequestMap()).thenReturn(new Hashtable<String, Object>());
        when(externalContextMock.getSessionMap()).thenReturn(emptyMap);
		// Enable the Development mode so that location info is attached to tags
		// when compiling a view page
		when(externalContextMock.getInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME))
				.thenReturn(ProjectStage.Development.name());

		// Also sets FacesContext.getCurrentInstance()
		facesContext = new StartupFacesContextImpl(externalContextMock, null, null, true);

		// Note: Application creation requires FacesContext.currentInstance
		configureFaces(externalContextMock); // see ApplicationFactory.getapplication()
	}

	public void verifyExpressionsIn(String view) throws Exception {
		Compiler compiler = createCompiler();

		// Needed to avoid NPE - used to check attribtue types etc.
		// Must be set before we create a facelet
		RuntimeConfig.getCurrentInstance(facesContext.getExternalContext())
				.setExpressionFactory(new ExpressionFactoryImpl());

		FaceletFactory faceletFactory = initializeFactory(compiler);
		Facelet facelet = faceletFactory.getFacelet(view);

		compilationDoneMs = System.currentTimeMillis();

		UIViewRoot viewRoot = new UIViewRoot();
		viewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
		// apply(..) tries to instinatiate the render factory
		// TODO Is tits creation expensive? Y => use custom stub kit & implem.

		facesContext.setViewRoot(viewRoot);

		// BEWARE: Already this may fail due to PropertyNotFoundException (TagValueExpression.setValue)
		// - this applies only to some attributes, e.g. binding
		facelet.apply(facesContext, facesContext.getViewRoot());

		componentTreeConstructionDoneMs = System.currentTimeMillis();
		
		verifyExpressionsIn(viewRoot);
	}

	private void verifyExpressionsIn(UIComponent viewRoot) {
		printComponent(viewRoot, "");
	}

	private void printComponent(UIComponent component, String indentation) {

		printSingleComponent(component, indentation);

		Iterator<UIComponent> children = component.getFacetsAndChildren();
		while(children.hasNext()) {
			printComponent(children.next(), indentation + "  ");
		}
	}

	/**
	 *
	 * @param component
	 * @param indentation
	 *
	 * @see org.apache.myfaces.renderkit.ErrorPageWriter#_writeAttributes
	 */
	private void printSingleComponent(UIComponent component, String indentation) {
		// TODO Use introspection to find out all attributes and get Value/MethodExpressions (readerMethod.exec instanceOf VE/ME)
		// then use getExpressionString to get the EL
		Location location = (Location) component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

		// Note: Location is normally (?) null for some components such as the UIViewRoot created by us,
		// ComponentResourceContainer, UIInstructions

		//Map<String, Object> customAttributes = new Hashtable<String, Object>(component.getAttributes());
		String customAttrsDisplay = "";// customAttributes.isEmpty()? "" : ", custom attrs: " + customAttributes;

		System.out.println(indentation + "Component " + (component.getClass()).getName() + " at " + location
			+ ", el attrs: " + extractAttributes(component) + customAttrsDisplay
			+ ((component.getClass().getName().endsWith(".compiler.UIInstructions")? component.toString().replaceAll("\n", "").trim() : ""))
		);
	}

	private final static Set<String> IGNORE_ATTRS = new TreeSet<String>(Arrays.asList(
			"parent", "rendererType", "inView", "id", "transient"));

	/**
	 * Note: commandButton.action => two attrs:
	 * {actionExpression=#{helloWorld.send}/MethodExpression, action=#{helloWorld.send}/MB}
	 */
	@SuppressWarnings("deprecation")
	private Map<String, String> extractAttributes(UIComponent component) {
		Map<String, String> attributes = new Hashtable<String, String>();

		try {
			BeanInfo info = Introspector.getBeanInfo(component.getClass());
			PropertyDescriptor[] pd = info.getPropertyDescriptors();
			ValueExpression valueExpression = null;

			for (int i = 0; i < pd.length; i++) {
				String attrName = pd[i].getName();
				if (pd[i].getWriteMethod() != null && !IGNORE_ATTRS.contains(attrName)) {
					final Method m = pd[i].getReadMethod();
					try {
						// first check if the property is a ValueExpression
						valueExpression = component.getValueExpression(attrName);
						// Value expr. is e.g. UIInput's value (whose value would be null now)
						if (valueExpression != null) {
							attributes.put(attrName, valueExpression.getExpressionString() + "/VE");
						} else {
							final Object v = m.invoke(component, (Object[]) null);
							if (v != null) {
								if (v instanceof Collection || v instanceof Map || v instanceof Iterator) {
									continue;
								}
								String str;
								if (v instanceof Expression) {
									String type = (v instanceof MethodExpression)? "ME"
											: (v instanceof ValueExpression)? "VE2" : v.getClass().getSimpleName();
									str = ((Expression) v).getExpressionString() + "/" + type;
								} else if (v instanceof javax.faces.el.ValueBinding) {
									str = ((javax.faces.el.ValueBinding) v).getExpressionString() + "/VB";
								} else if (v instanceof javax.faces.el.MethodBinding) {
									str = ((javax.faces.el.MethodBinding) v).getExpressionString() + "/MB";
								} else {
									str = null; //*/v.toString();
								}

								if (str != null) attributes.put(attrName, str);
							}
						}
					} catch (Exception e) {
						// do nothing
					}
				}
			}

			ValueExpression binding = component.getValueExpression("binding");
			if (binding != null) {
				attributes.put("binding", binding.getExpressionString());
			}
			
		} catch (Exception e) {
			// do nothing
		}

		// TODO For
		if (component instanceof UICommand) {
			// JSF 2? introduced actionExpression: MethodExpression instead of action: MethodBinding
			if (attributes.containsKey("actionExpression")) {
				attributes.remove("action");
			}
		}

		return attributes;
	}

	private FaceletHandler parseFaceletsViewFile(File view, Compiler compiler) {
		try {
			return compiler.compile(view.toURI().toURL(), "jhDummyAlias");
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse XHTML " + view.getAbsolutePath(), e);
		}
	}

	private Compiler createCompiler() {
		SAXCompiler compiler = new SAXCompiler();

		// From org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage.createCompiler()
        compiler.addTagLibrary(new CoreLibrary());
        compiler.addTagLibrary(new HtmlLibrary());
        compiler.addTagLibrary(new UILibrary());
        compiler.addTagLibrary(new JstlCoreLibrary());
        compiler.addTagLibrary(new JstlFnLibrary());
        compiler.addTagLibrary(new CompositeLibrary());
        compiler.addTagLibrary(new CompositeResourceLibrary());

		RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContextMock);
		compiler.setFaceletsProcessingConfigurations(
				runtimeConfig.getFaceletProcessingConfigurations());

		return compiler;
	}

	private void configureFaces(ExternalContext externalContext) {
		//RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
        //runtimeConfig.setExpressionFactory(expressionFactory);

		// Configures default factories, component types (so that Application can instantiate them) etc.
		new FacesConfigurator(externalContext).configure();
	}

	private FaceletFactory initializeFactory(Compiler c) {
		try {
			return new DefaultFaceletFactory(c, new FilesystemResolver(viewsRootFolder));
		} catch (IOException e) {
			throw new RuntimeException("FaceletFactory init failed", e);
		}
	}

	private static class FilesystemResolver extends ResourceResolver {

		private final File viewsRootFolder;

		public FilesystemResolver(File viewsRootFolder) {
			this.viewsRootFolder = viewsRootFolder;
		}

		@Override
		public URL resolveUrl(String path) {
			try {
				if ("/".equals(path)) {
					// Called to determine the base url to later remove it from
					// page URLs to get short aliases
					return viewsRootFolder.toURI().toURL();
				} else {
					// Path is what we pass into the Facelet factory
					// E.g. p=/Users/jholy/.../webapp/helloWorld.xhtml
					return new File(viewsRootFolder, path).toURI().toURL();
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to resolve " + path, e);
			}
		}
	}
}
