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
package och.api.model.chat;

import static java.lang.Boolean.*;
import static java.util.Collections.*;
import static och.util.Util.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatLog {
	
	public String id;
	public Date created;
	public List<ChatUser> users;
	public List<Message> messages;
	public int fromIndex;
	public Boolean hasNewOperators;
	public Map<Integer, String> clientRefs;
	
	//extra data
	public Map<Long, String> operators = emptyMap();
	
	public ChatLog(String id, Date created, 
			List<ChatUser> users, 
			List<Message> messages,
			Map<Integer, String> clientRefs,
			int fromIndex, 
			Boolean hasNewOperators) {
		this.created = created;
		this.id = id;
		this.users = users;
		this.messages = messages;
		this.clientRefs = clientRefs;
		this.fromIndex = fromIndex;
		this.hasNewOperators = hasNewOperators;
	}

	public int getOperatorsCount() {
		return isEmpty(users)? 0 : users.size()-1;
	}

	public boolean hasOperator(long operatorId) {
		if(isEmpty(users)) return false;
		for(ChatUser user : users){
			if(user.operatorId != null && user.operatorId.longValue() == operatorId){
				return true;
			}
		}
		return false;
	}
	
	public int getMsgCount(){
		if(isEmpty(messages)) return 0;
		return messages.size();
	}
	
	/**
	 * �?меются ли обновления в текущих данных
	 */
	public boolean hasUpdates() {
		return ! isEmpty(messages) || TRUE.equals(hasNewOperators);
	}

	public static int compareByDateAsc(ChatLog a, ChatLog b){
		return a.created.compareTo(b.created);
	}
	

}
