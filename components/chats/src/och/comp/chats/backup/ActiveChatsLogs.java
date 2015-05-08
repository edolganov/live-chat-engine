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
package och.comp.chats.backup;

import static java.util.Collections.*;
import static och.api.model.PropKey.*;
import static och.comp.chats.backup.ChatLogOps.*;
import static och.comp.chats.common.StoreOps.*;
import static och.util.FileUtil.*;
import static och.util.Util.*;
import static och.util.concurrent.AsyncListener.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import och.api.model.chat.ChatOperator;
import och.api.model.chat.Message;
import och.api.model.client.ClientSession;
import och.comp.chats.model.Chat;
import och.comp.chats.model.Chat.AddClientCommentRes;
import och.service.props.Props;
import och.util.concurrent.AsyncListener;
import och.util.concurrent.ExecutorsUtil;

import org.apache.commons.logging.Log;

public class ActiveChatsLogs {
	
	public static final String ACTIVE_LOGS_DIR = "_active-logs";

	private Log log = getLog(getClass());
	
	private AsyncListener asyncListener;
	private File logsRoot;
	private Props props;
	
	private boolean externalExecutor;
	private ExecutorService writeExecutor;
	
	public ActiveChatsLogs(File accsRoot, Props props, AsyncListener asyncListener) {
		this(accsRoot, props, null, asyncListener);
	}
	
	public ActiveChatsLogs(File accsRoot, Props props, ExecutorService writeExecutor, AsyncListener asyncListener) {
		
		this.logsRoot = new File(accsRoot, ACTIVE_LOGS_DIR);
		this.props = props;
		this.asyncListener = asyncListener;
		
		if(writeExecutor == null) writeExecutor = ExecutorsUtil.newSingleThreadExecutor("ActiveChatsLogs-write-thread");
		else externalExecutor = true;
		this.writeExecutor = writeExecutor;
	}
	
	public void shutdown() {
		if(!externalExecutor) writeExecutor.shutdown();
	}
	
	public void filterWorkDirByAccs(List<String> accIds) {
		
		HashSet<String> valids = new HashSet<>(accIds);
		
		List<String> curIds = getAccIdsNames(logsRoot);
		for(String id : curIds){
			if( ! valids.contains(id)){
				deleteDirRecursive(getAccDir(logsRoot, id));
			}
		}
		
	}
	
	public List<String> getCurAccIds(){
		return getAccIdsNames(logsRoot);
	}
	
	public Chat restoreChat(String accId, String chatId, ClientSession client) {
		
		File file = getChatLogFile(accId, chatId, false);
		if( ! file.exists()) return null;
		return readChat(file, client);
	}
	
	
	public List<Chat> removeInactiveLogs(String accId, Date now, long closeDelta) {
		
		File accDir = getAccLogsDir(accId, false);
		if( ! accDir.exists()) return emptyList();
		
		File[] accFiles = accDir.listFiles();
		if(isEmpty(accFiles)) return emptyList();
		
		long lastDate = now.getTime() - closeDelta;
		
		ArrayList<Chat> out = new ArrayList<>();
		
		for(File file : accFiles){
			if( ! isBakFile(file)) continue;
			if(file.lastModified() >= lastDate) continue;
			
			Chat chat = readChat(file, null);
			if(chat != null) out.add(chat);
			file.delete();
		}
		
		return out;
	}
	
	
	private Chat readChat(File file, ClientSession client) {
		
		String data = null;
		try {
			data = readFileUTF8(file);
		}catch(Exception e){
			log.error("can't readChat from file "+file+": "+e);
			return null;
		}
		
		if(client != null) return restoreChatForSession(data, client);
		else return restoreChatWithSavedSession(data);
	}
	
	

	public void createChatLogAsync(String accId, Chat chat, ClientSession client) {
		asyncEvent(()->createChatLog(accId, chat, client));
	}
	
	public void addClientMsgLogAsync(String accId, Chat chat, AddClientCommentRes result) {
		asyncEvent(()->addClientMsgLog(accId, chat, result));
	}
	
	public void removeChatLogsAsync(String accId, Chat chat) {
		asyncEvent(()->removeChatLogs(accId, chat), false);
	}
	
	public void addOperatorLogAsync(String accId, Chat chat, ChatOperator operator) {
		asyncEvent(()->addOperatorLog(accId, chat, operator));
	}
	
	public void addOperatorMsgLogAsync(String accId, Chat chat, Message result) {
		asyncEvent(()->addOperatorMsgLog(accId, chat, result));
	}
	
	private void asyncEvent(Runnable r){
		asyncEvent(r, true);
	}
	
	private void asyncEvent(Runnable r, boolean checkWork){
		if( checkWork && ! props.getBoolVal(chats_useActiveChatsLogs)) return;
		Future<?> f = writeExecutor.submit(r);
		fireAsyncEvent(asyncListener, f);
	}
	
	
	

	private void createChatLog(String accId, Chat chat, ClientSession client) {
		try {
			
			File file = getChatLogFile(accId, chat);
			if(file.exists()) return;
			
			file.createNewFile();
			writeFileUTF8(file, chatLine(client, chat), true);
			
		}catch(Exception e){
			log.error("can't createChatLog: accId="+accId+", client="+client+", chat="+chat+": "+e);
		}
	}
	
	private void removeChatLogs(String accId, Chat chat) {
		try {
			
			//remove log file
			File file = getChatLogFile(accId, chat);
			file.delete();
			
			//remove accs log dir if no logs
			File parent = file.getParentFile();
			if(isEmpty(parent.list())){
				parent.delete();
			}
			
		}catch(Exception e){
			log.error("can't removeChatLogs: accId="+accId+", chat="+chat+": "+e);
		}
	}
	
	private void addClientMsgLog(String accId, Chat chat, AddClientCommentRes result) {
		try {
			
			File file = getChatLogFile(accId, chat);
			if( ! file.exists()) return;
			
			writeFileUTF8(file, addClientMsgLine(result), true);
			
		}catch(Exception e){
			log.error("can't addClientMsgLog: accId="+accId+", chat="+chat+": "+e);
		}
	}
	
	private void addOperatorLog(String accId, Chat chat, ChatOperator operator) {
		try {
			
			File file = getChatLogFile(accId, chat);
			if( ! file.exists()) return;
			
			writeFileUTF8(file, addOperatorLine(operator), true);
			
		}catch(Exception e){
			log.error("can't addOperatorLog: accId="+accId+", chat="+chat+": "+e);
		}
	}
	
	private void addOperatorMsgLog(String accId, Chat chat, Message result) {
		try {
			
			File file = getChatLogFile(accId, chat);
			if( ! file.exists()) return;
			
			writeFileUTF8(file, addOperatorMsgLine(result), true);
			
		}catch(Exception e){
			log.error("can't addOperatorLog: accId="+accId+", chat="+chat+": "+e);
		}
	}
	
	
	public File getChatLogFile(String accId, Chat chat){
		return getChatLogFile(accId, chat.id);
	}
	
	public File getChatLogFile(String accId, String chatId){
		return getChatLogFile(accId, chatId, true);
	}
	
	public File getChatLogFile(String accId, String chatId, boolean init){
		File dir = getAccLogsDir(accId, init);
		return new File(dir, getBakName(chatId));
	}
	
	public File getAccLogsDir(String accId, boolean init) {
		File dir = getAccDir(logsRoot, accId);
		if(init) dir.mkdirs();
		return dir;
	}


	
	public static File getChatLogFile(File accLogDir, String chatId){
		return new File(accLogDir, getBakName(chatId));
	}
	
	public static File getChatLogFile(File logsRoot, String accId, String chatId){
		return new File(getAccDir(logsRoot, accId), getBakName(chatId));
	}
	
	public static String getBakName(String name){
		return name+".log";
	}
	
	public static boolean isBakFile(File file){
		return file.isFile() && file.getName().endsWith(".log");
	}









	


	
	

}
