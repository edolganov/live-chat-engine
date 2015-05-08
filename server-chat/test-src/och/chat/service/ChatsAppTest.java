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

import static och.api.model.PropKey.*;
import static och.api.model.chat.account.PrivilegeType.*;
import static och.api.model.client.ClientInfo.*;
import static och.api.model.user.SecurityContext.*;
import static och.chat.service.SecurityService.*;
import static och.comp.chats.common.StoreOps.*;
import static och.util.ConcurrentUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import och.api.exception.InvalidInputException;
import och.api.exception.ValidationException;
import och.api.exception.chat.ChatAccountBlockedException;
import och.api.exception.chat.ChatAccountPausedException;
import och.api.exception.chat.NoAvailableOperatorException;
import och.api.exception.chat.NoChatAccountException;
import och.api.exception.client.AccountNotAddedToSessionException;
import och.api.exception.user.AccessDeniedException;
import och.api.exception.web.MaxSessionsCountByIpException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatLogHist;
import och.api.model.chat.ChatOperator;
import och.api.model.chat.ChatUser;
import och.api.model.chat.Feedback;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.chat.config.Key;
import och.api.model.client.ClientInfo;
import och.api.model.client.ClientSession;
import och.api.model.user.User;
import och.api.model.web.ReqInfo;
import och.api.remote.chats.InitUserTokenReq;
import och.api.remote.chats.RemoveUserSessionReq;
import och.api.remote.chats.UpdateUserSessionsReq;
import och.comp.chats.ChatsAccService;
import och.comp.mail.MailService;
import och.comp.mail.stub.SenderStub;
import och.service.props.impl.MapProps;
import och.util.StringUtil;
import och.util.concurrent.AsyncListener;

import org.junit.Before;
import org.junit.Test;

import test.BaseTest;
import web.MockHttpServletRequest;
import web.MockServletContext;


public class ChatsAppTest extends BaseTest implements AsyncListener {
	
	public static final String TEMPLATES_PATH = "./server-chat/web/WEB-INF/templates";
	
	long reloadAccountsDelay = 50;
	
	MockServletContext servletContext = new MockServletContext();
	MapProps props = new MapProps();
	ChatsApp app;
	ChatsService chats;
	SecurityService security;
	File accountsDir;
	
	String ip1 = "1.1.1.1";
	String ip2 = "2.2.2.2";
	
	String userAgent1 = "userAgent1";
	String userAgent2 = "userAgent2";
	
	String session1 = "1";
	String session2 = "2";
	String session3 = "3";
	
	ClientSession client1 = new ClientSession(session1, ip1, userAgent1);
	ClientSession client2 = new ClientSession(session2, ip1, userAgent2);
	ClientSession client3 = new ClientSession(session3, ip2, userAgent1);
	
	ChatOperator operator1 = new ChatOperator(1, "name 1");
	ChatOperator operator2 = new ChatOperator(2, "name 2");
	
	String text1 = "привет";
	String text2 = "\n123\t";
	String text3 = "text3";
	
	CopyOnWriteArrayList<Future<?>> futures = new CopyOnWriteArrayList<>();
	SenderStub mailSender = new SenderStub();
	
	@Before
	public void before() throws Exception {
		
		accountsDir = new File(TEST_DIR, "accounts");
		
		props.putVal(chats_rootDir, accountsDir.getPath());
		props.putVal(chats_reloadDelay, reloadAccountsDelay);
		props.putVal(templates_path, TEMPLATES_PATH);
		props.putVal(mail_storeToDisc, true);
		props.putVal(mail_storeDir, TEST_PATH+"/mails");
		
		ChatsAccService accs = new ChatsAccService(new File(props.getStrVal(chats_rootDir)), props, this);
		MailService mail = new MailService(mailSender, props, this);
		
		app = ChatsApp.create(props, servletContext, accs, mail);
		chats = app.chats;
		security = app.security;
		
	}
	
	@Override
	public void onFutureEvent(Future<?> future) {
		futures.add(future);
	}
	
	
	
	@Test
	public void test_all() throws Exception{
		
		
		//sec
		test_sec_create_clientSession_with_exists_httpSession();
		test_sec_create_max_sessions_for_ip();
		test_sec_create_user_session();
		test_sec_updateUserSessions();
		test_sec_logout();
		
		//chat
		test_chat_sec();
		test_chat_create_remove_update_operators_on_userSession_change();
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			test_chat_start_with_no_account();
			test_chat_start_wtih_no_active_operators();
			test_chat_auto_reload_accounts();
			test_chat_close_chats_on_close_session();
			test_chat_add_comment_to_no_account();
			test_chat_init_again();
			test_chat_getChatLog();
			test_chat_maxClientMsgSizeAndClientAdd();
			test_chat_addFeedback();
			test_chat_updateUserContacts();
			test_chat_restoreOldChatsAfterCrash();
			
			test_chat_blocked();
			test_chat_paused();
			test_chat_clientRefHist();
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	private void test_chat_clientRefHist() throws Exception {

		String accId = "test_clientRefHist";
		chats.createAcc(accId);
		chats.putOperator(accId, operator1);
		chats.setActiveOperator(accId, operator1.id);
		
		ClientSession clientSession = createClientSession(accId);
		
		//create chat
		try {
			ReqInfo.putInfoToThreadLocal(new ReqInfo(null, null, null, "some1", null));
			chats.addComment(accId, clientSession, "1");
			chats.addComment(accId, clientSession, "2");
			
			ReqInfo.putInfoToThreadLocal(new ReqInfo(null, null, null, "some2", null));
			chats.addComment(accId, clientSession, "3");
			chats.addComment(accId, clientSession, "4");
		
		} finally {
			ReqInfo.removeInfoFromThreadLocal();
		}
		
		//create hist
		chats.closeChats(clientSession);
		lastFrom(futures).get();
		
		//check hist
		List<ChatLogHist> history = chats.getHistory(accId, new Date());
		assertEquals(1, history.size());
		
		Map<Integer, String> clientRefs = history.get(0).clientRefs;
		assertEquals(2, clientRefs.size());
		assertEquals("some1", clientRefs.get(0));
		assertEquals("some2", clientRefs.get(2));
	}
	
	
	public void test_chat_restoreOldChatsAfterCrash() throws Exception {
		
		String accId1 = "restoreOldChatsAfterCrash";
		
		chats.createAcc(accId1);
		chats.putOperator(accId1, operator1);
		chats.setActiveOperator(accId1, operator1.id);
		
		HttpSession session = null;
		ClientSession clientSession = null;
		String chatId = null;
		
		//create chat
		{
			MockHttpServletRequest req = mockReq();
			chats.checkChatAndInitClientSession(req, mockResp(), accId1);
			session = req.getSession(false);
			clientSession = (ClientSession)session.getAttribute(CLIENT_INFO);
			assertEquals(list(accId1), clientSession.getAccIds());
			
			chatId = chats.addComment(accId1, clientSession, "123").id;
			chats.addOperatorToChat(accId1, chatId, operator1.id, 0);
			chats.addComment(accId1, chatId, operator1.id, "345");
			
			ChatLog chatLog = chats.getChatLog(accId1, clientSession);
			assertEquals(2, chatLog.users.size());
			assertEquals(2, chatLog.messages.size());
		}
		
		//new app
		ChatsAccService accs = new ChatsAccService(new File(props.getStrVal(chats_rootDir)), props, this);
		MailService mail = new MailService(mailSender, props, this);
		ChatsApp app2 = ChatsApp.create(props, servletContext, accs, mail);
		ChatsService chats2 = app2.chats;
		//restore chat by id
		{
			MockHttpServletRequest req = mockReq();
			chats2.checkChatAndInitClientSession(req, mockResp(), accId1, chatId);
			
			HttpSession session2 = req.getSession(false);
			ClientSession clientSession2 = (ClientSession)session2.getAttribute(CLIENT_INFO);
			assertEquals(list(accId1), clientSession2.getAccIds());
			
			assertFalse(session.getId().equals(session2.getId()));
			assertFalse(clientSession.sessionId.equals(clientSession2.sessionId));
			
			assertNull(chats2.getChatLog(accId1, clientSession));
			ChatLog chatLog = chats2.getChatLog(accId1, clientSession2);
			assertEquals(chatId, chatLog.id);
			assertEquals(2, chatLog.users.size());
			assertEquals(2, chatLog.messages.size());
			
			//check all models
			assertEquals(1, chats2.getAllActiveChatLogs(accId1).size());
			assertEquals(1, chats2.getActiveChatLogs(accId1, operator1.id).size());
		}
		//no second restore for other session
		{
			MockHttpServletRequest req = mockReq();
			chats2.checkChatAndInitClientSession(req, mockResp(), accId1, chatId);
			ClientSession newClientSession = (ClientSession)req.getSession(false).getAttribute(CLIENT_INFO);
			assertNull(chats2.getChatLog(accId1, newClientSession));
		}
		//wrong ip - no restore
		{
			MockHttpServletRequest req = new MockHttpServletRequest();
			req.remoteAddr = "some";
			chats2.checkChatAndInitClientSession(req, mockResp(), accId1, chatId);
			ClientSession newClientSession = (ClientSession)req.getSession(false).getAttribute(CLIENT_INFO);
			assertNull(chats2.getChatLog(accId1, newClientSession));
		}
		//wrong agent - no restore
		{
			MockHttpServletRequest req = new MockHttpServletRequest();
			req.setUserAgent("1234");
			chats2.checkChatAndInitClientSession(req, mockResp(), accId1, chatId);
			ClientSession newClientSession = (ClientSession)req.getSession(false).getAttribute(CLIENT_INFO);
			assertNull(chats2.getChatLog(accId1, newClientSession));
		}
		
		//close all active chats - no more restore
		chats.closeChats(clientSession);
		getAndClearAllFutures(futures);
		{
			MockHttpServletRequest req = mockReq();
			chats2.checkChatAndInitClientSession(req, mockResp(), accId1, chatId);
			ClientSession newClientSession = (ClientSession)req.getSession(false).getAttribute(CLIENT_INFO);
			assertNull(chats2.getChatLog(accId1, newClientSession));
		}
		
	}
	
	
	
	private void test_chat_updateUserContacts() throws Exception {
		
		String accId1 = "updateUserContacts-1";
		String accId2 = "updateUserContacts-2";
		String accId3 = "updateUserContacts-3";
		int userId = 1;
		int userId2 = 2;
		String initEmail = "e1";
		String newEmail = "e2";
		
		chats.createAcc(accId1);
		chats.createAcc(accId2);
		chats.createAcc(accId3);
		
		chats.putOperator(accId1, new ChatOperator(userId, null, initEmail));
		chats.putOperator(accId2, new ChatOperator(userId, null, initEmail));
		chats.putOperator(accId3, new ChatOperator(userId2, null, initEmail));
		
		assertEquals(initEmail, chats.getOperator(accId1, userId).email);
		assertEquals(initEmail, chats.getOperator(accId2, userId).email);
		assertEquals(initEmail, chats.getOperator(accId3, userId2).email);
		
		//update
		chats.updateUserContact(userId, newEmail);
		getAndClearAllFutures(futures);
		
		assertEquals(newEmail, chats.getOperator(accId1, userId).email);
		assertEquals(newEmail, chats.getOperator(accId2, userId).email);
		assertEquals(initEmail, chats.getOperator(accId3, userId2).email);
	}
	
	
	private void test_chat_addFeedback() throws Exception {
		

		
		String accId = randomUUID();
		
		String maxStr = randomStr(props.getIntVal(chats_maxMsgSize));
		chats.createAcc(accId);
		chats.putOperator(accId, new ChatOperator(1, "some", "op1@mail.com"));
		chats.putOperator(accId, new ChatOperator(2, "some", "op2@mail.com"));
		
		//wrong text
		try {
			chats.addFeedback(accId, randomClientInfo(), maxStr + "1");			
		}catch(ValidationException e){
			//ok
		}
		
		int feedbacksCount = 0;
		
		//valid text
		{
			mailSender.tasks.clear();
			
			ClientInfo client = randomClientInfo();
			client.email = "b@b.bb";
			client.name = "some name";
			String text = "my text\n\n\n123";
			chats.addFeedback(accId, client, text);
			feedbacksCount++;
			getAndClearAllFutures(futures);
			
			List<Feedback> feedbacks = chats.getFeedbacks(accId, new Date());
			assertEquals(feedbacksCount, feedbacks.size());
			
			//check mails
			assertEquals(2, mailSender.tasks.size());
			{
				String mailText = mailSender.tasks.get(0).msg.text;
				assertTrue(mailText.contains(text));
				assertTrue(mailText.contains(client.email));
				assertTrue(mailText.contains(client.name));
			}
		}
		
		
		//dissable notifications
		{
			mailSender.tasks.clear();
			
			chats.putAccConfig(accId, Key.feedback_notifyOpsByEmail, "false");
			
			ClientInfo client = randomClientInfo();
			client.email = "b@b.bb";
			client.name = "some name";
			String text = "my text\n\n\n123";
			chats.addFeedback(accId, client, text);
			feedbacksCount++;
			getAndClearAllFutures(futures);
			
			List<Feedback> feedbacks = chats.getFeedbacks(accId, new Date());
			assertEquals(feedbacksCount, feedbacks.size());
			
			//check mails
			assertEquals(0, mailSender.tasks.size());
			
			
			chats.putAccConfig(accId, Key.feedback_notifyOpsByEmail, "true");
			chats.addFeedback(accId, client, text);
			feedbacksCount++;
			getAndClearAllFutures(futures);
			assertEquals(2, mailSender.tasks.size());
		}
		
		//set acc name
		{
			mailSender.tasks.clear();
			
			String accName = "renamed-acc";
			chats.putAccConfig(accId, Key.name, accName);
			
			ClientInfo client = randomClientInfo();
			client.email = "b@b.bb";
			client.name = "some name";
			String text = "my text\n\n\n123";
			chats.addFeedback(accId, client, text);
			feedbacksCount++;
			getAndClearAllFutures(futures);
			
			List<Feedback> feedbacks = chats.getFeedbacks(accId, new Date());
			assertEquals(feedbacksCount, feedbacks.size());
			
			//check mails
			assertEquals(2, mailSender.tasks.size());
			{
				String mailText = mailSender.tasks.get(0).msg.text;
				assertTrue(mailText.contains(text));
				assertTrue(mailText.contains(client.email));
				assertTrue(mailText.contains(client.name));
			}
		}
		
		//empty fields
		{
			mailSender.tasks.clear();
			
			ClientInfo client = randomClientInfo();
			client.email = "some";
			client.name = null;
			String text = "123";
			chats.addFeedback(accId, client, text);
			feedbacksCount++;
			getAndClearAllFutures(futures);
			
			List<Feedback> feedbacks = chats.getFeedbacks(accId, new Date());
			assertEquals(feedbacksCount, feedbacks.size());
			
			//check mails
			assertEquals(2, mailSender.tasks.size());
		}
		
		//html escape
		{
			mailSender.tasks.clear();
			
			String accName = "<b>renamed-acc</b>";
			chats.putAccConfig(accId, Key.name, accName);
			
			ClientInfo client = randomClientInfo();
			client.email = "<b>@b.bb";
			client.name = "<b>123</b>";
			String text = "<b>my text\n\n\n123</b>";
			chats.addFeedback(accId, client, text);
			feedbacksCount++;
			getAndClearAllFutures(futures);
			
			List<Feedback> feedbacks = chats.getFeedbacks(accId, new Date());
			assertEquals(feedbacksCount, feedbacks.size());
			
			//check mails
			assertEquals(2, mailSender.tasks.size());
			{
				String mailText = mailSender.tasks.get(0).msg.text;
				assertTrue( ! mailText.contains(text));
				assertTrue( ! mailText.contains(client.email));
				assertTrue( ! mailText.contains(client.name));
				assertTrue( ! mailText.contains(accId));
				assertTrue( ! mailText.contains(accName));
			}
		}

	}
	
	
	private void test_chat_maxClientMsgSizeAndClientAdd() throws Exception {
		
		String accountId = randomSimpleId();
		chats.createAcc(accountId);
		chats.putOperator(accountId, operator1);
		chats.setActiveOperator(accountId, operator1.id);
	
		ClientSession clientSession = createClientSession(accountId);
		
		
		//add
		String invalidText = StringUtil.randomStr(chats_maxMsgSize.intDefVal()+1);
		
		try {
			chats.addComment(accountId, clientSession, invalidText);
			fail_exception_expected();
		}catch(ValidationException e){
			//ok
		}
		
		ChatLog chatLog = chats.getChatLog(accountId, clientSession);
		assertNull(chatLog);
		
	}
	
	private void test_chat_paused() throws Exception {
		
		long userId1 = 100;
		long userId2 = 200;

		
		String accId = "test_chat_paused";
		chats.createAcc(accId);
		assertFalse(chats.isAccPaused(accId));
		
		chats.setAccPaused(accId, true);
		assertTrue(chats.isAccPaused(accId));
		
		//можем добавлять оператора в акк и делать его активным
		chats.putOperator(accId, new ChatOperator(userId1));
		chats.setActiveOperator(accId, userId1);
		
		chats.setAccPaused(accId, false);
		assertFalse(chats.isAccPaused(accId));
		
		//создаем клиентский чат
		String chatId;
		ClientSession clientSession;
		{

			clientSession = createClientSession(accId);
			
			chats.addComment(accId, clientSession, "some");
			ChatLog chatLog = chats.getChatLog(accId, clientSession);
			chatId = chatLog.id;
			assertEquals(1, chatLog.messages.size());
		}
		
		//добавляем коммент при активном чате
		{
			chats.addOperatorToChat(accId, chatId, userId1, 0);
			chats.addComment(accId, chatId, userId1, "some2");
			assertEquals(2, chats.getChatLogById(accId, chatId).messages.size());
		}
		
		chats.setAccPaused(accId, true);
		
		//не можем добавить коммент при запаузенном чате
		try {
			chats.addComment(accId, chatId, userId1, "some");
			fail_exception_expected();
		}catch(ChatAccountPausedException e){
			//ok
		}
		
		
		//не можем добавлять новых юзеров к чату
		{
			chats.putOperator(accId, new ChatOperator(userId2));
			chats.setActiveOperator(accId, userId2);
			
			try {
				chats.addOperatorToChat(accId, chatId, userId2, 0);
				fail_exception_expected();
			}catch(ChatAccountPausedException e){
				//ok
			}
		}
		
		
		//не можем добавлять юзерские комменты
		{
			try {
				chats.addComment(accId, clientSession, "some");
			}catch(ChatAccountPausedException e){
				//ok
			}
		}
		
		//можем добавлять фидбеки
		{
			chats.addFeedback(accId, randomClientInfo(), "some");
		}
		
		//проверяем что старт нового инстанса содержит нужные нам данные
		{
			ChatsService chats2 =  ChatsApp.create(props, servletContext).chats;
			assertTrue(chats2.isAccPaused(accId));
		}
		
		
		chats.setAccPaused(accId, false);
		
		//как только чат разпаузился -- можем снова работать операторами
		{
			chats.addOperatorToChat(accId, chatId, userId2, 1);
			chats.addComment(accId, chatId, userId1, "some");
			chats.addComment(accId, chatId, userId2, "some");
			assertEquals(4, chats.getChatLogById(accId, chatId).messages.size());
		}
		
	}
	
	
	private void test_chat_blocked() throws Exception {
		
		long userId1 = 100;
		long userId2 = 200;

		
		String accId = "test_chat_blocked";
		chats.createAcc(accId);
		assertFalse(chats.isAccBlocked(accId));
		
		chats.setAccBlocked(accId, true);
		assertTrue(chats.isAccBlocked(accId));
		
		//можем добавлять оператора в акк и делать его активным
		chats.putOperator(accId, new ChatOperator(userId1));
		chats.setActiveOperator(accId, userId1);
		
		chats.setAccBlocked(accId, false);
		assertFalse(chats.isAccBlocked(accId));
		
		//создаем клиентский чат
		String chatId;
		ClientSession clientSession;
		{
			clientSession = createClientSession(accId);
			
			chats.addComment(accId, clientSession, "some");
			ChatLog chatLog = chats.getChatLog(accId, clientSession);
			chatId = chatLog.id;
			assertEquals(1, chatLog.messages.size());
		}
		
		//добавляем коммент при активном чате
		{
			chats.addOperatorToChat(accId, chatId, userId1, 0);
			chats.addComment(accId, chatId, userId1, "some2");
			assertEquals(2, chats.getChatLogById(accId, chatId).messages.size());
		}
		
		chats.setAccBlocked(accId, true);
		
		//не можем добавить коммент при блокированном чате
		try {
			chats.addComment(accId, chatId, userId1, "some");
			fail_exception_expected();
		}catch(ChatAccountBlockedException e){
			//ok
		}
		
		
		//не можем добавлять новых юзеров к чату
		{
			chats.putOperator(accId, new ChatOperator(userId2));
			chats.setActiveOperator(accId, userId2);
			
			try {
				chats.addOperatorToChat(accId, chatId, userId2, 0);
				fail_exception_expected();
			}catch(ChatAccountBlockedException e){
				//ok
			}
		}
		
		
		//не можем добавлять юзерские комменты к блокированному чату
		{
			try {
				chats.addComment(accId, clientSession, "some");
			}catch(ChatAccountBlockedException e){
				//ok
			}
		}
		
		//не можем добавлять фидбеки
		{
			try {
				chats.addFeedback(accId, randomClientInfo(), "some");
			}catch(ChatAccountBlockedException e){
				//ok
			}
		}
		
		//проверяем что старт нового инстанса содержит нужные нам данные
		{
			ChatsService chats2 =  ChatsApp.create(props, servletContext).chats;
			assertTrue(chats2.isAccBlocked(accId));
		}
		
		
		
		chats.setAccBlocked(accId, false);
		
		//как только чат разблокировался -- можем снова работать операторами
		{
			chats.addOperatorToChat(accId, chatId, userId2, 1);
			chats.addComment(accId, chatId, userId1, "some");
			chats.addComment(accId, chatId, userId2, "some");
			assertEquals(4, chats.getChatLogById(accId, chatId).messages.size());
		}
		
	}
	
	
	private void test_chat_create_remove_update_operators_on_userSession_change() throws Exception {
		
		String accId1 = "acc1-"+randomSimpleId();
		String accId2 = "acc2-"+randomSimpleId();
		long userId = 100;
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			chats.createAcc(accId1);
			chats.createAcc(accId2);
			chats.putOperator(accId1, new ChatOperator(userId));
			chats.putOperator(accId2, new ChatOperator(userId));
			
			assertFalse(chats.isActiveOperator(accId1, userId));
		}finally {
			popUserFromSecurityContext();
		}
		
		
		String token = "some";
		MockHttpServletRequest req = mockReq();
		String userIp = req.getRemoteAddr();
		String userUserAgent = "someAgent";
		req.setUserAgent(userUserAgent);
		
		//init session
		Map<String, Set<PrivilegeType>> privsByAcc = new HashMap<>();
		privsByAcc.put(accId1, set(CHAT_OPERATOR));
		security.initUserToken(new InitUserTokenReq(token, userId, userIp, userUserAgent, privsByAcc));
		security.initUserSession(req, token);
		
		HttpSession session = req.getSession(false);
		assertNotNull(session);
		security.sessionCreated(new HttpSessionEvent(session));
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			assertFalse(chats.isActiveOperator(accId1, userId));
			assertFalse(chats.isActiveOperator(accId2, userId));
		}finally {
			popUserFromSecurityContext();
		}
		

		//save session to holder
		app.sessionsHolder.sessionCreated(new HttpSessionEvent(session));
		
		//update session
		privsByAcc.remove(accId1);
		privsByAcc.put(accId2, set(CHAT_OPERATOR));
		security.updateUserSessions(new UpdateUserSessionsReq(userId, privsByAcc));
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			assertTrue(chats.isOperator(accId1, userId));
			assertTrue(chats.isOperator(accId2, userId));
			
			assertFalse(chats.isActiveOperator(accId1, userId));
			assertFalse(chats.isActiveOperator(accId2, userId));
		}finally {
			popUserFromSecurityContext();
		}
		
		//обновление активности оператора для одного акка
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			chats.setActiveOperator(accId1, userId);
			chats.setActiveOperator(accId2, userId);
			assertTrue(chats.isActiveOperator(accId1, userId));
			assertTrue(chats.isActiveOperator(accId2, userId));
			
			//удаляем привилегию только в одной сессии
			Map<String, Set<PrivilegeType>> newPrivs = new HashMap<>();
			newPrivs.put(accId1, set(CHAT_OPERATOR));
			newPrivs.put(accId2, set(CHAT_OPERATOR));
			newPrivs.put("some-other-acc", set(CHAT_OPERATOR));
			security.updateUserSessions(new UpdateUserSessionsReq(userId, privsByAcc));
			
			assertTrue(chats.isActiveOperator(accId1, userId));
			assertTrue(chats.isActiveOperator(accId2, userId));
		}finally {
			popUserFromSecurityContext();
		}
		
		
		//remove session
		security.sessionDestroyed(new HttpSessionEvent(session));
		pushToSecurityContext_SYSTEM_USER();
		try {
			assertFalse(chats.isActiveOperator(accId1, userId));
			assertFalse(chats.isActiveOperator(accId2, userId));
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	
	
	private void test_sec_logout() throws Exception {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			long user1 = 1;
			long user2 = 2;
			
			String accId1 = "test_sec_logout-1";
			String accId2 = "test_sec_logout-2";
			
			//create accs
			{
				app.chats.createAcc(accId1);
				app.chats.createAcc(accId2);
			}
			
			//register users
			{
				app.chats.putOperator(accId1, new ChatOperator(user1));
				app.chats.putOperator(accId2, new ChatOperator(user2));
				
				assertFalse(app.chats.isActiveOperator(accId1, user1));
				assertFalse(app.chats.isActiveOperator(accId2, user2));
			}
			
			//login user
			MockHttpServletRequest req = mockReq();
			String token1 = "valid1";
			{
				String userIp = req.getRemoteAddr();
				String userUserAgent = "someAgent";
				req.setUserAgent(userUserAgent);
				
				Map<String, Set<PrivilegeType>> initPrivs = map(accId1, set(PrivilegeType.CHAT_OPERATOR));
				security.initUserToken(new InitUserTokenReq(token1, user1, userIp, userUserAgent, initPrivs));
				security.initUserSession(req, token1);
				app.sessionsHolder.sessionCreated(new HttpSessionEvent(req.getSession(false)));
				
				//юзер стал активным каскадно после секьюрити сервиса - отключили эту фишку
				//assertTrue(app.chats.isActiveOperator(accId1, user1));
				assertFalse(app.chats.isActiveOperator(accId1, user1));
				
				//делаем активным юзера через явный вызов апи чатов
				assertFalse(app.chats.isActiveOperator(accId2, user2));
				app.chats.setActiveOperator(accId2, user2);
				assertTrue(app.chats.isActiveOperator(accId2, user2));
			}
			
			
			//logout user
			{
				security.removeUserSession(new RemoveUserSessionReq(token1));
				assertFalse(app.chats.isActiveOperator(accId1, user1));
				assertTrue(app.chats.isActiveOperator(accId2, user2));
			}
			
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	private void test_sec_updateUserSessions() throws Exception {
		
		MockHttpServletRequest req = mockReq();
		String userIp = req.getRemoteAddr();
		String userUserAgent = "someAgent";
		req.setUserAgent(userUserAgent);
		
		String token = "valid";
		long userId = 99;
		
		//init session
		Map<String, Set<PrivilegeType>> initPrivs = new HashMap<>();
		initPrivs.put("test", new HashSet<PrivilegeType>());
		security.initUserToken(new InitUserTokenReq(token, userId, userIp, userUserAgent, initPrivs));
		assertEquals(userId, security.initUserSession(req, token));
		
		
		User user1 = security.getUserFromSession(req);
		Map<String, Set<PrivilegeType>> privs1 = user1.getParam(PRIVILEGES_PARAM);
		assertNotNull(privs1.get("test"));
		
		//add session to holder
		app.sessionsHolder.sessionCreated(new HttpSessionEvent(req.getSession(false)));
		
		
		//update session
		Map<String, Set<PrivilegeType>> newPrivs = new HashMap<>();
		newPrivs.put("test2", new HashSet<PrivilegeType>());
		security.updateUserSessions(new UpdateUserSessionsReq(userId, newPrivs));
		
		//check updated session
		User user2 = security.getUserFromSession(req);
		Map<String, Set<PrivilegeType>> privs2 = user2.getParam(PRIVILEGES_PARAM);
		assertNotSame(privs1, privs2);
		assertNull(privs2.get("test"));
		assertNotNull(privs2.get("test2"));
		
		
		//remove privs
		security.updateUserSessions(new UpdateUserSessionsReq(userId, null));
		User user3 = security.getUserFromSession(req);
		Map<String, Set<PrivilegeType>> privs3 = user3.getParam(PRIVILEGES_PARAM);
		assertNotNull(privs3);
		assertTrue(privs3.size() == 0);
	}
	
	
	
	
	
	
	private void test_chat_sec() throws Exception {
		
		String accId = randomSimpleId();
		
		//create
		{
			try {
				chats.createAcc(accId);
				fail_exception_expected();
			}catch (AccessDeniedException e) {
				//ok
			}
			
			pushToSecurityContext_SYSTEM_USER();
			try {
				chats.createAcc(accId);
			} finally {
				popUserFromSecurityContext();
			}
		}
		
		//get acc info
		{
			try {
				chats.getAllActiveChatLogs(accId);
				fail_exception_expected();
			}catch (AccessDeniedException e) {
				//ok
			}
			
			//by admin
			pushToSecurityContext_SYSTEM_USER();
			try {
				chats.getAllActiveChatLogs(accId);
			} finally {
				popUserFromSecurityContext();
			}
			
			//by acc user
			Map<String, Set<PrivilegeType>> privs = new HashMap<>();
			privs.put(accId, set(CHAT_OPERATOR));
			User user = new User(operator1.id);
			user.putParam(SecurityService.PRIVILEGES_PARAM, privs);
			
			pushToSecurityContext(user);
			try {
				chats.getAllActiveChatLogs(accId);
			}finally {
				popUserFromSecurityContext();
			}

			
		}

		
		
	}
	
	
	private void test_sec_create_user_session() throws Exception {
		
		MockHttpServletRequest req = mockReq();
		String userIp = req.getRemoteAddr();
		String userUserAgent = "someAgent";
		req.setUserAgent(userUserAgent);
		
		String token1 = "valid1";
		String token2 = "valid2";
		String token3 = "valid3";
		int userId1 = 99;
		int userId2 = 999;
		
		
		//no token in req
		try {
			security.initUserSession(req, null);
			fail_exception_expected();
		}catch (InvalidInputException e) {
			assertExceptionWithText(e, "reqToken is null");
		}
		
		
		//bad token
		try {
			security.initUserSession(req, "unknown");
			fail_exception_expected();
		}catch (InvalidInputException e) {
			assertExceptionWithText(e, "no data by token");
		}
		
		
		//bad client ip
		security.initUserToken(new InitUserTokenReq("bad-ip", userId1, "some-bad-ip", userUserAgent));
		try {
			security.initUserSession(req, "bad-ip");
			fail_exception_expected();
		}catch (AccessDeniedException e) {
			assertExceptionWithText(e,"invalid client ip");
		}
		
		
		//bad user agent
		security.initUserToken(new InitUserTokenReq("bad-user-agent", userId1, userIp, "other-user-agent"));
		try {
			security.initUserSession(req, "bad-user-agent");
			fail_exception_expected();
		}catch (AccessDeniedException e) {
			assertExceptionWithText(e, "invalid userAgent");
		}
		
		
		//token was deleted
		props.putVal(users_waitChatSessionTokenLivetime, 1);
		security.initUserToken(new InitUserTokenReq("token-deleted", userId1, userIp, userUserAgent));
		Thread.sleep(10);
		try {
			security.initUserSession(req, "token-deleted");
			fail_exception_expected();
		}catch (InvalidInputException e) {
			assertExceptionWithText(e, "no data by token");
		}
		
		
		//valid
		props.removeVal(users_waitChatSessionTokenLivetime);
		
		//with privs
		Map<String, Set<PrivilegeType>> initPrivs = new HashMap<>();
		initPrivs.put("test", new HashSet<PrivilegeType>());
		security.initUserToken(new InitUserTokenReq(token1, userId1, userIp, userUserAgent, initPrivs));
		
		security.initUserToken(new InitUserTokenReq(token2, userId2, userIp, userUserAgent));
		security.initUserToken(new InitUserTokenReq(token3, userId1, userIp, userUserAgent));
		
		assertEquals(userId1, security.initUserSession(req, token1));
		//again
		assertEquals(userId1, security.initUserSession(req, token1));
		//other token with same user id
		assertEquals(userId1, security.initUserSession(req, token3));
		
		//token with wrong user id
		try {
			security.initUserSession(req, token2);
			fail_exception_expected();
		}catch (InvalidInputException e) {
			assertExceptionWithText(e, "already has a session for other user");
		}
		
		//check session
		{
			User user = security.getUserFromSession(req);
			assertEquals(userId1, user.id);
			Map<String, Set<PrivilegeType>> privs = user.getParam(PRIVILEGES_PARAM);
			assertNotNull(privs.get("test"));
		}
		
		//add session to holder
		app.sessionsHolder.sessionCreated(new HttpSessionEvent(req.getSession(false)));
		
		//remove session
		security.removeUserSession(new RemoveUserSessionReq(token1));
		assertNull(security.getUserFromSession(req));
		
	}
	
	
	
	private void test_chat_getChatLog(){
		
		String accountId = randomSimpleId();
		chats.createAcc(accountId);
		chats.putOperator(accountId, operator1);
		chats.setActiveOperator(accountId, operator1.id);
		
		
		ClientSession clientSession = createClientSession(accountId);
		
		//before add
		assertNull(chats.getChatLog(accountId, clientSession));
		
		//add
		String text = "test";
		chats.addComment(accountId, clientSession, text);
		ChatLog chatLog = chats.getChatLog(accountId, clientSession);
		assertNotNull(chatLog);
		assertEquals(1, chatLog.messages.size());
		assertEquals(text, chatLog.messages.get(0).text);
		assertEquals(0, chatLog.operators.size());
		
		chats.addOperatorToChat(accountId, chatLog.id, operator1.id, 0);
		chatLog = chats.getChatLog(accountId, clientSession);
		{
			List<ChatUser> users = chatLog.users;
			assertEquals(2, users.size());
			
			String operator = chatLog.operators.get(users.get(1).operatorId);
			assertEquals(operator1.name, operator);
		}
		
	}
	
	
	private void test_chat_init_again() {
		
		String accountId1 = randomSimpleId();
		String accountId2 = randomSimpleId();
		
		MockHttpServletRequest req = mockReq();
		
		chats.createAcc(accountId1);
		chats.createAcc(accountId2);
		
		chats.putOperator(accountId1, operator1);
		chats.putOperator(accountId2, operator1);
		
		chats.setActiveOperator(accountId1, operator1.id);
		chats.setActiveOperator(accountId2, operator1.id);
		
		//double
		chats.checkChatAndInitClientSession(req, mockResp(), accountId1);
		chats.checkChatAndInitClientSession(req, mockResp(), accountId1);
		
		HttpSession session = req.getSession(false);
		ClientSession clientSession = (ClientSession)session.getAttribute(CLIENT_INFO);
		assertEquals(1, clientSession.getAccIds().size());
		assertEquals(list(accountId1), clientSession.getAccIds());
		
		//double
		chats.checkChatAndInitClientSession(req, mockResp(), accountId2);
		chats.checkChatAndInitClientSession(req, mockResp(), accountId2);
		assertEquals(2, clientSession.getAccIds().size());
		assertEquals(list(accountId1, accountId2), clientSession.getAccIds());
	}
	
	
	
	private void test_chat_add_comment_to_no_account() {
		
		String accountId = randomSimpleId();
		
		chats.createAcc(accountId);
		chats.putOperator(accountId, operator1);
		chats.setActiveOperator(accountId, operator1.id);
		
		ClientSession clientSession = createClientSession(accountId);
		
		chats.removeAcc(accountId);
		
		try {
			chats.addComment(accountId, clientSession, "123");
			fail_exception_expected();
		}catch (NoChatAccountException e) {
			//ok
		}
	}



	private void test_chat_close_chats_on_close_session() throws Exception {
		
		String accId1 = randomSimpleId();
		String accId2 = randomSimpleId();
		String accId3 = randomSimpleId();
		
		MockHttpServletRequest req = mockReq();
		req.remoteAddr = ip2;
		chats.createAcc(accId1);
		chats.createAcc(accId2);
		chats.createAcc(accId3);
		
		chats.putOperator(accId1, operator1);
		chats.putOperator(accId2, operator1);
		chats.putOperator(accId3, operator1);
		
		chats.setActiveOperator(accId1, operator1.id);
		chats.setActiveOperator(accId2, operator1.id);
		chats.setActiveOperator(accId3, operator1.id);
		
		chats.checkChatAndInitClientSession(req, mockResp(), accId1);
		chats.checkChatAndInitClientSession(req, mockResp(), accId2);
		
		HttpSession session = req.getSession(false);
		ClientSession clientSession = (ClientSession)session.getAttribute(CLIENT_INFO);
		assertNotNull(clientSession);
		assertEquals(2, clientSession.getAccIds().size());
		assertEquals(list(accId1, accId2), clientSession.getAccIds());
		
		chats.addComment(accId1, clientSession, "123");
		chats.addComment(accId2, clientSession, "345");
		try {
			chats.addComment(accId3, clientSession, "345");
			fail_exception_expected();
		}catch (AccountNotAddedToSessionException e) {
			//ok
		}
		
		//close by event 
		app.security.sessionDestroyed(new HttpSessionEvent(session));
		try {
			chats.addComment(accId1, clientSession, "123");
			fail_exception_expected();
		}catch (AccountNotAddedToSessionException e) {
			//ok
		}
		try {
			chats.addComment(accId2, clientSession, "345");
			fail_exception_expected();
		}catch (AccountNotAddedToSessionException e) {
			//ok
		}
		
		//close by method
		chats.checkChatAndInitClientSession(req, mockResp(), accId1);
		chats.addComment(accId1, clientSession, "123");
		chats.closeChats(clientSession);
		try {
			chats.addComment(accId1, clientSession, "123");
			fail_exception_expected();
		}catch (AccountNotAddedToSessionException e) {
			//ok
		}
	}
	
	

	private void test_chat_auto_reload_accounts() throws Exception {
		
		String accountId = randomSimpleId();
		
		try {
			chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
			fail_exception_expected();
		}catch (NoChatAccountException e) {
			//ok
		}
		
		File account2Dir = new File(accountsDir, getAccountDirName(accountId));
		assertFalse(account2Dir.exists());
		assertTrue(account2Dir.mkdir());
		
		Thread.sleep(reloadAccountsDelay*2);
		
		chats.putOperator(accountId, operator1);
		chats.setActiveOperator(accountId, operator1.id);
		chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
		
	}
	
	private void test_chat_start_wtih_no_active_operators(){
		
		String accountId = randomSimpleId();
		
		//no operators
		chats.createAcc(accountId);
		try {
			chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {/*ok*/}
		
		
		//no operators again
		chats.setActiveOperator(accountId, operator1.id);
		try {
			chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {/*ok*/}
		
		
		//no active operators
		chats.putOperator(accountId, operator1);
		try {
			chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {/*ok*/}
		
		
		//1 active operator
		chats.setActiveOperator(accountId, operator1.id);
		chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
		
	
		
		//no active operators
		chats.removeActiveOperator(accountId, operator1.id);
		try {
			chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {/*ok*/}
		
		
		chats.setActiveOperator(accountId, operator1.id);
		chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
		
		
		
		//no operators
		chats.removeOperator(accountId, operator1.id);
		try {
			chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {/*ok*/}
		
	}
	
	private void test_chat_start_with_no_account(){
		
		String accountId = randomSimpleId();
		
		try {
			chats.checkChatAndInitClientSession(mockReq(), mockResp(), accountId);
			fail_exception_expected();
		}catch (NoChatAccountException e) {
			//ok
		}
		
	}
	
	
	private void test_sec_create_clientSession_with_exists_httpSession(){
		
		MockHttpServletRequest req = mockReq();
		//first call
		{
			security.initClientSessionForAcc(req, "some");
			
			ClientSession clientSession = (ClientSession)req.getSession().getAttribute(CLIENT_INFO);
			assertNotNull(clientSession);
			assertTrue(clientSession.containsAccId("some"));
		}
		//second call
		{
			security.initClientSessionForAcc(req, "some");
			security.initClientSessionForAcc(req, "some2");
			
			ClientSession clientSession = (ClientSession)req.getSession().getAttribute(CLIENT_INFO);
			assertTrue(clientSession.containsAccId("some"));
			assertTrue(clientSession.containsAccId("some2"));
		}
		//remove session obj
		{
			req.getSession().removeAttribute(CLIENT_INFO);
			
			security.initClientSessionForAcc(req, "some");
			ClientSession clientSession = (ClientSession)req.getSession().getAttribute(CLIENT_INFO);
			assertNotNull(clientSession);
			assertTrue(clientSession.containsAccId("some"));
			assertFalse(clientSession.containsAccId("some2"));
		}
		
		
	}

	private void test_sec_create_max_sessions_for_ip() {
		
		//default max count
		Integer maxCount = security.getMaxSessionsCountForChatClient(ip1);
		assertEquals(chats_maxSessionsByIP.intDefVal(), maxCount);
		
		MockHttpServletRequest lastReq = null;
		for (int i = 0; i < maxCount; i++) {
			lastReq = mockReq();
			lastReq.remoteAddr = ip1;
			app.sessionsCounter.requestInitialized(new ServletRequestEvent(servletContext, lastReq));
			security.initClientSessionForAcc(lastReq, null);
			app.sessionsCounter.sessionCreated(new HttpSessionEvent(lastReq.getSession(false)));
		}
		
		//try more then max
		try {
			MockHttpServletRequest req = mockReq();
			req.remoteAddr = ip1;
			app.sessionsCounter.requestInitialized(new ServletRequestEvent(servletContext, req));
			security.initClientSessionForAcc(req, null);
			
			fail_exception_expected();
		}catch (MaxSessionsCountByIpException e) {
			//ok
		}
		
		//try other
		{
			MockHttpServletRequest req = mockReq();
			req.remoteAddr = ip2;
			app.sessionsCounter.requestInitialized(new ServletRequestEvent(servletContext, req));
			security.initClientSessionForAcc(req, null);
		}
		
		//destroy one
		{
			app.sessionsCounter.sessionDestroyed(new HttpSessionEvent(lastReq.getSession(false)));
			MockHttpServletRequest req = mockReq();
			req.remoteAddr = ip1;
			app.sessionsCounter.requestInitialized(new ServletRequestEvent(servletContext, req));
			security.initClientSessionForAcc(req, null);
		}
		
		
		//change max count
		int newMaxForAll = chats_maxSessionsByIP.intDefVal() + 10;
		int newMaxForIp1 = newMaxForAll + 10;
		props.putVal(chats_maxSessionsByIP, newMaxForAll);
		assertEquals(newMaxForAll, security.getMaxSessionsCountForChatClient(ip1));
		assertEquals(newMaxForAll, security.getMaxSessionsCountForChatClient(ip2));
		
		props.putVal(chats_maxSessionsByIP+"_"+ip1, newMaxForIp1);
		assertEquals(newMaxForIp1, security.getMaxSessionsCountForChatClient(ip1));
		assertEquals(newMaxForAll, security.getMaxSessionsCountForChatClient(ip2));
		
		//with new max ip
		app.sessionsCounter.clearSessionsCount(ip1);
		for (int i = 0; i < newMaxForIp1; i++) {
			lastReq = mockReq();
			lastReq.remoteAddr = ip1;
			app.sessionsCounter.requestInitialized(new ServletRequestEvent(servletContext, lastReq));
			security.initClientSessionForAcc(lastReq, null);
			app.sessionsCounter.sessionCreated(new HttpSessionEvent(lastReq.getSession(false)));
		}
		
		//try more then max
		try {
			MockHttpServletRequest req = mockReq();
			req.remoteAddr = ip1;
			app.sessionsCounter.requestInitialized(new ServletRequestEvent(servletContext, req));
			security.initClientSessionForAcc(req, null);
			
			fail_exception_expected();
		}catch (MaxSessionsCountByIpException e) {
			//ok
		}
		
		
	}
	
	
	private ClientSession createClientSession(String accId){
		MockHttpServletRequest req = mockReq();
		chats.checkChatAndInitClientSession(req, mockResp(), accId);
		
		HttpSession session = req.getSession(false);
		return (ClientSession)session.getAttribute(CLIENT_INFO);
	}

}
