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
package och.chat.web.servlet.test;

import static och.api.model.user.SecurityContext.*;
import static och.util.Util.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ValidationException;
import och.api.model.chat.ChatOperator;
import och.chat.web.JsonGetServlet;

@SuppressWarnings("serial")
@WebServlet("/test/createActiveOperator")
public class CreateActiveOperator extends JsonGetServlet<Void> {
	
	@Override
	protected Void doJsonGet(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
		
			String accountId = req.getParameter("accountId");
			if( ! hasText(accountId)) throw new ValidationException("accountId is empty");
			
			ChatOperator operator = new ChatOperator(1, "Тестовый оператор");
			app.chats.putOperator(accountId, operator);
			app.chats.setActiveOperator(accountId, operator.id);
			
			return null;
		}finally {
			popUserFromSecurityContext();
		}
	}

}
