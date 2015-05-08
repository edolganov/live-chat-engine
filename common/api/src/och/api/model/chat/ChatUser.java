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

import och.api.model.client.ClientSession;

public class ChatUser {
	
	public Long operatorId;
	public String userId;
	public String userName;
	public String userEmail;
	
	public ChatUser() {
		super();
	}
	
	public ChatUser(ChatOperator operator){
		this(operator.id);
	}
	public ChatUser(Long operatorId) {
		this.operatorId = operatorId;
		this.userId = null;
		this.userEmail = null;
		this.userName = null;
	}
	
	
	
	public ChatUser(ClientSession client){
		this(
			client.getUserId(), 
			client.info.email, 
			client.info.name);
	}
	
	public ChatUser(String userId, String userEmail, String userName) {
		this.operatorId = null;
		this.userId = userId;
		this.userEmail = userEmail;
		this.userName = userName;
	}
	
	
	
	public boolean isClient() {
		return userId != null;
	}

}
