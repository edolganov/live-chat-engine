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


import java.util.Collection;

import och.api.exception.chat.NoAvailableOperatorException;
import och.api.exception.chat.NoChatException;
import och.api.exception.chat.NoPreviousOperatorPositionException;
import och.api.exception.chat.NotActiveOperatorException;
import och.api.exception.chat.OperatorPositionAlreadyExistsException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatOperator;
import och.api.model.client.ClientSession;
import och.junit.AssertExt;

import org.junit.Test;

public class ChatsAccTest extends AssertExt {
	
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
	
	ChatOperator operator1 = new ChatOperator(1);
	ChatOperator operator2 = new ChatOperator(2);
	
	String text1 = "привет";
	String text2 = "\n123\t";
	String text3 = "text3";
	
	
	
	@Test
	public void test_addOperator_concurrent(){
		
		ChatsAcc acc = new ChatsAcc();
		
		acc.putOperatorAndSetActive(operator1);
		
		ChatLog log = acc.initAngGetActiveChat(client1);
		
		//not active
		try {
			acc.addOperator(log.id, operator2.id, 0);
			fail_exception_expected();
		}catch (NotActiveOperatorException e) {
			//ok
		}
		acc.putOperatorAndSetActive(operator2);
		
		//no chat
		try {
			acc.addOperator(log.id+100, operator1.id, 0);
			fail_exception_expected();
		}catch (NoChatException e) {
			//ok
		}
		
		
		//valid
		acc.addOperator(log.id, operator1.id, 0);
		
		//again
		try {
			acc.addOperator(log.id, operator1.id, 0);
			fail_exception_expected();
		}catch (OperatorPositionAlreadyExistsException e) {
			//ok
		}
		
		//no prev
		try {
			acc.addOperator(log.id, operator1.id, 2);
			fail_exception_expected();
		}catch (NoPreviousOperatorPositionException e) {
			//ok
		}
		
		//same op again - no effect
		acc.addOperator(log.id, operator1.id, 1);
		try {
			acc.addOperator(log.id, operator2.id, 2);
			fail_exception_expected();
		}catch (NoPreviousOperatorPositionException e) {
			//ok
		}
		
		
		acc.addOperator(log.id, operator2.id, 1);
		acc.addOperator(log.id, operator1.id, 2);
	}
	
	
	
	
	@Test
	public void test_initChat(){
		
		ChatsAcc acc = new ChatsAcc();
		
		try {
			acc.initActiveChat(client1);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {
			//ok
		}
		
		acc.putOperator(operator1);
		try {
			acc.initActiveChat(client1);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {
			//ok
		}
		
		
		acc.setActiveOperator(operator1.id);
		acc.initActiveChat(client1);
		
		
		acc.removeActiveOperator(operator1.id);
		try {
			acc.initActiveChat(client1);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {
			//ok
		}
		
		
		acc.setActiveOperator(operator1.id);
		acc.initActiveChat(client1);
		
		
		
		acc.removeOperator(operator1.id);
		try {
			acc.initActiveChat(client1);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {
			//ok
		}
		
		
		acc.putOperatorAndSetActive(operator1);
		acc.initActiveChat(client1);
	}
	
	
	
	@Test
	public void test_getOperatorChats(){
		
		
		ChatsAcc acc = new ChatsAcc();
		acc.putOperatorAndSetActive(operator1);
		{
			assertNull(acc.getActiveChatsByOperator(operator1.id));
			assertNull(acc.getActiveChatsByOperator(operator2.id));
		}
		
		ChatLog log1 = acc.initAngGetActiveChat(client1);
		ChatLog log2 = acc.initAngGetActiveChat(client2);
		
		
		assertNull(acc.getActiveChatsByOperator(operator1.id));
		acc.addOperator(log1.id, operator1.id, 0);
		acc.addOperator(log2.id, operator1.id, 0);
		{
			Collection<ChatLog> list = acc.getActiveChatsByOperator(operator1.id);
			assertNotNull(list);
			assertEquals(2, list.size());
		}
		
		acc.closeChat(client1);
		acc.closeChat(client1);
		{
			Collection<ChatLog> list = acc.getActiveChatsByOperator(operator1.id);
			assertNotNull(list);
			assertEquals(1, list.size());
		}
		
		acc.closeChat(client2);
		{
			Collection<ChatLog> list = acc.getActiveChatsByOperator(operator1.id);
			assertNotNull(list);
			assertEquals(0, list.size());
		}
		
		
		//can add comment after operator quit
		acc.initActiveChat(client1);
		acc.addComment(client1, "132");
		acc.removeActiveOperator(operator1.id);
		acc.addComment(client1, "345");
		
		
		acc.putOperatorAndSetActive(operator2);
		ChatLog log3 = acc.initAngGetActiveChat(client2);
		acc.addOperator(log3.id, operator2.id, 0);
		{
			Collection<ChatLog> list = acc.getActiveChatsByOperator(operator2.id);
			assertNotNull(list);
			assertEquals(1, list.size());
		}
		
	}
	
	@Test
	public void test_addComment(){
		
		ChatsAcc acc = new ChatsAcc();
		acc.putOperatorAndSetActive(operator1);
		ChatLog chat1 = acc.initAngGetActiveChat(client1);
		
		assertTrue(acc.addComment(client1, text1));
		assertTrue(acc.addComment(client1, text2));
		{
			ChatLog logs = acc.getActiveChat(client1);
			assertEquals(2, logs.messages.size());
			assertEquals(text1, logs.messages.get(0).text);
			assertEquals(text2, logs.messages.get(1).text);
		}
		
		assertFalse(acc.addComment(client2, text1));
		assertNull(acc.getActiveChat(client2));
		
		//operator
		acc.addOperator(chat1.id, operator1.id, 0);
		assertTrue(acc.addComment(chat1.id, operator1.id, text3));
		assertFalse(acc.addComment(chat1.id, operator2.id, text3));
		assertFalse(acc.addComment(chat1.id+"-no", operator1.id, text3));
		{
			ChatLog logs = acc.getActiveChat(client1);
			assertEquals(3, logs.messages.size());
			assertEquals(text1, logs.messages.get(0).text);
			assertEquals(text2, logs.messages.get(1).text);
			assertEquals(text3, logs.messages.get(2).text);
		}
	}
	
	
	@Test
	public void test_closeChat(){
		
		ChatsAcc acc = new ChatsAcc();
		acc.putOperatorAndSetActive(operator1);
		
		String chatId1 = acc.initActiveChat(client1).id;
		String chatId2 = acc.initActiveChat(client2).id;
		
		assertNotNull(acc.getActiveChatById(chatId1));
		assertNotNull(acc.getActiveChatById(chatId2));
		
		acc.closeChat(client1);
		assertNull(acc.getActiveChatById(chatId1));
		assertNotNull(acc.getActiveChatById(chatId2));
		
		acc.closeChat(client2);
		assertNull(acc.getActiveChatById(chatId1));
		assertNull(acc.getActiveChatById(chatId2));
		assertNull(acc.getActiveChat(client1));
		assertNull(acc.getActiveChat(client2));
		
	}
	
	
	
	@Test
	public void test_getActiveChat(){
		
		ChatsAcc acc = new ChatsAcc();
		try {
			acc.initActiveChat(client1);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {
			//ok
		}
		assertNull(acc.getActiveChat(client1));
		
		acc.putOperatorAndSetActive(operator1);
		acc.putOperatorAndSetActive(operator1);
		acc.putOperatorAndSetActive(operator1);
		
		ChatLog log = acc.initAngGetActiveChat(client1);
		assertEquals(1, log.users.size());
		
		acc.addOperator(log.id, operator1.id, 0);
		log = acc.getActiveChat(client1);
		{
			assertNotNull(log);
			assertEquals(0, log.messages.size());
			assertEquals(2, log.users.size());
			assertEquals(client1.getUserId(), log.users.get(0).userId);
			assertEquals(operator1.id, log.users.get(1).operatorId.longValue());
		}
		
		//put again
		{
			ChatLog log2 = acc.initAngGetActiveChat(client1);
			assertEquals(log.id, log2.id);
		}
		
		
		//other session
		ChatLog log2 = acc.initAngGetActiveChat(client2);
		assertFalse(log.id.equals(log2.id));
		
		
		//remove operator
		acc.removeActiveOperator(operator1.id);
		try {
			acc.initActiveChat(client1);
			fail_exception_expected();
		}catch (NoAvailableOperatorException e) {
			//ok
		}
		
	}

}
