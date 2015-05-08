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

import java.util.Date;

import och.api.model.client.ClientInfo;

public class CreateChatLog {
	
	public String id;
	public Date created;
	public ClientInfo clientInfo;
	
	public CreateChatLog() {
		super();
	}

	public CreateChatLog(String id, Date created, ClientInfo clientInfo) {
		this.id = id;
		this.created = created;
		this.clientInfo = clientInfo;
	}
	
	

}
