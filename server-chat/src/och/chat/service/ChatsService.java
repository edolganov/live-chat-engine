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
package och.chat.service;

import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.chat.config.Key.*;
import static och.api.model.user.SecurityContext.*;
import static och.api.model.web.ReqInfo.*;
import static och.chat.service.SecurityService.*;
import static och.comp.ops.ChatOps.*;
import static och.util.Util.*;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.annotation.Secured;
import och.api.exception.chat.ChatAccountBlockedException;
import och.api.exception.chat.ChatAccountPausedException;
import och.api.exception.chat.NoChatAccountException;
import och.api.exception.chat.NoPreviousOperatorPositionException;
import och.api.exception.chat.OperatorPositionAlreadyExistsException;
import och.api.exception.client.AccountNotAddedToSessionException;
import och.api.exception.user.AccessDeniedException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatLogHist;
import och.api.model.chat.ChatOperator;
import och.api.model.chat.ChatUpdateData;
import och.api.model.chat.Feedback;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.chat.config.Key;
import och.api.model.client.ClientInfo;
import och.api.model.client.ClientSession;
import och.api.model.user.User;
import och.api.model.user.UserRole;
import och.api.model.web.ReqInfo;
import och.chat.service.event.client.ClientSessionDesroyedEvent;
import och.chat.service.event.user.UserSessionDestroyedEvent;
import och.chat.service.model.UserSession;
import och.comp.chats.ChatsAcc;
import och.comp.chats.ChatsAccService;
import och.comp.chats.model.InitChatData;
import och.comp.mail.SendReq;
import och.util.concurrent.AsyncListener;

public class ChatsService extends BaseChatService {

	SecurityService security;
	ChatsAccService accs;
	
	public ChatsService(ChatsAppContext c) {
		super(c);
		security = c.root.security;
		accs = c.accs;
		
		
		//reload by timer
		long reloadDelay = c.props.getLongVal(chats_reloadDelay);
		c.async.scheduleWithFixedDelay("reload chat accounts", ()-> 
			accs.reloadAccs(), 
			reloadDelay, reloadDelay);
		
		
		//events
		c.events.addListener(ClientSessionDesroyedEvent.class, (event) -> closeChats(event.session));
		c.events.addListener(UserSessionDestroyedEvent.class, (event) -> removeActiveOperators(event.session));
	}
	
	public void setAsyncListener(AsyncListener asyncListener){
		accs.setAsyncListener(asyncListener);
	}
	
	public void checkChatAndInitClientSession(HttpServletRequest req, HttpServletResponse resp, String accId){
		checkChatAndInitClientSession(req, resp, accId, null);
	}

	public void checkChatAndInitClientSession(HttpServletRequest req, HttpServletResponse resp, String accId, String oldChatId){
		
		validateForText(accId, "accId");
		
		//check chat's account
		ChatsAcc acc = accs.findAcc(accId);
		checkForBlockedOrPaused(accId);
		
		//check online operators if need
		ClientSession curSession = security.getClientSession(req);
		if(curSession != null || oldChatId == null){
			acc.checkHasActiveOperators();
		}
		
		//init or get session
		boolean isNewSession = security.initClientSessionForAcc(req, accId);
		if(!isNewSession) return;
		
		//restore old chat
		ClientSession clientSession = security.getClientSession(req);
		ChatLog restored = accs.restoreOldChatIfNeed(accId, clientSession, oldChatId);
		
		if(restored != null) log.info("["+accId+"] "+"CLIENT chat restored: "
				+"chatId="+restored.id
				+", msgCount="+restored.getMsgCount()
				+", req="+getReqInfoStr());
		
	}

	public InitChatData addComment(String accId, ClientSession clientSession, String text){
		
		ChatsAcc acc = checkClientAndFindChats(accId, clientSession);
		checkForBlockedOrPaused(accId);
		
		checkNewMsgToAdd(props, text);
		
		InitChatData initResult = acc.initActiveChat(clientSession);
		String chatId = initResult.id;
		acc.addComment(clientSession, text);
		
		log.info("["+accId+"] "+"CLIENT comment added: "
				+"chatId="+chatId
				+", size="+(isEmpty(text)? 0 : text.length())
				+", req="+getReqInfoStr());
		
		return initResult;
	}
	
	public void addFeedback(String accId, ClientInfo clientInfo, String text){
		
		checkForBlocked(accId);
		checkNewMsgToAdd(props, text);
		
		ChatsAcc acc = accs.findAcc(accId);
		
		//async add to file
		accs.addFeedbackAsync(accId, clientInfo, text);
		
		//emails
		if( props.getBoolVal(chats_emailNotifications) 
				&& acc.getBoolVal(feedback_notifyOpsByEmail)) {
			
			List<ChatOperator> ops = acc.getRegistredOperators();
			List<String> emails = convert(ops, (op)-> op.email);
			String accName = acc.getStrVal(Key.name);
			sendFeedbackPosted(accId, accName, clientInfo, text, emails);
		}
		
		log.info("["+accId+"] "+"CLIENT feedback added: "
				+"size="+(isEmpty(text)? 0 : text.length())
				+", req="+getReqInfoStr());
		
	}
	
	
	public ChatLog getChatLog(String accId, ClientSession clientSession){
		return getChatLog(accId, clientSession, null);
	}
	
	public ChatLog getChatLog(String accId, ClientSession clientSession, ChatUpdateData updateData){
		ChatsAcc acc = checkClientAndFindChats(accId, clientSession);
		ChatLog chatLog = acc.getActiveChat(clientSession, updateData);
		return acc.fillOperators(chatLog);
	}

	public void closeChats(ClientSession session){
		
		for (String accId : session.getAccIds()) {
			ChatsAcc acc = accs.getAcc(accId);
			if(acc == null) continue;
			
			String chatId = acc.closeChat(session);
			
			log.info("["+accId+"] "+"CLIENT chat closed: "
					+"chatId="+chatId
					+", req="+getReqInfoStr());
		}
		
		session.clearAccIds();
	}
	
	
	
	@Secured
	public ChatLog getChatLogById(String accId, String chatId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		ChatLog chatLog = acc.getActiveChatById(chatId);
		return acc.fillOperators(chatLog);
	}
	
	
	@Secured
	public void createAcc(String accId){
		
		checkAccessFor_ADMIN();
		
		accs.createAcc(accId);
		
		log.info("acc created: uid="+accId
				+", req="+getReqInfoStr());
	}
	
	@Secured
	public void removeAcc(String accId){
		
		checkAccessFor_ADMIN();
		
		accs.removeAcc(accId);
		
		log.info("acc removed: uid="+accId
				+", req="+getReqInfoStr());
	}
	
	
	@Secured
	public Collection<String> getAccIds(){
		checkAccessFor_ADMIN();
		return accs.getAccIds();
	}
	
	
	@Secured
	public void putOperator(String accId, ChatOperator operator){
		
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		
		acc.putOperator(operator);
		
		log.info("["+accId+"] "+"OPERATOR added: opId="+operator.id
				+", req="+getReqInfoStr());
	}
	
	
	@Secured
	public void updateUserContact(long userId, String email) {
		
		checkAccessFor_ADMIN();
		
		List<ChatsAcc> allAccs = accs.getAllAccs();
		for (ChatsAcc acc : allAccs) {
			boolean updated = acc.updateOperatorContact(userId, email);
			if(updated){
				log.info("["+acc.getId()+"] "+"OPERATOR updated: opId="+userId
						+", req="+getReqInfoStr());
			}
		}
	}
	
	@Secured
	public void removeOperator(String accId, long opId){
		
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		
		acc.removeOperator(opId);
		
		log.info("["+accId+"] "+"OPERATOR removed: opId="+opId
				+", req="+getReqInfoStr());
	}
	
	@Secured
	public void setActiveOperator(String accId, long operatorId){
		
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		
		boolean updated = acc.setActiveOperator(operatorId);
		
		if(updated){
			log.info("["+accId+"] "+"OPERATOR active: opId="+operatorId
					+", req="+getReqInfoStr());
		}
	}
	
	@Secured
	public boolean isOperator(String accId, long opId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		return acc.isOperator(opId);
	}
	
	@Secured
	public boolean isActiveOperator(String accId, long opId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		return acc.isActiveOperator(opId);
	}
	
	@Secured
	public void removeActiveOperator(String accId, long opId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		boolean done = acc.removeActiveOperator(opId);
		
		if(done) log.info("["+accId+"] "+"OPERATOR inactive: opId="+opId
				+", req="+getReqInfoStr());
	}
	
	@Secured
	public int getActiveOperatorsCount(String accId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		return acc.getActiveOperatorsCount();
	}
	
	@Secured
	public ChatOperator addOperatorToChat(String accId, String chatId, long operatorId, int position) throws OperatorPositionAlreadyExistsException, NoPreviousOperatorPositionException {
		
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		
		checkForBlockedOrPaused(accId);
		
		ChatOperator out = acc.addOperator(chatId, operatorId, position);
		
		log.info("["+accId+"] "+"OPERATOR joined chat: "
				+"chatId="+chatId
				+", opId="+operatorId
				+", index="+position
				+", req="+getReqInfoStr());
		
		return out;
	}

	@Secured
	public ChatOperator getOperator(String accId, long operatorId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		ChatOperator op = acc.getOperator(operatorId);
		return op == null? null : op.clone();
	}
	
	@Secured
	public Collection<ChatLog> getActiveChatLogs(String accId, long operatorId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		return acc.getActiveChatsByOperator(operatorId);
	}
	
	@Secured
	public Collection<ChatLog> getAllActiveChatLogs(String accId){
		return getAllActiveChatLogs(accId, null);
	}
	
	@Secured
	public Collection<ChatLog> getAllActiveChatLogs(String accId, Map<String, ChatUpdateData> updatesMap){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		Collection<ChatLog> out = acc.getAllActiveChats(updatesMap);
		return acc.fillOperators(out);
	}
	
	@Secured
	public ChatLog getActiveChatLog(String accId, String chatId, ChatUpdateData updateData){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		ChatLog log = acc.getActiveChat(chatId, updateData);
		return acc.fillOperators(log);
	}
	
	
	@Secured
	public void addComment(String accId, String chatId, long operatorId, String text){
		
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		checkForBlockedOrPaused(accId);
		
		checkNewMsgToAdd(props, text);
		
		acc.addComment(chatId, operatorId, text);
		
		log.info("["+accId+"] "+"OPERATOR comment added: "
				+"chatId="+chatId
				+", opId="+operatorId
				+", size="+(isEmpty(text)? 0 : text.length())
				+", req="+getReqInfoStr());
	}
	
	
	@Secured
	public List<ChatOperator> getRegistredOperators(String accId){
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		return acc.getRegistredOperators();
	}
	
	@Secured
	public List<ChatLogHist> getHistory(String accId, Date date){
		
		ChatsAcc acc = checkUserAccessAndFindChats(accId);
		
		List<ChatLogHist> out = accs.getHistory(accId, date);
		acc.fillOperators(out);
		return out;
	}
	
	@Secured
	public List<Feedback> getFeedbacks(String accId, Date date){
		
		checkUserAccessAndFindChats(accId);
		
		List<Feedback> out = accs.getFeedbacks(accId, date);
		return out;
	}
	
	
	@Secured
	public void setAccBlocked(String accId, boolean val){
		
		checkAccessFor_ADMIN();
		
		boolean done = accs.setBlocked(accId, val);
		
		if(done) log.info("["+accId+"] "+(val? "blocked" : "unblocked")+", req="+getReqInfoStr());
	}
	
	public boolean isAccBlocked(String accId){
		return accs.isBlocked(accId);
	}
	
	@Secured
	public void setAccPaused(String accId, boolean val){
		
		checkAccessFor_ADMIN();
		
		boolean done = accs.setPaused(accId, val);
		
		if(done) log.info("["+accId+"] "+(val? "paused" : "unpaused")+", req="+getReqInfoStr());
	}
	
	public boolean isAccPaused(String accId){
		return accs.isPaused(accId);
	}
	
	
	public boolean isAccExists(String accId){
		ChatsAcc acc = accs.getAcc(accId);
		return acc != null;
	}
	
	
	@Secured
	public void putAccConfig(String accId, Key key, String val) {
		
		checkAccessFor_ADMIN();
		
		ChatsAcc acc = accs.getAcc(accId);
		if(acc == null) return;
		
		acc.putConfig(key, val);
		
		log.info("["+accId+"] "+"put config: "
				+"key="+key
				+", val="+val
				+", req="+getReqInfoStr());
		
	}
	
	@Secured
	public String getAccConfig(String accId, Key key){
		
		checkAccessFor_ADMIN();
		
		ChatsAcc acc = accs.getAcc(accId);
		if(acc == null) return null;
		
		return acc.getStrVal(key);
	}
	
	
	
	private ChatsAcc checkClientAndFindChats(String accId, ClientSession clientSession) throws NoChatAccountException {
		
		if( ! clientSession.containsAccId(accId)) {
			throw new AccountNotAddedToSessionException(accId);
		}
		return accs.findAcc(accId);
	}
	
	
	
	private ChatsAcc checkUserAccessAndFindChats(String accId, PrivilegeType... validTypes) throws AccessDeniedException, NoChatAccountException {
		checkAccessForUser(accId, validTypes);
		return accs.findAcc(accId);
	}
	
	private void checkForBlockedOrPaused(String accId){
		checkForBlocked(accId);
		if(accs.isPaused(accId)) throw new ChatAccountPausedException(accId);
	}
	
	private void checkForBlocked(String accId){
		if(accs.isBlocked(accId)) throw new ChatAccountBlockedException(accId);
	}
	
	private void removeActiveOperators(UserSession session){
		pushToSecurityContext_SYSTEM_USER();
		try {
			long userId = session.userId;
			for (String accId : accs.getAccIds()) {
				removeActiveOperator(accId, userId);
			}
		} finally {
			popUserFromSecurityContext();
		}
	}
	
	
	
	private void sendFeedbackPosted(String accId, String accName, ClientInfo clientInfo, String text, List<String> emails) {
		
		if(isEmpty(emails)) return;
		
		ReqInfo reqInfo = getReqInfo();
		
		try {
			HashMap<String, String> params = new HashMap<>();
			params.put("accName", hasText(accName)? accName : accId);
			params.put("userName", clientInfo.name == null? "" : clientInfo.name);
			params.put("userEmail", clientInfo.email == null? "" : clientInfo.email);
			params.put("reqInfo", reqInfo == null? "" : reqInfo.getFinalRef());
			params.put("text", text == null? "" : text);
			
			String replayTo = clientInfo.email;
			String subject = c.templates.fromTemplate("feedback-subject.ftl", params);
			String html = c.templates.fromTemplate("feedback-text.ftl", params);
			
			SendReq req = new SendReq(emails, subject, html);
			req.replyTo(replayTo);
			
			c.mails.sendAsync(req);
			
		} catch (Exception e) {
			log.error("can't send email", e);
		}
	}
	
	
	
	
	
	private static void checkAccessForUser(String accId, PrivilegeType... validTypes){
		boolean hasAccess = hasAccessForUser(accId, validTypes);
		if( ! hasAccess){
			throw new AccessDeniedException();
		}
	}
	
	
	private static boolean hasAccessForUser(String accId, PrivilegeType... validTypes){
		 
		if(hasAccessFor(UserRole.ADMIN)) return true;
	
		User user = findUserFromSecurityContext();
		Map<String, Set<PrivilegeType>> privilegesByAcc = user.getParam(PRIVILEGES_PARAM);
		Set<PrivilegeType> set = privilegesByAcc.get(accId);
		
		if(isEmpty(validTypes)){
			return ! isEmpty(set);
		} 
		
		for(PrivilegeType valid : validTypes){
			if(set.contains(valid)){
				return true;
			}
		}
		return false;
		
	}





}
