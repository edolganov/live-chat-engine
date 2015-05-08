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

import static och.util.Util.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import och.api.model.chat.ChatOperator;
import och.api.model.client.ClientSession;
import och.service.props.impl.MapProps;
import och.util.DateUtil;
import och.util.FileUtil;

public class HistoryStubGenerator {
	
	
	public static void main(String[] args) throws Exception {
		
		String rootPath = "../app/server-chat/data/accounts";
		String accId = "demo";
		
		ClientSession client = new ClientSession(randomSimpleId(), "127.0.0.1", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
		ChatOperator operator1 = new ChatOperator(1, "Тестовый оператор 1");
		
		SimpleDateFormat dayFormat = new SimpleDateFormat(ChatsAccService.YYYY_MM_DD);
		
		MapProps props = new MapProps();
		
		File root = new File(rootPath);
		if( ! root.exists()){
			throw new IllegalArgumentException("unexists dir: "+root);
		}
		
		final CopyOnWriteArrayList<Future<?>> futures = new CopyOnWriteArrayList<>();
		ChatsAccService accs = new ChatsAccService(root, props, (f)-> 
			futures.add(f));
		
		ChatsAcc chats = accs.createAcc(accId);
		chats.putOperatorAndSetActive(operator1);
		
		//создание контента
		for (int i = 0; i < 10; i++) {
			generateChat(chats, client, operator1);			
		}
		accs.shutdown();
		
		lastFrom(futures).get();
		
		//проверка наличи данных
		Date now = new Date();
		File accDir = new File(root, "acc-"+accId);
		File dayDir = new File(accDir, "logs_"+dayFormat.format(now));
		if( ! dayDir.exists()){
			throw new IllegalArgumentException("unexists dir: "+dayDir);
		}
		File[] logs = dayDir.listFiles();
		if(isEmpty(logs)){
			throw new IllegalArgumentException("no logs files: "+dayDir);
		}
		
		
		//репликация его на год назад
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		
		for (int i = 1; i < 366; i++) {
			
			c.add(Calendar.DAY_OF_MONTH, -1);
			
			Date prevDate = c.getTime();
			prevDate = DateUtil.dateStart(prevDate);
			
			File prevDir = new File(accDir, "logs_"+dayFormat.format(prevDate));
			if(prevDir.exists()) continue;
			prevDir.mkdir();
			
			for (int j = 0; j < logs.length; j++) {
				File log = logs[j];
				String id = randomSimpleId();
				FileUtil.copyFile(log, new File(prevDir, id+".log"));
			}
		}
		
		//create arcs
		new ChatsAccService(root, props).shutdown();
		
		System.out.println("all done");
		
	}

	public static void generateChat(ChatsAcc chats, ClientSession client, ChatOperator operator) {
		
		String chatId = chats.initActiveChat(client).id;
		
		chats.addComment(client, "Hello! Это тестовое сообщение клиента!");
		chats.addOperator(chatId, operator.id, 0);
		chats.addComment(chatId, operator.id, "Answer - Это ответ");
		for (int i = 0; i < 5; i++) {
			chats.addComment(client, "бла-бла-бла фывадловыфф фывдарлфыв лфылыфф");
			chats.addComment(chatId, operator.id, "ывдлаоывдло ыфвдалоыфвджл фывадлоыфва фывафв ффы вы");
		}
		
		chats.closeChat(client);
	}

}
