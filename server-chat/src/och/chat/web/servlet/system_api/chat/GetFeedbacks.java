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
package och.chat.web.servlet.system_api.chat;

import static och.util.Util.*;

import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.Feedback;
import och.chat.web.JsonPostServlet;
import och.comp.web.annotation.RoleSecured;

@RoleSecured
@WebServlet("/system-api/chat/feedbacks")
@SuppressWarnings("serial")
public class GetFeedbacks extends JsonPostServlet<GetFeedbacksReq, GetFeedbacksResp> {
	
	
	@Override
	protected GetFeedbacksResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, 
			GetFeedbacksReq data) throws Throwable {
		
		List<Feedback> list = chats.getFeedbacks(data.accId, data.getDate());
		if(isEmpty(list)) return null;
		
		return new GetFeedbacksResp(list);
		
	}

}
