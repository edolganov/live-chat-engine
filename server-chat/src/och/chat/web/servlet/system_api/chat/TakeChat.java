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

import static och.api.model.user.SecurityContext.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.chat.OperatorPositionAlreadyExistsException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatUpdateData;
import och.chat.web.JsonPostServlet;
import och.chat.web.model.ChatLogFullResp;
import och.comp.web.annotation.RoleSecured;

@RoleSecured
@WebServlet("/system-api/chat/take")
@SuppressWarnings("serial")
public class TakeChat extends JsonPostServlet<TakeChatReq, TakeChatResp> {
	
	
	@Override
	protected TakeChatResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, 
			TakeChatReq data) throws Throwable {
		
		long userId = findUserIdFromSecurityContext();
		
		ChatUpdateData state = data.state;
		TakeChatResp out = new TakeChatResp();
		try {
			
			chats.addOperatorToChat(data.accId, data.chatId, userId, state.usersCount - 1);
			
		}catch (OperatorPositionAlreadyExistsException e) {
			out.concurrentError = true;
		}
		
		ChatLog chatLog = chats.getActiveChatLog(data.accId, data.chatId, state);
		out.chatLog = chatLog == null? null : new ChatLogFullResp(chatLog, true);
		return out;
		
	}

}
