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

import java.util.Date;
import java.util.List;
import java.util.Map;

import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatUser;
import och.api.model.chat.Message;

public class ChatLogStore {
	
	public Date created;
	public Date ended;
	public List<ChatUser> users;
	public List<Message> messages;
	public Map<Integer, String> clientRefs;

	public ChatLogStore() {}

	public ChatLogStore(ChatLog chatLog, Date ended){
		this.users = chatLog.users;
		this.messages = chatLog.messages;
		this.clientRefs = chatLog.clientRefs;
		this.created = chatLog.created;
		this.ended = ended;
	}
}
