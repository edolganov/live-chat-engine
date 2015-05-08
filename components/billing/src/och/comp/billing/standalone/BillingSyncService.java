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
package och.comp.billing.standalone;

import static java.math.BigDecimal.*;
import static java.util.Collections.*;
import static och.api.model.PropKey.*;
import static och.api.model.RemoteCache.*;
import static och.api.model.RemoteChats.*;
import static och.api.model.billing.PaymentBase.*;
import static och.api.model.billing.PaymentExt.*;
import static och.api.model.billing.PaymentType.*;
import static och.api.model.chat.account.PrivilegeType.*;
import static och.api.model.tariff.TariffMath.*;
import static och.comp.db.main.table.MainTables.*;
import static och.comp.ops.BillingOps.*;
import static och.comp.ops.ServersOps.*;
import static och.comp.web.JsonOps.*;
import static och.util.DateUtil.*;
import static och.util.ExceptionUtil.*;
import static och.util.FileUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.model.HoursAndMinutes.*;
import static och.util.sql.SingleTx.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import och.api.exception.ExpectedException;
import och.api.model.billing.AdminSyncResp;
import och.api.model.billing.LastSyncInfo;
import och.api.model.billing.PaymentExt;
import och.api.model.billing.UserBalance;
import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.ChatAccountPrivileges;
import och.api.model.server.ServerRow;
import och.api.model.tariff.Tariff;
import och.api.remote.chats.GetPausedStateReq;
import och.api.remote.chats.GetUnblockedAccsReq;
import och.api.remote.chats.ResultAccsResp;
import och.comp.cache.Cache;
import och.comp.cache.server.CacheServerContext;
import och.comp.cache.server.CacheServerContextHolder;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.MainDb;
import och.comp.db.main.table._f.TariffLastPay;
import och.comp.db.main.table.billing.CreatePayment;
import och.comp.db.main.table.billing.GetAllBlockedUsers;
import och.comp.db.main.table.billing.UpdateUserAccsBlocked;
import och.comp.db.main.table.chat.GetAllChatAccounts;
import och.comp.db.main.table.chat.UpdateChatAccountByUid;
import och.comp.db.main.table.chat.privilege.GetAllChatAccountPrivileges;
import och.comp.db.main.table.tariff.GetAllTariffs;
import och.comp.mail.MailService;
import och.comp.ops.BillingOps.PausedStateResp;
import och.service.props.Props;
import och.util.model.CallableVoid;
import och.util.model.HasInitState;
import och.util.model.HoursAndMinutes;
import och.util.sql.ConcurrentUpdateSqlException;
import och.util.timer.TimerExt;

import org.apache.commons.logging.Log;

public class BillingSyncService implements HasInitState, CacheServerContextHolder {
	
	private Log log = getLog(getClass());
	private Props props;
	private MainDb db;
	private UniversalQueries universal;
	private MailService mailService;
	private Cache cache;
	
	private TimerExt paySyncTimer;
	private TimerExt cacheMonitorTimer;
	private TimerExt blockSyncTimer;
	
	public CallableVoid syncAccsListener;

	@Override
	public void setCacheServerContext(CacheServerContext c) {
		this.props = c.props;
		this.db = c.mainDb;
		this.universal = db.universal;
		this.mailService = c.mailService;
		this.cache = c.cache;
	}
	
	
	@Override
	public void init() throws Exception {
		
		checkStateForEmpty(props, "props");
		checkStateForEmpty(cache, "cache");
		checkStateForEmpty(mailService, "mailService");
		checkStateForEmpty(universal, "universal");
		
		
		//block and pause sync
		if(props.getBoolVal(billing_sync_fillBlockedCacheOnStart)){
			long delay = props.getLongVal(billing_sync_fillBlockedCacheOnStartDelay);
			if(delay < 1){
				reinitAccsBlocked(props, db, cache);
				reinitAccsPaused(props, db);
			} else {
				TimerExt timer = new TimerExt("BillingSyncService-fillBlockedCache", false);
				timer.trySchedule(()->{
					reinitAccsBlocked(props, db, cache);
					reinitAccsPaused(props, db);
					timer.cancel();
				}, delay);			
			}
		}
		
		
		loadLastSyncInfo();
		
		//skip timer
		if( props.getBoolVal(billing_sync_debug_DisableTimer)) return;
		
		paySyncTimer = new TimerExt("BillingSyncService-pay-sync", false);
		paySyncTimer.tryScheduleAtFixedRate(()-> 
			doSyncWork(true, null, null), 
			props.getLongVal(billing_sync_timerDelay), 
			props.getLongVal(billing_sync_timerDelta));
		
		
		blockSyncTimer = new TimerExt("BillingSyncService-block-sync", false);
		blockSyncTimer.tryScheduleAtFixedRate(()-> {
				checkAccBlocks();
				checkAccPaused();
			},
			props.getLongVal(billing_sync_blockCheckTimerDelay), 
			props.getLongVal(billing_sync_blockCheckTimerDelta));
		
		
		cacheMonitorTimer = new TimerExt("BillingSyncService-tasks-checker", false);
		cacheMonitorTimer.tryScheduleAtFixedRate(()-> 
			checkTasksFromCache(), 
			props.getLongVal(billing_sync_taskMonitorTimerDelay), 
			props.getLongVal(billing_sync_taskMonitorTimerDelta));
		
	}
	
	
	private void loadLastSyncInfo() {
		File file = new File(props.getStrVal(billing_sync_lastSyncFile));
		String lastSyncInfo = file.exists()? tryReadFileUTF8(file) : null;
		cache.tryPutCache(BILLING_LAST_SYNC, lastSyncInfo);
	}
	
	private void saveLastSyncInfo(int updated) {
		
		if( ! props.getBoolVal(billing_sync_lastSyncStore)) return;
		
		String lastSyncInfo = toJson(new LastSyncInfo(updated), true);
		cache.tryPutCache(BILLING_LAST_SYNC, lastSyncInfo);
		tryWriteFileUTF8(new File(props.getStrVal(billing_sync_lastSyncFile)), lastSyncInfo);
	}


	public void checkTasksFromCache() throws Exception{
		
		String reqId = cache.tryRemoveCache(BILLING_SYNC_REQ);
		if(reqId == null) return;
		
		cache.tryPutCache(BILLING_SYNC_RESP, BILLING_SYNC_RESP_START_PREFIX+"started at "+new Date());
		
		log.info("doSyncWork by admin req: "+reqId);
		long start = System.currentTimeMillis();
		
		int updatedCount = doSyncWork(false, null, null);
		
		long worktime = System.currentTimeMillis() - start;
		log.info("done. worktime: "+worktime+"ms");
		
		AdminSyncResp result = new AdminSyncResp(reqId, updatedCount, worktime);
		cache.tryPutCache(BILLING_SYNC_RESP, toJson(result, true));
		
	}
	
	
	
	
	
	
	public int doSyncWork(boolean checkWorkTime) throws Exception {
		return doSyncWork(checkWorkTime, null, null);
	}
	
	public int doSyncWork(boolean checkWorkTime, Date nowPreset) throws Exception {
		return doSyncWork(checkWorkTime, nowPreset, null);
	}


	public int doSyncWork(boolean checkWorkTime, 
			Date nowPreset, 
			CallableVoid beforeDbUpdateListener) throws Exception {
		
		Date now = nowPreset != null? nowPreset : new Date();
		
		if(props.getBoolVal(billing_sync_debug_DisableSync)) return -1;
		
		//проверка актуальности старта
		if(checkWorkTime && props.getBoolVal(billing_sync_debug_CheckWorkTime)){
			int dayOfMonth = dayOfMonth(now);
			int endDay = props.getIntVal(billing_sync_endSyncDay);
			int startDay = props.getIntVal(billing_sync_startSyncDay);
			if(dayOfMonth < startDay) return -2;
			if(dayOfMonth > endDay) return -3;
			
			HoursAndMinutes nowHHmm = getHoursAndMinutes(now);
			if(dayOfMonth == startDay){
				HoursAndMinutes startHHmm = tryParseHHmm(props.getStrVal(billing_sync_startSyncTime), null);
				if(startHHmm != null && nowHHmm.compareTo(startHHmm) < 0) return -2;
			}
			if(dayOfMonth == endDay){
				HoursAndMinutes endHHmm = tryParseHHmm(props.getStrVal(billing_sync_endSyncTime), null);
				if(endHHmm != null && nowHHmm.compareTo(endHHmm) > 0) return -3;
			}
		}
		
		Date curMonthStart = monthStart(now);
		
		//get all accs
		HashSet<Long> needPayAccs = new HashSet<Long>();
		HashMap<Long, ChatAccount> accsById = new HashMap<>();
		List<ChatAccount> allAccs = universal.select(new GetAllChatAccounts());
		for (ChatAccount acc : allAccs) {
			accsById.put(acc.id, acc);
			if(isNeedToPay(acc, curMonthStart)) 
				needPayAccs.add(acc.id);
		}
		
		if(props.getBoolVal(billing_sync_log)) log.info("sync accs to pay ("+needPayAccs.size()+"): "+needPayAccs);
		if(isEmpty(needPayAccs)) {
			saveLastSyncInfo(0);
			return 0;
		}
		
		//get tariffs
		List<Tariff> tariffs = universal.select(new GetAllTariffs());
		HashMap<Long, Tariff> tariffsById = new HashMap<>();
		for(Tariff t : tariffs) tariffsById.put(t.id, t);
		
		//find owners
		HashMap<Long, Set<ChatAccount>> accsByUser = new HashMap<>();
		List<ChatAccountPrivileges> allUsersPrivs = universal.select(new GetAllChatAccountPrivileges());
		for (ChatAccountPrivileges data : allUsersPrivs) {
			if(data.privileges.contains(CHAT_OWNER)){
				ChatAccount acc = accsById.get(data.accId);
				if(acc == null) continue;
				putToSetMap(accsByUser, data.userId, acc);
			}
		}
		
		
		if(beforeDbUpdateListener != null) beforeDbUpdateListener.call();
		
		//sync by owners
		ArrayList<SyncPayError> syncErrors = new ArrayList<>();
		for (Entry<Long, Set<ChatAccount>> entry : accsByUser.entrySet()) {
			Long userId = entry.getKey();
			Set<ChatAccount> userAccs = entry.getValue();
			try {
				
				if(syncAccsListener != null) syncAccsListener.call();
				
				List<SyncPayError> curErrors = syncUserAccs(userId, userAccs, tariffsById, curMonthStart, now);
				if(curErrors.size() > 0) syncErrors.addAll(curErrors);
				
			}
			//произошла смена тарифа, до того как успели посчитать
			//новая попытка расчета будет на следующем вызове таймера
			catch(ConcurrentUpdateSqlException e){
				//удаляем из выходного результата акки
				for(ChatAccount acc : userAccs) needPayAccs.remove(acc.id);
			}
			catch(Throwable t){
				log.error("can't sync accs for user="+userId+": "+t);
				syncErrors.add(new SyncPayError(userId, userAccs, t));
			}
		}
		
		
		if(syncErrors.size() > 0) 
			sendSyncErrorMailToAdmin("Sync billing errors", syncErrors);
		
		
		int updated = needPayAccs.size();
		
		saveLastSyncInfo(updated);
		
		return updated;
	}



	



	private List<SyncPayError> syncUserAccs(
			long userId, 
			Set<ChatAccount> userAccs, 
			Map<Long, Tariff> tariffs, 
			Date curMonthStart, 
			Date now) throws ConcurrentUpdateSqlException, Exception {
		
		List<SyncPayError> errors = new ArrayList<>();
		BigDecimal totalPrice = ZERO;
		Date lastMonthStart = addMonths(curMonthStart, -1);
		Date lastMonthEnd = monthEnd(lastMonthStart);
		
		ArrayList<ChatAccount> accsToUpdate = new ArrayList<>();
		for(ChatAccount acc : userAccs){
			if(isNeedToPay(acc, curMonthStart)) {
				Tariff tariff = tariffs.get(acc.tariffId);
				if(tariff == null) {
					errors.add(new SyncPayError(userId, singleton(acc), new IllegalStateException("can't find tariff")));
					continue;
				}
				
				accsToUpdate.add(acc);
				
				BigDecimal price = tariff.price;
				if(price.compareTo(ZERO) < 1) continue;
				
				//если целый месяц - полный тариф, иначе расчет по периоду
				BigDecimal accAmount;
				Date oldPayDate = acc.tariffLastPay;
				if(oldPayDate.compareTo(lastMonthStart) == 0) accAmount = price;
				else accAmount = calcForPeriod(price, oldPayDate, lastMonthEnd, ZERO);
				
				totalPrice = totalPrice.add(accAmount);
			}
		}
		
		
		//final amount
		BigDecimal amount = totalPrice;
		boolean hasBill = ZERO.compareTo(amount) != 0;
		BigDecimal[] updatedBalance = {null};
		BigDecimal minActiveBalance = props.getBigDecimalVal(billing_minActiveBalance);
		boolean[] accBlocked = {false};
		
		//update db
		doInSingleTxMode(()->{
			
			boolean isBlocked = findBalance(universal, userId).accsBlocked;
			
			//bill
			if( ! isBlocked && hasBill) {
				
				long payId = universal.nextSeqFor(payments);
				PaymentExt payment = createSystemBill(payId, userId, amount, now, TARIFF_MONTH_BIll, collectionToStr(accsToUpdate));
				universal.update(new CreatePayment(payment));
				
				updatedBalance[0] = appendBalance(universal, userId, payment.amount);
				
				
				//если баланс стал отрицательным и то блокируем акки
				if(updatedBalance[0].compareTo(minActiveBalance) < 0){
					accBlocked[0] = true;
					universal.update(new UpdateUserAccsBlocked(userId, true));
				}
				
			}
			
			//update last pay dates
			if(accsToUpdate.size() > 0) {
				for (ChatAccount acc : accsToUpdate) {
					int result = universal.updateOne(new UpdateChatAccountByUid(
							acc.uid, 
							acc.tariffLastPay,
							new TariffLastPay(curMonthStart)));
					//concurrent check
					if(result == 0) throw new ConcurrentUpdateSqlException("UpdateChatAccountByUid: uid="+acc.uid);
				}
			}
			
		});
		
		//update cache
		if(updatedBalance[0] != null) {
			cache.tryPutCache(getBalanceCacheKey(userId), updatedBalance[0].toString());
		}
		
		//update chat servers
		if(accBlocked[0]){
			sendAccsBlocked(props, db, cache, userId, true);
		}
		
		
		return errors;
		
		
	}
	
	private boolean isNeedToPay(ChatAccount acc, Date curMonthStart){
		if(acc.tariffStart.compareTo(curMonthStart) >= 0) return false;
		return acc.tariffLastPay.before(curMonthStart);
	}
	
	
	
	/** проверить рассинхрон заблокированных акков и послать письмо админу */
	public void checkAccBlocks() throws Exception {
		
		List<UserBalance> list = universal.select(new GetAllBlockedUsers());
		if(isEmpty(list)) return;
		
		List<SyncBlockError> syncErrors = new ArrayList<>();
		
		for (UserBalance userBalance : list) {
			long userId = userBalance.userId;
			Map<String, List<String>> unblockedAccs = getUnblockedAccs(userId);
			if( ! isEmpty(unblockedAccs)){
				for (Entry<String, List<String>> entry : unblockedAccs.entrySet()) {
					String serverUrl = entry.getKey();
					List<String> accs = entry.getValue();
					syncErrors.add(new SyncBlockError(userId, serverUrl, accs));
				}
			}
			
			
		}
		
		if(syncErrors.size() > 0) 
			sendSyncErrorMailToAdmin("Unblocked accs error", syncErrors);
		
	}
	
	public void checkAccPaused() throws Exception {
		
		Map<Long, ServerRow> servers = getServersMap(universal);
		PausedStateResp state = getPausedState(universal);
		
		checkAccPaused(servers, state.pausedAccs, true);
		checkAccPaused(servers, state.unpausedAccs, false);
	}
	
	
	private void checkAccPaused(Map<Long, ServerRow> servers, Set<ChatAccount> accs, boolean expectedPaused){
		
		HashSet<String> serverUrls = new HashSet<String>();
		HashSet<String> uids = new HashSet<String>();
		for (ChatAccount acc : accs) {
			ServerRow server = servers.get(acc.serverId);
			if(server == null) continue;
			uids.add(acc.uid);
			serverUrls.add(server.createUrl(URL_CHAT_GET_PAUSED_STATE));
		}
	
		Map<String, List<String>> errorAccs = new HashMap<String, List<String>>();
		for(String url : serverUrls){
			try {
				GetPausedStateReq req = new GetPausedStateReq(uids, expectedPaused);
				ResultAccsResp result = postEncryptedJson(props, url, req, ResultAccsResp.class);
				if(result == null) continue;
				if(isEmpty(result.accs)) continue;
				errorAccs.put(url, result.accs);
			}catch(Exception e){
				ExpectedException.logError(log, e, "can't connect to "+url);
			}
		}
		
		if( isEmpty(errorAccs)) return;
		
		List<SyncPausedError> syncErrors = new ArrayList<>();
		for (Entry<String, List<String>> entry : errorAccs.entrySet()) {
			syncErrors.add(new SyncPausedError(entry.getKey(), expectedPaused, entry.getValue()));
		}
		
		String msg = expectedPaused? "Unpaused accs error" : "Paused accs error";
		sendSyncErrorMailToAdmin(msg, syncErrors);
		
	}
	


	private Map<String, List<String>> getUnblockedAccs(long userId) {
		
		List<ChatAccount> accs = db.chats.getOwnerAccsInfo(userId);
		
		Map<String, List<String>> out = new HashMap<String, List<String>>();
		
		HashSet<String> serverUrls = new HashSet<String>();
		HashSet<String> uids = new HashSet<String>();
		for (ChatAccount acc : accs) {
			uids.add(acc.uid);
			serverUrls.add(acc.server.createUrl(URL_CHAT_GET_UNBLOKED));
		}
		
		for(String url : serverUrls){
			try {
				ResultAccsResp result = postEncryptedJson(props, url, new GetUnblockedAccsReq(uids), ResultAccsResp.class);
				if(result == null) continue;
				if(isEmpty(result.accs)) continue;
				out.put(url, result.accs);
			}catch(Exception e){
				ExpectedException.logError(log, e, "can't connect to "+url);
			}
		}
		
		return out;
	}


	private void sendSyncErrorMailToAdmin(String title, List<?> syncErrors) {
		
		if(isEmpty(syncErrors)) return;
		
		if(props.getBoolVal(billing_sync_debug_DisableSendErrors)) return;
		
		List<String> msgs = convert(syncErrors, (d)-> toJson(d, true));
		mailService.sendAsyncWarnData(title, msgs);
	}
	
	
	public static class SyncPayError {
		
		public Long userId;
		public Set<ChatAccount> userAccs;
		public String errorMsg;
		
		public SyncPayError(Long userId, Set<ChatAccount> userAccs, Throwable t) {
			this.userId = userId;
			this.userAccs = userAccs;
			this.errorMsg = printStackTrace(t);
		}
		
	}
	
	
	public static class SyncBlockError {
		
		public Long userId;
		public String serverUrl;
		public List<String> unblockedAccs;
		
		public SyncBlockError(Long userId, String serverUrl,
				List<String> unblockedAccs) {
			super();
			this.userId = userId;
			this.serverUrl = serverUrl;
			this.unblockedAccs = unblockedAccs;
		}
	}
	
	public static class SyncPausedError {

		public String serverUrl;
		public boolean expectedPaused;
		public List<String> accs;
		
		public SyncPausedError(String serverUrl, boolean expectedPaused,
				List<String> accs) {
			this.serverUrl = serverUrl;
			this.expectedPaused = expectedPaused;
			this.accs = accs;
		}
	}

}
