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
package och.chat.web.model;

import static java.lang.Boolean.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatUser;
import och.api.model.chat.Message;

public class ChatLogResp {
	
	public Integer fromIndex;
	public List<Message> messages;
	public List<ChatUser> users;
	public Map<Long, String> operators;
	public Boolean hasNewOperators;
	public Map<Integer, String> clientRefs;
	
	//extra
	public String id;
	
	protected ChatLogResp(){}
	
	public ChatLogResp(ChatLog chatLog){
		this(chatLog, true, true);
	}
	
	protected ChatLogResp(ChatLog chatLog, boolean replaceClientInfo, boolean clearClientRefs){
		this.fromIndex = chatLog.fromIndex;
		this.messages = chatLog.messages;
		this.hasNewOperators = chatLog.hasNewOperators;
		this.clientRefs = clearClientRefs? null : chatLog.clientRefs;
		
		//skip init data if no updates
		if( ! hasUpdates()) {
			return;
		}
		
		this.operators = chatLog.operators;
		
		//remove client info
		List<ChatUser> logUsers = chatLog.users;
		if(isEmpty(logUsers)) this.users = null;
		else {
			this.users = new ArrayList<>();
			for(ChatUser user : logUsers){
				if(user.isClient()) {
					String clientInfo = replaceClientInfo? "client" : user.userId;
					this.users.add(new ChatUser(clientInfo, null, null));
				}
				else this.users.add(user);
			}
		}
	}

	/**
	 * Имеются ли обновления в текущих данных
	 */
	public boolean hasUpdates() {
		return ! isEmpty(messages) || TRUE.equals(hasNewOperators);
	}

}
