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

import static och.util.Util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import och.front.service.BillingService;
import och.front.service.ChatService;
import och.front.service.FrontApp;
import och.front.service.SecurityService;

import org.apache.commons.logging.Log;

@SuppressWarnings("serial")
public class SimpleFrontServlet extends HttpServlet {
	
	protected Log log = getLog(getClass());
	protected FrontApp app;
	protected SecurityService security;
	protected ChatService chats;
	protected BillingService billing;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		app = FrontAppProvider.get(getServletContext());
		security = app.security;
		chats = app.chats;
		billing = app.billing;
	}

}
