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

import static och.api.model.user.SecurityContext.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/chat/setNickname")
@SuppressWarnings("serial")
public class SetAccUserNickname extends JsonPostServlet<SetAccUserNicknameReq, Void> {
	
	@Override
	protected Void doJsonPost(HttpServletRequest req, HttpServletResponse resp, SetAccUserNicknameReq data) throws Throwable {
		
		long userId = findUserIdFromSecurityContext();
		if(userId == data.userId){
			chats.setNickname(data.accId, data.nick);
		} else {
			chats.setNickname(data.accId, data.userId, data.nick);
		}
		
		return null;
	}
}
