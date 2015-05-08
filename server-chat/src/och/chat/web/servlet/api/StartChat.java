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
package och.chat.web.servlet.api;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.ChatLog;
import och.api.model.client.ClientSession;
import och.chat.web.model.ChatLogResp;

@WebServlet("/api/chat/start")
public class StartChat extends BaseApiPostServlet<StartChatReq, StartChatResp>{
	
	private static final long serialVersionUID = 1L;
	

	@Override
	protected StartChatResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, StartChatReq data) throws Throwable {
		
		String accId = data.id;
		String oldChatId = data.oldChatId;
		chats.checkChatAndInitClientSession(req, resp, accId, oldChatId);
		
		ClientSession clientSession = security.findClientSession(req);
		ChatLog chatLog = chats.getChatLog(accId, clientSession);
		
		if(chatLog == null) return new StartChatResp();
		
		ChatLogResp respLog = new ChatLogResp(chatLog);
		respLog.id = chatLog.id;
		return new StartChatResp(respLog);
	}

}
