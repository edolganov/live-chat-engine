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
package och.comp.chats;

import static java.util.Collections.*;
import static och.api.model.PropKey.*;
import static och.api.model.chat.config.Key.*;
import static och.api.model.client.ClientInfo.*;
import static och.comp.chats.ChatsAccService.*;
import static och.comp.chats.backup.ActiveChatsLogs.*;
import static och.comp.chats.common.StoreOps.*;
import static och.util.DateUtil.*;
import static och.util.FileUtil.*;
import static och.util.Util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import och.api.exception.ValidationException;
import och.api.exception.chat.MaxChatsForAccPerDayException;
import och.api.exception.chat.MaxChatsFromIpPerDayException;
import och.api.exception.chat.MaxFeedbacksForAccPerDayException;
import och.api.exception.chat.MaxFeedbacksFromIpPerDayException;
import och.api.exception.chat.MsgsPerChatLimitException;
import och.api.exception.chat.SimgleMsgsPerTimeLimitException;
import och.api.exception.user.AccessDeniedException;
import och.api.model.PropKey;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatLogHist;
import och.api.model.chat.ChatOperator;
import och.api.model.chat.ChatUpdateData;
import och.api.model.chat.Feedback;
import och.api.model.client.ClientInfo;
import och.api.model.client.ClientSession;
import och.api.model.web.ReqInfo;
import och.comp.chats.backup.ActiveChatsLogs;
import och.comp.chats.history.LogsArchive;
import och.comp.chats.model.Chat;
import och.service.props.impl.MapProps;
import och.util.DateUtil;
import och.util.FileUtil;
import och.util.ReflectionsUtil;
import och.util.StringUtil;
import och.util.concurrent.AsyncListener;

import org.junit.Test;

import test.BaseTest;

public class ChatsAccServiceTest extends BaseTest implements AsyncListener {
	
	String accId1 = randomUUID();
	String accId2 = randomUUID();
	
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
	
	ChatOperator operator1 = new ChatOperator(1, "operator1");
	ChatOperator operator2 = new ChatOperator(2, "operator2");
	ChatOperator operator3 = new ChatOperator(3, "operator3");
	
	String text1 = "привет";
	String text2 = "\n123\t";
	String text3 = "text3";
	
	
	ArrayList<Future<?>> chatWriteFutures = new ArrayList<>();
	MapProps props = new MapProps();
	
	
	@Override
	public void onFutureEvent(Future<?> future) {
		chatWriteFutures.add(future);
	}
	
	void waitLastWrite() throws Exception{
		int size = chatWriteFutures.size();
		if(size > 1) chatWriteFutures.get(size-2).get();
		chatWriteFutures.get(size-1).get();
	}
	
	
	@Test
	public void test_activeLogs_cleanActiveChatsLogs() throws Exception {
		
		ActiveChatsLogs activeLogs = new ActiveChatsLogs(TEST_DIR, props, this);
		
		//create logs
		{
			ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
			ChatsAcc chats = accs.createAcc(accId1);
			chats.putOperatorAndSetActive(operator1);
			
			chats.initActiveChat(client1);
			chats.addComment(client1, "привет\n\n\n123");
			
			waitLastWrite();
			
			assertEquals(list(accId1), activeLogs.getCurAccIds());
		}
		
		
		//start new accs
		Date now = addDays(new Date(), 1);
		long closeDelta = 1L;
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		assertEquals(1, accs.cleanActiveChatsLogs(now, closeDelta));
		assertEquals(0, accs.cleanActiveChatsLogs(now, closeDelta));
		
		waitLastWrite();
		assertEquals(emptyList(), activeLogs.getCurAccIds());
		
	}
	
	
	@Test
	public void test_activeLogs_removeUnknownAccs() throws Exception{
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		
		chats.initActiveChat(client1);
		chats.addComment(client1, "привет\n\n\n123");
		
		waitLastWrite();
		
		
		ActiveChatsLogs activeLogs = new ActiveChatsLogs(TEST_DIR, props, this);
		assertEquals(list(accId1), activeLogs.getCurAccIds());
		
		//create new logs
		File logsDir = new File(TEST_DIR, ACTIVE_LOGS_DIR);
		File newAccDir = new File(logsDir, "acc-.123");
		newAccDir.mkdir();
		new File(newAccDir, "temp").createNewFile();
		
		assertEquals(set(accId1, ".123"), new HashSet<>(activeLogs.getCurAccIds()));
		
		//start new accs - clean
		new ChatsAccService(TEST_DIR, props, this);
		assertEquals(list(accId1), activeLogs.getCurAccIds());
	}
	
	
	@Test
	public void test_activeLogs_restore() throws Exception{
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		chats.putOperatorAndSetActive(operator2);
		
		String chatId1 = chats.initActiveChat(client1).id;
		String chatId2 = chats.initActiveChat(client2).id;
		
		//chat1
		{
			chats.addComment(client1, "привет\n\n\n123");
			chats.addComment(client1, "123 123 123");
			chats.addOperator(chatId1, operator1.id, 0);
			chats.addComment(chatId1, operator1.id, "some text\n\n\n123");
			chats.addComment(client1, "dsfdf");
			chats.addComment(chatId1, operator1.id, "dd");
		}
		//chat 2
		{
			chats.addComment(client2, "привет\n\n\n123");
			chats.addOperator(chatId2, operator1.id, 0);
			chats.addComment(chatId2, operator1.id, "dddddddd");
			chats.addComment(chatId2, operator1.id, "dd");
			chats.addOperator(chatId2, operator2.id, 1);
			chats.addComment(chatId2, operator1.id, "sdf");
			chats.addComment(client2, "zzzzz");
		}
		
		waitLastWrite();
		
		ActiveChatsLogs activeLogs = new ActiveChatsLogs(TEST_DIR, props, this);
		//restore logic
		{
			//1
			{
				assertNull(activeLogs.restoreChat(accId1, chatId1+"123", client1));
				assertNull(activeLogs.restoreChat(accId1, chatId1, client2));
				
				Chat log = activeLogs.restoreChat(accId1, chatId1, client1);
				assertNotNull(log);
				
				ChatLog chatLog = log.toLog();
				assertEquals(5, chatLog.messages.size());
				assertEquals(2, chatLog.users.size());				
			}
			//2
			{
				Chat log = activeLogs.restoreChat(accId1, chatId2, client2);
				assertNotNull(log);
				
				ChatLog chatLog = log.toLog();
				assertEquals(5, chatLog.messages.size());
				assertEquals(3, chatLog.users.size());				
			}
		}
		
		//link restored to session
		//old accs alredy contains chat
		assertNull(accs.restoreOldChatIfNeed(accId1, client1, chatId1));
		assertNull(accs.restoreOldChatIfNeed(accId1, client1, chatId1));
		accs.shutdown();
		
		
		//new accs
		ChatsAccService accs2 = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc acc = accs2.getAcc(accId1);
		assertNull(acc.getActiveChat(client1));
		assertNull(acc.getActiveChat(client2));
		
		//1
		{
			assertNull(accs2.restoreOldChatIfNeed(accId2, client1, chatId1));
			assertNull(accs2.restoreOldChatIfNeed(accId1, client2, chatId1));
			assertNull(accs2.restoreOldChatIfNeed(accId1, client1, chatId2));
			
			ChatLog chatLog = accs2.restoreOldChatIfNeed(accId1, client1, chatId1);
			assertNotNull(chatLog);
			assertEquals(5, chatLog.messages.size());
			assertEquals(2, chatLog.users.size());
			
			assertNotNull(acc.getActiveChat(client1));
			assertNull(acc.getActiveChat(client2));
		}
		//2
		{
			ChatLog chatLog = accs2.restoreOldChatIfNeed(accId1, client2, chatId2);
			assertNotNull(chatLog);
			assertEquals(5, chatLog.messages.size());
			assertEquals(3, chatLog.users.size());
			
			assertNotNull(acc.getActiveChat(client2));
		}
		
		
		//close new chats
		acc.closeAllChats();
		accs2.shutdown();
		
		Thread.sleep(50);
		
		
		//new accs have no logs
		{
			ChatsAccService accs3 = new ChatsAccService(TEST_DIR, props, this);
			assertNull(accs3.restoreOldChatIfNeed(accId1, client1, chatId1));
			assertNull(accs3.restoreOldChatIfNeed(accId1, client2, chatId2));
			accs3.shutdown();
		}
	}
	
	
	
	@Test
	public void test_activeLogs_create() throws Exception{
		
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		
		File logsDir = new File(TEST_DIR, ACTIVE_LOGS_DIR);
		String chatId = null;
		File chatLogFile = null;
		String curLogContent = "";
		String newLogContent = "";
		
		//create chat
		{
			chatId = chats.initActiveChat(client1).id;
			waitLastWrite();
			
			chatLogFile = getChatLogFile(logsDir, accId1, chatId);
			assertTrue(chatLogFile.exists());
			
			newLogContent = FileUtil.readFileUTF8(chatLogFile);
			assertTrue(newLogContent.length() > curLogContent.length());
			curLogContent = newLogContent;
		}
		
		//add user msg
		{
			chats.addComment(client1, "привет\n\n\n123");
			waitLastWrite();
			
			newLogContent = FileUtil.readFileUTF8(chatLogFile);
			assertTrue(newLogContent.length() > curLogContent.length());
			curLogContent = newLogContent;
		}
		
		//add operator
		{
			chats.addOperator(chatId, operator1.id, 0);
			waitLastWrite();
			
			newLogContent = FileUtil.readFileUTF8(chatLogFile);
			assertTrue(newLogContent.length() > curLogContent.length());
			curLogContent = newLogContent;
			
		}
		
		//add operator msg
		{
			chats.addComment(chatId, operator1.id, "some text\n\n\n123");
			waitLastWrite();
			
			newLogContent = FileUtil.readFileUTF8(chatLogFile);
			assertTrue(newLogContent.length() > curLogContent.length());
			curLogContent = newLogContent;
		}
		
		//close chat
		{
			chats.closeChat(client1);
			waitLastWrite();
			
			assertFalse(chatLogFile.exists());
			assertFalse(chatLogFile.getParentFile().exists());
		}
		
	}
	
	
	
	
	@Test
	public void test_clientRefs() throws Exception{
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		
		String chatId = chats.initActiveChat(client1).id;
		chats.addOperator(chatId, operator1.id, 0);
		
		chats.addComment(client1, "test1");
		assertNull(chats.getActiveChat(client1).clientRefs);
		
		try {
			
			//ref
			ReqInfo.putInfoToThreadLocal(new ReqInfo(null, null, "some1", null, null));
			chats.addComment(client1, "test");
			{
				Map<Integer, String> clientRefs = chats.getActiveChat(client1).clientRefs;
				assertEquals("some1", clientRefs.get(1));				
			}
			
			//origRef
			ReqInfo.putInfoToThreadLocal(new ReqInfo(null, null, null, "some2", null));
			chats.addComment(client1, "test");
			chats.addComment(client1, "test");
			chats.addComment(client1, "test");
			{
				Map<Integer, String> clientRefs = chats.getActiveChat(client1).clientRefs;
				assertEquals(2, clientRefs.size());
				assertEquals("some1", clientRefs.get(1));
				assertEquals("some2", clientRefs.get(2));
			}
			
			//empty ref
			ReqInfo.putInfoToThreadLocal(new ReqInfo(null, null, null, null, null));
			chats.addComment(client1, "test");
			assertEquals(2, chats.getActiveChat(client1).clientRefs.size());
			
			
			//use fromIndex
			{
				Map<Integer, String> clientRefs = chats.getActiveChat(client1, new ChatUpdateData(0, 1)).clientRefs;
				assertEquals(2, clientRefs.size());
				assertEquals("some1", clientRefs.get(1));
				assertEquals("some2", clientRefs.get(2));
			}
			{
				Map<Integer, String> clientRefs = chats.getActiveChat(client1, new ChatUpdateData(1, 1)).clientRefs;
				assertEquals(2, clientRefs.size());
				assertEquals("some1", clientRefs.get(1));
				assertEquals("some2", clientRefs.get(2));
			}
			{
				Map<Integer, String> clientRefs = chats.getActiveChat(client1, new ChatUpdateData(2, 1)).clientRefs;
				assertEquals(1, clientRefs.size());
				assertEquals("some2", clientRefs.get(2));
			}
			{
				Map<Integer, String> clientRefs = chats.getActiveChat(client1, new ChatUpdateData(3, 1)).clientRefs;
				assertEquals(null, clientRefs);
			}
			
			
		} finally {
			ReqInfo.removeInfoFromThreadLocal();
		}
		
	}
	
	
	@Test
	public void test_crud_config() throws Exception{
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		accs.createAcc(accId1);
		
		boolean initVal = feedback_notifyOpsByEmail.boolDefVal();
		
		ChatsAcc chats = accs.getAcc(accId1);
		assertEquals(initVal, chats.getBoolVal(feedback_notifyOpsByEmail));
		
		chats.putConfig(feedback_notifyOpsByEmail, ! initVal);
		
		waitLastWrite();
		assertEquals( ! initVal, chats.getBoolVal(feedback_notifyOpsByEmail));
		
		//read from file
		ChatsAccService accs2 = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc chats2 = accs2.getAcc(accId1);
		assertEquals( ! initVal, chats2.getBoolVal(feedback_notifyOpsByEmail));
		
	}
	
	
	@Test
	public void test_client_feedbacks_day_limits_by_acc() throws Exception{
		
		int maxByAcc = 10;
		props.putVal(chats_maxFeedbacksForAccPerDay, maxByAcc);
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		accs.createAcc(accId1);
		accs.createAcc(accId2);
		

		String text = "123";
		
		//acc1
		for (int i = 0; i < maxByAcc; i++) {
			accs.addFeedbackAsync(accId1, randomClientInfo(), text);
		}
		try {
			accs.addFeedbackAsync(accId1, randomClientInfo(), text);
			fail_exception_expected();
		}catch(MaxFeedbacksForAccPerDayException e){
			//ok
		}
		
		//acc2
		for (int i = 0; i < maxByAcc; i++) {
			accs.addFeedbackAsync(accId2, randomClientInfo(), text);
		}
		try {
			accs.addFeedbackAsync(accId2, randomClientInfo(), text);
			fail_exception_expected();
		}catch(MaxFeedbacksForAccPerDayException e){
			//ok
		}
		
		
		//custom limit
		int customMax = maxByAcc+1;
		props.putVal(chats_maxFeedbacksForAccPerDay+"_"+accId1, customMax);
		accs.addFeedbackAsync(accId1, randomClientInfo(), text);
		
		try {
			accs.addFeedbackAsync(accId1, randomClientInfo(), text);
			fail_exception_expected();
		}catch(MaxFeedbacksForAccPerDayException e){
			//ok
		}
		
		//спасает переход на след. день
		ReflectionsUtil.setField(accs, "customCurDayPreset", addDays(new Date(), 1).getTime());
		
		//acc1
		for (int i = 0; i < customMax; i++) {
			accs.addFeedbackAsync(accId1, randomClientInfo(), text);
		}
		try {
			accs.addFeedbackAsync(accId1, randomClientInfo(), text);
			fail_exception_expected();
		}catch(MaxFeedbacksForAccPerDayException e){
			//ok
		}
		
		//acc2
		for (int i = 0; i < maxByAcc; i++) {
			accs.addFeedbackAsync(accId2, randomClientInfo(), text);
		}
		try {
			accs.addFeedbackAsync(accId2, randomClientInfo(), text);
			fail_exception_expected();
		}catch(MaxFeedbacksForAccPerDayException e){
			//ok
		}
		
	}
	
	
	@Test
	public void test_client_feedbacks_day_limits_by_ip() throws Exception{
		
		int maxByIp = 10;
		
		props.putVal(chats_maxFeedbacksFromIpPerDay, maxByIp);
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		accs.createAcc(accId1);
		
		ClientInfo clientInfo1 = randomClientInfo();
		ClientInfo clientInfo2 = randomClientInfo();
		String text = "123";
		
		//ip1
		for (int i = 0; i < maxByIp; i++) {
			accs.addFeedbackAsync(accId1, clientInfo1, text);
		}
		try {
			accs.addFeedbackAsync(accId1, clientInfo1, text);
			fail_exception_expected();
		}catch(MaxFeedbacksFromIpPerDayException e){
			//ok
		}
		
		
		//ip2
		for (int i = 0; i < maxByIp; i++) {
			accs.addFeedbackAsync(accId1, clientInfo2, text);
		}
		try {
			accs.addFeedbackAsync(accId1, clientInfo2, text);
			fail_exception_expected();
		}catch(MaxFeedbacksFromIpPerDayException e){
			//ok
		}
		
		
		//custom limit by ip
		int customMax = maxByIp+1;
		props.putVal(chats_maxFeedbacksFromIpPerDay+"_"+clientInfo1.ip, customMax);
		accs.addFeedbackAsync(accId1, clientInfo1, text);
		
		try {
			accs.addFeedbackAsync(accId1, clientInfo1, text);
			fail_exception_expected();
		}catch(MaxFeedbacksFromIpPerDayException e){
			//ok
		}
		
		//спасает переход на след. день
		ReflectionsUtil.setField(accs, "customCurDayPreset", addDays(new Date(), 1).getTime());
		
		//ip1
		for (int i = 0; i < customMax; i++) {
			accs.addFeedbackAsync(accId1, clientInfo1, text);
		}
		try {
			accs.addFeedbackAsync(accId1, clientInfo1, text);
			fail_exception_expected();
		}catch(MaxFeedbacksFromIpPerDayException e){
			//ok
		}
		
		//ip2
		for (int i = 0; i < maxByIp; i++) {
			accs.addFeedbackAsync(accId1, clientInfo2, text);
		}
		try {
			accs.addFeedbackAsync(accId1, clientInfo2, text);
			fail_exception_expected();
		}catch(MaxFeedbacksFromIpPerDayException e){
			//ok
		}
		
	}
	
	
	@Test
	public void test_write_read_feedbacks() throws Exception{
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		accs.createAcc(accId1);
		
		String text = "123";
		String text2 = "345";
		Date now = DateUtil.parseStandartDateTime("30.09.2014 12:00:00");
		Date afterMinute = DateUtil.parseStandartDateTime("30.09.2014 12:10:00");
		Date nextDay = DateUtil.parseStandartDate("01.10.2014");
		Date prevDay = DateUtil.parseStandartDate("28.09.2014");
		
		//no email
		try {
			accs.addFeedbackAsync(accId1, randomClientInfo(false), text, now);
			fail_exception_expected();
		}catch(ValidationException e){
			//ok
		}
		
		//write
		ClientInfo clientInfo = randomClientInfo();
		{
			accs.addFeedbackAsync(accId1, clientInfo, text, now);
			accs.addFeedbackAsync(accId1, clientInfo, text2, afterMinute);
			waitLastWrite();
		}
		
		//read now
		{
			List<Feedback> list = accs.getFeedbacks(accId1, now);
			assertEquals(2, list.size());
			assertEquals(clientInfo.email, list.get(0).user.userEmail);
			assertEquals(clientInfo.name, list.get(0).user.userName);
			assertEquals(text, list.get(0).text);
			assertEquals(clientInfo.email, list.get(1).user.userEmail);
			assertEquals(clientInfo.name, list.get(1).user.userName);
			assertEquals(text2, list.get(1).text);
		}
		
		//empty day
		{
			assertEquals(0, accs.getFeedbacks(accId1, prevDay).size());
			assertEquals(0, accs.getFeedbacks(accId1, nextDay).size());
			assertEquals(null, accs.getFeedbacks(accId1+"123", prevDay));
		}
		
	}
	
	
	
	
	
	
	@Test
	public void test_client_ip_ban(){
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		
		chats.initActiveChat(new ClientSession(session1, ip1, userAgent1));
		
		props.putVal(chats_blockClientByIp+"_"+ip1, true);
		try {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
			fail_exception_expected();
		}catch(AccessDeniedException e){
			//ok
		}
		
		props.putVal(chats_blockClientByIp+"_"+ip1, false);
		chats.initActiveChat(new ClientSession(session1, ip1, userAgent1));
	}
	
	
	
	@Test
	public void test_client_createChats_day_limits_by_acc() throws Exception{
		
		int maxForAcc = 10;
		
		props.putVal(chats_maxChatsForAccPerDay, maxForAcc);
		
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		
		ChatsAcc chats1 = accs.createAcc(accId1);
		chats1.putOperatorAndSetActive(operator1);

		ChatsAcc chats2 = accs.createAcc(accId2);
		chats2.putOperatorAndSetActive(operator1);
		
		
		
		//acc1
		for (int i = 0; i < maxForAcc; i++) {
			chats1.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
		}
		try {
			chats1.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsForAccPerDayException e){
			//ok
		}
		
		//acc2
		for (int i = 0; i < maxForAcc; i++) {
			chats2.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
		}
		try {
			chats2.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsForAccPerDayException e){
			//ok
		}
		
		
		//custom limit by ip
		int customMax = maxForAcc+1;
		props.putVal(chats_maxChatsForAccPerDay+"_"+accId1, customMax);
		chats1.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
		
		try {
			chats1.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsForAccPerDayException e){
			//ok
		}
		
		//закрытие всех текущих чатов не поможет
		chats1.closeAllChats();
		try {
			chats1.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsForAccPerDayException e){
			//ok
		}
		
		//спасает переход на след. день
		ReflectionsUtil.setField(accs, "customCurDayPreset", addDays(new Date(), 1).getTime());
		
		//acc1
		for (int i = 0; i < customMax; i++) {
			chats1.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
		}
		try {
			chats1.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsForAccPerDayException e){
			//ok
		}
		//acc2
		for (int i = 0; i < maxForAcc; i++) {
			chats2.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
		}
		try {
			chats2.initActiveChat(new ClientSession(randomSimpleId(), randomClientInfo().ip, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsForAccPerDayException e){
			//ok
		}
		
	}
	
	
	@Test
	public void test_client_createChats_day_limits_by_ip() throws Exception{
		
		int maxByIpCommon = 10;
		
		props.putVal(chats_maxChatsFromIpPerDay, maxByIpCommon);
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		
		//ip1
		for (int i = 0; i < maxByIpCommon; i++) {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
		}
		try {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsFromIpPerDayException e){
			//ok
		}
		
		
		//ip2
		for (int i = 0; i < maxByIpCommon; i++) {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip2, userAgent1));
		}
		try {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip2, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsFromIpPerDayException e){
			//ok
		}
		
		
		//custom limit by ip
		int customMax = maxByIpCommon+1;
		props.putVal(chats_maxChatsFromIpPerDay+"_"+ip1, customMax);
		chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
		
		try {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsFromIpPerDayException e){
			//ok
		}
		
		//закрытие всех текущих чатов не поможет
		chats.closeAllChats();
		try {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsFromIpPerDayException e){
			//ok
		}
		
		//спасает переход на след. день
		ReflectionsUtil.setField(accs, "customCurDayPreset", addDays(new Date(), 1).getTime());
		
		//ip1
		for (int i = 0; i < customMax; i++) {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
		}
		try {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip1, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsFromIpPerDayException e){
			//ok
		}
		
		//ip2
		for (int i = 0; i < maxByIpCommon; i++) {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip2, userAgent1));
		}
		try {
			chats.initActiveChat(new ClientSession(randomSimpleId(), ip2, userAgent1));
			fail_exception_expected();
		}catch(MaxChatsFromIpPerDayException e){
			//ok
		}
		
	}
	
	
	@Test
	public void test_msgSizeLimit() throws Exception {
		
		
		int max = props.getIntVal(chats_maxMsgSize);
		String maxMsg = StringUtil.randomStr(max);
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		String chatId = chats.initActiveChat(client1).id;
		chats.addOperator(chatId, operator1.id, 0);
		
		chats.addComment(client1, maxMsg);
		chats.addComment(chatId, operator1.id, maxMsg);
		
		try {
			chats.addComment(client1, maxMsg+"1");
			fail_exception_expected();
		}catch(ValidationException e){
			//ok
		}
		
		try {
			chats.addComment(chatId, operator1.id, maxMsg+"1");
			fail_exception_expected();
		}catch(ValidationException e){
			//ok
		}
		
	}
	
	
	@Test
	public void test_msgsLimit() throws Exception {
		
		
		String text = StringUtil.randomStr(props.getIntVal(chats_maxMsgSize));
		
		int max = props.getIntVal(chats_maxMsgsPerChat);
		
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		String chatId = chats.initActiveChat(client1).id;
		chats.addOperator(chatId, operator1.id, 0);
		
		for (int i = 0; i < max; i++) {
			if(i % 2 == 0) chats.addComment(client1, text);
			else chats.addComment(chatId, operator1.id, text);
		}
		
		
		try {
			chats.addComment(client1, text);
			fail_exception_expected();
		}catch(MsgsPerChatLimitException e){
			//ok
		}
		
		try {
			chats.addComment(chatId, operator1.id, text);
			fail_exception_expected();
		}catch(MsgsPerChatLimitException e){
			//ok
		}
		

		chats.closeAllChats();
		
		lastFrom(chatWriteFutures).get();
	}
	
	
	@Test
	public void test_clientMsgLimit() throws Exception {
		
		String text = "123";
		int max = props.getIntVal(PropKey.chats_maxSingleMsgsPerTime);
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		String chatId = chats.initActiveChat(client1).id;
		chats.addOperator(chatId, operator1.id, 0);
		
		for (int i = 0; i < max; i++) {
			chats.addComment(client1, text);			
		}
		try {
			chats.addComment(client1, text);
			fail_exception_expected();
		}catch(SimgleMsgsPerTimeLimitException e){
			//ok
		}
		
		//next round
		chats.addComment(chatId, operator1.id, text);
		
		for (int i = 0; i < max; i++) {
			chats.addComment(client1, text);			
		}
		
		try {
			chats.addComment(client1, text);
			fail_exception_expected();
		}catch(SimgleMsgsPerTimeLimitException e){
			//ok
		}
	}
	
	
	@Test
	public void test_removeReq() throws Exception {
		
		String text = "123";
		
		//создаем чат
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc chats1 = accs.createAcc(accId1);
		chats1.putOperatorAndSetActive(operator1);
		chats1.initActiveChat(client1);
		chats1.addComment(client1, text);
		
		ChatsAcc chats2 = accs.createAcc(accId1);
		chats2.putOperatorAndSetActive(operator2);
		chats2.initActiveChat(client2);
		chats2.addComment(client2, text);
		
		//проверяем что он не записался в файл
		File accDir = accs.getAccDir(accId1);
		assertEquals(0, getSubDirsCount(accDir));
		
		//создаем флаг удаления и синхронизируем
		createRemoveReqFlag(accDir);
		accs.reloadAccs();
		
		lastFrom(chatWriteFutures).get();
		
		//проверяем что чаты сохранились перед удалением акка
		File newDir = getDirToDeleted(accDir.getParent(), accDir.getName(), 0);
		assertEquals(true, newDir.exists());
		assertEquals(1, getSubDirsCount(newDir));
		assertEquals(false, hasRemoveReqFlag(newDir));
		
	}
	
	
	private static int getSubDirsCount(File dir) {
		File[] files = dir.listFiles();
		if(isEmpty(files)) return 0;
		int count = 0;
		for (File file : files) {
			if(file.isDirectory()) count++;
		}
		return count;
	}

	@Test
	public void test_blockFlagUpdate(){
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		
		//заблокируем чат
		accs.createAcc(accId1);
		assertEquals(false, accs.isBlocked(accId1));
		accs.setBlocked(accId1, true);
		assertEquals(true, accs.isBlocked(accId1));
		
		//удалим файл флага - рассинхрон
		File accDir = accs.getAccDir(accId1);
		removeBlockedFlag(accDir);
		assertEquals(false, hasBlockedFlag(accDir));
		assertEquals(true, accs.isBlocked(accId1));
		
		//синхр
		accs.reloadAccs();
		assertEquals(false, hasBlockedFlag(accDir));
		assertEquals(false, accs.isBlocked(accId1));
		
		
		//создадим флаг блокировки - рассинхр
		createBlockedFlag(accDir);
		assertEquals(true, hasBlockedFlag(accDir));
		assertEquals(false, accs.isBlocked(accId1));
		
		
		//синхр
		accs.reloadAccs();
		assertEquals(true, hasBlockedFlag(accDir));
		assertEquals(true, accs.isBlocked(accId1));
	}
	
	
	
	@Test
	public void test_block() throws Exception {
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		
		//unknow chat
		assertFalse(accs.isBlocked(accId1));
		accs.setBlocked(accId1, true);
		assertFalse(accs.isBlocked(accId1));
		
		//known chat
		accs.createAcc(accId1);
		assertFalse(accs.isBlocked(accId1));
		
		accs.setBlocked(accId1, true);
		assertTrue(accs.isBlocked(accId1));
		
		accs.setBlocked(accId1, false);
		assertFalse(accs.isBlocked(accId1));
		
		accs.setBlocked(accId1, true);
		assertTrue(accs.isBlocked(accId1));
		
		
		//restored
		ChatsAccService resrored = new ChatsAccService(TEST_DIR, props, this);
		assertTrue(resrored.isBlocked(accId1));
		
	}
	
	
	
	
	
	@Test
	public void test_getHist()throws Exception{
		
		ChatsAccService accs = new ChatsAccService(TEST_DIR, props, this);
		ChatsAcc chats = accs.createAcc(accId1);
		chats.putOperatorAndSetActive(operator1);
		chats.initActiveChat(client1);
		chats.addComment(client1, "test");
		chats.closeChat(client1);
		
		String chatId2 = chats.initActiveChat(client1).id;
		chats.addComment(client1, "test2");
		chats.addOperator(chatId2, operator1.id, 0);
		chats.addComment(chatId2, operator1.id, "anwer");
		chats.closeChat(client1);
		
		waitLastWrite();
		
		Date now = new Date();
		Date yesterday = DateUtil.addDays(now, -1);
		
		assertTrue(isEmpty(accs.getHistory(accId1, yesterday)));
		
		List<ChatLogHist> logs = accs.getHistory(accId1, now);
		assertTrue( ! isEmpty(logs));
		assertEquals(2, logs.size());
		{
			ChatLogHist log = logs.get(0);
			assertEquals(1, log.messages.size());
		}
		{
			ChatLogHist log = logs.get(1);
			assertEquals(2, log.messages.size());
		}
	}
	
	
	
	
	@Test
	public void test_call_LogsArchive() throws Exception{
		
		final AtomicInteger callCounter = new AtomicInteger();
		LogsArchive arcStub = new LogsArchive() {
			@Override
			public synchronized Date tryCreateArcsIfNeed(File root, String accId, Date lastScanned) {
				callCounter.addAndGet(1);
				return lastScanned;
			}
		}; 
		
		ChatsAccService accs1 = new ChatsAccService(TEST_DIR, props, this, arcStub);
		assertEquals(0, callCounter.get());
		
		accs1.createAcc("test1");
		accs1.createAcc("test2");
		assertEquals(0, callCounter.get());
		accs1.shutdown();
		
		ChatsAccService accs2 = new ChatsAccService(TEST_DIR, props, this, arcStub);
		assertEquals(2, callCounter.get());
		callCounter.set(0);
		
		ChatsAcc chats = accs2.createAcc("test1");
		chats.putOperatorAndSetActive(operator1);
		chats.initActiveChat(client1);
		chats.addComment(client1, "test");
		chats.closeChat(client1);
		waitLastWrite();
		
		assertEquals(1, callCounter.get());
		callCounter.set(0);
	}
	
	
	
	@Test
	public void test_saveOperatorsToFile() throws Exception{
		
		//set
		{
			ChatsAccService accountChats = new ChatsAccService(TEST_DIR, props, this);
			ChatsAcc chats1 = accountChats.createAcc(accId1);
			chats1.putOperatorAndSetActive(operator1);
			
			ChatsAcc chats2 = accountChats.createAcc(accId2);
			chats2.putOperatorAndSetActive(operator1);
			chats2.putOperatorAndSetActive(operator2);
			
			assertNotNull(chats1.getOperator(operator1.id));
			assertNull(chats1.getOperator(operator2.id));
			assertNotNull(chats2.getOperator(operator1.id));
			assertNotNull(chats2.getOperator(operator2.id));
		}
		
		waitLastWrite();
		
		
		String newName = "new name of operator 1";
		
		//load from file
		{
			ChatsAccService accountChats = new ChatsAccService(TEST_DIR, props, this);
			ChatsAcc chats1 = accountChats.createAcc(accId1);
			ChatsAcc chats2 = accountChats.createAcc(accId2);
			
			assertNotNull(chats1.getOperator(operator1.id));
			assertNull(chats1.getOperator(operator2.id));
			assertNotNull(chats2.getOperator(operator1.id));
			assertNotNull(chats2.getOperator(operator2.id));
			
			
			//change
			chats1.putOperator(new ChatOperator(operator1.id, newName));
			chats2.putOperator(operator3);
			chats2.removeOperator(operator2.id);
			
			assertFalse(chats2.isActiveOperator(operator2.id));
			assertFalse(chats2.isOperator(operator2.id));
			
			assertEquals(newName, chats1.getOperator(operator1.id).name);
			assertNotNull(chats2.getOperator(operator3.id));
			assertNull(chats2.getOperator(operator2.id));
			
		}
		
		waitLastWrite();
		
		//load chages
		{
			ChatsAccService accountChats = new ChatsAccService(TEST_DIR, props, this);
			ChatsAcc chats1 = accountChats.createAcc(accId1);
			ChatsAcc chats2 = accountChats.createAcc(accId2);
			assertEquals(newName, chats1.getOperator(operator1.id).name);
			assertNotNull(chats2.getOperator(operator3.id));
			assertNull(chats2.getOperator(operator2.id));
		}
		
		
	}
	
	
	@Test
	public void test_closeChat_and_removeDir() throws Exception{
		
		File accountDir = new File(TEST_DIR, getAccountDirName(accId1));
		
		ChatsAccService accountChats = new ChatsAccService(TEST_DIR, props, this);
		{
			ChatsAcc chats = accountChats.createAcc(accId1);
			chats.putOperatorAndSetActive(operator1);
			chats.initActiveChat(client1);
			chats.addComment(client1, "test");
			chats.closeChat(client1);
		}
		assertTrue(accountDir.exists());
		
		
		File deletedDir = findDirToDeleted(accountDir);
		assertFalse(deletedDir.exists());
		
		accountChats.removeAcc(accId1).get();
		assertFalse(accountDir.exists());
		assertTrue(deletedDir.exists());
		
		//create-delete again
		File deletedDir2 = findDirToDeleted(accountDir);
		{
			ChatsAcc chats = accountChats.createAcc(accId1);
			chats.putOperatorAndSetActive(operator1);
			chats.initActiveChat(client1);
			chats.addComment(client1, "test2");
			chats.closeChat(client1);
		}
		assertTrue(accountDir.exists());
		assertTrue(deletedDir.exists());
		assertFalse(deletedDir2.exists());
		
		accountChats.removeAcc(accId1).get();
		assertFalse(accountDir.exists());
		assertTrue(deletedDir.exists());
		assertTrue(deletedDir2.exists());
	}

	
	@Test
	public void test_load_and_reload(){
		
		
		File accountDir = new File(TEST_DIR, getAccountDirName(accId1));
		accountDir.mkdirs();
		
		//load
		ChatsAccService accountChats = new ChatsAccService(TEST_DIR, props, this);
		assertNotNull(accountChats.getAcc(accId1));
		
		//remove dir
		assertTrue(deleteDirRecursive(accountDir));
		assertNotNull(accountChats.getAcc(accId1));
		
		//add dir
		File accountDir2 = new File(TEST_DIR, getAccountDirName(accId2));
		accountDir2.mkdir();
		assertNull(accountChats.getAcc(accId2));
		
		//reload
		accountChats.reloadAccs();
		assertNull(accountChats.getAcc(accId1));
		assertNotNull(accountChats.getAcc(accId2));
		
	}
	
	
	@Test
	public void test_save_closed_chat() throws Exception {
		
		ChatsAccService accountChats = new ChatsAccService(TEST_DIR, props, this);
		
		//server1
		accountChats.createAcc(accId1);
		assertTrue(accountChats.getAccDir(accId1).exists());
		
		ChatsAcc chats = accountChats.getAcc(accId1);
		
		chats.putOperatorAndSetActive(operator1);
		chats.putOperatorAndSetActive(operator2);
		
		String chatId1 = chats.initActiveChat(client1).id;
		chats.addComment(client1, text1);
		chats.addComment(chatId1, operator1.id, text2);
		chats.addComment(client1, text2);
		chats.addComment(chatId1, operator2.id, text3);
		
		//write
		chats.closeChat(client1);
		waitLastWrite();
		
		
		
		//server2
		ChatsAcc chats2 = accountChats.createAcc(accId2);
		chats2.putOperatorAndSetActive(operator1);
		chats2.initActiveChat(client1);
		chats2.addComment(client1, text1);
		chats2.closeChat(client1);
		waitLastWrite();
		
	}
	
	
	@Test
	public void test_create_read_delete(){
		
		ChatsAccService accountChats = new ChatsAccService(TEST_DIR, props);
		
		//create
		assertNull(accountChats.getAcc(accId1));
		assertNotNull(accountChats.createAcc(accId1));
		
		//read
		assertNotNull(accountChats.getAcc(accId1));
		
		
		//delete
		accountChats.removeAcc(accId1);
		assertNull(accountChats.getAcc(accId1));
		
		
		//create again
		accountChats.createAcc(accId1);
		assertNotNull(accountChats.getAcc(accId1));
		
		
	}

}
