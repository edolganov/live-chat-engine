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
package och.front.web.servlet;

import static och.api.model.chat.account.PrivilegeType.*;
import static och.api.model.user.SecurityContext.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.tariff.Tariff;
import och.api.model.user.User;
import och.front.web.SimpleFrontServlet;

@WebServlet(value="/cabinet")
@SuppressWarnings("serial")
public class CabinetPage extends SimpleFrontServlet {
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		resp.sendRedirect("/cabinet");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			User user = getUserFromSecurityContext();
			if(user == null){
				resp.sendRedirect("/enter");
				return;
			}
			
			Collection<ChatAccount> chatAccs = chats.getAccsForOperator(user.id);
			Map<String, Set<PrivilegeType>> privsByAcc = chats.getAllAccountsPrivilegesForUser(user.id);
			boolean isOwnerOfAnyAcc = isOwnerOfAnyAcc(privsByAcc);
			BigDecimal balance = isOwnerOfAnyAcc ? billing.getCurUserBalance() : null;
			BigDecimal payConfirmVal = billing.paypal_getPayConfirmVal();
			List<Tariff> publicTariffs = chats.getPublicTariffs();
			
			
			req.setAttribute("publicTariffs", publicTariffs);
			req.setAttribute("chatAccs", chatAccs);
			req.setAttribute("privsByAcc", privsByAcc);
			req.setAttribute("isOwnerOfAnyAcc", isOwnerOfAnyAcc);
			req.setAttribute("balance", balance);
			req.setAttribute("payConfirmVal", payConfirmVal);
			
			forward(req, resp, "/WEB-INF/jsp/front/cabinet.jsp");
			
		}
		catch(IOException e){
			throw e;
		}
		catch(Exception e){
			throw new IllegalStateException("can't show page", e);
		}
	}

	
	public static boolean isOwnerOfAnyAcc(Map<String, Set<PrivilegeType>> privsByAcc) {
		if(isEmpty(privsByAcc)) return false;
		Collection<Set<PrivilegeType>> values = privsByAcc.values();
		for (Set<PrivilegeType> set : values) {
			if(set.contains(CHAT_OWNER)) return true;
		}
		return false;
	}

}
