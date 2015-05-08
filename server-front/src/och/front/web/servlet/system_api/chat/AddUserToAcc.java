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
package och.front.web.servlet.system_api.chat;

import static och.api.model.chat.account.PrivilegeType.*;

import java.util.HashSet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.user.UserNotFoundException;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.user.User;
import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/chat/addUser")
@SuppressWarnings("serial")
public class AddUserToAcc extends JsonPostServlet<AddUserToAccReq, Void> {
	
	@Override
	protected Void doJsonPost(HttpServletRequest req, HttpServletResponse resp, AddUserToAccReq data) throws Throwable {
		
		User user = users.getUserByLoginOrEmail(data.login);
		if(user == null) throw new UserNotFoundException();
		
		HashSet<PrivilegeType> privs = new HashSet<PrivilegeType>();
		if(data.isModerator) privs.add(CHAT_MODER);
		if(data.isOperator) privs.add(CHAT_OPERATOR);
		
		chats.addUserPrivileges(data.accId, user.id, privs);
		
		return null;
	}
}
