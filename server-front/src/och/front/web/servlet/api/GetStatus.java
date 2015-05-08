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
package och.front.web.servlet.api;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.chat.NoChatAccountException;
import och.api.model.PropKey;
import och.api.model.server.ServerRow;

@WebServlet(value="/api/status")
@SuppressWarnings("serial")
public class GetStatus extends BaseApiPostServlet<GetStatusReq, GetStatusResp>{
	
	@Override
	protected GetStatusResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, GetStatusReq data) throws Throwable {
		
		String accId = data.id;
		
		chats.checkAndLogReferer(req, data.ref, accId);
		
		ServerRow server = chats.getServerByAcc(accId);
		
		if(server == null) throw new NoChatAccountException();
		
		return new GetStatusResp(
				server.httpUrl, 
				server.httpsUrl,
				app.props.getIntVal(PropKey.chats_maxMsgSize));
	}

}
