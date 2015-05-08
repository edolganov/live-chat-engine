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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatOperator;
import och.api.model.client.ClientSession;
import och.chat.web.JsonGetServlet;
import och.chat.web.model.ChatLogResp;
import och.chat.web.servlet.test.StressTestServlet;
import och.util.NumberUtil;

@SuppressWarnings("serial")
@WebServlet("/test/stressTest")
public class StressTestServlet extends JsonGetServlet<Object> {
	
	
	static final int accCount = 100;
	static final int sessionsCountForAcc = 10;
	static final int maxCommentsForSession = 6;
	
	
	Random r = new Random();
	ArrayList<String> accs = new ArrayList<>();
	ChatOperator op1 = new ChatOperator(1, "oooop1");
	ChatOperator op2 = new ChatOperator(2, "oooop2");
	
	ConcurrentHashMap<String, UserSessions> userSessionsByAcc = new ConcurrentHashMap<>();
	
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		for (int i = 0; i < accCount; i++) {
			accs.add("stress-test-"+NumberUtil.zeroFormattedStr(i+1, 4));
		}
		
		//create test accs
		pushToSecurityContext_SYSTEM_USER();
		try {
			Collection<String> curChats = chats.getAccIds();
			if( ! curChats.contains(accs.get(accs.size()-1))){
				log.info("create test accs...");
				for (String accId : accs) {
					chats.createAcc(accId);
					chats.putOperator(accId, op1);
					chats.putOperator(accId, op2);
				}
				log.info("done");
			}
			
			log.info("init chats active ops...");
			for (String accId : accs) {
				chats.setActiveOperator(accId, op1.id);
				chats.setActiveOperator(accId, op2.id);
			}
			log.info("done");
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	@Override
	protected Object doJsonGet(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
		
		boolean readOnly = req.getParameter("read-only") != null;
		boolean writeOnly = req.getParameter("write-only") != null;
		
		//70% read op
		//30% write op
		if( ! writeOnly && (readOnly ||  r.nextInt(100)+1 <= 70)){
			return readOp(req);
		} else {
			return wrireOp(req);
		}
	}
	
	private Object wrireOp(HttpServletRequest req) throws Exception {
		
		String accId = accs.get(r.nextInt(accCount));
		boolean isUser = r.nextInt(100) < 70;
		if(isUser){
			return addUserComment(req, accId);
		} else {
			return addOpComment(req, accId);
		}
	}

	private Object addUserComment(HttpServletRequest req, String accId) {
		
		UserSessions newSessions = new UserSessions(this);
		UserSessions sessions = userSessionsByAcc.putIfAbsent(accId, newSessions);
		if(sessions == null){
			sessions = newSessions;
		}
		
		ClientSession clientSession = sessions.getSession();
		clientSession.addAccId(accId);
		
		chats.addComment(accId, clientSession, "текст от клиента!! длыоадфы фдвыдаофы фывдавоа 1223 dasadfa");
		ChatLog chatLog = chats.getChatLog(accId, clientSession);
		return chatLog == null? null : new ChatLogResp(chatLog);
	}
	
	void closeSession(ClientSession session) {
		chats.closeChats(session);
	}
	
	private Object addOpComment(HttpServletRequest req, String accId) {
		
		ChatOperator op = r.nextBoolean()? op1 : op2;
		long operatorId = op.id;
		
		Collection<ChatLog> state = app.chats.getActiveChatLogs(accId, operatorId);
		if(isEmpty(state)) return "can't addOpComment: no chats for accId="+accId+", opId="+operatorId;
		
		ArrayList<ChatLog> logs = new ArrayList<>(state);
		ChatLog randomChat = logs.get(r.nextInt(logs.size()));
		chats.addComment(accId, randomChat.id, operatorId, "Текст от оператора -- "+new Date());
		
		ChatLog chatLog = chats.getChatLogById(accId, randomChat.id);
		return chatLog == null? null : new ChatLogResp(chatLog);
	}

	private Object readOp(HttpServletRequest req) throws Exception {
		
		String accId = accs.get(r.nextInt(accCount));
		
		ChatOperator op = r.nextBoolean()? op1 : op2;
		long operatorId = op.id;
		
		Collection<ChatLog> state = app.chats.getActiveChatLogs(accId, operatorId);
		if(isEmpty(state)) return "can't readOp: no chats for accId="+accId+", opId="+operatorId;
		
		ArrayList<ChatLog> logs = new ArrayList<>(state);
		ChatLog randomChat = logs.get(r.nextInt(logs.size()));
		
		ChatLog chatLog = chats.getChatLogById(accId, randomChat.id);
		return chatLog == null? null : new ChatLogResp(chatLog);
	}
	

	//clients
	static class UserSessions {
		
		private StressTestServlet owner;
		private ConcurrentHashMap<String, SessionState> statesById = new ConcurrentHashMap<>();
		private Random r = new Random();
		
		public UserSessions(StressTestServlet owner) {
			this.owner = owner;
		}


		public UserSessions(){
			for (int i = 0; i < sessionsCountForAcc; i++) {
				ClientSession session = ClientSession.randomSession();
				statesById.put(session.sessionId, new SessionState(session, 0));
			}
		}

		
		public ClientSession getSession() {
			
			SessionState curState = null;
			ArrayList<String> keys = new ArrayList<>(statesById.keySet());
			if(keys.size() > 0){
				String randomId = keys.get(r.nextInt(keys.size()));
				curState = statesById.remove(randomId);
			}
			
			if(curState == null){
				curState = new SessionState(ClientSession.randomSession(), 0);
			}
			
			ClientSession session = curState.session;
			int newReqCount = curState.reqCount + 1;
			if(newReqCount > maxCommentsForSession){
				owner.closeSession(session);
				session = ClientSession.randomSession();
				newReqCount = 0;
			}
			
			SessionState newState = new SessionState(session, newReqCount);
			statesById.put(session.sessionId, newState);
			
			return session;
		}
		
		
	}
	
	static class SessionState {
		
		public final ClientSession session;
		public final int reqCount;
		
		public SessionState(ClientSession session, int reqCount) {
			this.session = session;
			this.reqCount = reqCount;
		}
	}


}
