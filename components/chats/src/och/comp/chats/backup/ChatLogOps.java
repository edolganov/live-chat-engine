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
package och.comp.chats.backup;

import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.json.GsonUtil.*;

import java.util.List;

import och.api.model.chat.ChatOperator;
import och.api.model.chat.Message;
import och.api.model.client.ClientSession;
import och.comp.chats.model.Chat;
import och.comp.chats.model.Chat.AddClientCommentRes;

import org.apache.commons.logging.Log;

public class ChatLogOps {
	
	private static final Log log = getLog(ChatLogOps.class);
	
	public static final String START = "#START=";
	public static final String CL_MSG = "#CL_MSG=";
	public static final String OP = "#OP=";
	public static final String OP_MSG = "#OP_MSG=";
	public static final String LINE_SEP = "\n";
	

	public static String chatLine(ClientSession client, Chat chat) {
		String out = defaultGson.toJson(new CreateChatLog(chat.id, chat.created, client.info));
		return START + out + LINE_SEP;
	}
	
	public static String addClientMsgLine(AddClientCommentRes result) {
		String out = defaultGson.toJson(result);
		return CL_MSG + out + LINE_SEP;
	}
	
	public static String addOperatorLine(ChatOperator operator) {
		String out = defaultGson.toJson(operator);
		return OP + out + LINE_SEP;
	}
	
	public static String addOperatorMsgLine(Message result) {
		String out = defaultGson.toJson(result);
		return OP_MSG + out + LINE_SEP;
	}
	
	public static Chat restoreChatWithSavedSession(String data) {
		return restoreChat(data, false, null);
	}
	
	public static Chat restoreChatForSession(String data, ClientSession client) {
		if(client == null) return null;
		return restoreChat(data, true, client);
	}
	
	private static Chat restoreChat(String data, boolean checkSession, ClientSession client) {
		
		if( ! hasText(data)) return null;
		if(checkSession && client == null) return null;
		
		try {
			List<String> lines = strToList(data, LINE_SEP);
			if(isEmpty(lines)) return null;
			if(lines.size() == 1) return null;
			
			String chatLine = lines.get(0);
			if( ! hasText(chatLine)) return null;
			if( ! chatLine.startsWith(START)) return null;
			
			CreateChatLog chatInfo = defaultGson.fromJson(chatLine.substring(START.length()), CreateChatLog.class);
			if(chatInfo == null || chatInfo.clientInfo == null) return null;
			if( checkSession && ! client.info.equals(chatInfo.clientInfo)) return null;
			
			if(client == null){
				client = new ClientSession(randomSimpleId(), chatInfo.clientInfo);
			}
			
			Chat chat = new Chat(chatInfo.id, chatInfo.created, client, null);
			for (int i = 1; i < lines.size(); i++) {
				String line = lines.get(i);
				if( ! hasText(line)) continue;
				
				try {
					if( line.startsWith(CL_MSG)) addClientMsg(chat, client, line.substring(CL_MSG.length()));
					else if(line.startsWith(OP)) addOp(chat, line.substring(OP.length()));
					else if(line.startsWith(OP_MSG)) addOpMsg(chat, line.substring(OP_MSG.length()));
				}catch(Exception e){
					log.warn("exception while add log line to chat "+chatInfo.id+": "+e);
					continue;
				}
			}
			
			return chat;
			
		}catch(Exception e){
			return null;
		}
	}


	private static void addClientMsg(Chat chat, ClientSession client, String data) {
		if( ! hasText(data)) return;
		
		AddClientCommentRes addInfo = defaultGson.fromJson(data, AddClientCommentRes.class);
		if(addInfo == null) return;
		
		chat.addMsg(addInfo.msg, addInfo.ref);
	}
	
	private static void addOp(Chat chat, String data) {
		if( ! hasText(data)) return;
		
		ChatOperator op = defaultGson.fromJson(data, ChatOperator.class);
		if(op == null) return;
		
		chat.addOperator(op);
	}
	
	private static void addOpMsg(Chat chat, String data) {
		if( ! hasText(data)) return;
		
		Message msg = defaultGson.fromJson(data, Message.class);
		if(msg == null) return;
		
		chat.addMsg(msg, null);
	}


}
