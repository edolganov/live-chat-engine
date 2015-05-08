package web;

/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.FileTypeMap;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mock implementation of the {@link javax.servlet.ServletContext} interface.
 *
 * <p>Used for testing the Spring web framework; only rarely necessary for testing
 * application controllers. As long as application components don't explicitly
 * access the ServletContext, ClassPathXmlApplicationContext or
 * FileSystemXmlApplicationContext can be used to load the context files for testing,
 * even for DispatcherServlet context definitions.
 *
 * <p>For setting up a full WebApplicationContext in a test environment, you can
 * use XmlWebApplicationContext (or GenericWebApplicationContext), passing in an
 * appropriate MockServletContext instance. You might want to configure your
 * MockServletContext with a FileSystemResourceLoader in that case, to make your
 * resource paths interpreted as relative file system locations.
 *
 * <p>A common setup is to point your JVM working directory to the root of your
 * web application directory, in combination with filesystem-based resource loading.
 * This allows to load the context files as used in the web application, with
 * relative paths getting interpreted correctly. Such a setup will work with both
 * FileSystemXmlApplicationContext (which will load straight from the file system)
 * and XmlWebApplicationContext with an underlying MockServletContext (as long as
 * the MockServletContext has been configured with a FileSystemResourceLoader).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see #MockServletContext(org.springframework.core.io.ResourceLoader)
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 * @see org.springframework.web.context.support.GenericWebApplicationContext
 * @see org.springframework.context.support.ClassPathXmlApplicationContext
 * @see org.springframework.context.support.FileSystemXmlApplicationContext
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MockServletContext implements ServletContext {

	private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";


	private final Log logger = LogFactory.getLog(getClass());

	private final String resourceBasePath;

	private String contextPath = "";

	private final Map contexts = new HashMap();

	private final Properties initParameters = new Properties();

	private final Hashtable attributes = new Hashtable();

	private String servletContextName = "MockServletContext";

	/**
	 * Create a new MockServletContext, using the specified ResourceLoader
	 * and no base path.
	 * @param resourceLoader the ResourceLoader to use (or null for the default)
	 */
	public MockServletContext() {
		this("");
	}

	/**
	 * Create a new MockServletContext.
	 * @param resourceBasePath the WAR root directory (should not end with a slash)
	 * @param resourceLoader the ResourceLoader to use (or null for the default)
	 */
	public MockServletContext(String resourceBasePath) {
		this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");

		// Use JVM temp dir as ServletContext temp dir.
		String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
		if (tempDir != null) {
			this.attributes.put("tempDir", new File(tempDir));
		}
	}


	/**
	 * Build a full resource location for the given path,
	 * prepending the resource base path of this MockServletContext.
	 * @param path the path as specified
	 * @return the full resource path
	 */
	protected String getResourceLocation(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return this.resourceBasePath + path;
	}


	public void setContextPath(String contextPath) {
		this.contextPath = (contextPath != null ? contextPath : "");
	}

	/* This is a Servlet API 2.5 method. */
	@Override
	public String getContextPath() {
		return this.contextPath;
	}

	public void registerContext(String contextPath, ServletContext context) {
		this.contexts.put(contextPath, context);
	}

	@Override
	public ServletContext getContext(String contextPath) {
		if (this.contextPath.equals(contextPath)) {
			return this;
		}
		return (ServletContext) this.contexts.get(contextPath);
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public int getMinorVersion() {
		return 5;
	}

	@Override
	public String getMimeType(String filePath) {
		return MimeTypeResolver.getMimeType(filePath);
	}

	@Override
	public Set getResourcePaths(String path) {
		return new HashSet<>();
//		String actualPath = (path.endsWith("/") ? path : path + "/");
//		Resource resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
//		try {
//			File file = resource.getFile();
//			String[] fileList = file.list();
//			if (ObjectUtils.isEmpty(fileList)) {
//				return null;
//			}
//			Set resourcePaths = new LinkedHashSet(fileList.length);
//			for (int i = 0; i < fileList.length; i++) {
//				String resultPath = actualPath + fileList[i];
//				if (resource.createRelative(fileList[i]).getFile().isDirectory()) {
//					resultPath += "/";
//				}
//				resourcePaths.add(resultPath);
//			}
//			return resourcePaths;
//		}
//		catch (IOException ex) {
//			logger.warn("Couldn't get resource paths for " + resource, ex);
//			return null;
//		}
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return null;
//		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
//		if (!resource.exists()) {
//			return null;
//		}
//		try {
//			return resource.getURL();
//		}
//		catch (MalformedURLException ex) {
//			throw ex;
//		}
//		catch (IOException ex) {
//			logger.warn("Couldn't get URL for " + resource, ex);
//			return null;
//		}
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return null;
//		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
//		if (!resource.exists()) {
//			return null;
//		}
//		try {
//			return resource.getInputStream();
//		}
//		catch (IOException ex) {
//			logger.warn("Couldn't open InputStream for " + resource, ex);
//			return null;
//		}
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
//		if (!path.startsWith("/")) {
//			throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
//		}
//		return new MockRequestDispatcher(path);
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String path) {
		return null;
	}

	@Override
	public Servlet getServlet(String name) {
		return null;
	}

	@Override
	public Enumeration getServlets() {
		return Collections.enumeration(Collections.EMPTY_SET);
	}

	@Override
	public Enumeration getServletNames() {
		return Collections.enumeration(Collections.EMPTY_SET);
	}

	@Override
	public void log(String message) {
		logger.info(message);
	}

	@Override
	public void log(Exception ex, String message) {
		logger.info(message, ex);
	}

	@Override
	public void log(String message, Throwable ex) {
		logger.info(message, ex);
	}

	@Override
	public String getRealPath(String path) {
		return contextPath != null? contextPath+path : path;
//		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
//		try {
//			return resource.getFile().getAbsolutePath();
//		}
//		catch (IOException ex) {
//			logger.warn("Couldn't determine real path of resource " + resource, ex);
//			return null;
//		}
	}

	@Override
	public String getServerInfo() {
		return "MockServletContext";
	}

	@Override
	public String getInitParameter(String name) {
		return this.initParameters.getProperty(name);
	}

	public void addInitParameter(String name, String value) {
		this.initParameters.setProperty(name, value);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return this.initParameters.keys();
	}

	@Override
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return this.attributes.keys();
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			this.attributes.remove(name);
		}
	}

	@Override
	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public void setServletContextName(String servletContextName) {
		this.servletContextName = servletContextName;
	}

	@Override
	public String getServletContextName() {
		return this.servletContextName;
	}


	/**
	 * Inner factory class used to just introduce a Java Activation Framework
	 * dependency when actually asked to resolve a MIME type.
	 */
	private static class MimeTypeResolver {

		public static String getMimeType(String filePath) {
			return FileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
		}
	}


	@Override
	public int getEffectiveMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEffectiveMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(String servletName,
			Class<? extends Servlet> servletClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Class<? extends Filter> filterClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSessionTrackingModes(
			Set<SessionTrackingMode> sessionTrackingModes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(String className) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void declareRoles(String... roleNames) {
		// TODO Auto-generated method stub
		
	}

}
