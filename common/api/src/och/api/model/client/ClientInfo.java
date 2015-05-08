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
package och.api.model.client;

import java.util.Random;

public class ClientInfo {
	
	public String ip;
	public String userAgent;
	
	//extra data
	public String email;
	public String name;
	
	
	public ClientInfo() {
		super();
	}

	public ClientInfo(String ip, String userAgent) {
		super();
		this.ip = ip;
		this.userAgent = userAgent;
	}
	
	public String getUserId() {
		return UserIdOps.getUserId(this);
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result
				+ ((userAgent == null) ? 0 : userAgent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientInfo other = (ClientInfo) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (userAgent == null) {
			if (other.userAgent != null)
				return false;
		} else if (!userAgent.equals(other.userAgent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClientInfo [ip=" + ip + ", userAgent=" + userAgent + "]";
	}
	
	
	public static ClientInfo randomClientInfo(){
		return randomClientInfo(true);
	}
	
	public static ClientInfo randomClientInfo(boolean withExtraData){
		Random r = new Random();
		String ip = r.nextInt(256)+"."+r.nextInt(256)+"."+r.nextInt(256)+"."+r.nextInt(256);
		ClientInfo out = new ClientInfo(ip, "some user agent ### "+System.currentTimeMillis());
		
		if(withExtraData){
			out.email = "some@mail.com";
			out.name = "some";
		}
		
		return out;
	}

}
