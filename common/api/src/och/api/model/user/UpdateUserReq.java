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
package och.api.model.user;

import och.api.model.BaseBean;
import och.api.model.ValidationProcess;

public class UpdateUserReq extends BaseBean {
	
	public String email;
	public String login;
	public String psw;
	public byte[] pswHash;
	public String pswSalt;
	
	public UpdateUserReq(String email, String login, String psw) {
		super();
		this.email = email;
		this.login = login;
		this.psw = psw;
	}

	@Override
	protected void checkState(ValidationProcess v) {
		if(email != null) User.checkEmail(email, v);
		if(login != null) User.checkLogin(login, v);
		if(psw != null) v.checkForText(psw, "psw");
		
	}
	
	

}
