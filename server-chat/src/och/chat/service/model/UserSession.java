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
package och.chat.service.model;

import static java.util.Collections.*;
import static och.util.Util.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import och.api.model.chat.account.PrivilegeType;
import och.api.model.client.ClientInfo;
import och.api.remote.chats.InitUserTokenReq;


public class UserSession implements Cloneable {
	
	public final ClientInfo info;
	public final long userId;
	public final String token;
	public Map<String, Set<PrivilegeType>> privsByAcc = emptyMap();
	
	public UserSession(InitUserTokenReq data){
		this.info = new ClientInfo(data.clientIp, data.clientUserAgent);
		this.userId = data.userId;
		this.token = data.token;
		if(data.privsByAcc != null) {
			this.privsByAcc = data.privsByAcc;
		}
	}
	
	public Map<String, Set<PrivilegeType>> clonePrivsByAcc(){
		
		if(isEmpty(privsByAcc)) return emptyMap();
		HashMap<String, Set<PrivilegeType>> out = new HashMap<>();
		for (Entry<String, Set<PrivilegeType>> entry : privsByAcc.entrySet()) {
			out.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}
		return out;
	}
	
	@Override
	public UserSession clone() {
		try {
			Map<String, Set<PrivilegeType>> privs = clonePrivsByAcc();
			UserSession out = (UserSession) super.clone();
			out.privsByAcc = privs;
			return out;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
	


}
