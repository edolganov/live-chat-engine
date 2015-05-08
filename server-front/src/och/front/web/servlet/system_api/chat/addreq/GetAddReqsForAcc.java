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
package och.front.web.servlet.system_api.chat.addreq;

import static java.util.Collections.*;
import static och.util.Util.*;

import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.account.AccIdReq;
import och.api.model.chat.account.ChatAccountAddReq;
import och.api.model.user.User;
import och.api.model.user.UserResp;
import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;
import och.util.model.Pair;

@RoleSecured
@WebServlet("/system-api/chat/addReqsListForAcc")
@SuppressWarnings("serial")
public class GetAddReqsForAcc extends JsonPostServlet<AccIdReq, List<UserResp>> {
	
	@Override
	protected List<UserResp> doJsonPost(HttpServletRequest req, HttpServletResponse resp, AccIdReq data) throws Throwable {
		
		List<ChatAccountAddReq> reqs = chats.getReqsByAcc(data.accId);
		if(isEmpty(reqs)) return emptyList();
		
		Map<Long, ChatAccountAddReq> map = toMap(reqs, (i)-> 
			new Pair<>(i.userId, i)); 
		
		List<User> usersList = users.getUsersByIds(map.keySet());
		List<UserResp> out = convert(usersList, (user)-> 
			new UserResp(user, "reqDate", map.get(user.id).created));
		
		return out;
	}
}
