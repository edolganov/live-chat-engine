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
package och.chat.web.servlet.system_api.chat;

import static java.util.Collections.*;
import static och.api.model.user.SecurityContext.*;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.chat.NoChatAccountException;
import och.api.model.chat.ChatLog;
import och.chat.web.JsonPostServlet;
import och.chat.web.model.ChatLogFullResp;
import och.comp.web.annotation.RoleSecured;

@RoleSecured
@WebServlet("/system-api/chat/state")
@SuppressWarnings("serial")
public class AccsState extends JsonPostServlet<AccsStateReq, AccsStateResp> {
	
	
	@Override
	protected AccsStateResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, 
			AccsStateReq data) throws Throwable {
		
		long operatorId = findUserIdFromSecurityContext();
		
		AccsStateResp out = new AccsStateResp();
		
		for (String accId : data.uids) {
			Collection<ChatLog> logs = null;
			try {
				logs = chats.getAllActiveChatLogs(accId);
			}catch (NoChatAccountException e) {
				log.warn("can't find account by id: "+accId);
				logs = emptyList();
			}
			ArrayList<ChatLogFullResp> list = new ArrayList<>();
			for(ChatLog log : logs){
				list.add(new ChatLogFullResp(log));
			}
			
			out.putChats(accId, list);
			out.putOpStatus(accId, chats.isActiveOperator(accId, operatorId));
			out.putActiveCount(accId, chats.getActiveOperatorsCount(accId));
		}
		
		return out;
	}

}
