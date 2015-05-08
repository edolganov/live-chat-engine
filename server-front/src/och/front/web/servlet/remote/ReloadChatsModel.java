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
package och.front.web.servlet.remote;

import static och.api.model.RemoteFront.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.remote.front.ReloadChatsModelReq;
import och.front.web.EncryptedJsonPostServlet;

@WebServlet(URL_SYNC_RELOAD_CHATS_MODELS)
@SuppressWarnings("serial")
public class ReloadChatsModel extends EncryptedJsonPostServlet<ReloadChatsModelReq, Void>{
	
	@Override
	protected Void doJsonPost(HttpServletRequest req, HttpServletResponse resp,
			ReloadChatsModelReq data) throws Throwable {
		
		//skip cur app req
		if(app.id.equals(data.reqAppId)) {
			log.warn("skip req from same app: "+app.id);
			return null;
		}
		
		chats.reloadModel(data);
		
		return null;
	}

}
