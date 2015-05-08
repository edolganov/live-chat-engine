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

import static java.lang.System.*;
import static java.util.Collections.*;
import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.web.ReqInfo.*;
import static och.comp.chats.common.StoreOps.*;
import static och.comp.ops.ChatOps.*;
import static och.comp.ops.SecurityOps.*;
import static och.util.DateUtil.*;
import static och.util.FileUtil.*;
import static och.util.Util.*;
import static och.util.concurrent.AsyncListener.*;
import static och.util.json.GsonUtil.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import och.api.exception.ValidationException;
import och.api.exception.chat.NoChatAccountException;
import och.api.model.chat.ChatLog;
import och.api.model.chat.ChatLogHist;
import och.api.model.chat.ChatOperator;
import och.api.model.chat.Feedback;
import och.api.model.chat.Message;
import och.api.model.chat.config.AccConfig;
import och.api.model.client.ClientInfo;
import och.api.model.client.ClientSession;
import och.api.model.web.ReqInfo;
import och.comp.chats.backup.ActiveChatsLogs;
import och.comp.chats.common.StoreOps;
import och.comp.chats.history.LogsArchive;
import och.comp.chats.history.LogsArchiveImpl;
import och.comp.chats.model.Chat;
import och.comp.chats.model.Chat.AddClientCommentRes;
import och.comp.chats.model.ChatLogStore;
import och.comp.chats.model.FeedbackStore;
import och.comp.chats.stat.AccsStat;
import och.service.props.Props;
import och.util.FileUtil;
import och.util.concurrent.AsyncListener;
import och.util.concurrent.ExecutorsUtil;
import och.util.geoip.GeoIp;
import och.util.timer.TimerExt;

import org.apache.commons.logging.Log;

public class ChatsAccService implements ChatsAccListener {
	

	private static Log log = getLog(ChatsAccService.class);
	
	public static final String LOGS_DIR_PREFIX = "logs_";
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final String OPERATORS_FILE_NAME = "operators.json";
	public static final String CONFIG_FILE_NAME = "config.json";
	public static final String BLOCKED_FLAG = ".blocked";
	public static final String PAUSED_FLAG = ".paused";
	public static final String REMOVE_REQ_FLAG = ".removeReq";
	public static final String TMPL_PREFIX = "z_";
	
	private ReadWriteLock rw = new ReentrantReadWriteLock();
	private Lock read = rw.readLock();
	private Lock write = rw.writeLock();
	
	private Props props;
	private File root;
	private SimpleDateFormat dayFormat = new SimpleDateFormat(YYYY_MM_DD);
	private AsyncListener asyncListener;
	private LogsArchive logsArchive;
	private ActiveChatsLogs activeLogs;
	private AccsStat accsStat;
	
	
	private ExecutorService writeExecutor;
	private ExecutorService extraWriteExecutor;
	private TimerExt timer;
	
	//model
	private static class AccData {
		
		ChatsAcc acc;
		boolean blocked;
		boolean paused;
		
		public AccData(ChatsAcc acc, boolean blocked, boolean paused) {
			super();
			this.acc = acc;
			this.blocked = blocked;
			this.paused = paused;
		}

		@Override
		public String toString() {
			return "[acc=" + acc + ", blocked=" + blocked
					+ ", paused=" + paused + "]";
		}
		

	}
	
	private HashMap<String, AccData> accsById = new HashMap<>();
	

	//limits
	private static class DayLimits {
		ConcurrentHashMap<String, Integer> chatsPerIp = new ConcurrentHashMap<>();
		ConcurrentHashMap<String, Integer> feedsPerIp = new ConcurrentHashMap<>();
		ConcurrentHashMap<String, Integer> chatsPerAcc = new ConcurrentHashMap<>();
		ConcurrentHashMap<String, Integer> feedsPerAcc = new ConcurrentHashMap<>();
	}
	private volatile long lastCheckDay;
	private volatile DayLimits dayLimits;
	private Long customCurDayPreset; //for tests
	
	
	
	public ChatsAccService(File root, Props props) {
		this(root, props, null, null);
	}
	
	public ChatsAccService(File root, Props props, AsyncListener asyncListener) {
		this(root, props, asyncListener, null);
	}
	
	public ChatsAccService(File root, Props props, AsyncListener asyncListener, LogsArchive logsArchive) {
		
		root.mkdirs();
		if(root.isFile()) throw new IllegalArgumentException("root must be a dir: "+root);
		
		this.root = root;
		this.props = props;
		this.writeExecutor = ExecutorsUtil.newSingleThreadExecutor("ChatsAccService-write-thread");
		this.asyncListener = asyncListener;
		
		if(logsArchive == null) logsArchive = new LogsArchiveImpl();
		this.logsArchive = logsArchive;
		
		
		this.extraWriteExecutor = ExecutorsUtil.newSingleThreadExecutor("ChatsAccService-extra-write-thread");
		this.timer = new TimerExt("ChatsAccService-timer", false);
		
		this.activeLogs = new ActiveChatsLogs(root, props, extraWriteExecutor, asyncListener);
		this.accsStat = new AccsStat(root, props, extraWriteExecutor, asyncListener, timer);
		
		if(props.getBoolVal(chats_cleanActiveChatLogs)){
			timer.tryScheduleAtFixedRate(() -> cleanActiveChatsLogs(), 
					props.getLongVal(chats_cleanActiveChatLogs_Delay), 
					props.getLongVal(chats_cleanActiveChatLogs_Delay));		
		}
		
		
		loadAccs();
	}
	


	public void shutdown(){
		writeExecutor.shutdown();
		extraWriteExecutor.shutdown();
		timer.cancel();
		
		activeLogs.shutdown();
		accsStat.shutdown();
	}
	
	public void setAsyncListener(AsyncListener asyncListener){
		this.asyncListener = asyncListener;
	}

	public void setGeoIp(GeoIp geoIp) {
		accsStat.setGeoIp(geoIp);
	}
	
	
	public synchronized void reloadAccs(){
		HashSet<String> existsAccs = new HashSet<>();
		read.lock();
		try {
			existsAccs.addAll(accsById.keySet());
		}finally {
			read.unlock();
		}
		
		ArrayList<String> added = new ArrayList<>();
		ArrayList<String> removed = new ArrayList<>();
		ArrayList<String> updated = new ArrayList<>();
		
		List<String> curAccs = getAccIdsNames(root);
		for (String accId : curAccs) {
			
			File accDir = getAccDir(accId);
			if(hasRemoveReqFlag(accDir)) continue;
			
			boolean exists = existsAccs.remove(accId);
			if(exists) {
				boolean isUpdated = reloadAcc(accId);
				if(isUpdated) updated.add(accId);
			} else {
				createAcc(accId);
				added.add(accId);
			}
		}
		
		removed.addAll(existsAccs);
		for(String accountId : existsAccs){
			removeAcc(accountId);
		}
		
		if(added.size() > 0) log.info("loaded "+added.size()+" accs: "+added);
		if(updated.size() > 0) log.info("updated "+updated.size()+" accs: "+updated);
		if(removed.size() > 0) log.info("removed "+removed.size()+" accs: "+existsAccs);
		
	}
	

	public ChatsAcc createAcc(String accId){
		write.lock();
		try {
			
			AccData info = accsById.get(accId);
			if(info != null) return info.acc;
			
			File accDir = getAccDir(accId);
			accDir.mkdir();
			
			ChatsAcc chats = new ChatsAcc(accId, null, null, this);
			boolean blocked = hasBlockedFlag(accDir);
			boolean paused = hasPausedFlag(accDir);
			accsById.put(accId, new AccData(chats, blocked, paused));
			
			//доп. примеры файлов-флагов
			createBlockedFlagExample(accDir);
			createRemoveReqFlagExample(accDir);
			createPausedFlagExample(accDir);
			
			return chats;		
		}finally {
			write.unlock();
		}
	}
	
	
	/** обновить состояние акка */
	private boolean reloadAcc(String accId){
		write.lock();
		try {
			
			boolean updated = false;
			
			AccData data = accsById.get(accId);
			if(data == null) return updated;
			
			File accDir = getAccDir(accId);
			
			boolean isBlocked = data.blocked;
			if(isBlocked && ! hasBlockedFlag(accDir)){
				log.info("sync: unblock acc: "+accId);
				setBlockedUnsafe(accId, data, false);
				updated = true;
			}
			else if(!isBlocked && hasBlockedFlag(accDir)){
				log.info("sync: block acc: "+accId);
				setBlockedUnsafe(accId, data, true);
				updated = true;
			}
			
			boolean isPaused = data.paused;
			if(isPaused && ! hasPausedFlag(accDir)){
				log.info("sync: unpaused acc: "+accId);
				setPausedUnsafe(accId, data, false);
				updated = true;
			}
			else if(!isPaused && hasPausedFlag(accDir)){
				log.info("sync: paused acc: "+accId);
				setPausedUnsafe(accId, data, true);
				updated = true;
			}
			
			return updated;
			
		}finally {
			write.unlock();
		}
	}


	public ChatsAcc getAcc(String accId){
		read.lock();
		try {
			AccData info = accsById.get(accId);
			return info == null? null : info.acc;
		}finally {
			read.unlock();
		}
	}
	
	public List<ChatsAcc> getAllAccs(){
		read.lock();
		try {
			Collection<AccData> values = accsById.values();
			List<ChatsAcc> out = convert(values, (i) -> i.acc);
			return out;
		}finally {
			read.unlock();
		}
	}
	
	public ChatsAcc findAcc(String accId) throws NoChatAccountException {
		ChatsAcc chats = getAcc(accId);
		if(chats == null) {
			throw new NoChatAccountException();
		}
		return chats;
	}
	
	public Collection<String> getAccIds(){
		read.lock();
		try {
			return new ArrayList<>(accsById.keySet());
		}finally {
			read.unlock();
		}
	}
	
	public Future<?> removeAcc(String accId){
		write.lock();
		try {
			AccData info = accsById.remove(accId);
			if(info != null){
				info.acc.closeAllChats();
			}
			
			Future<?> future = writeExecutor.submit(() -> {
				renameAccDirToDeleted(accId);
			});
			fireAsyncEvent(asyncListener, future);
			return future;
			
		}finally {
			write.unlock();
		}
	}
	
	
	public File getAccDir(String accId) {
		return StoreOps.getAccDir(root, accId);
	}
	
	public List<ChatLogHist> getHistory(String accId, Date date){
		
		if(getAcc(accId) == null) return null;
		
		File accDir = getAccDir(accId);
		File dayDir = new File(accDir, getLogsDayDirName(date));
		if( ! dayDir.exists()) return null;
		
		File[] logsCandidats = dayDir.listFiles();
		if(isEmpty(logsCandidats)) return null;
		
		ArrayList<ChatLogHist> out = new ArrayList<>();
		for(File file : logsCandidats){
			ChatLogHist log = readChatHist(file);
			if(log == null) continue;
			out.add(log);
		}
		
		sort(out, ChatLog::compareByDateAsc);
		
		return out;
		
	}
	
	public List<Feedback> getFeedbacks(String accId, Date date){
		
		if(getAcc(accId) == null) return null;
		
		File accDir = getAccDir(accId);
		File dayDir = new File(accDir, getLogsDayDirName(date));
		if( ! dayDir.exists()) return emptyList();
		
		File[] feedCandidats = dayDir.listFiles();
		if(isEmpty(feedCandidats)) return emptyList();
		
		ArrayList<Feedback> out = new ArrayList<>();
		for(File file : feedCandidats){
			Feedback feed = readFeedback(file);
			if(feed == null) continue;
			out.add(feed);
		}
		
		sort(out, Feedback::compareByDateAsc);
		
		return out;
		
	}

	
	public boolean isBlocked(String accId){
		read.lock();
		try {
			AccData info = accsById.get(accId);
			return info != null && info.blocked;
		}finally {
			read.unlock();
		}
	}
	
	public boolean setBlocked(String accId, boolean val){
		write.lock();
		try {
			
			AccData info = accsById.get(accId);
			if(info == null) return false;
			
			setBlockedUnsafe(accId, info, val);
			return true;
			
		}finally {
			write.unlock();
		}
	}
	
	private void setBlockedUnsafe(String accId, AccData info, boolean val){

		info.blocked = val;
		
		File accountDir = getAccDir(accId);
		if(val)createBlockedFlag(accountDir);
		else removeBlockedFlag(accountDir);
	}
	
	
	public boolean isPaused(String accId){
		read.lock();
		try {
			AccData info = accsById.get(accId);
			return info != null && info.paused;
		}finally {
			read.unlock();
		}
	}
	
	
	public boolean setPaused(String accId, boolean val) {
		write.lock();
		try {
			
			AccData info = accsById.get(accId);
			if(info == null) return false;
			
			setPausedUnsafe(accId, info, val);
			return true;
			
		}finally {
			write.unlock();
		}
	}
	
	private void setPausedUnsafe(String accId, AccData info, boolean val){

		info.paused = val;
		
		File accountDir = getAccDir(accId);
		if(val)createPausedFlag(accountDir);
		else removePausedFlag(accountDir);
	}

	
	public Future<?> addFeedbackAsync(String accId, ClientInfo info, String text){
		return addFeedbackAsync(accId, info, text, null);
	}

	public Future<?> addFeedbackAsync(String accId, ClientInfo info, String text, Date datePreset){
		
		checkFeedbackInput(accId, info, text);
		
		ReqInfo reqInfo = getReqInfo();

		Future<?> future = writeExecutor.submit(() -> 
			writeFeedback(accId, info, text, datePreset, reqInfo));
		fireAsyncEvent(asyncListener, future);
		
		accsStat.addFeedbackStatAsync(accId, info);
		
		return future;
	}
	
	private void checkFeedbackInput(String accId, ClientInfo info, String text) {
		
		findAcc(accId);
		
		validateForText(info.email, "email");
		validateForText(text, "text");
		
		checkCanCreateFeedback(accId, info);
	}
	
	public ChatLog restoreOldChatIfNeed(String accId, ClientSession clientSession, String oldChatId) {
		
		if(clientSession == null) return null;
		if(oldChatId == null) return null;
		
		ChatsAcc acc = getAcc(accId);
		if(acc == null) return null;
		
		Chat chat = activeLogs.restoreChat(accId, oldChatId, clientSession);
		if(chat == null) return null;
		
		boolean added = acc.addChat(clientSession, chat);
		return added? chat.toLog() : null;
		
	}
	
	
	
	
	@Override
	public void beforeAddClientMsg(String chatId, int clientIndex, List<Message> curMsgs, String newMsg)
			throws ValidationException {
		beforeAddMsgCheck(chatId, clientIndex, curMsgs, newMsg);
	}

	@Override
	public void beforeAddOperatorMsg(String chatId, int operatorIndex, List<Message> curMsgs, String newMsg) 
			throws ValidationException {
		beforeAddMsgCheck(chatId, operatorIndex, curMsgs, newMsg);
	}
	
	private void beforeAddMsgCheck(String chatId, int userIndex, List<Message> curMsgs, String newMsg)
			throws ValidationException {
		
		//check new msg
		checkNewMsgToAdd(props, newMsg);
		
		//check max limits
		if(isEmpty(curMsgs)) return;
		checkMaxMsgsPerChat(props, curMsgs.size());
		checkMaxSingleMsgsPerTime(props, curMsgs, userIndex);
	}
	
	
	@Override
	public void checkCanCreateChat(String accId, ClientSession client) {
		
		String ip = client.info.ip;
		
		checkBlockedByIp(props, ip);
		
		DayLimits dayLimits = getCurDayLimits();
		
		int createdByIp = tryParseInt(dayLimits.chatsPerIp.get(ip), 0);
		int createdForAcc = tryParseInt(dayLimits.chatsPerAcc.get(accId), 0);
		
		//checks
		checkMaxChatsFromIpPerDay(props, ip, createdByIp);
		checkMaxChatsForAccPerDay(props, accId, createdForAcc);
		
		//all ok
		dayLimits.chatsPerIp.put(ip, createdByIp + 1);
		dayLimits.chatsPerAcc.put(accId, createdForAcc + 1);
	}
	
	@Override
	public void onChatCreated(String accId, Chat chat, ClientSession client) {
		activeLogs.createChatLogAsync(accId, chat, client);
		accsStat.createChatStatAsync(accId, chat, client);
	}
	
	@Override
	public void onChatClientMsgAdded(String accId, Chat chat, AddClientCommentRes result) {
		activeLogs.addClientMsgLogAsync(accId, chat, result);
		accsStat.addClientMsgStatAsync(accId, chat, result);
	}
	
	@Override
	public void onChatOperatorAdded(String accId, Chat chat, ChatOperator operator) {
		activeLogs.addOperatorLogAsync(accId, chat, operator);
		accsStat.addOperatorStatAsync(accId, chat, operator);
	}
	
	@Override
	public void onChatOperatorMsgAdded(String accId, Chat chat, Message result) {
		activeLogs.addOperatorMsgLogAsync(accId, chat, result);
		accsStat.addOperatorMsgStatAsync(accId, chat, result);
	}



	
	public void checkCanCreateFeedback(String accId, ClientInfo info){
		
		String ip = info.ip;
		
		checkBlockedByIp(props, ip);
		
		DayLimits dayLimits = getCurDayLimits();
		
		int createdByIp = tryParseInt(dayLimits.feedsPerIp.get(ip), 0);
		int createdForAcc = tryParseInt(dayLimits.feedsPerAcc.get(accId), 0);
		
		checkMaxFeedbacksFromIpPerDay(props, ip, createdByIp);
		checkMaxFeedbacksForAccPerDay(props, accId, createdForAcc);
		
		dayLimits.feedsPerIp.put(ip, createdByIp + 1);
		dayLimits.feedsPerAcc.put(accId, createdForAcc + 1);
	}
	
	
	public int cleanActiveChatsLogs(){
		return cleanActiveChatsLogs(null, null);
	}
	
	public int cleanActiveChatsLogs(Date now, Long closeDelta){
		
		List<ChatsAcc> accs = getAllAccs();
		if(isEmpty(accs)) return 0;
		
		if(now == null) now = new Date();
		if(closeDelta == null) closeDelta = props.getLongVal(chats_cleanActiveChatLogs_CloseDelta);
		
		int count = 0;
		for(ChatsAcc acc : accs){
			List<Chat> chats = activeLogs.removeInactiveLogs(acc.getId(), now, closeDelta);
			if(isEmpty(chats)) continue;
			for(Chat chat : chats){
				boolean closed = acc.closeUnknownChat(chat);
				if(closed) {
					count++;
					log.info("closed unknown chat "+chat.id+" for acc "+acc.getId());
				}
			}
		}
		return count;
	}

	
	
	@Override
	public void onChatClose(String accId, Chat chat) {
		
		activeLogs.removeChatLogsAsync(accId, chat);
		accsStat.closeChatStatAsync(accId, chat);
		
		ChatLog log = chat.toLog();
		
		Future<?> future = writeExecutor.submit(() -> 
			writeChat(accId, log));
		fireAsyncEvent(asyncListener, future);
	}
	
	@Override
	public void onOperatorsUpdate(String accId, List<ChatOperator> operators) {
		Future<?> future = writeExecutor.submit(() -> 
			writeOperators(accId, operators));
		fireAsyncEvent(asyncListener, future);
	}
	
	@Override
	public void onConfigSetted(String accId, AccConfig config) {
		Future<?> future = writeExecutor.submit(() -> 
			writeAccConfig(accId, config));
		fireAsyncEvent(asyncListener, future);
	}
	
	
	
	
	private void loadAccs() {
		
		List<String> accIds = getAccIdsNames(root);
		
		if(accIds.size() > 0) log.info("load "+accIds.size()+" accounts");
		
		for (String accId : accIds) {
			AccData data = loadAccData(accId);
			accsById.put(accId, data);
			
			scanToArch(data.acc);
		}
		
		activeLogs.filterWorkDirByAccs(accIds);

	}
	
	private AccData loadAccData(String accId){
		
		File accDir = getAccDir(accId);
		
		//read operators
		List<ChatOperator> operators = null;
		File operatorsFile = new File(accDir, OPERATORS_FILE_NAME);
		if(operatorsFile.exists()){
			try {
				operators = getList(readFileUTF8(operatorsFile), ChatOperator.class, defaultGson);
			}catch (Throwable t) {
				log.error("can't read "+operatorsFile+": "+t);
			}
		}
		
		//read config
		AccConfig config = null;
		File configFile = new File(accDir, CONFIG_FILE_NAME);
		if(configFile.exists()){
			try {
				config = defaultGson.fromJson(readFileUTF8(configFile), AccConfig.class);
			}catch(Throwable t){
				log.error("can't read "+configFile+": "+t);
			}
		}
		
		
		ChatsAcc acc = new ChatsAcc(accId, operators, config, this);
		boolean blocked = hasBlockedFlag(accDir);
		boolean paused = hasPausedFlag(accDir);
		AccData info = new AccData(acc, blocked, paused);
		return info;
	}


	private void renameAccDirToDeleted(String accId) {
		try {
			File accDir = getAccDir(accId);
			if( ! accDir.exists()) return;
			
			removeRemoveReqFlag(accDir);
			
			File newDir = findDirToDeleted(accDir);
			boolean renamed = accDir.renameTo(newDir);
			if(!renamed) throw new IOException("can't raname "+accDir+" to "+newDir);
		}catch (Throwable t) {
			log.error("can't renameAccountDirToDeleted: "+t);
		}
	}


	private void writeChat(String accId, ChatLog chatLog){
		
		if(isEmpty(chatLog.messages)) return;
				
		try {
			
			Date now = new Date();
			
			File accDir = getAccDir(accId);
			if( ! accDir.exists()) return;
			
			File dayDir = new File(accDir, getLogsDayDirName(now));
			dayDir.mkdir();
			
			String fileName = chatLog.id+".log";
			File file = new File(dayDir, fileName);
			
			ChatLogStore storeData = new ChatLogStore(chatLog, now);
			String json = toJson(storeData, true);
			writeFileUTF8(file, json);
			
			ChatsAcc chats = getAcc(accId);
			if(chats != null) {
				scanToArch(chats);
			}
			
		}catch (Throwable t) {
			log.error("can't writeChat: "+t);
		}
		
	}
	
	private void writeFeedback(String accId, ClientInfo info, String text, Date datePreset, ReqInfo reqInfo){
		try {
			
			Date now = datePreset == null? new Date() : datePreset;
			
			File accDir = getAccDir(accId);
			if( ! accDir.exists()) return;
			
			File dayDir = new File(accDir, getLogsDayDirName(now));
			dayDir.mkdir();
			
			String fileName = now.getTime()+"-"+System.nanoTime()+".feed";
			File file = new File(dayDir, fileName);
			
			String ref = reqInfo == null? null : reqInfo.getFinalRef();
			
			FeedbackStore storeData = new FeedbackStore(info, now, text, ref);
			String json = toJson(storeData, true);
			writeFileUTF8(file, json);
			
		}catch (Throwable t) {
			log.error("can't writeFeedback: "+t);
		}
	}
	
	
	private ChatLogHist readChatHist(File file){
		try {
			if(file.isDirectory()) return null;
			
			String name = file.getName();
			if( ! name.endsWith(".log")) return null;
			String id = getFileNameWithoutType(name);
			
			String json = FileUtil.readFileUTF8(file);
			ChatLogStore data = defaultGson.fromJson(json, ChatLogStore.class);
			
			return new ChatLogHist(id, data.created, data.users, data.messages, data.clientRefs, data.ended);
			
		}catch (Throwable t) {
			log.error("can't readChat: "+t);
			return null;
		}
	}
	
	private Feedback readFeedback(File file) {
		try {
			if(file.isDirectory()) return null;
			
			String name = file.getName();
			if( ! name.endsWith(".feed")) return null;
			
			String json = FileUtil.readFileUTF8(file);
			FeedbackStore data = defaultGson.fromJson(json, FeedbackStore.class);
			
			return new Feedback(data.user, data.date, data.text, data.ref);
			
		}catch (Throwable t) {
			log.error("can't readFeedback: "+t);
			return null;
		}
	}
	
	

	public String getLogsDayDirName(Date date) {
		return LOGS_DIR_PREFIX + dayFormat.format(date);
	}
	
	private void writeOperators(String accId, List<ChatOperator> operators) {
		
		try {
			
			File accDir = getAccDir(accId);
			if( ! accDir.exists()) return;
			
			File operatorsFile = new File(accDir, OPERATORS_FILE_NAME);
			if(isEmpty(operators)){
				operatorsFile.delete();
			} else {
				writeFileUTF8(operatorsFile, toJson(operators, true));
			}
			
		}catch (Throwable t) {
			log.error("can't writeOperators: "+t);
		}
		
	}
	
	private void writeAccConfig(String accId, AccConfig config){
		
		try {
			
			File accDir = getAccDir(accId);
			if( ! accDir.exists()) return;
			
			File file = new File(accDir, CONFIG_FILE_NAME);
			if(config == null) {
				file.delete();
			} else {
				writeFileUTF8(file, toJson(config, true));				
			}
			
		}catch (Throwable t) {
			log.error("can't writeAccConfig: "+t);
		}
		
	}

	
	private void scanToArch(ChatsAcc chats){
		String accId = chats.getId();
		Date oldDate = chats.lastScannedToArc;
		Date newDate = logsArchive.tryCreateArcsIfNeed(root, accId, oldDate);
		chats.lastScannedToArc = newDate;
	}
	
	
	//при конкурентном доступе могут теряться лимиты - это допустимо (ради лок-free)
	private DayLimits getCurDayLimits(){
		
		long curDay = dateStart(customCurDayPreset == null? currentTimeMillis() : customCurDayPreset);
		
		//reset debug value
		if(customCurDayPreset != null) customCurDayPreset = null;
		
		//new day - reset limits	
		if(curDay > this.lastCheckDay){
			this.lastCheckDay = curDay;
			this.dayLimits = new DayLimits();
		}
		
		DayLimits out = this.dayLimits;
		return out;
	}
	
	
	
	
	

	
	
	public static File findDirToDeleted(File accDir) {
		String parent = accDir.getParent();
		String name = accDir.getName();
		File newDir = null;
		int index = 0;
		while(true){
			newDir = getDirToDeleted(parent, name, index);
			if(newDir.exists()){
				index++;
			} else {
				break;
			}
		}
		return newDir;
	}
	
	public static File getDirToDeleted(String parent, String name, int index){
		String newName = "#"+name+(index == 0? "": "#"+index);
		return new File(parent, newName);
	}
	
	
	public static void createBlockedFlag(File accDir){
		tryCreateNewFile(new File(accDir, BLOCKED_FLAG));
	}
	public static boolean hasBlockedFlag(File accDir) {
		return new File(accDir, BLOCKED_FLAG).exists();
	}
	public static void removeBlockedFlag(File accDir){
		new File(accDir, BLOCKED_FLAG).delete();
	}
	public static void createBlockedFlagExample(File accDir){
		tryCreateNewFile(new File(accDir, TMPL_PREFIX+BLOCKED_FLAG+".tmpl"));
	}
	
	
	public static void createRemoveReqFlag(File accDir){
		tryCreateNewFile(new File(accDir, REMOVE_REQ_FLAG));
	}
	public static boolean hasRemoveReqFlag(File accDir) {
		return new File(accDir, REMOVE_REQ_FLAG).exists();
	}
	public static void removeRemoveReqFlag(File accDir){
		new File(accDir, REMOVE_REQ_FLAG).delete();
	}
	public static void createRemoveReqFlagExample(File accDir){
		tryCreateNewFile(new File(accDir, TMPL_PREFIX+REMOVE_REQ_FLAG+".tmpl"));
	}
	
	
	public static void createPausedFlag(File accDir){
		tryCreateNewFile(new File(accDir, PAUSED_FLAG));
	}
	public static boolean hasPausedFlag(File accDir) {
		return new File(accDir, PAUSED_FLAG).exists();
	}
	public static void removePausedFlag(File accDir){
		new File(accDir, PAUSED_FLAG).delete();
	}
	public static void createPausedFlagExample(File accDir){
		tryCreateNewFile(new File(accDir, TMPL_PREFIX+PAUSED_FLAG+".tmpl"));
	}



}
