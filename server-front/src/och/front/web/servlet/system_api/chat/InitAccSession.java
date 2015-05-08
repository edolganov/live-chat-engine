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
package och.front.web.servlet.system_api.chat;

import static och.util.Util.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.chat.OutdatedChatAccountsException;
import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/chat/initAccSession")
@SuppressWarnings("serial")
public class InitAccSession extends JsonPostServlet<InitAccSessionReq, String> {
	
	public InitAccSession() {
		this.checkInputDataForEmpty = false;
	}
	
	
	@Override
	protected String doJsonPost(HttpServletRequest req, HttpServletResponse resp, 
			InitAccSessionReq data) throws Throwable {
		
		boolean full = data == null? false : data.full;
		
		//check accs for move
		if(full && ! isValidAccsData(data.fullCheckAccs)){
			throw new OutdatedChatAccountsException();
		}
		
		String token = chats.initUserTokenInAccServers(req, full);
		return token;
	}
	
	public boolean isValidAccsData(Map<String, List<String>> data){
		
		if(isEmpty(data)) return true;
		
		for (Entry<String, List<String>> entry : data.entrySet()) {
			
			Long serverId = tryParseLong(entry.getKey(), null);
			if(serverId == null) return false;
			
			if( ! chats.checkAllAccExists(serverId, entry.getValue())){
				return false;
			}
		}
		
		return true;
	}

}
