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

import static java.lang.System.*;
import static och.util.DateUtil.*;

import java.util.Date;

import och.api.exception.user.UserActivationExpiredException;



public class UserExt extends User {
	
	public byte[] pswHash;
	public String pswSalt;
	
	public String activationCode;
	public Date activationStateDate;
	
	public String baseOperatorNickname;

	public UserExt() {
		super();
	}

	public UserExt(long id, String login, String email, UserStatus status, 
			byte[] pswHash, String pswSalt, Date activationStateDate, String activationCode) {
		super(id, login, email, status);
		this.pswHash = pswHash;
		this.pswSalt = pswSalt;
		this.activationStateDate = activationStateDate;
		this.activationCode = activationCode;
	}
	
	public void checkActivateExpiredTime(long activateExpiredTime) throws UserActivationExpiredException {
		long stateDay = dateStart(getActivationStateTime());
		long curDay = dateStart(currentTimeMillis());
		if((curDay - stateDay) > activateExpiredTime)
			throw new UserActivationExpiredException(activationStateDate);
	}
	
	
	public User getUser() {
		User out = new User();
		out.id = id;
		out.email = email;
		out.login = login;
		out.status = status;
		out.roles = roles;
		return out;
	}
	
	public void update(UpdateUserReq req) {
		if(req.login != null) this.login = req.login;
		if(req.email != null) this.email = req.email;
		if(req.pswHash != null) {
			this.pswHash = req.pswHash;
			this.pswSalt = req.pswSalt;
		}
		
	}
	
	
	
	

	public byte[] getPswHash() {
		return pswHash;
	}

	public void setPswHash(byte[] pswHash) {
		this.pswHash = pswHash;
	}

	public String getPswSalt() {
		return pswSalt;
	}

	public void setPswSalt(String pswSalt) {
		this.pswSalt = pswSalt;
	}

	public Date getActivationStateDate() {
		return activationStateDate;
	}
	
	public long getActivationStateTime() {
		return activationStateDate != null ? activationStateDate.getTime() : 0;
	}

	public void setActivationStateDate(Date activationStateDate) {
		this.activationStateDate = activationStateDate;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public String getBaseOperatorNickname() {
		return baseOperatorNickname;
	}

	public void setBaseOperatorNickname(String baseOperatorNickname) {
		this.baseOperatorNickname = baseOperatorNickname;
	}


	

	
	

}
