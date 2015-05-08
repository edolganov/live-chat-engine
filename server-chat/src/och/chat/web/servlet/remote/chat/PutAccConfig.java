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
package och.chat.web.servlet.remote.chat;

import static och.api.model.RemoteChats.*;
import static och.api.model.user.SecurityContext.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.config.Key;
import och.api.remote.chats.PutAccConfigReq;
import och.chat.web.EncryptedJsonPostServlet;

@WebServlet(URL_CHAT_PUT_ACC_CONFIG)
@SuppressWarnings("serial")
public class PutAccConfig extends EncryptedJsonPostServlet<PutAccConfigReq, Void> {
	
	@Override
	protected Void doJsonPost(HttpServletRequest req, HttpServletResponse resp, PutAccConfigReq data) throws Throwable {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			Key key = data.key();
			if(key == null) return null;
			
			chats.putAccConfig(data.accId, key, data.val);
			
			return null;
			
		} finally {
			popUserFromSecurityContext();
		}
		
		
	}

}
