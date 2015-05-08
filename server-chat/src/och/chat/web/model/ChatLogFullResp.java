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

import java.util.Date;

import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatLogHist;

public class ChatLogFullResp extends ChatLogResp {
	
	public Date created;
	public Date ended;
	public boolean closed;
	
	public ChatLogFullResp(ChatLog chatLog){
		this(chatLog, false);
	}
	
	public ChatLogFullResp(ChatLogHist histLog){
		this(histLog, false);
		this.ended = histLog.ended;
	}
	

	public ChatLogFullResp(ChatLog chatLog, boolean replaceClientInfo) {
		super(chatLog, replaceClientInfo, false);
		this.id = chatLog.id;
		this.created = chatLog.created;
	}
	
	private ChatLogFullResp(){
		super();
	}
	
	public static ChatLogFullResp createClosedChat(String id){
		ChatLogFullResp out = new ChatLogFullResp();
		out.id = id;
		out.closed = true;
		return out;
	}

}
