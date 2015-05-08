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

import och.api.model.BaseBean;
import och.api.model.ValidationProcess;
import och.api.model.user.UserExt;

public class RemoveOperatorReq extends BaseBean {
	
	public String accountId;
	public long userId = -1;
	

	public RemoveOperatorReq() {
		super();
	}
	
	public RemoveOperatorReq(String accountId, UserExt user){
		this(accountId, user.id);
	}


	public RemoveOperatorReq(String accountId, long userId) {
		this.accountId = accountId;
		this.userId = userId;
	}


	@Override
	protected void checkState(ValidationProcess v) {
		v.checkForText(accountId, "accountId");
		v.checkForValid(userId > -1, "userId");
	}

}
