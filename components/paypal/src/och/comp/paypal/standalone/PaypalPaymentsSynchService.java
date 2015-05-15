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
package och.comp.paypal.standalone;

import static java.lang.Math.*;
import static och.api.model.PropKey.*;
import static och.api.model.billing.PaymentBase.*;
import static och.api.model.billing.PaymentStatus.*;
import static och.comp.ops.BillingOps.*;
import static och.util.DateUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.sql.SingleTx.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import och.api.model.billing.PaymentBase;
import och.api.model.billing.PaymentExt;
import och.api.model.billing.PaymentStatus;
import och.comp.cache.Cache;
import och.comp.cache.server.CacheServerContext;
import och.comp.cache.server.CacheServerContextHolder;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.MainDb;
import och.comp.db.main.table._f.PayStatus;
import och.comp.db.main.table._f.Updated;
import och.comp.db.main.table.billing.GetPaypalWaitPayments;
import och.comp.db.main.table.billing.UpdatePaymentById;
import och.comp.db.main.table.billing.UpdateUserAccsBlocked;
import och.comp.mail.MailService;
import och.comp.paypal.PaypalClient;
import och.comp.paypal.PaypalClientStub;
import och.comp.paypal.PaypalSoapClient;
import och.service.props.Props;
import och.service.props.impl.FileProps;
import och.util.concurrent.ExecutorsUtil;
import och.util.model.HasInitState;
import och.util.timer.TimerExt;

import org.apache.commons.logging.Log;

public class PaypalPaymentsSynchService implements HasInitState, CacheServerContextHolder {
	
	private Log log = getLog(getClass());
	private Props props;
	private MainDb db;
	private UniversalQueries universal;
	private PaypalClient client;
	private MailService mailService;
	private Cache cache;
	
	private ExecutorService singleExecutor;
	private TimerExt syncTimer;
	private TimerExt sendErrorsTimer;
	private long lastLongSync;
	private ArrayList<String> errorsToSend = new ArrayList<String>();
	
	@Override
	public void setCacheServerContext(CacheServerContext c) {
		this.props = c.props;
		this.db = c.mainDb;
		this.universal = db.universal;
		this.mailService = c.mailService;
		this.cache = c.cache;
	}
	
	public void setClient(PaypalClient client) {
		this.client = client;
	}


	@Override
	public void init() throws Exception {
		
		checkStateForEmpty(props, "props");
		checkStateForEmpty(cache, "cache");
		checkStateForEmpty(mailService, "mailService");
		checkStateForEmpty(universal, "universal");
		
		if(client == null){
			if(props.getBoolVal(paypal_enabled)){
				Props connectProps = FileProps.createPropsWithoutUpdate(new File(props.findVal(paypal_configPath)));
				client = new PaypalSoapClient(props, connectProps);				
			} else {
				client = new PaypalClientStub();
			}
		}
		
		//skip timers
		if( props.getBoolVal(paypal_sync_debug_DisableTimer)) return;
		
		syncTimer = new TimerExt("PaypalPaymentsSynchService-sync", false);
		syncTimer.tryScheduleAtFixedRate(()-> doSyncWork(), 
				props.getLongVal(paypal_sync_timerDelay), props.getLongVal(paypal_sync_timerDelta));
		
		sendErrorsTimer = new TimerExt("PaypalPaymentsSynchService-send-errors", false);
		sendErrorsTimer.tryScheduleAtFixedRate(()-> sendErrorsToAdmin(), 
				props.getLongVal(paypal_sync_sendErrorsDelay), props.getLongVal(paypal_sync_sendErrorsDelta));
		
		singleExecutor = ExecutorsUtil.newSingleThreadExecutor("PaypalPaymentsSynchService-single-execute");

	}

	public void stop(){
		if(syncTimer != null) syncTimer.cancel();
		if(sendErrorsTimer != null) sendErrorsTimer.cancel();
		if(singleExecutor != null) singleExecutor.shutdown();
	}
	
	public synchronized void doSyncWork() throws Exception{
		doSyncWork(null);
	}
	
	public synchronized void doSyncWork(Date nowPreset) throws Exception{
		
		if(props.getBoolVal(paypal_sync_debug_DisableSync)) return;
		
		Date now = nowPreset == null? new Date() : nowPreset;
		
		List<PaymentExt> allList = universal.select(new GetPaypalWaitPayments());
		if(props.getBoolVal(paypal_sync_log)) log.info("sync: "+allList);
		if(isEmpty(allList)) return;
		
		//long (1 per hour)
		long curTime = now.getTime();
		long delta = curTime - lastLongSync;
		if(delta > props.getLongVal(paypal_sync_longUpdateDelta)){
			lastLongSync = curTime;
			syncPayments(allList, now);
			return;
		}
		
		//fast (1 per minute)
		long maxDelta = props.getLongVal(paypal_sync_fastUpdatePeriod);
		ArrayList<PaymentExt> fastList = new ArrayList<PaymentExt>();
		for(PaymentExt p : allList){
			long paymentDelta = abs(curTime - p.updated.getTime());
			if(paymentDelta <= maxDelta){
				fastList.add(p);
			}
		}
		syncPayments(fastList, now);
		
		
	}

	protected void syncPayments(List<PaymentExt> list, Date now) {
		
		if(isEmpty(list)) return;
		
		ArrayList<PaymentExt> newPayments = new ArrayList<PaymentExt>();
		ArrayList<PaymentExt> oldPayments = new ArrayList<PaymentExt>();
		
		long curTime = now.getTime();
		for(PaymentExt p : list){
			long createdDelta = abs(curTime - p.created.getTime());
			if(createdDelta <= ONE_DAY) newPayments.add(p);
			else oldPayments.add(p);
		}
		
		syncPaymentsByDate(newPayments, now);
		syncPaymentsByIdAsync(oldPayments, now);
	}
	
	protected void syncPaymentsByDate(List<PaymentExt> list, Date now){
		
		if(isEmpty(list)) return;
		
		HashMap<String, PaymentExt> oldByIds = new HashMap<>();
		Date minDate = null;
		for (PaymentExt p : list) {
			oldByIds.put(p.externalId, p);
			if(minDate == null || minDate.after(p.created)) minDate = p.created;
		}
		
		int daysBefore = (int)dateDiffInDays(now, minDate);
		if(daysBefore == 0) daysBefore = 1;
		
		List<PaymentBase> paymentHistory = null;
		try {
			paymentHistory = client.getPaymentHistory(daysBefore);
		}catch(Exception e){
			addErrorToSend("can't getPaymentHistory: "+e);
			return;
		}
		
		if(isEmpty(paymentHistory)) {
			addErrorToSend("empty history by daysBefore: "+daysBefore+" for sync payments: "+list);
			return;
		};
		
		for(PaymentBase newItem : paymentHistory){
			PaymentExt old = oldByIds.remove(newItem.externalId);
			if(old == null) continue;
			trySyncPayment(old, newItem, now);
		}
		
	}

	protected void syncPaymentsByIdAsync(List<PaymentExt> list, Date now){
		
		if(isEmpty(list)) return;
		
		for (PaymentExt p : list) {
			if(singleExecutor != null) singleExecutor.submit(() -> trySyncPaymnentById(p, now));
			else trySyncPaymnentById(p, now);
		}
	}
	
	private void trySyncPaymnentById(PaymentExt p, Date now){
		try {
			PaymentBase newData = client.getPayment(p.created, p.externalId);
			trySyncPayment(p, newData, now);
		}catch(Exception e){
			addErrorToSend("can't syncPaymentsByIdAsync: "+e);
			return;
		}
	}
	
	
	private void trySyncPayment(PaymentExt cur, PaymentBase newData, Date now) {
		try {
			
			PaymentStatus newStatus = newData.paymentStatus;
			if(newStatus == cur.paymentStatus){
				return;
			}
			
			UpdatePaymentById updateStatusReq = new UpdatePaymentById(cur.id, new Updated(now), new PayStatus(newStatus));
			BigDecimal minActiveBalance = props.getBigDecimalVal(billing_minActiveBalance);
			
			//error state
			if(newStatus == ERROR){
				addErrorToSend("payment with ERROR status: "+newData);
				universal.update(updateStatusReq);
				return;
			}
			
			//success state
			if(cur.paymentStatus == WAIT && newStatus == COMPLETED){
				
				long userId = cur.userId;
				BigDecimal[] newBalance = {null};
				boolean[] unblocked = {false};
				
				doInSingleTxMode(()->{
					
					BigDecimal curBalance = findBalance(universal, userId).balance;
					newBalance[0] = appendBalance(universal, userId, cur.amount);
					
					if(isNeedDeblockAccsState(curBalance, newBalance[0], minActiveBalance)) {
						unblocked[0] = true;
						universal.update(new UpdateUserAccsBlocked(userId, false));
						db.chats.updateOwnersAccsLastPay(userId, now);
					}
					
					universal.update(updateStatusReq);
				});
				
				cache.tryPutCache(getBalanceCacheKey(userId), newBalance[0].toString());
				
				//update chat servers
				if(unblocked[0]){
					sendAccsBlocked(props, db, cache, userId, false);
				}
				
				return;
			}
			
			//unknown state
			addErrorToSend("unknown payment state: old data: "+cur + ", new data: "+newData);
			
			
		} catch(Exception e){
			addErrorToSend("can't sync payment "+cur+": "+e);
		}
	}

	
	private void sendErrorsToAdmin() {
		
		List<String> errors = popAllErrorsToSend();
		if(isEmpty(errors)) return;
		
		//filter by stop terms
		List<String> stopTerms = strToList(
				props.getStrVal(paypal_sync_skipErrorTerms), 
				props.getStrVal(paypal_sync_skipErrorTermsSep));
		if( ! isEmpty(stopTerms)){
			ArrayList<String> filtered = new ArrayList<String>();
			nextErr: for (String err : errors) {
				for (String stopTerm : stopTerms) {
					if(err.contains(stopTerm)) continue nextErr;
				}
				filtered.add(err);
			}
			if(isEmpty(filtered)) return;
			errors = filtered;
		}
		
		
		if(props.getBoolVal(paypal_sync_debug_DisableSendErrors)) return;
		
		mailService.sendAsyncWarnData("Sync payments errors", errors);
	}
	
	
	private void addErrorToSend(String error){
		synchronized (errorsToSend) {
			errorsToSend.add(error);
		}
	}
	
	private List<String> popAllErrorsToSend(){
		synchronized (errorsToSend) {
			ArrayList<String> copy = new ArrayList<>(errorsToSend);
			errorsToSend.clear();
			return copy;
		}
	}
	
	public List<String> getErrorsToSend(){
		synchronized (errorsToSend) {
			return new ArrayList<>(errorsToSend);
		}
	}

}
