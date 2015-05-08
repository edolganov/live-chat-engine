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
package och.api.remote.chats;

import static java.util.Collections.*;

import java.util.Map;
import java.util.Set;

import och.api.model.BaseBean;
import och.api.model.ValidationProcess;
import och.api.model.chat.account.PrivilegeType;

public class InitUserTokenReq extends BaseBean {
	
	public String token;
	public long userId = -1;
	public String clientIp;
	public String clientUserAgent;
	public Map<String, Set<PrivilegeType>> privsByAcc = emptyMap();
	

	public InitUserTokenReq() {
		super();
	}

	public InitUserTokenReq(String token, long userId, String clientIp, String clientUserAgent) {
		this(token, userId, clientIp, clientUserAgent, null);
	}


	public InitUserTokenReq(String token, long userId, String clientIp,
			String clientUserAgent, Map<String, Set<PrivilegeType>> privsByAcc) {
		this.token = token;
		this.userId = userId;
		this.clientIp = clientIp;
		this.clientUserAgent = clientUserAgent;
		if(privsByAcc != null) this.privsByAcc = privsByAcc;			
		
	}



	@Override
	protected void checkState(ValidationProcess v) {
		v.checkForText(token,"token");
		v.checkForText(clientIp, "clientIp");
		v.checkForText(clientUserAgent, "clientUserAgent");
		v.checkForValid(userId > -1, "userId");
	}



	@Override
	public String toString() {
		return "InitUserTokenReq [token=" + token + ", userId=" + userId
				+ "]";
	}
	
	

}
