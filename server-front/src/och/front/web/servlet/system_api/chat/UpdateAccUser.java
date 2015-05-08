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

import static och.api.model.chat.account.PrivilegeType.*;
import static och.util.Util.*;

import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.account.PrivilegeType;
import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/chat/updateUser")
@SuppressWarnings("serial")
public class UpdateAccUser extends JsonPostServlet<UpdateAccUserReq, Void> {
	
	@Override
	protected Void doJsonPost(HttpServletRequest req, HttpServletResponse resp, UpdateAccUserReq data) throws Throwable {
		
		Set<PrivilegeType> curPrivs = chats.getAccPrivilegesForUser(data.accId, data.userId);
		if(isEmpty(curPrivs)) return null;
		Set<PrivilegeType> privsToAdd = set();
		Set<PrivilegeType> privsToDelete = set();
		
		//change moder priv
		if(curPrivs.contains(CHAT_MODER)){
			if( ! data.isModerator) privsToDelete.add(CHAT_MODER);
		} else {
			if( data.isModerator) privsToAdd.add(CHAT_MODER);
		}
		
		//change op priv
		if(curPrivs.contains(CHAT_OPERATOR)){
			if( ! data.isOperator) privsToDelete.add(CHAT_OPERATOR);
		} else {
			if( data.isOperator) privsToAdd.add(CHAT_OPERATOR);
		}
		
		if(privsToAdd.size() > 0){
			chats.addUserPrivileges(data.accId, data.userId, privsToAdd);
		}
		if(privsToDelete.size() > 0){
			chats.removeUserPrivileges(data.accId, data.userId, privsToDelete);
		}
		
		return null;
	}
}
