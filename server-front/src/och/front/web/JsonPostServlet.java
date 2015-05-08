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
package och.front.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import och.api.model.BaseBean;
import och.comp.web.BaseJsonPostServlet;
import och.front.service.BillingService;
import och.front.service.ChatService;
import och.front.service.FrontApp;
import och.front.service.SecurityService;
import och.front.service.UserService;

@SuppressWarnings("serial")
public abstract class JsonPostServlet<I extends BaseBean, O> extends BaseJsonPostServlet<I, O>{
	
	protected FrontApp app;
	protected ChatService chats;
	protected SecurityService security;
	protected UserService users;
	protected BillingService billing;
	
	@Override
	protected void baseInit(ServletConfig config) throws ServletException {
		app = FrontAppProvider.get(getServletContext());
		chats = app.chats;
		security = app.security;
		users = app.users;
		billing = app.billing;
	}
	
	@Override
	protected WebSecurityProvider getSecurityProvider() {
		return app.security;
	}
	
}
