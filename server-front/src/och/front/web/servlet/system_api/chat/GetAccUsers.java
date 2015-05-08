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

import static och.util.Util.*;

import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.user.User;
import och.api.model.user.UserResp;
import och.comp.web.annotation.RoleSecured;
import och.front.service.model.UserAccInfo;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/chat/users")
@SuppressWarnings("serial")
public class GetAccUsers extends JsonPostServlet<GetAccUsersReq, GetAccUsersResp> {
	
	@Override
	protected GetAccUsersResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, GetAccUsersReq data) throws Throwable {
		
		
		Map<Long, UserAccInfo> privsByUsers = chats.getAccUsers(data.accId);
		List<User> usersList = users.getUsersByIds(privsByUsers.keySet());
		
		List<UserResp> out = convert(usersList, (user) -> {
			UserAccInfo info = privsByUsers.get(user.id);
			return new UserResp(
					user, 
					"privs", info.privs,
					"nickname", info.nickname);
		});
		
		return new GetAccUsersResp(out);
	}
}
