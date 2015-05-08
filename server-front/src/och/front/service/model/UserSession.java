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
package och.front.service.model;

import java.util.HashMap;
import java.util.Map;

import och.api.model.client.ClientInfo;
import och.api.model.user.User;

public class UserSession {
	
	public final ClientInfo info;
	public final User user;
	public final Map<String, Object> attrs = new HashMap<String, Object>();

	public UserSession(String ip, String userAgent, User user, Map<String, Object> initAttrs) {
		this.info = new ClientInfo(ip, userAgent);
		this.user = user;
		if(initAttrs != null) {
			attrs.putAll(initAttrs);
		}
	}
		

}
