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

import static och.util.Util.*;

import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.EmptyReq;
import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.ChatAccountResp;
import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/chat/addReqsList")
@SuppressWarnings("serial")
public class GetUserReqs extends JsonPostServlet<EmptyReq, List<ChatAccountResp>> {
	
	public GetUserReqs() {
		this.checkInputDataForEmpty = false;
	}
	
	@Override
	protected List<ChatAccountResp> doJsonPost(HttpServletRequest req, HttpServletResponse resp, EmptyReq data) throws Throwable {
		
		List<ChatAccount> list = chats.getAccsWithUserReqs();
		List<ChatAccountResp> out = convert(list, (acc)-> {
			ChatAccountResp accResp = new ChatAccountResp(acc);
			if(acc.params != null){
				accResp.params.putAll(acc.params);
			}
			return accResp;
		});
		
		return out;
	}
}
