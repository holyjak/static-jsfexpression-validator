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

import net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert;

import javax.faces.context.ExternalContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static net.jakubholy.jeeutils.jsfelcheck.util.ArgumentAssert.assertNotNull;

/**
 * Simple and very limited ExternalContext implementation for use
 * outside of any container.
 * <p>
 *     Its main capabilities is loading of resources (via a ResourceLocator)
 *     and storage of applicatin/session/request/parameter key value pairs.
 * </p>
 */
public class StandaloneExternalContext extends ExternalContext {

	public static interface ResourceLocator {
		public URL getResource(String path) throws MalformedURLException;
		public InputStream getResourceAsStream(String path);
		/** @see javax.servlet.ServletContext#getResourcePaths(String) */
		public Set<String> getResourcePaths(String path);
	}

	public static class SingleFolderResourceLocator implements ResourceLocator {

		private final File resourceRoot;

		public SingleFolderResourceLocator(File resourceRoot) {
			this.resourceRoot = resourceRoot;
		}

		@Override
		public URL getResource(String path) throws MalformedURLException {
			File file = new File(resourceRoot, path);
			return file.exists()? file.toURI().toURL() : null;
		}

		@Override
		public InputStream getResourceAsStream(String path) {
			try {
				URL resource = getResource(path);
				return (resource == null)? null : resource.openStream();
			} catch (IOException e) {
				throw new RuntimeException("Failed to get the resource " + path
					+ "(root dir: " + resourceRoot.getAbsolutePath() + ")", e);
			}
		}

		@Override
		public Set<String> getResourcePaths(String path) {
			Set<String> relativeSubPaths = new HashSet<String>();
			File pathDir = new File(resourceRoot, path);
			if (pathDir.isDirectory()) {
				File[] subFiles = pathDir.listFiles();
				for (File file : subFiles) {
					relativeSubPaths.add(
							"/" + file.getName() + (file.isDirectory()? "/" : "")
					);
				}
			}
			return relativeSubPaths;
		}
	}

	private final Map<String, String> initParameterMap = new Hashtable<String, String>();
	private final Map<String, Object> sessionMap = new Hashtable<String, Object>();
	private final Map<String, Object> applicationMap = new Hashtable<String, Object>();
	private final Map<String,Object> requestMap = new Hashtable<String, Object>();
	private final ResourceLocator resourceLocator;

	/**
	 * A context that locates resources via the given locator.
	 * @param resourceLocator (required) locator for resources that users of this context may try to access
	 * @param initParams (optional) fake servlet init parameters to pass on to the users via the initParameterMap
	 */
	public StandaloneExternalContext(ResourceLocator resourceLocator, Map<String, String> initParams) {
		this.resourceLocator = assertNotNull(resourceLocator, "resourceLocator", ResourceLocator.class);
		if (initParams != null) {
			this.initParameterMap.putAll(initParams);
		}
	}

	/**
	 * A context that locates resources in the filesystem under the given directory.
	 * @param resourceFolder (required) usually webroot folder (so that e.g. webroot/resources/ can be accessed)
	 * @param initParams (optional) fake servlet init parameters to pass on to the users via the initParameterMap
	 */
	public StandaloneExternalContext(File resourceFolder, Map<String, String> initParams) {
		this(new SingleFolderResourceLocator(resourceFolder), initParams);
	}

	// --------------------------------------------------------- Resources
	
	@Override
	public URL getResource(String path) throws MalformedURLException {
		return resourceLocator.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return resourceLocator.getResourceAsStream(path);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return resourceLocator.getResourcePaths(path);
	}

	// --------------------------------------------------------- Param maps

	@Override
	public Map<String, Object> getApplicationMap() {
		return applicationMap;
	}

	@Override
	public String getInitParameter(String name) {
		return initParameterMap.get(name);
	}

	@Override
	public Map getInitParameterMap() {
		return initParameterMap;
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return requestMap;
	}

	@Override
	public Map<String, Object> getSessionMap() {
		return sessionMap;
	}

	@Override
	public Map<String, String> getRequestParameterMap() {
		return Collections.emptyMap();
	}

	@Override
	public Iterator<String> getRequestParameterNames() {
		List<String> empty = Collections.emptyList();
		return empty.iterator();
	}

	@Override
	public Map<String, String[]> getRequestParameterValuesMap() {
		return Collections.emptyMap();
	}

	// --------------------------------------------------------- Unsupported


	@Override
	public Map<String, Object> getRequestCookieMap() {
		throw unsupportedException();
	}

	@Override
	public Map<String, String> getRequestHeaderMap() {
		throw unsupportedException();
	}

	@Override
	public Map<String, String[]> getRequestHeaderValuesMap() {
		throw unsupportedException();
	}

	@Override
	public void dispatch(String path) throws IOException {
		throw unsupportedException();
	}

	@Override
	public String encodeActionURL(String url) {
		throw unsupportedException();
	}

	@Override
	public String encodeNamespace(String name) {
		throw unsupportedException();
	}

	@Override
	public String encodeResourceURL(String url) {
		throw unsupportedException();
	}

	@Override
	public String getAuthType() {
		throw unsupportedException();
	}

	@Override
	public Object getContext() {
		throw unsupportedException();
	}

	@Override
	public String getRemoteUser() {
		throw unsupportedException();
	}

	@Override
	public Object getRequest() {
		throw unsupportedException();
	}

	@Override
	public String getRequestContextPath() {
		throw unsupportedException();
	}

	@Override
	public Locale getRequestLocale() {
		throw unsupportedException();
	}

	@Override
	public Iterator<Locale> getRequestLocales() {
		throw unsupportedException();
	}

	@Override
	public String getRequestPathInfo() {
		throw unsupportedException();
	}

	@Override
	public String getRequestServletPath() {
		throw unsupportedException();
	}

	@Override
	public Object getResponse() {
		throw unsupportedException();
	}

	@Override
	public Object getSession(boolean create) {
		throw unsupportedException();
	}

	@Override
	public Principal getUserPrincipal() {
		throw unsupportedException();
	}

	@Override
	public boolean isUserInRole(String role) {
		throw unsupportedException();
	}

	@Override
	public void log(String message) {
		throw unsupportedException();
	}

	@Override
	public void log(String message, Throwable exception) {
		throw unsupportedException();
	}

	@Override
	public void redirect(String url) throws IOException {
		throw unsupportedException();
	}

	// --------------------------------------------------------- Necessary overrides

	@Override
	public String getMimeType(String file) {
		return "application/xhtml+xml"; // hard-coded for simplicity
	}


	// ---------------------------------------------------------

	private RuntimeException unsupportedException() {
		return new UnsupportedOperationException();
	}
}
