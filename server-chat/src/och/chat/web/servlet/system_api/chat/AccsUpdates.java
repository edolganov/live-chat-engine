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
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.chat.NoChatAccountException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatUpdateData;
import och.chat.web.JsonPostServlet;
import och.chat.web.model.ChatLogFullResp;
import och.chat.web.servlet.system_api.chat.AccsUpdatesReq.AccUpdateReq;
import och.comp.web.annotation.RoleSecured;

@RoleSecured
@WebServlet("/system-api/chat/updates")
@SuppressWarnings("serial")
public class AccsUpdates extends JsonPostServlet<AccsUpdatesReq, AccsStateResp> {
	
	
	@Override
	protected AccsStateResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, 
			AccsUpdatesReq data) throws Throwable {
		
		
		long operatorId = findUserIdFromSecurityContext();
		
		AccsStateResp out = new AccsStateResp();
		
		//обновление чатов
		for (Entry<String, AccUpdateReq> entry : data.updates.entrySet()) {
			
			String accId = entry.getKey();
			AccUpdateReq reqInfo = entry.getValue();
			
			if(isEmpty(accId)) continue;
			if(isEmpty(reqInfo)) continue;
			
			fillChats(accId, reqInfo, out, data.compact);
			fillOpActive(operatorId, accId, reqInfo, out);
			fillOpActiveCount(accId, reqInfo, out);
			
			
		}
		
		
		//сокращаем ответ, если нет обновлений
		if(data.compact && isEmpty(out.infoByAcc)){
			return null;
		}
		
		return out;
	}

	void fillChats(String accId, AccUpdateReq reqInfo, AccsStateResp out, boolean compact){
		
		Map<String, ChatUpdateData> fromIndexes = reqInfo.chats;
		if(fromIndexes == null) fromIndexes = emptyMap();
		
		Collection<ChatLog> logs = emptyList();
		try {
			logs = chats.getAllActiveChatLogs(accId, fromIndexes);
		}catch (NoChatAccountException e) {
		}
		
		if(logs == null) logs = emptyList();
		
		
		ArrayList<ChatLogFullResp> list = new ArrayList<>();
		for(ChatLog log : logs){
			
			ChatUpdateData updateData = fromIndexes.remove(log.id);
			boolean needShort = updateData != null && updateData.fromIndex > 0;
			
			boolean replaceClientInfo = needShort;
			ChatLogFullResp respLog = new ChatLogFullResp(log, replaceClientInfo);
			if(needShort)respLog.created = null;
			
			if(compact && ! respLog.hasUpdates()){
				continue;
			}
			
			list.add(respLog);
		}
		
		if(fromIndexes.size() > 0){
			for (String deletedChatId : fromIndexes.keySet()) {
				list.add(ChatLogFullResp.createClosedChat(deletedChatId));
			}
		}
		
		if(list.size() > 0) out.putChats(accId, list);
		
	}
	
	void fillOpActive(long operatorId, String accId, AccUpdateReq reqInfo, AccsStateResp out){
			
		Boolean oldVal = reqInfo.opActive;
		if(oldVal == null) return;
		
		boolean newVal = chats.isActiveOperator(accId, operatorId);
		if(oldVal != newVal){
			out.putOpStatus(accId, newVal);
		}
		
	}
	
	private void fillOpActiveCount(String accId, AccUpdateReq reqInfo, AccsStateResp out) {
		
		Integer oldVal = reqInfo.activeCount;
		if(oldVal == null) return;
		
		int newVal = chats.getActiveOperatorsCount(accId);
		if(oldVal != newVal){
			out.putActiveCount(accId, newVal);
		}
		
	}

}
