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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import och.chat.web.model.ChatLogFullResp;

public class AccsStateResp {
	
	public static class AccInfo {
		public List<ChatLogFullResp> chats;
		public Boolean opActive;
		public Integer activeCount;
	}
	
	public Map<String, AccInfo> infoByAcc;
	
	public void putChats(String accId, List<ChatLogFullResp> chats){
		getOrCreate(accId).chats = chats;
	}
	
	public void putOpStatus(String accId, Boolean opActive){
		getOrCreate(accId).opActive = opActive;
	}
	
	public void putActiveCount(String accId, int activeCount){
		getOrCreate(accId).activeCount = activeCount;
	}
	

	private AccInfo getOrCreate(String accId) {
		
		if(infoByAcc == null) infoByAcc = new HashMap<>();
		
		AccInfo info = infoByAcc.get(accId);
		if(info == null) {
			info = new AccInfo();
			infoByAcc.put(accId, info);
		}
		return info;
	}

}
