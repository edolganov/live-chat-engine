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
package och.front.web.filter;

import static och.api.model.user.SecurityContext.*;
import static och.api.model.web.ReqInfo.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ExpectedException;
import och.api.model.user.User;
import och.front.service.FrontApp;
import och.front.service.SecurityService;
import och.front.web.FrontAppProvider;
import och.util.servlet.BaseFilter;

import org.apache.commons.logging.Log;


@WebFilter(
		urlPatterns={
			"/enter",
			"/enter/*",
			"/cabinet", 
			"/cabinet/*",
			"/system-api",
			"/system-api/*",
			"/payment/",
			"/payment/*"
		}
)
public class SecurityFilter extends BaseFilter {
	
	Log log = getLog(getClass());
	
	FrontApp app;
	SecurityService security;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		app = FrontAppProvider.get(filterConfig.getServletContext());
		security = app.security;
		
	}

	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		
		
		try {
			User restored = security.restoreUserSession(req, resp);
			if(restored != null){
				set_CSRF_ProtectTokenCookieFromSession(req, resp);
			}
		}catch (Exception e) {
			ExpectedException.logError(log, e, "can't restoreUserSession");
			chain.doFilter(req, resp);
			return;
		}
		
		
		User user = security.getUserFromSession(req);
		
		//no user session
		if(user == null){
			putInfoToThreadLocal(req);
			try {
				chain.doFilter(req, resp);
			}finally {
				removeInfoFromThreadLocal();
			}
			return;
		}
		
		//with user session
		req.setAttribute("user", user);
		pushToSecurityContext(user);
		putInfoToThreadLocal(req);
		try {
			chain.doFilter(req, resp);
		}finally {
			popUserFromSecurityContext();
			removeInfoFromThreadLocal();
		}
		
	}

	@Override
	public void destroy() {}

}
