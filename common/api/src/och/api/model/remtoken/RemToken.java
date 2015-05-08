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
package och.api.model.remtoken;

import java.util.Date;

public class RemToken {
	
	public String uid;
	public byte[] tokenHash;
	public String tokenSalt;
	public long userId;
	public Date lastVisited;
	
	
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public byte[] getTokenHash() {
		return tokenHash;
	}
	public void setTokenHash(byte[] tokenHash) {
		this.tokenHash = tokenHash;
	}
	public String getTokenSalt() {
		return tokenSalt;
	}
	public void setTokenSalt(String tokenSalt) {
		this.tokenSalt = tokenSalt;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public Date getLastVisited() {
		return lastVisited;
	}
	public void setLastVisited(Date lastVisited) {
		this.lastVisited = lastVisited;
	}
	
	
	

}
