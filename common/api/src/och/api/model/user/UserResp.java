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

import java.util.HashMap;
import java.util.Map;

public class UserResp {
	
	public long id;
	public String login;
	public String nickname;
	
	//extra
	public Map<String, Object> params = new HashMap<String, Object>();
	
	public UserResp(User user){
		this(user, null, null, null, null);
	}
	
	public UserResp(User user, String paramKey, Object paramVal){
		this(user, paramKey, paramVal, null, null);
	}
	
	public UserResp(User user, String paramKey, Object paramVal, String paramKey2, Object paramVal2){
		
		this.id = user.id;
		this.login = user.login;
		
		
		if(paramKey != null){
			params.put(paramKey, paramVal);
		}
		if(paramKey2 != null){
			params.put(paramKey2, paramVal2);
		}
	}
	
	public UserResp(long id, String login) {
		this.id = id;
		this.login = login;
	}
	
	
	

}
