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
import och.comp.chats.model.InitChatData;

@WebServlet("/api/chat/add")
public class AddMsg extends BaseApiPostServlet<AddMsgReq, AddMsgResp> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected AddMsgResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, AddMsgReq data) throws Throwable {
		
		String accountId = data.id;
		
		ClientSession clientSession = app.security.findClientSession(req);
		InitChatData initChat = chats.addComment(accountId, clientSession, data.text);
		ChatLog chatLog = chats.getChatLog(accountId, clientSession, data.updateData);
		
		ChatLogResp respLog = chatLog == null? null : new ChatLogResp(chatLog);
		
		//save id if new chat
		if(initChat.isNew){
			respLog.id = chatLog.id;
		}
		
		return new AddMsgResp(respLog);
	}

}
