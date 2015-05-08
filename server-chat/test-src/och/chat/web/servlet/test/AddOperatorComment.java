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
package och.chat.web.servlet.test;

import static och.api.model.user.SecurityContext.*;
import static och.util.Util.*;


import java.util.Collection;
import java.util.Date;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ValidationException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatOperator;
import och.chat.web.JsonGetServlet;

@SuppressWarnings("serial")
@WebServlet("/test/addOperatorComment")
public class AddOperatorComment extends JsonGetServlet<Void> {
	
	@Override
	protected Void doJsonGet(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			String accountId = req.getParameter("accountId");
			if( ! hasText(accountId)) throw new ValidationException("accountId is empty");
			
			boolean addOperatorOnly = tryParseBool(req.getParameter("addOperatorOnly"), false);
			boolean beNextOperator = tryParseBool(req.getParameter("beNext"), false);
			long operatorId = tryParseLong(req.getParameter("opId"), 999L);
			
			chats.putOperator(accountId, new ChatOperator(operatorId, "robot"));
			chats.setActiveOperator(accountId, operatorId);
			
			Collection<ChatLog> allChats = chats.getAllActiveChatLogs(accountId);
			for (ChatLog chatLog : allChats) {
				int opCount = chatLog.getOperatorsCount();
				
				//first operator
				if(opCount == 0){
					
					chats.addOperatorToChat(accountId, chatLog.id, operatorId, opCount);
					if(addOperatorOnly) continue;
					
					chats.addComment(accountId, chatLog.id, operatorId, "Текст от оператора -- "+new Date());
					continue;
				} 
				
				//next operator
				if( ! chatLog.hasOperator(operatorId)){
					if( ! beNextOperator) continue;
					chats.addOperatorToChat(accountId, chatLog.id, operatorId, opCount);
				}
				if(addOperatorOnly) continue;
				chats.addComment(accountId, chatLog.id, operatorId, "Текст от оператора -- "+new Date());
			}
			
			return null;
		}finally {
			popUserFromSecurityContext();
		}
	}

}
