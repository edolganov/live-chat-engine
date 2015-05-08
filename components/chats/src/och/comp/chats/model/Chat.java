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
package och.comp.chats.model;

import static java.util.Collections.*;
import static och.api.model.web.ReqInfo.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatOperator;
import och.api.model.chat.ChatUpdateData;
import och.api.model.chat.ChatUser;
import och.api.model.chat.Message;
import och.api.model.client.ClientSession;
import och.api.model.web.ReqInfo;
import och.util.model.Pair;

public class Chat {
	
	public final Date created;
	public final String id;
	
	private final List<ChatUser> users = new ArrayList<>();
	private final List<Message> messages = new ArrayList<>();
	private List<Pair<Integer, String>> clientRefs;
	
	public ChatValidatorListener listener;
	
	
	public Chat(ClientSession client){
		this(null, null, client, null);
	}
	
	public Chat(ClientSession client, ChatValidatorListener listener){
		this(null, null, client, listener);
	}
	
	public Chat(String id, Date created, ClientSession client, ChatValidatorListener listener){
		
		this.id = id == null? randomSimpleId() : id;
		this.created = created == null? new Date() : created;
		this.listener = listener;
		
		users.add(new ChatUser(client));
		
	}
	
	public synchronized void addOperator(ChatOperator operator){
		if(findIndex(operator) > -1) return;
		users.add(new ChatUser(operator));
	}
	
	public synchronized List<ChatUser> getOperators(){
		ArrayList<ChatUser> out = new ArrayList<>();
		for(ChatUser user : users){
			if( ! user.isClient()) out.add(user);
		}
		return out;
	}
	
	public int getOperatorsCount(){
		return users.size() - 1;
	}
	
	public Long getOperatorId(int index){
		if(index >= users.size()) return null;
		return users.get(index).operatorId;
	}
	
	
	public static class AddClientCommentRes {
		public final Message msg;
		public final String ref;
		public AddClientCommentRes(Message msg, String ref) {
			super();
			this.msg = msg;
			this.ref = ref;
		}
	}
	
	public synchronized AddClientCommentRes addComment(ClientSession client, String text) {
		return addComment(client, text, null);
	}
	
	public synchronized AddClientCommentRes addComment(ClientSession client, String text, String ref) {
		int index = findIndex(client);
		if(index < 0) return null;
		
		if(listener != null) listener.beforeAddClientMsg(id, index, messages, text);
		
		Message msg = new Message(index, text);
		messages.add(msg);
		
		//update ref if need
		if(ref == null){
			ReqInfo reqInfo = getReqInfo();
			ref = reqInfo == null? null : reqInfo.getFinalRef();
		}
		boolean refUpdated = updateClientRefs(messages.size() - 1, ref);
		if( ! refUpdated) ref = null;
		
		return new AddClientCommentRes(msg, ref);
	}
	
	private boolean updateClientRefs(int msgIndex, String ref) {
		
		if( ! hasText(ref)) return false;
		
		if(isEmpty(clientRefs) || ! lastFrom(clientRefs).second.equals(ref)) {
			if(clientRefs == null) clientRefs = new ArrayList<>();
			clientRefs.add(new Pair<>(msgIndex, ref));
			return true;
		}
		return false;
	}

	public synchronized Message addComment(ChatOperator operator, String text) {
		
		if( ! hasText(text)) return null;
		
		int index = findIndex(operator);
		if(index < 0) return null;
		
		if(listener != null) {
			listener.beforeAddOperatorMsg(id, index, messages, text);
		}
		
		Message msg = new Message(index, text);
		messages.add(msg);
		return msg;
	}
	
	public synchronized void addMsg(Message msg, String ref){
		
		if(msg == null) return;
		if(msg.text == null) return;
		if(msg.date == null) return;
		if(msg.userIndex < 0 || msg.userIndex >= users.size()) return;
		
		messages.add(msg);
		
		if( ! hasText(ref)) return;
		if(clientRefs == null) clientRefs = new ArrayList<>();
		clientRefs.add(new Pair<>(messages.size() - 1, ref));
	}
	
	public synchronized ChatLog toLog() {
		return toLog(null);
	}

	public synchronized ChatLog toLog(ChatUpdateData updateData) {
		
		if(updateData == null) updateData = new ChatUpdateData();
		int fromIndex = updateData.fromIndex;
		
		
		Boolean hasNewOperators = null;
		if(updateData.usersCount > 0 && users.size() > updateData.usersCount){
			hasNewOperators = true;
		}
		
		
		List<Message> m = null;
		Map<Integer, String> refs = null;
		if(isEmpty(messages) || fromIndex >= messages.size()){
			m = emptyList();
		} else {
			m = new ArrayList<>(subList(messages, fromIndex));
			refs = getRefsMap(fromIndex);
		}
		List<ChatUser> u = new ArrayList<>(users);
		
		
		
		return new ChatLog(id, created, u, m, refs, fromIndex, hasNewOperators);
	}
	
	private Map<Integer, String> getRefsMap(int fromIndex) {
		
		if(isEmpty(clientRefs)) return null;
		
		Map<Integer, String> out = null;
		for (int i = clientRefs.size()-1; i > -1; i--) {
			Pair<Integer, String> pair = clientRefs.get(i);
			if(pair.first < fromIndex) break;
			
			if(out == null) out = new HashMap<>();
			out.put(pair.first, pair.second);
		}
		return out;
	}

	private int findIndex(ClientSession client) {
		String userId = client.getUserId();
		int size = users.size();
		for (int i = 0; i < size; i++) {
			if(userId.equals(users.get(i).userId)) return i;
		}
		return -1;
	}
	
	private int findIndex(ChatOperator operator) {
		long id = operator.id;
		int size = users.size();
		Long curOpId;
		for (int i = 0; i < size; i++) {
			curOpId = users.get(i).operatorId;
			if(curOpId != null 
				&& curOpId.longValue() == id) return i;
		}
		return -1;
	}
	
	

	@Override
	public String toString() {
		return "Chat [id=" + id + ", created=" + created + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chat other = (Chat) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	

}
