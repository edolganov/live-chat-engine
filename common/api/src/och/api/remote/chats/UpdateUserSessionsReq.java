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

public class UpdateUserSessionsReq extends BaseBean {
	
	public long userId = -1;
	public Map<String, Set<PrivilegeType>> privilegesByAccount;
	
	public UpdateUserSessionsReq() {
		super();
	}

	public UpdateUserSessionsReq(long userId, Map<String, Set<PrivilegeType>> privilegesByAccount) {
		this.userId = userId;
		this.privilegesByAccount = privilegesByAccount;
	}





	@Override
	protected void checkState(ValidationProcess v) {
		v.checkForValid(userId > -1, "userId");
		v.checkForValid(privilegesByAccount != null, "privilegesByAccount");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Set<PrivilegeType>> privilegesByAccount() {
		return privilegesByAccount == null? (Map)emptyMap() : privilegesByAccount;
	}

}
