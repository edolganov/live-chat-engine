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
package och.front.web.servlet.system_api.chat;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.account.PauseAccResp;
import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/chat/pauseAcc")
@SuppressWarnings("serial")
public class PauseAcc extends JsonPostServlet<PauseAccReq, PauseAccResp> {
	
	@Override
	protected PauseAccResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, PauseAccReq data) throws Throwable {
		
		PauseAccResp result = null;
		
		if(data.val) result = chats.pauseAccByUser(data.accId);
		else result = chats.unpauseAccByUser(data.accId);
		
		return result;
	}
}
