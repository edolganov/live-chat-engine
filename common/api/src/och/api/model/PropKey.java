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
package och.api.model;

import static och.util.Util.*;
import static och.util.sql.Dialect.*;

import java.math.BigDecimal;

import och.service.props.KeyWithDef;


public enum PropKey implements KeyWithDef {
	
	//SaaS mode or Tool mode
	toolMode(false),
	
	//admin
	admin_Emails,
	
	admin_emailNotify_AccCreated(true),
	
	
	//base app
	app_debug_createRemoteData(true),
	
	
	//front app
	frontApp_id,
	frontApp_cabinet_useHttpUrlsForChats(false),
	
	
	//front clusters
	frontServerUrls,

	
	//chat app
	chatApp_id,
	//chat debug
	chatApp_debug_failInitToken(false),
	
	
	//cur server env
	httpServerUrl,
	httpsServerUrl,
	webInf,
	
	//net props
	netProps_cache_files(
			"./net-props/db.properties"
			+ " ./net-props/cache.properties"
			+ " ./net-props/common.properties"),
	
	netProps_front_host("localhost"),
	netProps_front_port(12140),
	netProps_front_maxConns(30),
	netProps_front_secureKey(),
	netProps_front_files(
			"./net-props/db.properties"
			+ " ./net-props/cache.properties"
			+ " ./net-props/common.properties"
			+ " ./net-props/front.properties"
			+ " ./net-props/front-ban.properties"),
	
	netProps_chats_host("localhost"),
	netProps_chats_port(12145),
	netProps_chats_maxConns(30),
	netProps_chats_secureKey(),
	netProps_chats_files(
			"./net-props/common.properties"
			+ " ./net-props/chats.properties"),
	
	netPropsClient_waitConnect(true),
	netPropsClient_updateTime(1000*60*2),
	
	
	
	//remote api
	remote_encyptedKey,
	remote_connTimeout(6000),
	remote_readTimeout(6000),
	
	//db
	db_dialect(DB_DEFAULT),
	db_driver,
	db_url,
	db_user,
	db_psw,
	db_maxConnections(10),
	db_idleConnections(5),
	db_skipDbCreation(false),
	db_reinit(false),
	db_debug_LogSql(false),
	
	
	//cache
	cache_encyptedKey,
	cache_remote_host("localhost"),
	cache_remote_port(12160),
	cache_remote_maxConns(30),
	cache_remote_idleConns(5),
	cache_cleanTime(1000L*60*60),
	cache_plugins(
			"och.comp.paypal.standalone.PaypalPaymentsSynchService"
			+ ",och.comp.billing.standalone.BillingSyncService"
			+ ",och.comp.billing.standalone.HostMultiOwnersAlarmService"),
	
	
	//mail
	mail_fromMail("no-reply@host"),
	mail_username,
	mail_password,
	mail_smtp_host,
	mail_smtp_port,
	mail_smtp_auth(true),
	mail_smtp_starttls_enable(true),
	mail_errorWaitDelta(1000),
	mail_storeToDisc(true),
	mail_storeDir("../sended_mails"),
	mail_storeClearOld(true),
	mail_storeOldMaxDays(5),
	mail_trySendCount(3),
	mail_skipSslCertCheck(false),
	mail_debug(false),
	
	
	//templates
	templates_path,
	
	
	//captcha: see https://www.google.com/recaptcha/intro/index.html
	captcha_enabled(true),
	captcha_publicKey, 
	captcha_privateKey,
	
	
	//web sec
	remToken_maxCount(30),
	remToken_deleteCount(10),
	
	//tariffs
	tariffs_init_val(0),
	tariffs_init_delta(5),
	tariffs_init_count(21),
	tariffs_init_publicIds("2,5"),
	tariffs_default_tariff(2),
	tariffs_maxOperators("2-2,4-10,5-10"),
	tariffs_minChangeTariffBill(0),
	tariffs_maxChangedInDay(3),
	tariff_pausePrice(0),
	
	//chats
	chats_rootDir("../data/accounts"),
	chats_reloadDelay(1000L*60*2),
	chats_sessionLivetime(60*5), //sec - 60*5=5min
	chats_server_init_urls("http://127.0.0.1:10180 https://127.0.0.1:10543"),
	chats_init_accounts, //1 demo, 1 demo2 ("1" - ownerId, "demo" - chat uid)
	chats_init_tariff(1),
	chats_emailNotifications(true),
	
	chats_useActiveChatsLogs(true),
	chats_cleanActiveChatLogs(true),
	chats_cleanActiveChatLogs_Delay(1000L*60*60),
	chats_cleanActiveChatLogs_CloseDelta(1000L*60*60*3),
	
	chats_useStat(true),
	chats_stat_Delay(1000L*30),
	
	//chats limits
	chats_blockClientByIp,  //special: chats_blockClientByIp_127.0.0.1=true
	chats_blockByHost, //special: chats_blockByHost_ya.ru=true
	chats_maxAccsForUser(10), //for chat owners
	chats_maxSessionsByIP(30), 				//special: chats_maxSessionsByIP_127.0.0.1=400	
	chats_maxSingleMsgsPerTime(30),
	chats_maxMsgsPerChat(1000),
	chats_maxMsgSize(2000),
	chats_maxChatsFromIpPerDay(200), 		//special: chats_maxChatsFromIpPerDay_127.0.0.1=300
	chats_maxChatsForAccPerDay(3000),		//special: chats_maxChatsForAccPerDay_{accId}
	chats_maxFeedbacksFromIpPerDay(200), 	//special: chats_maxFeedbacksFromIpPerDay_127.0.0.1=300
	chats_maxFeedbacksForAccPerDay(3000),		//special: chats_maxFeedbacksForAccPerDay_{accId}
	
	chats_hosts_stat_use(true),
	chats_hosts_stat_FlushDelay(1000L*60*30),
	chats_hosts_stat_FlushDelta(1000L*60*30),
	chats_hosts_unimportant(), //special: chats_hosts_unimportant_127.0.0=1
	
	chats_hosts_multiOwnersAlarmUse(true),
	chats_hosts_multiOwnersAlarmDelay(1000L*60*4),
	chats_hosts_multiOwnersAlarmDelta(1000L*60*60*24),
	chats_hosts_multiOwnersAlarmVal(2),
	chats_hosts_multiOwners_DisableSendErrors(true),
	
	//users
	users_init_adminLogins("admin,root"),
	users_init_adminPsws("admin,root"),
	users_init_moderLogins("moderator,moder"),
	users_init_moderPsws("moderator,moder"),
	users_autoActivation(false),
	users_activationUrl("http://127.0.0.1:10280/system-api/user/activate"),
	users_expiredDays(3),
	users_expiredTime,
	users_banUserFlagLiveTime(1000*60*60),
	users_loginsWithoutCaptchaCount(3),
	users_waitChatSessionTokenLivetime(1000L*60*5),
	
	//payment
	payment_payReqTokenLivetime(1000*60*60),
	payment_payConfirmDataLivetime(1000*60*60),
	
	
	//billing
	billing_minActiveBalance(0),
	
	
	//billing sync
	billing_sync_debug_DisableSync(false),
	billing_sync_debug_DisableTimer(false),
	billing_sync_debug_DisableSendErrors(false),
	billing_sync_debug_CheckWorkTime(true),
	billing_sync_timerDelay(1000L*60*2),
	billing_sync_timerDelta(1000L*60*60),
	billing_sync_taskMonitorTimerDelay(1000L*60),
	billing_sync_taskMonitorTimerDelta(1000L*60),
	billing_sync_blockCheckTimerDelay(1000L*60*20),
	billing_sync_blockCheckTimerDelta(1000L*60*60*4),
	billing_sync_log(false),
	billing_sync_startSyncDay(1),
	billing_sync_startSyncTime("06:00"),
	billing_sync_endSyncDay(3),
	billing_sync_endSyncTime("23:59"),
	billing_sync_fillBlockedCacheOnStart(true),
	billing_sync_fillBlockedCacheOnStartDelay(1000L*60),
	billing_sync_lastSyncStore(true),
	billing_sync_lastSyncFile("./lastBillingSync.json"),
	
	
	//paypal
	paypal_enabled(false),
	paypal_key("paypal"),
	paypal_clientStub(false),
	paypal_configPath,
	paypal_accessTokenLiveTime(1000L*60*60*4),
	paypal_preConfirmUri("/payment/paypal/ipn"),
	paypal_successUri("/cabinet?successPayment"),
	paypal_failUri("/cabinet?failPayment"),

	//paypal sync
	paypal_sync_debug_DisableSync(false),
	paypal_sync_debug_DisableTimer(false),
	paypal_sync_debug_DisableSendErrors(false),
	paypal_sync_timerDelay(1000L*60),
	paypal_sync_timerDelta(1000L*60),
	paypal_sync_sendErrorsDelay(1000L*60*60),
	paypal_sync_sendErrorsDelta(1000L*60*60),
	paypal_sync_fastUpdatePeriod(1000L*60*10),
	paypal_sync_longUpdateDelta(1000L*60*60),
	paypal_sync_skipErrorTerms,
	paypal_sync_skipErrorTermsSep('#'),
	paypal_sync_log(false),
	
	//2checkout
	toCheckout_key("2checkout"),
	toCheckout_accId(""),
	toCheckout_secretWord(""),
	toCheckout_demoMode(false),
	toCheckout_redirectUrl_demo("https://sandbox.2checkout.com/checkout/purchase"),
	toCheckout_redirectUrl_prod("https://www.2checkout.com/checkout/purchase"),
	
	
	//promo
	promo_startBonus(5),
	
	
	//geo-ip
	geo_ip_dbPath("unknown"),
	geo_ip_dbInRAM(true),
	
	//google analytics
	ga_account(),
	ga_domain(),
	
	//i18n
	i18n_dirPath("./i18n"),
	
	
	stub, 
	;
	
	private final String defVal;
	
	private PropKey(){
		this(null);
	}
	
	private PropKey(Object defVal){
		this.defVal = defVal == null? null : defVal.toString();
	}
	
	@Override
	public String strDefVal(){
		return defVal;
	}
	
	@Override
	public Boolean boolDefVal(){
		return tryParseBool(defVal, null);
	}
	
	@Override
	public Integer intDefVal(){
		return tryParseInt(defVal, null);
	}
	
	@Override
	public Long longDefVal(){
		return tryParseLong(defVal, null);
	}
	
	@Override
	public Double doubleDefVal(){
		return tryParseDouble(defVal, null);
	}
	
	@Override
	public BigDecimal bigDecimalDefVal(){
		return tryParseBigDecimal(defVal, null);
	}

}
