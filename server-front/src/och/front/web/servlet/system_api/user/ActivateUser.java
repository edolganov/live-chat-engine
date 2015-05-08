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
package och.front.web.servlet.system_api.user;

import static och.util.ExceptionUtil.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.user.User;
import och.front.web.SimpleFrontServlet;

@WebServlet("/system-api/user/activate")
@SuppressWarnings("serial")
public class ActivateUser extends SimpleFrontServlet {
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String email = req.getParameter("email");
		String code = req.getParameter("code");
		
		if(isEmpty(email) || isEmpty(code)) return;
		
		req.setAttribute("userEmail", email);
		
		try {			
			
			app.users.activateUser(email, code);
			
			
			//add start bonus
			try {
				User user = app.users.getUserByLoginOrEmail(email);
				if(user != null){
					app.billing.addStartBonus(user.id);			
				}
			}catch(Throwable t){
				log.error("can't add start bonus: "+t);
			}
			
			forward(req, resp, "/WEB-INF/jsp/front/activate/activated.jsp");
			
		}catch (Exception e) {
			
			req.setAttribute("errorMsg", getMessageOrType(e));
			forward(req, resp, "/WEB-INF/jsp/front/activate/activation-failed.jsp");
		}
		
	}
	
	

}
