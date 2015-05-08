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


import static och.api.model.client.ClientInfo.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.List;


public class ClientSession {
	
	//basic
	public final String sessionId;
	public final ClientInfo info;
	
	//extra
	private ArrayList<String> accIds = new ArrayList<>();
	
	
	public ClientSession(String sessionId, String ip, String userAgent) {
		this(sessionId, new ClientInfo(ip, userAgent));
	}
	
	public ClientSession(String sessionId, ClientInfo info) {
		this.info = info;
		this.sessionId = sessionId;
	}


	public String getUserId() {
		return info.getUserId();
	}
	
	
	
	public synchronized boolean addAccId(String accId){
		if(accId == null) return false;
		if(accIds.contains(accId)) return false;
		accIds.add(accId);
		return true;
	}
	
	public synchronized boolean containsAccId(String accId){
		return accIds.contains(accId);
	}
	
	public synchronized List<String> getAccIds(){
		ArrayList<String> copy = new ArrayList<>(accIds);
		return copy;
	}
	
	public synchronized void clearAccIds(){
		accIds.clear();
	}
	
	
	@Override
	public String toString() {
		return "ClientSession [sessionId=" + sessionId + ", info=" + info + "]";
	}
	

	public static ClientSession randomSession(){
		ClientSession out = new ClientSession(randomUUID(), randomClientInfo());
		return out;
	}
	

}
