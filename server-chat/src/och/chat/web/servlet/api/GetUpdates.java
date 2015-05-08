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

@WebServlet("/api/chat/updates")
public class GetUpdates extends BaseApiPostServlet<GetUpdatesReq, GetUpdatesResp>{
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected GetUpdatesResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, GetUpdatesReq data) throws Throwable {
		
		ClientSession clientSession = security.findClientSession(req);
		
		String accountId = data.id;
		ChatLog chatLog = chats.getChatLog(accountId, clientSession, data.updateData);
		
		//ужимает ответ, если нет обновлений
		if(data.compact){
			if(chatLog == null || ! chatLog.hasUpdates()) return null;
		}
		
		ChatLogResp respLog = chatLog == null? null : new ChatLogResp(chatLog);
		return new GetUpdatesResp(respLog);
	}

}
