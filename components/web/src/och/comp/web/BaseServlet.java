/*
 * Copyright 2015 Evgeny Dolganov (evgenij.dolganov@gmail.com).
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
package och.comp.web;

import static och.api.model.user.SecurityContext.*;
import static och.util.StreamUtil.*;
import static och.util.Util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.user.User;
import och.api.model.user.UserRole;
import och.comp.web.annotation.ClientSecured;
import och.comp.web.annotation.RoleSecured;

import org.apache.commons.logging.Log;


public abstract class BaseServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static interface WebSecurityProvider {
		
		User getUserFromSession(HttpServletRequest req);
		
		boolean hasClientSession(HttpServletRequest req);
		
	}
	
	
	protected final Log log = getLog(getClass());
	
	public final boolean securedByRole;
	public final boolean securedByClient;
	public final UserRole[] accessRoles;
	public WebSecurityProvider securityProvider;
	
	
	public BaseServlet() {
		
		RoleSecured sec = getClass().getAnnotation(RoleSecured.class);
		if(sec != null){
			securedByRole = true;
			UserRole[] roles = sec.value();
			accessRoles = isEmpty(roles)? null : roles;
		} else {
			securedByRole = false;
			accessRoles = null;
		}
		
		ClientSecured clientSec = getClass().getAnnotation(ClientSecured.class);
		securedByClient = clientSec != null;
	}
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		baseInit(config);
		securityProvider = getSecurityProvider();
	}
	
	protected abstract void baseInit(ServletConfig config) throws ServletException;
	
	protected abstract WebSecurityProvider getSecurityProvider() throws ServletException;
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		beforeService(req, resp);
		
		
		//user session
		if(securedByRole && securityProvider != null){
			
			User user = securityProvider.getUserFromSession(req);
			if(user == null){
				accessDenied(req, resp);
				return;
			}
			
			//roles
			if(accessRoles != null && !hasAccessFor(accessRoles)){
				accessDenied(req, resp);
				return;
			}
		}	
			
		//anonymous client's session
		if(securedByClient && securityProvider != null){
			if( ! securityProvider.hasClientSession(req)){
				accessDenied(req, resp);
				return;
			}
		}
		
		
		super.service(req, resp);
	}
	
	protected void beforeService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {}
	
	protected void accessDenied(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().println("GET is unsupported");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().println("POST is unsupported");
	}
	
	protected void printStream(InputStream in, OutputStream out) throws IOException {
		try {
			copy(in, out, 4096, false, null);
		}finally {
			try {
				in.close();
			}catch (Throwable t) {
				log.error("can't close InputStream "+in, t);
			}
		}
	}

}
