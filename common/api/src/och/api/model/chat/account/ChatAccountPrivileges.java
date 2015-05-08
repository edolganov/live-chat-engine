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
package och.api.model.chat.account;

import static java.util.Collections.*;
import static och.util.Util.*;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class ChatAccountPrivileges {
	
	public static final int MAX_NICKNAME_SIZE = 60;
	
	public long userId;
	public long accId;
	public Set<PrivilegeType> privileges = new HashSet<>();
	public String nickname;
	

	public ChatAccountPrivileges() {
		super();
	}
	
	public ChatAccountPrivileges(long userId, long accId) {
		this(userId, accId, new HashSet<PrivilegeType>());
	}

	public ChatAccountPrivileges(long userId, long accId, Set<PrivilegeType> privileges) {
		this.userId = userId;
		this.accId = accId;
		setChatPrivileges(privileges);
	}

	
	public Set<PrivilegeType> clonePrivileges(){
		if(isEmpty(privileges)) return set();
		return new HashSet<>(privileges);
	}


	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setAccId(long accId) {
		this.accId = accId;
	}

	public void setChatPrivileges(String val){
		if( ! hasText(val)) {
			setChatPrivileges((Set<PrivilegeType>)null);
			return;
		}
		
		HashSet<PrivilegeType> set = new HashSet<>();
		StringTokenizer st = new StringTokenizer(val, ",");
		while(st.hasMoreTokens()){
			int code = tryParseInt(st.nextToken(), -1);
			PrivilegeType role = tryGetEnumByCode(code, PrivilegeType.class, null);
			if(role != null) set.add(role);
		}
		setChatPrivileges(set);
	}
	
	public void setChatPrivileges(Set<PrivilegeType> set){
		if(isEmpty(set)) set = emptySet();
		this.privileges = new HashSet<>(set);
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	
	
}
