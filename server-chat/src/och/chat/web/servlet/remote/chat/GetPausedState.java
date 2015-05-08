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

import och.api.remote.chats.GetPausedStateReq;
import och.api.remote.chats.ResultAccsResp;
import och.chat.web.EncryptedJsonPostServlet;

@WebServlet(URL_CHAT_GET_PAUSED_STATE)
@SuppressWarnings("serial")
public class GetPausedState extends EncryptedJsonPostServlet<GetPausedStateReq, ResultAccsResp> {
	
	@Override
	protected ResultAccsResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, GetPausedStateReq data) throws Throwable {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			ResultAccsResp out = new ResultAccsResp();
			
			boolean isPaused = data.isPaused;
			for(String acc : data.fromAccs){
				if( ! chats.isAccExists(acc)) continue;
				boolean isCurPaused = chats.isAccPaused(acc);
				if( isPaused != isCurPaused) {
					out.accs.add(acc);
				}
			}
			
			return out;
			
		} finally {
			popUserFromSecurityContext();
		}
		
		
	}

}
