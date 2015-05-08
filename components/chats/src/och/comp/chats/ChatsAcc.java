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
package och.comp.chats;

import static java.util.Collections.*;
import static och.comp.chats.ChatsAccListenerStub.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import och.api.exception.chat.NoAvailableOperatorException;
import och.api.exception.chat.NoChatException;
import och.api.exception.chat.NoPreviousOperatorPositionException;
import och.api.exception.chat.NotActiveOperatorException;
import och.api.exception.chat.OperatorPositionAlreadyExistsException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatOperator;
import och.api.model.chat.ChatUpdateData;
import och.api.model.chat.ChatUser;
import och.api.model.chat.Message;
import och.api.model.chat.config.AccConfig;
import och.api.model.chat.config.AccConfigRead;
import och.api.model.chat.config.Key;
import och.api.model.client.ClientSession;
import och.comp.chats.model.Chat;
import och.comp.chats.model.Chat.AddClientCommentRes;
import och.comp.chats.model.InitChatData;

public class ChatsAcc implements AccConfigRead {
	
	private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
	private Lock read = rw.readLock();
	private Lock write = rw.writeLock();
	
	//model
	private HashMap<Long, ChatOperator> operatorsById = new HashMap<>();
	private HashSet<Long> activeOperators = new HashSet<>();
	
	private HashMap<String, Chat> chatsById = new HashMap<>();
	private HashMap<String, Chat> chatsByClientSession = new HashMap<>();
	private HashMap<Long, Set<Chat>> chatsByOperator = new HashMap<>();
	
	private AccConfig config;
	
	//external
	public Date lastScannedToArc;
	private String id;
	private ChatsAccListener chatsListener;
	
	public ChatsAcc() {
		this(null, null, null, null);
	}
	
	public ChatsAcc(String id, Collection<ChatOperator> initOperators, AccConfig config, ChatsAccListener chatsListener) {
		this.id = id;
		this.chatsListener = chatsListener == null? STUB_INSTANCE : chatsListener;
		this.config = config != null? config : new AccConfig();
		
		if( ! isEmpty(initOperators)){
			for (ChatOperator operator : initOperators) {
				operatorsById.put(operator.id, operator);
			}
		}
		
		
	}
	
	public String getId(){
		return id;
	}
	
	public void putConfig(Key key, Object val){
		
		AccConfig state;
		
		write.lock();
		try {
			config.putVal(key, val);
			state = config.clone();
		}finally {
			write.unlock();
		}
		
		chatsListener.onConfigSetted(id, state);
	}
	
	@Override
	public String getStrVal(Key key) {
		read.lock();
		try {
			return config.getStrVal(key);
		}finally {
			read.unlock();
		}
	}

	@Override
	public Boolean getBoolVal(Key key) {
		read.lock();
		try {
			return config.getBoolVal(key);
		}finally {
			read.unlock();
		}
	}

	@Override
	public Integer getIntVal(Key key) {
		read.lock();
		try {
			return config.getIntVal(key);
		}finally {
			read.unlock();
		}
	}
	
	
	public void putOperator(ChatOperator operator){
		
		if(operator == null) return;
		operator = operator.clone();
		
		List<ChatOperator> newState;
		
		write.lock();
		try {
			
			//if new data is not full - try to restore full data from old
			ChatOperator old = operatorsById.get(operator.id);
			if(old != null){
				if( ! hasText(operator.email)) operator.email = old.email;
			}

			
			operatorsById.put(operator.id, operator);
			newState = getOperatorsState();
		}finally {
			write.unlock();
		}
		
		chatsListener.onOperatorsUpdate(id, newState);
	}
	
	public void removeOperator(long operatorId){
		
		List<ChatOperator> newState;
		
		write.lock();
		try {
			operatorsById.remove(operatorId);
			activeOperators.remove(operatorId);
			chatsByOperator.remove(operatorId);
			newState = getOperatorsState();
		}finally {
			write.unlock();
		}
		
		chatsListener.onOperatorsUpdate(id, newState);
	}
	

	public boolean setActiveOperator(long operatorId){
		write.lock();
		try {
			if( ! operatorsById.containsKey(operatorId)) return false;
			activeOperators.add(operatorId);
			return true;
		} finally {
			write.unlock();
		}
	}
	
	public boolean isOperator(long opId){
		read.lock();
		try {
			return operatorsById.containsKey(opId);
		} finally {
			read.unlock();
		}
	}
	
	public boolean isActiveOperator(long operatorId){
		read.lock();
		try {
			return getActiveOperatorUnsafe(operatorId) != null;
		} finally {
			read.unlock();
		}
	}
	
	private ChatOperator getActiveOperatorUnsafe(long operatorId){
		if(activeOperators.contains(operatorId)){
			return operatorsById.get(operatorId);
		}
		return null;
	}
	
	public boolean removeActiveOperator(long opId){
		write.lock();
		try {
			boolean done = activeOperators.remove(opId);
			chatsByOperator.remove(opId);
			return done;
		} finally {
			write.unlock();
		}
	}
	
	public void putOperatorAndSetActive(ChatOperator op){
		putOperator(op);
		setActiveOperator(op.id);
	}
	
	public int getActiveOperatorsCount(){
		read.lock();
		try {
			return activeOperators.size();
		}finally {
			read.unlock();
		}
	}
	
	public void checkHasActiveOperators() throws NoAvailableOperatorException {
		read.lock();
		try {
			checkHasOperators();
		} finally {
			read.unlock();
		}
	}
	
	public ChatOperator getOperator(long opId){
		read.lock();
		try {
			ChatOperator operator = operatorsById.get(opId);
			ChatOperator clone = operator == null? null : operator.clone();
			return clone;
		} finally {
			read.unlock();
		}
	}
	
	public List<ChatOperator> getRegistredOperators(){
		read.lock();
		try {
			ArrayList<ChatOperator> out = new ArrayList<>();
			for (ChatOperator op : operatorsById.values()) {
				ChatOperator clone = op.clone();
				out.add(clone);
			}
			return out;
		} finally {
			read.unlock();
		}
	}
	
	
	
	
	public InitChatData initActiveChat(ClientSession client) throws NoAvailableOperatorException {
		
		String sessionId = client.sessionId;
		boolean isNew = false;
		
		//read-write pattern
		read.lock();
		Chat chat = chatsByClientSession.get(sessionId);
		
		if(chat == null){
			read.unlock();
			write.lock();
			try {
				chat = chatsByClientSession.get(sessionId);
				if(chat == null){
					
					checkHasOperators();
					
					chatsListener.checkCanCreateChat(id, client);
					
					Chat newChat = new Chat(client, chatsListener);
					chatsById.put(newChat.id, newChat);
					chatsByClientSession.put(sessionId, newChat);
					chat = newChat;
					isNew = true;
					
					chatsListener.onChatCreated(id, newChat, client);
				}
				read.lock();
			}finally {
				write.unlock();
			}
		}
		
		try {
			checkHasOperators();
			return new InitChatData(chat.id, isNew);
		}finally {
			read.unlock();
		}
	}
	
	public ChatLog initAngGetActiveChat(ClientSession client) throws NoAvailableOperatorException {
		String chatId = initActiveChat(client).id;
		return getActiveChatById(chatId);
	}
	
	public ChatLog getActiveChat(ClientSession client){
		return getActiveChat(client, null);
	}
	
	public ChatLog getActiveChat(ClientSession client, ChatUpdateData updateData){
		
		Chat chat = null;
		String sessionId = client.sessionId;
		
		read.lock();
		try {
			chat = chatsByClientSession.get(sessionId);
		}finally{
			read.unlock();
		}
		
		return chat == null? null : chat.toLog(updateData);
	}
	
	public ChatLog getActiveChatById(String id){
		
		Chat chat = null;
		
		read.lock();
		try {
			chat = chatsById.get(id);
		}finally{
			read.unlock();
		}
		
		return chat == null? null : chat.toLog();
	}
	
	public ChatOperator addOperator(String chatId, long operatorId, int operatorIndex) throws NotActiveOperatorException, NoChatException, OperatorPositionAlreadyExistsException, NoPreviousOperatorPositionException {
		
		write.lock();
		try {
			
			ChatOperator operator = getActiveOperatorUnsafe(operatorId);
			if( operator == null) throw new NotActiveOperatorException(operatorId);
			
			Chat chat = chatsById.get(chatId);
			if(chat == null) throw new NoChatException();
			
			int operatorsCount = chat.getOperatorsCount();
			if(operatorIndex < operatorsCount) throw new OperatorPositionAlreadyExistsException(operatorsCount);
			if(operatorIndex > operatorsCount) throw new NoPreviousOperatorPositionException(operatorsCount);
			
			chat.addOperator(operator);
			putToSetMap(chatsByOperator, operator.id, chat);
			
			ChatOperator clone = operator.clone();
			chatsListener.onChatOperatorAdded(id, chat, operator);
			
			return clone;
			
		}finally{
			write.unlock();
		}
	}
	
	public boolean updateOperatorContact(long operatorId, String email) {
		
		List<ChatOperator> newState;
		
		write.lock();
		try {
			
			ChatOperator operator = operatorsById.get(operatorId);
			if(operator == null) return false;
			
			operator.email = email;
			newState = getOperatorsState();
		}finally{
			write.unlock();
		}
		
		chatsListener.onOperatorsUpdate(id, newState);
		
		return true;
	}

	
	
	public Collection<ChatLog> getActiveChatsByOperator(long operatorId){
		
		ArrayList<Chat> chats = null;
		
		read.lock();
		try {
			Set<Chat> set = chatsByOperator.get(operatorId);
			chats = set == null? null : new ArrayList<>(set);
		}finally{
			read.unlock();
		}
		
		if(chats == null) return null;
		ArrayList<ChatLog> out = new ArrayList<>();
		for(Chat chat : chats) out.add(chat.toLog());
		return out;
	}
	
	public Collection<ChatLog> getAllActiveChats(){
		return getAllActiveChats(null);
	}
	
	public Collection<ChatLog> getAllActiveChats(Map<String, ChatUpdateData> fromIndexes){
		
		if(isEmpty(fromIndexes)) fromIndexes = emptyMap();
		
		ArrayList<Chat> chats = null;
		
		read.lock();
		try {
			chats = new ArrayList<>(chatsById.values());
		}finally{
			read.unlock();
		}
		
		ArrayList<ChatLog> out = new ArrayList<>();
		for(Chat chat : chats) {
			ChatUpdateData updateData = fromIndexes.get(chat.id);
			out.add(chat.toLog(updateData));
		}
		return out;
	}
	
	public ChatLog getActiveChat(String chatId, ChatUpdateData updateData){
		
		Chat chat = null;
		
		read.lock();
		try {
			chat = chatsById.get(chatId);
		}finally{
			read.unlock();
		}
		
		if(chat == null) return null;
		return chat.toLog(updateData);
	}
	
	
	
	public String closeChat(ClientSession client){
		return closeChat(client.sessionId);
	}
	
	public String closeChat(String sessionId){
		
		Chat chat = null;
		
		write.lock();
		try {
			
			chat = chatsByClientSession.remove(sessionId);
			if(chat == null) return null;
			
			chatsById.remove(chat.id);
			
			for(Set<Chat> set : chatsByOperator.values()) set.remove(chat);
			
		}finally {
			write.unlock();
		}
		
		chatsListener.onChatClose(id, chat);
		
		return chat.id;
	}
	

	public void closeAllChats() {
		
		Set<String> allSessions;
		
		write.lock();
		try {
			allSessions = new HashSet<>(chatsByClientSession.keySet());
		}finally {
			write.unlock();
		}
		
		for (String sessionId : allSessions) {
			closeChat(sessionId);
		}
		
	}
	
	
	
	public boolean addComment(ClientSession client, String text){
		String sessionId = client.sessionId;
		Chat chat = null;
		
		read.lock();
		try {
			chat = chatsByClientSession.get(sessionId);
		}finally {
			read.unlock();
		}
		
		if(chat == null) return false;
		
		AddClientCommentRes result = chat.addComment(client, text);
		if(result != null) chatsListener.onChatClientMsgAdded(id, chat, result);
		return result != null;
	}
	
	
	public boolean addComment(String chatId, long operatorId, String text){
		
		Chat chat = null;
		ChatOperator operator = null;
		
		read.lock();
		try {
			chat = chatsById.get(chatId);
			operator = operatorsById.get(operatorId);
		}finally {
			read.unlock();
		}
		
		if(chat == null || operator == null) return false;
		
		Message result = chat.addComment(operator, text);
		if(result != null) chatsListener.onChatOperatorMsgAdded(id, chat, result);
		return result != null;
	}
	
	public <T extends ChatLog> Collection<T> fillOperators(Collection<T> col) {
		
		if(isEmpty(col)) return col;
		
		for (ChatLog log : col) {
			fillOperators(log);
		}
		
		return col;
	}
	
	
	public ChatLog fillOperators(ChatLog chatLog) {
		
		if(chatLog == null) return null;
		if( ! chatLog.hasUpdates()) return chatLog;
		
		read.lock();
		try {
			HashMap<Long, String> operators = new HashMap<>();
			List<ChatUser> users = chatLog.users;
			for (ChatUser user : users) {
				
				Long operatorId = user.operatorId;
				if(operatorId == null) continue;
				if(operators.containsKey(operatorId)) continue;
				
				ChatOperator operator = operatorsById.get(operatorId);
				if(operator == null) continue;
				
				operators.put(operatorId, operator.name);
				
			}
			
			if(operators.size() > 0){
				chatLog.operators = operators;
			}
			
			return chatLog;
		}finally {
			read.unlock();
		}
	}
	
	
	public boolean addChat(ClientSession client, Chat chat) {
		
		write.lock();
		try {
			
			if(chatsById.containsKey(chat.id)) return false;
			
			
			String sessionId = client.sessionId;
			Chat curChat = chatsByClientSession.get(sessionId);
			if(curChat != null) return false;
			
			chatsByClientSession.put(sessionId, chat);
			chatsById.put(chat.id, chat);
			
			List<ChatUser> ops = chat.getOperators();
			for(ChatUser op : ops){
				Long opId = op.operatorId;
				if( ! operatorsById.containsKey(opId)) continue;
				putToSetMap(chatsByOperator, opId, chat);
			}
			
			return true;
			
		}finally{
			write.unlock();
		}
		
	}
	
	public boolean closeUnknownChat(Chat chat){
		
		read.lock();
		try {
			if(chatsById.containsKey(chat.id)) 
				return false;
		}finally{
			read.unlock();
		}
		
		chatsListener.onChatClose(id, chat);
		
		return true;
	}
	
	
	
	private void checkHasOperators(){
		if(activeOperators.size() == 0) throw new NoAvailableOperatorException();
	}
	
	private List<ChatOperator> getOperatorsState(){
		ArrayList<ChatOperator> out = new ArrayList<>();
		Collection<ChatOperator> values = operatorsById.values();
		for (ChatOperator operator : values) {
			ChatOperator clone = operator.clone();
			out.add(clone);
		}
		return out;
	}




	

}
