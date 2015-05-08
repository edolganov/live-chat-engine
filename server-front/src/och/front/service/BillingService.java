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
package och.front.service;

import static java.math.BigDecimal.*;
import static java.util.Collections.*;
import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.billing.PaymentBase.*;
import static och.api.model.billing.PaymentStatus.*;
import static och.api.model.billing.PaymentType.*;
import static och.api.model.user.SecurityContext.*;
import static och.api.model.web.ReqInfo.*;
import static och.comp.db.main.table.MainTables.*;
import static och.comp.ops.BillingOps.*;
import static och.util.Util.*;
import static och.util.sql.SingleTx.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import och.api.annotation.Secured;
import och.api.exception.ExpectedException;
import och.api.exception.billing.NoDataToConfirmPaymentException;
import och.api.exception.user.UserNotFoundException;
import och.api.model.billing.PayData;
import och.api.model.billing.PaymentBase;
import och.api.model.billing.PaymentExt;
import och.api.model.billing.PaymentType;
import och.api.model.billing.UserBalance;
import och.api.model.billing.UserStartBonus;
import och.api.model.user.User;
import och.comp.cache.client.CacheClient;
import och.comp.db.base.universal.SelectRows;
import och.comp.db.main.table.billing.CreatePayment;
import och.comp.db.main.table.billing.GetPaymentsByUserId;
import och.comp.db.main.table.billing.GetStartBonusByUserId;
import och.comp.db.main.table.billing.SelectUserBalanceById;
import och.comp.db.main.table.billing.SelectUserBalancesByIds;
import och.comp.db.main.table.billing.UpdateUserAccsBlocked;
import och.comp.db.main.table.billing.UpdateUserStartBonus;
import och.comp.mail.SendReq;
import och.comp.paypal.PaypalClient;
import och.comp.paypal.PaypalClientListener;
import och.comp.tocheckout.ToCheckoutProvider;
import och.front.service.event.DBInitedEvent;
import och.front.service.event.admin.UpdateModelsEvent;
import och.service.billing.BillingProvider;
import och.service.props.Props;
import och.util.sql.ConcurrentUpdateSqlException;


public class BillingService extends BaseFrontService {
	
	HashMap<String, BillingProvider> providers = new HashMap<>();
	PaypalClient paypalClient;
	ToCheckoutProvider toCheckoutProvider;
	ChatService chats;
	CacheClient cache;

	public BillingService(FrontAppContext c) {
		super(c);
	}
	
	@Override
	public void init() throws Exception {
		paypalClient = c.paypalClient;
		chats = c.root.chats;
		cache = c.cache;
		toCheckoutProvider = c.toCheckoutProvider;
		
		Props props = c.props;
		providers.put(props.getStrVal(paypal_key), paypalClient);
		providers.put(props.getStrVal(toCheckout_key), c.toCheckoutProvider);
		
		paypalClient.addListener(new PaypalClientListener() {
			@Override
			public void onPaymentWarning(String respData) {
				c.mails.sendAsyncWarnData("Warnings in payments", respData);
			}
		});
		
		c.events.addListener(DBInitedEvent.class, (event)-> loadBalancesToCache());
		c.events.addListener(UpdateModelsEvent.class, (event)-> updateAllUserBalanceCaches());
	}
	
	
	@Secured
	public BigDecimal getCurUserBalance() throws Exception {
		
		long userId = findUserIdFromSecurityContext();
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			return getUserBalance(userId);
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	@Secured
	public BigDecimal getUserBalance(long userId) throws UserNotFoundException, Exception {
		
		checkAccessFor_MODERATOR();

		String cacheKey = getBalanceCacheKey(userId);
		BigDecimal val = tryParseBigDecimal(cache.tryGetVal(cacheKey), null) ;
		if(val == null){
			UserBalance data = universal.selectOne(new SelectUserBalanceById(userId));
			if(data == null) throw new UserNotFoundException(userId);
			
			val = data.balance == null? ZERO : data.balance;
			cache.putCacheAsync(cacheKey, val.toString());
		}
		
		return val;
		
	}
	
	
	
	@Secured
	public void updateUserBalanceCache(Long id, boolean async){
		updateUserBalanceCache(singleton(id), async);
	}
	
	
	@Secured
	public void updateUserBalanceCache(Set<Long> ids, boolean async){
		
		checkAccessFor_ADMIN();
		
		try {
			
			SelectRows<UserBalance> select = ids.size() == 1? new SelectUserBalanceById(firstFrom(ids)) : new SelectUserBalancesByIds(ids);
			List<UserBalance> infos = universal.select(select);
			for (UserBalance info : infos) {
				String key = getBalanceCacheKey(info.userId);
				String val = info.balance.toString();
				if(async) cache.putCacheAsync(key, val);
				else cache.tryPutCache(key, val);
			}
			
		}catch(Throwable t){
			ExpectedException.logError(log, t, "can't updateUserBalanceCaches");
		}
	}
	
	private void updateAllUserBalanceCaches(){
		
		pushToSecurityContext_SYSTEM_USER();
		try {
		
			Map<String, Long> allOwners = chats.getAllAccOwners();
			HashSet<Long> ids = new HashSet<Long>(allOwners.values());
			updateUserBalanceCache(ids, false);
		
		} finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	@Secured
	public PayData sendPayReq(String providerKey, BigDecimal val) throws Exception{
		
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		BillingProvider provider = getProvider(providerKey);
		
		validateState(val.doubleValue() > 0, "val");
		
		PayData payData = provider.payWithAccReq(val, userId);
		
		if(payData.token == null)
			payData.token = randomUUID();
		
		String cacheData = toJson(new PayReqCacheData(payData.token, val));
		cache.putCache(getPayReqCacheKey(userId), cacheData , props.getIntVal(payment_payReqTokenLivetime));
		
		log.info("pay req sended: userId="+userId
				+", login="+user.login
				+", req="+getReqInfoStr());
		
		return payData;
	}

	
	@Secured
	public BigDecimal paypal_preparePayConfirm(String userPayId, String token) throws NoDataToConfirmPaymentException, IOException {
		
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		PayReqCacheData reqData = checkAndGetPayReq(token);
		
		String confirmData = toJson(new PayConfirmCacheData(reqData.token, reqData.val, userPayId));
		cache.putCache(getConfirmPayCacheKey(userId), confirmData, props.getIntVal(payment_payConfirmDataLivetime));
		
		log.info("pay confirm prepared: userId="+userId
				+", login="+user.login
				+", req="+getReqInfoStr());
		
		return reqData.val;
		
	}
	
	@Secured
	public BigDecimal paypal_getPayConfirmVal(){
		
		long userId = findUserIdFromSecurityContext();
		
		PayConfirmCacheData data = tryParseJson(cache.tryGetVal(getConfirmPayCacheKey(userId)), PayConfirmCacheData.class);
		return data == null? BigDecimal.ZERO : data.val;
	}
	
	@Secured
	public void cancelPayment(){
		
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		//clear caches
		cache.removeCacheAsync(getPayReqCacheKey(userId));
		cache.removeCacheAsync(getConfirmPayCacheKey(userId));
		
		log.info("pay canceled: userId="+userId
				+", login="+user.login
				+", req="+getReqInfoStr());
	}
	
	

	
	@Secured
	public void paypal_finishPayment() throws NoDataToConfirmPaymentException, Exception {
		findUserFromSecurityContext();
		paypal_finishPayment(null);
	}
	
	
	@Secured
	public void paypal_finishPayment(Date nowPreset) throws NoDataToConfirmPaymentException, Exception {
		
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		PayConfirmCacheData data = tryParseJson(cache.tryGetVal(getConfirmPayCacheKey(userId)), PayConfirmCacheData.class);
		if( data == null) throw new NoDataToConfirmPaymentException();
		
		//call paypal
		PaymentBase result = paypalClient.finishPayment(data.token, data.userPayId, data.val, userId);
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			addPay_Full(user, result, nowPreset);
		} finally {
			popUserFromSecurityContext();
		}
	}
	
	@Secured
	public void tochekout_finishPayment(String token, String txId) throws NoDataToConfirmPaymentException, Exception {
		tochekout_finishPayment(token, txId, null);
	}
	
	@Secured
	public void tochekout_finishPayment(String token, String txId, Date nowPreset) throws NoDataToConfirmPaymentException, Exception {
		
		User user = findUserFromSecurityContext();
		
		PayReqCacheData payData = checkAndGetPayReq(token);
		PaymentBase result = toCheckoutProvider.finishPayment(payData.val, user.id, txId);
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			addPay_Full(user, result, nowPreset);
		} finally {
			popUserFromSecurityContext();
		}
	}
	
	
	/**
	 * Positive amount. Unblock account if need
	 */
	@Secured
	public void addPay_Full(User user, PaymentBase payData, Date nowPreset) throws Exception{
		
		checkAccessFor_ADMIN();
		
		Date now = nowPreset == null? new Date() : nowPreset;
		
		long userId = user.id;
		
		//clear caches anyway
		cache.removeCacheAsync(getPayReqCacheKey(userId));
		cache.removeCacheAsync(getConfirmPayCacheKey(userId));
		
		PaymentExt payment = new PaymentExt(payData);
		payment.id = universal.nextSeqFor(payments);
		payment.userId = userId;
		payment.payType = PaymentType.REPLENISHMENT;
		payment.updated = now;
		BigDecimal minActiveBalance = props.getBigDecimalVal(billing_minActiveBalance);
		
		//update db
		BigDecimal[] updatedBalance = {null};
		boolean[] unblocked = {false};
		
		doInSingleTxMode(()->{
			
			universal.update(new CreatePayment(payment));
			
			if(payment.paymentStatus == COMPLETED){
				
				BigDecimal curBalance = findBalance(universal, userId).balance;
				updatedBalance[0] = appendBalance(universal, userId, payment.amount);
				
				if(isNeedDeblockAccsState(curBalance, updatedBalance[0], minActiveBalance)) {
					unblocked[0] = true;
					universal.update(new UpdateUserAccsBlocked(userId, false));
					db.chats.updateOwnersAccsLastPay(userId, now);
				}
			}
		});
		
		//update balance cache if need
		if(updatedBalance[0] != null){
			cache.tryPutCache(getBalanceCacheKey(userId), updatedBalance[0].toString());
		}
		
		//update chat servers
		if(unblocked[0]){
			sendAccsBlocked(props, db, cache, userId, false);
		}
		
		sendPaymentConfimedEmailAsync(user.email);
		
		log.info("pay finished: userId="+userId
				+", login="+user.login
				+(updatedBalance[0] != null? ", newBalance="+updatedBalance[0] : "")
				+", req="+getReqInfoStr());
	}
	
	
	@Secured
	public List<PaymentExt> getPayments(int limit, int offset) throws Exception{
		
		long userId = findUserIdFromSecurityContext();
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			return getPayments(userId, limit, offset);
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	@Secured
	public List<PaymentExt> getPayments(long userId, int limit, int offset) throws Exception{
		
		checkAccessFor_ADMIN();
		
		return universal.select(new GetPaymentsByUserId(userId, limit, offset));
	}
	
	
	@Secured
	public void payBill(long userId, BigDecimal amount, Date created, PaymentType payType, String details) throws Exception{
		
		checkAccessFor_ADMIN();
		
		payBill(userId, amount, created, payType, details, true);
	}
	
	/**
	 * Negative amount
	 */
	@Secured
	public void payBill(
			long userId, 
			BigDecimal amount, 
			Date created, 
			PaymentType payType, 
			String details, 
			boolean updateCache) throws ConcurrentUpdateSqlException, Exception{
		
		checkAccessFor_ADMIN();
		
		long id = universal.nextSeqFor(payments);
		PaymentExt payment = PaymentExt.createSystemBill(id, userId, amount, created, payType, details);
		
		BigDecimal newVal = doPayment(userId, payment, updateCache);
		
		log.info("bill payed: userId="+userId
				+(newVal != null? ", newBalance="+newVal : "")
				+", req="+getReqInfoStr());
		
	}
	
	/**
	 * Positive amount. No unblocks if blocked
	 */
	@Secured
	public void addPay_Simple(
			long userId, 
			BigDecimal amount, 
			Date created, 
			PaymentType payType, 
			String details, 
			boolean updateCache) throws ConcurrentUpdateSqlException, Exception{
		
		checkAccessFor_ADMIN();
		
		amount = amount.abs();
		
		long id = universal.nextSeqFor(payments);
		PaymentExt payment = PaymentExt.createSystemPayment(id, userId, amount, created, payType, details);
		
		BigDecimal newVal = doPayment(userId, payment, updateCache);
		
		log.info("pay added: userId="+userId
				+(newVal != null? ", newBalance="+newVal : "")
				+", req="+getReqInfoStr());
		
	}
	
	private BigDecimal doPayment(
			long userId, 
			PaymentExt payment,  
			boolean updateCache) throws ConcurrentUpdateSqlException, Exception{
		
		//update db
		BigDecimal[] updatedBalance = {null};
		doInSingleTxMode(()->{
			
			universal.update(new CreatePayment(payment));
			
			updatedBalance[0] = appendBalance(universal, userId, payment.amount);
			
		});
		
		if(updateCache){
			cache.tryPutCache(getBalanceCacheKey(userId), updatedBalance[0].toString());
		}
		
		return updatedBalance[0];
		
	}
	
	
	public boolean addStartBonus(long userId) throws Exception{
		
		pushToSecurityContext_SYSTEM_USER();
		try {
		
			UserStartBonus curVal = universal.selectOne(new GetStartBonusByUserId(userId));
			if(isEmpty(curVal) || curVal.startBonusAdded) return false;
			
			BigDecimal bonusVal = props.getBigDecimalVal(promo_startBonus);
			if(ZERO.compareTo(bonusVal) >= 0) return false;
			
			Boolean[] valid = {true};
			
			doInSingleTxMode(()->{
				
				Integer result = universal.updateOne(new UpdateUserStartBonus(userId, false, true));
				if(result == 0) {
					valid[0] = false;
					return;
				}
				
				addPay_Simple(userId, bonusVal, new Date(), START_BONUS, null, false);
				
			});
			
			if( ! valid[0]) return false;
			
			//all done - update balance cache
			updateUserBalanceCache(userId, true);
			
			return true;
			
		}finally {
			popUserFromSecurityContext();
		}
		
		
	}
	
	
	
	
	
	
	
	private void loadBalancesToCache() throws Exception{
		log.info("load balances to cache...");
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			Map<String, Long> accOwners = chats.getAllAccOwners();
			Collection<Long> userIds = accOwners.values();
			
			List<UserBalance> balances = universal.select(new SelectUserBalancesByIds(userIds));
			for (UserBalance b : balances) {
				cache.tryPutCache(getBalanceCacheKey(b.userId), b.balance.toString());
			}
			
		}finally {
			popUserFromSecurityContext();
		}
		
		log.info("done");
	}
	
	
	
	private void sendPaymentConfimedEmailAsync(String email) {
		try {
			String subject = c.templates.fromTemplate("payment-confirmed-subject.ftl");
			String html = c.templates.fromTemplate("payment-confirmed-text.ftl");
			c.mails.sendAsync(new SendReq(email, subject, html));
		} catch (Exception e) {
			log.error("can't send email", e);
		}
	}
	
	private BillingProvider getProvider(String providerKey) {
		BillingProvider provider = providers.get(providerKey);
		validateForEmpty(provider, "provider");
		return provider;
	}
	
	
	private PayReqCacheData checkAndGetPayReq(String token){
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		PayReqCacheData reqData = tryParseJson(cache.tryGetVal(getPayReqCacheKey(userId)), PayReqCacheData.class);
		if( isEmpty(reqData)) throw new NoDataToConfirmPaymentException();
		if( ! reqData.token.equals(token)) throw new NoDataToConfirmPaymentException();
		return reqData;
	}
	
	static String getPayReqCacheKey(long userId){
		return "pay-req-"+userId;
	}
	
	static String getConfirmPayCacheKey(long userId){
		return "pay-confirm-"+userId;
	}
	
	public static class PayReqCacheData {
		
		public String token;
		public BigDecimal val;
		
		public PayReqCacheData() {
		}
		
		public PayReqCacheData(String token, BigDecimal val) {
			this.token = token;
			this.val = val;
		}
	}
	
	public static class PayConfirmCacheData {
		
		public String token;
		public BigDecimal val;
		public String userPayId;
		
		public PayConfirmCacheData() {
		}
		
		public PayConfirmCacheData(String token, BigDecimal val, String userPayId) {
			this.token = token;
			this.val = val;
			this.userPayId = userPayId;
		}
	}

}
