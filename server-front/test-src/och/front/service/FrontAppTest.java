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
import static och.api.model.PropKey.*;
import static och.api.model.RemoteCache.*;
import static och.api.model.billing.PaymentBase.*;
import static och.api.model.billing.PaymentProvider.*;
import static och.api.model.billing.PaymentStatus.*;
import static och.api.model.billing.PaymentType.*;
import static och.api.model.chat.account.ChatAccountPrivileges.*;
import static och.api.model.chat.account.PrivilegeType.*;
import static och.api.model.tariff.Tariff.*;
import static och.api.model.tariff.TariffMath.*;
import static och.api.model.user.SecurityContext.*;
import static och.api.model.user.UserRole.*;
import static och.api.model.user.UserStatus.*;
import static och.comp.db.main.table.MainTables.*;
import static och.comp.ops.BillingOps.*;
import static och.comp.paypal.PaypalClientStub.*;
import static och.front.service.SecurityService.*;
import static och.util.DateUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.sql.Dialect.*;
import static och.util.sql.SingleTx.*;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.http.Cookie;

import och.api.exception.InvalidInputException;
import och.api.exception.ValidationException;
import och.api.exception.billing.NoDataToConfirmPaymentException;
import och.api.exception.chat.AccountsLimitException;
import och.api.exception.chat.ChatAccountBlockedException;
import och.api.exception.chat.ChatAccountNotPausedException;
import och.api.exception.chat.ChatAccountPausedException;
import och.api.exception.chat.HostBlockedException;
import och.api.exception.chat.NoAvailableServerException;
import och.api.exception.chat.NoChatAccountException;
import och.api.exception.tariff.ChangeTariffLimitException;
import och.api.exception.tariff.NotPublicTariffException;
import och.api.exception.tariff.OperatorsLimitException;
import och.api.exception.tariff.TariffNotFoundException;
import och.api.exception.tariff.UpdateTariffOperatorsLimitException;
import och.api.exception.user.AccessDeniedException;
import och.api.exception.user.BannedUserException;
import och.api.exception.user.DuplicateUserDataException;
import och.api.exception.user.InvalidLoginDataForUpdateException;
import och.api.exception.user.InvalidUserActivationCodeException;
import och.api.exception.user.NotActivatedUserException;
import och.api.exception.user.UnmodifiableAdminUserException;
import och.api.exception.user.UserActivationExpiredException;
import och.api.exception.user.UserNotFoundException;
import och.api.exception.user.UserSessionAlreadyExistsException;
import och.api.model.PropKey;
import och.api.model.billing.AdminSyncResp;
import och.api.model.billing.PayData;
import och.api.model.billing.PaymentBase;
import och.api.model.billing.PaymentExt;
import och.api.model.billing.PaymentProvider;
import och.api.model.billing.PaymentStatus;
import och.api.model.billing.PaymentType;
import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.chat.config.Key;
import och.api.model.chat.host.ClientHost;
import och.api.model.remtoken.ClientRemToken;
import och.api.model.remtoken.RemToken;
import och.api.model.server.ServerRow;
import och.api.model.tariff.Tariff;
import och.api.model.user.LoginUserReq;
import och.api.model.user.UpdateUserReq;
import och.api.model.user.User;
import och.api.model.user.UserExt;
import och.api.model.user.UserRole;
import och.comp.billing.standalone.BillingSyncService;
import och.comp.billing.standalone.HostMultiOwnersAlarmService;
import och.comp.cache.Cache;
import och.comp.cache.client.CacheClient;
import och.comp.cache.impl.CacheImpl;
import och.comp.cache.server.CacheServerContext;
import och.comp.cache.server.CacheSever;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.MainDb;
import och.comp.db.main.table.MainTables;
import och.comp.db.main.table._f.IsFull;
import och.comp.db.main.table._f.TariffLastPay;
import och.comp.db.main.table._f.TariffStart;
import och.comp.db.main.table.billing.CreatePayment;
import och.comp.db.main.table.billing.GetPaymentByExternalId;
import och.comp.db.main.table.billing.GetPaymentById;
import och.comp.db.main.table.billing.GetStartBonusByUserId;
import och.comp.db.main.table.billing.SelectUserBalanceById;
import och.comp.db.main.table.billing.UpdateUserAccsBlocked;
import och.comp.db.main.table.billing.UpdateUserBalance;
import och.comp.db.main.table.billing.UpdateUserBalanceUnsafe;
import och.comp.db.main.table.chat.GetAllChatAccounts;
import och.comp.db.main.table.chat.GetChatAccount;
import och.comp.db.main.table.chat.UpdateAllChatAccounts;
import och.comp.db.main.table.chat.UpdateChatAccountByUid;
import och.comp.db.main.table.chat.host.CreateClientHost;
import och.comp.db.main.table.chat.host.CreateClientHostAccOwner;
import och.comp.db.main.table.chat.host.DeleteClientHostAccOwner;
import och.comp.db.main.table.chat.host.UpdateClientHostImportant;
import och.comp.db.main.table.chat.privilege.GetChatAccountPrivileges;
import och.comp.db.main.table.remtoken.SelectRemTokensByUser;
import och.comp.db.main.table.server.GetAllServers;
import och.comp.db.main.table.server.UpdateServerById;
import och.comp.db.main.table.tariff.GetAllTariffs;
import och.comp.db.main.table.user.SelectUserById;
import och.comp.mail.stub.MailServiceStub;
import och.comp.mail.stub.SenderStub;
import och.comp.ops.BillingOps;
import och.comp.paypal.PaypalClientStub;
import och.comp.paypal.standalone.PaypalPaymentsSynchService;
import och.email.parser.ActivationEmailParser;
import och.email.parser.RestorePswEmailParser;
import och.front.service.ChatService.UpdateTariffOps;
import och.front.service.model.UserAccInfo;
import och.service.props.WriteProps;
import och.service.props.impl.MapProps;
import och.util.DateUtil;
import och.util.StringUtil;
import och.util.concurrent.ExecutorsUtil;
import och.util.model.Pair;
import och.util.servlet.WebUtil;
import och.util.sql.ConcurrentUpdateSqlException;

import org.apache.commons.logging.Log;
import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Test;

import test.BaseTest;
import test.TestException;
import web.MockHttpServletRequest;
import web.MockHttpServletResponse;
import web.MockServletContext;

public class FrontAppTest extends BaseTest {
	
	public static final String TEMPLATES_PATH = "./server-front/web/WEB-INF/templates";
	
	private static Log log = getLog(FrontAppTest.class);
	
	SenderStub mailSender = new SenderStub();
	int cachePort = 12159;
	MockServletContext servletContext = new MockServletContext();
	MockHttpServletRequest req = new MockHttpServletRequest();
	MockHttpServletResponse resp = new MockHttpServletResponse();
	ArrayList<Future<?>> asyncFutures = new ArrayList<>();
	ArrayList<Future<?>> scheduleFutures = new ArrayList<>();
	PaypalClientStub paypalClient = new PaypalClientStub();
	
	
	MapProps props;
	FrontApp app;
	FrontAppContext c;
	MainDb db;
	UniversalQueries universal;
	ChatService chats;
	SecurityService security;
	CacheSever cacheSever;
	UserService users;
	BillingService billing;
	CacheClient cacheClient;
	
	
	
	long userId1;
	String login1 = "user";
	String mail1 = "user@user.ru";
	String psw1 = "123";
	
	long userId2;
	String login2 = "user2";
	String mail2 = "user2@user.ru";
	String psw2 = StringUtil.createStr('я', 200);
	
	long userId3;
	String login3 = "user3";
	String mail3 = "user3@user.ru";
	String psw3 = "123";
	
	long userId4;
	String login4 = "user4";
	String mail4 = "user4@user.ru";
	String psw4 = "123";
	
	
	
	String cacheSecureKey = "some132";
	String server1HttpUrl = "http://test1";
	String server1HttpsUrl = "https://test1";
	String server2HttpUrl = "http://test2";
	String server2HttpsUrl = "https://test2";
	String server3HttpUrl = "http://test3";
	String server3HttpsUrl = "https://test3";
	
	long serverId1 = 1;
	long serverId2 = 2;
	long serverId3;
	
	@Before
	public void before() throws Exception{
		
		props = baseFrontProps(TEST_DIR);
		props.putVal(cache_remote_port, cachePort);
		props.putVal(db_reinit, true);
		props.putVal(chats_server_init_urls, server1HttpUrl+" "+server1HttpsUrl+","+server2HttpUrl+" "+server2HttpsUrl);
		props.putVal(app_debug_createRemoteData, false);
		props.putVal(billing_sync_debug_DisableTimer, true);
		props.putVal(mail_storeToDisc, false);
		props.putVal(billing_sync_fillBlockedCacheOnStartDelay, 0);
		props.putVal(billing_sync_lastSyncStore, false);
		props.putVal(db_debug_LogSql, true);
		props.putVal(cache_encyptedKey, cacheSecureKey);
		
		cacheSever = new CacheSever(cachePort, 2, 10000, cacheSecureKey, props);
		cacheSever.runAsync();
		
		
		app = FrontApp.create(
				props, 
				servletContext, 
				new MailServiceStub(mailSender, props),
				paypalClient);
		c = app.c;
		db = c.db;
		universal = db.universal;
		chats = app.chats;
		users = app.users;
		security = app.security;
		cacheClient = c.cache;
		billing = app.billing;
		
		c.async.addListener((future) -> asyncFutures.add(future));
		c.async.addScheduleListener((future) -> scheduleFutures.add(future));
		cacheClient.addListener((future) -> asyncFutures.add(future));
		
	}
	
	public static MapProps baseFrontProps(File testDir) throws Exception{
		
		MapProps props = new MapProps();
		
		putDbProps(props, testDir);
		
		props.putVal(admin_Emails, "admin@host.com");
		props.putVal(users_activationUrl, "http://test");
		props.putVal(cache_remote_maxConns, 2);
		props.putVal(cache_remote_idleConns, 2);
		props.putVal(templates_path, TEMPLATES_PATH);
		props.putVal(captcha_publicKey, "test");
		props.putVal(captcha_privateKey, "test");
		
		props.putVal("", "");
		return props;
	}
	
	
	
	public static void putDbProps(WriteProps props, File testDir) throws Exception{
		
		boolean usePostgre = false;
		if(usePostgre){
			props.putVal(db_dialect, DB_POSTGRESQL);
			props.putVal(db_driver, "org.postgresql.Driver");
			props.putVal(db_url, "jdbc:postgresql://localhost/och-test");
			props.putVal(db_user, "");
			props.putVal(db_psw, "");
			props.putVal(db_maxConnections, 10);
			props.putVal(db_idleConnections, 5);			
		}
		//h2
		else {
			
			int serverPort = 9400;
			
			runH2ServerIfNeed(testDir, serverPort);
			
			Class.forName("org.h2.Driver");
			
			int logMode = 1; //1-err,2-info,3-debug
			//String url = "jdbc:h2:mem:db-"+randomSimpleId() + ";DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT="+logMode;
			//String url = "jdbc:h2:"+new File(testDir, "test.db").getAbsolutePath();
			String url = "jdbc:h2:tcp://localhost:"+serverPort+"/~/test";
			
			props.putVal(db_dialect, DB_H2);
			props.putVal(db_driver, "org.h2.Driver");
			props.putVal(db_url, url);
			props.putVal(db_user, "sa");
			props.putVal(db_psw, "");
			props.putVal(db_maxConnections, 10);
			props.putVal(db_idleConnections, 5);
		}
	}
	
	private static void runH2ServerIfNeed(File testDir, int port) {
		try {
			
			Server.createTcpServer(
					"-tcpPort", ""+port,
					"-tcpAllowOthers").start();
			
		}catch(Exception e){
			log.error("can't runH2Server: "+e);
		}
	}

	@Test
	public void test_all() throws Exception {
		
		//DB
		test_init_db();
		test_db_getServers();
		
		//USER
		test_user_create();
		test_user_get();
		test_user_duplicates();
		test_user_invalid_inputs();
		test_user_unexists();
		test_user_activate();
		test_user_ban();
		test_user_unban();
		test_user_activate_from_email_text();
		test_user_invalid_activation_states();
		test_user_send_activation_email_again();
		test_user_change_psw();
		test_user_update();
		test_user_psw_secure();
		test_ban_user_runtime_with_exists_session();
		
		
		//ROLE
		test_set_get_roles();
		
		
		//SECURITY
		test_create_restore_remMe();
		test_logout_remMe();
		test_create_without_remMe();
		test_remMeToken_secure();
		test_remMeToken_maxCount();
		test_invalidLoginsCount();
		
		
		//chats
		test_chats_createServerAndAcc_BySystem();
		test_chats_createAccs_byUser();
		test_chats_renameAccs_BySystem_ByUser();
		test_chats_changePrivsForUser();
		test_chats_operatorsLimits();
		test_chats_accsLimits();
		test_chats_accPauseLimits();
		test_chats_nicknames();
		
		
		//billing
		test_billing_atomicChangeBalance();
		test_billing_put_balances_to_cache();
		test_billing_get_balance_from_cache_or_db();
		test_billing_paypal_paySteps();
		test_billing_paySync();
		test_billing_payBill();
		test_billing_monthBill();
		test_billing_sendErrorsWithEmails();
		test_billing_callFromCacheFlag();
		test_billing_blockUserAccs();
		test_billing_promoStartBonus();
		test_billing_2checkout_paySteps();
		
		
		//tariffs
		test_tariff_defaultTariffs();
		test_tariff_update_invalidInputs_limitations();
		test_tariff_update_maxInDayLimit();
		test_tariff_update();
		
		//tariffs, billing concurrents
		test_tariff_billing_concurrent();
		
		
		//client hosts
		test_clientHosts_Crud();
		test_clientHosts_AlarmDetect();
		test_clientHosts_Block();
	}
	
	
	private void test_billing_promoStartBonus() throws Exception {
		
		long userId = userId1;
		BigDecimal initBalance = null;
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			initBalance = billing.getUserBalance(userId);
		}finally {
			popUserFromSecurityContext();
		}
		
		assertFalse(universal.selectOne(new GetStartBonusByUserId(userId)).startBonusAdded);
		
		//first call
		{
			assertTrue(billing.addStartBonus(userId));
			assertTrue(universal.selectOne(new GetStartBonusByUserId(userId)).startBonusAdded);
		}
		
		//second call
		{
			assertFalse(billing.addStartBonus(userId));
			assertTrue(universal.selectOne(new GetStartBonusByUserId(userId)).startBonusAdded);
		}
		
		lastFrom(asyncFutures).get();
		
		//check balance
		pushToSecurityContext_SYSTEM_USER();
		try {
			BigDecimal curBalance = billing.getUserBalance(userId);
			int deltaVal = promo_startBonus.bigDecimalDefVal().intValue();
			assertEquals(deltaVal, curBalance.subtract(initBalance).intValue());
			
			BigDecimal cacheVal = tryParseBigDecimal(cacheClient.tryGetVal(getBalanceCacheKey(userId)), null) ;
			assertEquals(deltaVal, cacheVal.subtract(initBalance).intValue());
			
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	
	
	private void test_clientHosts_Block() {
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader("Referer", "http://ya.ru");
		
		chats.checkAndLogReferer(req, null, "demo");
		
		//by host
		{
			props.putVal(chats_blockByHost+"_ya.ru", true);
			try {
				chats.checkAndLogReferer(req, null, "demo");
				fail_exception_expected();
			}catch(HostBlockedException e){
				//ok
			}
			
			props.putVal(chats_blockByHost+"_ya.ru", false);
			chats.checkAndLogReferer(req, null, "demo");
		}
		
		
		
		//by host and uid
		{
			props.putVal(chats_blockByHost+"_ya.ru_demo", true);
			try {
				chats.checkAndLogReferer(req, null, "demo");
				fail_exception_expected();
			}catch(HostBlockedException e){
				//ok
			}
			
			props.putVal(chats_blockByHost+"_ya.ru_demo", false);
			chats.checkAndLogReferer(req, null, "demo");
		}
		
		
		//by host and accs owner
		{
			props.putVal(chats_blockByHost+"_ya.ru_owner_100", true);
			try {
				chats.checkAndLogReferer(req, null, "demo");
				fail_exception_expected();
			}catch(HostBlockedException e){
				//ok
			}
			
			props.putVal(chats_blockByHost+"_ya.ru_owner_100", false);
			chats.checkAndLogReferer(req, null, "demo");
		}
		
		
		
	}

	private void test_clientHosts_AlarmDetect() throws Exception {
		
		boolean important = true;
		long userId = 100;
		long userId2 = 101;
		
		String name1 = "1.ru";
		String refer1 = "http://"+name1;
		String refer2 = "http://2.ru";
		
		//create stat
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			universal.update(new DeleteClientHostAccOwner(userId));
			universal.update(new DeleteClientHostAccOwner(userId2));
			assertEquals(0, db.clientHosts.getHostsWithOwners(important, 1).size());
			
			String acc1 = chats.getOwnerAccIds(userId).get(0);
			String acc2 = chats.getOwnerAccIds(userId2).get(0);
			
			assertFalse(chats.saveClientsHostsStat());
			chats.checkAndLogReferer(req, refer1, acc1).get();
			chats.checkAndLogReferer(req, refer1, acc2).get();
			chats.checkAndLogReferer(req, refer2, acc1).get();
			assertTrue(chats.saveClientsHostsStat());
			
		}finally {
			popUserFromSecurityContext();
		}
		
		props.putVal(chats_hosts_multiOwners_DisableSendErrors, false);
		MailServiceStub mailService = new MailServiceStub(mailSender, props);
		mailSender.tasks.clear();
		
		HostMultiOwnersAlarmService alarm = new HostMultiOwnersAlarmService();
		alarm.setCacheServerContext(new CacheServerContext(props, cacheSever, db, mailService));
		alarm.init();
		
		//есть мульти владельцы - есть письмо
		{
			assertEquals(2, db.clientHosts.getHostsWithOwners(important, 1).size());
			assertEquals(1, db.clientHosts.getHostsWithOwners(important, 2).size());
			
			alarm.doCheckWork();
			assertEquals(1, mailSender.tasks.size());			
		}
		
		//поменяли флаг критичности - нет письма
		{
			universal.update(new UpdateClientHostImportant(name1, false));
			mailSender.tasks.clear();
			
			assertEquals(0, db.clientHosts.getHostsWithOwners(important, 2).size());
			assertEquals(1, db.clientHosts.getHostsWithOwners(important, 1).size());
			
			alarm.doCheckWork();
			assertEquals(0, mailSender.tasks.size());			
		}
		
		
		//вернули флаг критичности - есть письмо
		{
			universal.update(new UpdateClientHostImportant(name1, true));
			mailSender.tasks.clear();
			
			assertEquals(1, db.clientHosts.getHostsWithOwners(important, 2).size());
			assertEquals(2, db.clientHosts.getHostsWithOwners(important, 1).size());
			
			alarm.doCheckWork();
			assertEquals(1, mailSender.tasks.size());			
		}
		
		
		
		//нет мульти владельцев - нет письма
		{
			universal.update(new DeleteClientHostAccOwner(userId));
			mailSender.tasks.clear();
			
			assertEquals(0, db.clientHosts.getHostsWithOwners(important, 2).size());
			assertEquals(1, db.clientHosts.getHostsWithOwners(important, 1).size());
			
			alarm.doCheckWork();
			assertEquals(0, mailSender.tasks.size());			
		}
		
	}

	private void test_clientHosts_Crud() throws Exception {
		
		long hostId1 = 0;
		long hostId2 = 0;
		String name1 = "ya.ru";
		String name2 = "ya.ru2";
		boolean important = true;
		long userId = 100;
		long userId2 = 101;
		
		
		//create host
		{
			hostId1 = universal.nextSeqFor(MainTables.client_hosts);
			universal.update(new CreateClientHost(new ClientHost(hostId1, name1, important)));
			
			List<ClientHost> list = db.clientHosts.getHostsWithOwners(important, 0);
			assertEquals(1, list.size());
			assertEquals(name1, list.get(0).name);
			
			assertEquals(0, db.clientHosts.getHostsWithOwners( ! important, 0).size());
			assertEquals(0, db.clientHosts.getHostsWithOwners( important, 1).size());
			
			hostId2 = universal.nextSeqFor(MainTables.client_hosts);
			universal.update(new CreateClientHost(new ClientHost(hostId2, name2, important)));
			assertEquals(2, db.clientHosts.getHostsWithOwners( important, 0).size());
		}
		
		//create link
		{
			universal.update(new CreateClientHostAccOwner(hostId1, userId));
			
			try {
				universal.update(new CreateClientHostAccOwner(hostId1, userId));
				fail_exception_expected();
			}catch(SQLException e){
				//ok
			}
			
			universal.update(new CreateClientHostAccOwner(hostId1, userId2));
			
			List<ClientHost> hosts = db.clientHosts.getHostsWithOwners( important, 0);
			assertEquals(2, hosts.size());
			
			ClientHost host = hosts.get(0);
			assertNotNull(host.owners);
			assertEquals(2, host.owners.size());
			assertEquals(userId, host.owners.get(0).id);
			assertEquals(userId2, host.owners.get(1).id);
			
			assertEquals(1, db.clientHosts.getHostsWithOwners(important, 1).size());
			assertEquals(1, db.clientHosts.getHostsWithOwners(important, 2).size());
		}
		
		
		//create stat
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			universal.update(new DeleteClientHostAccOwner(userId));
			universal.update(new DeleteClientHostAccOwner(userId2));
			assertEquals(0, db.clientHosts.getHostsWithOwners(important, 1).size());
			
			String acc1 = chats.getOwnerAccIds(userId).get(0);
			String acc2 = chats.getOwnerAccIds(userId2).get(0);
			
			assertFalse(chats.saveClientsHostsStat());
			chats.checkAndLogReferer(req, "http://1.ru", acc1).get();
			chats.checkAndLogReferer(req, "http://1.ru", acc2).get();
			chats.checkAndLogReferer(req, "http://2.ru", acc1).get();
			assertTrue(chats.saveClientsHostsStat());
			
			assertEquals(1, db.clientHosts.getHostsWithOwners(important, 2).size());
			assertEquals(2, db.clientHosts.getHostsWithOwners(important, 1).size());
			
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	
	private void test_chats_nicknames() throws Exception {
		
		long ownerId = 100;
		String accUid = "demo";
		String nick = "some test nick";
		
		pushToSecurityContext_SYSTEM_USER(()->{
			
			
			ChatAccount acc = chats.getAccByUid(accUid, false);
			assertEquals(null, db.universal.selectOne(new GetChatAccountPrivileges(acc.id, ownerId)).nickname);
			
			//set
			chats.setNickname(accUid, ownerId, nick);
			
			//check model
			Map<Long, UserAccInfo> map = chats.getAccOperators(accUid);
			assertTrue(map.size() > 0);
			UserAccInfo info = map.get(ownerId);
			assertNotNull(info);
			assertEquals(nick, info.nickname);
			
			//check db
			assertEquals(nick, db.universal.selectOne(new GetChatAccountPrivileges(acc.id, ownerId)).nickname);
			
			
			//check to long
			try {
				chats.setNickname(accUid, ownerId, randomStr(MAX_NICKNAME_SIZE+1));
				fail_exception_expected();
			}catch(ValidationException e){
				//ok
			}
			
			pushToSecurityContext(new User(userId1));
			try {
				
				//можно редактировать свой никнейм
				chats.setNickname(accUid, nick);
				
				//но чужой можно только для владельца или модера
				chats.setNickname(accUid, ownerId, nick);
				fail_exception_expected();
			}catch(AccessDeniedException e){
				//ok
			}finally {
				popUserFromSecurityContext();
			}
			
		});
		
	}
	
	
	
	private void test_chats_accPauseLimits() throws Exception {
		
		String userLogin = "u_PauseLimits";
		String userMail = userLogin+"@dd.dd";
		long userId = -1;
		int maxChangesInDay = props.getIntVal(tariffs_maxChangedInDay);
		
		//создаем юзера
		pushToSecurityContext_SYSTEM_USER();
		try {
			userId = users.createUser(new User(userLogin, userMail), "123", false);
		}finally {
			popUserFromSecurityContext();
		}
		
		
		pushToSecurityContext(new User(userId));
		try {
			
			String uid = chats.createAccByUser("pauseLimits_1");
			
			long initTariff	= chats.getAccsForOperator(userId).get(0).tariffId;
			
			//нельзя убирать с паузы просто так
			try {
				chats.unpauseAccByUser(uid);
				fail_exception_expected();
			}catch(ChatAccountNotPausedException e){
				//ok
			}
			
			//нельзя проставлять паузы через метод смены тарифа
			try{
				chats.updateAccTariffByUser(uid, PAUSE_TARIFF_ID);
				fail_exception_expected();
			}catch(InvalidInputException e){
				//ok
			}
			
			
			//паузим макс количество раз
			for (int i = 0; i < maxChangesInDay; i++) {
				
				chats.pauseAccByUser(uid);
				
				assertEquals(PAUSE_TARIFF_ID, chats.getAccsForOperator(userId).get(0).tariffId);
				
				chats.unpauseAccByUser(uid);
				
				assertEquals(initTariff, chats.getAccsForOperator(userId).get(0).tariffId);
			}
			
			//больше нельзя
			try {
				chats.pauseAccByUser(uid);
				fail_exception_expected();
			}catch(ChangeTariffLimitException e){
				//ok
			}
			assertEquals(initTariff, chats.getAccsForOperator(userId).get(0).tariffId);
			
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	private void test_chats_accsLimits() throws Exception {
		
		String userLogin = "u_accsLimits";
		String userMail = userLogin+"@dd.dd";
		long userId = -1;
		int defaultMaxAccs = props.getIntVal(chats_maxAccsForUser);
			
		//создаем юзера
		pushToSecurityContext_SYSTEM_USER();
		try {
			userId = users.createUser(new User(userLogin, userMail), "123", false);
		}finally {
			popUserFromSecurityContext();
		}
	
		pushToSecurityContext(new User(userId));
		try {
			
			//создаем макс кол-во чатов
			{
				for (int i = 0; i < defaultMaxAccs; i++) {
					chats.createAccByUser("some");
				}
			}
			
			//след.акк будет вызывать ошибку
			try {
				chats.createAccByUser("some");
				fail_exception_expected();
			}catch(AccountsLimitException e){
				//ok
			}
			
			//увеличиваем макс для конкретного юзера
			props.putVal(chats_maxAccsForUser+"-"+userId, defaultMaxAccs+1);
			chats.createAccByUser("some");
			try {
				chats.createAccByUser("some");
				fail_exception_expected();
			}catch(AccountsLimitException e){
				//ok
			}
			
			
		}finally {
			popUserFromSecurityContext();
		}
		
		
	}
	
	
	
	
	private void test_billing_blockUserAccs() throws Exception {
		
		MailServiceStub mailService = new MailServiceStub(mailSender, props);
		
		//Отключаем все другие акки от апдейта
		Date longFuture = parseStandartDateTime("02.09.2040 3:00:00");
		universal.update(new UpdateAllChatAccounts(new TariffStart(longFuture), new TariffLastPay(longFuture)));
		
		
		MapProps paypalProps = new MapProps();
		paypalProps.putVal(paypal_sync_debug_DisableTimer, true);
		
		PaypalPaymentsSynchService paySync = new PaypalPaymentsSynchService();
		paySync.setCacheServerContext(new CacheServerContext(paypalProps, cacheSever, db, mailService));
		paySync.setClient(paypalClient);
		paySync.init();
		
		BillingSyncService billingSync = new BillingSyncService();
		billingSync.setCacheServerContext(new CacheServerContext(props, cacheSever, db, mailService));
		billingSync.init();
		
		
		ArrayList<Pair<Long, Boolean>> blockReqs = new ArrayList<>();
		BillingOps.SEND_ACCS_BLOCKED_LISTENER = (ownerId, val) -> blockReqs.add(new Pair<Long, Boolean>(ownerId, val));
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			int tariffId = 2;
			long userId = userId4;
			String accUid = "billing_blockUserAccs";
			
			List<String> oldAccs = db.chats.getOwnerAccs(userId);
			assertTrue(oldAccs.size() > 0);
			

			
			//create acc
			chats.createAcc(serverId1, accUid, userId, "test_monthBill", tariffId);
			chats.setOperatorForAcc(accUid, userId);
			assertEquals(1, chats.getAccOperators(accUid).size());
			

			
			//ушли в минус - заблокированы
			{
				correctBalance(userId, new BigDecimal(4.99d));
				assertEquals("4.99", billing.getUserBalance(userId).toString());
				
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-5.00";
				assertFalse(findBalance(universal, userId).accsBlocked);
				
				Date pastPay = parseStandartDateTime("01.08.2014 00:00:00");
				Date now = parseStandartDateTime("02.09.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, billingSync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
				
				//заблокированы
				assertEquals("-0.01", billing.getUserBalance(userId).toString());
				assertTrue(findBalance(universal, userId).accsBlocked);
				
				//расчет тарифов обновился
				assertEquals(parseStandartDateTime("01.09.2014 00:00:00"), universal.selectOne(new GetChatAccount(accUid)).tariffLastPay);
				
				//старые тарифы не обновили дату оплаты, т.к. она у них позже текущей
				for(String uid : oldAccs) assertEquals(longFuture, universal.selectOne(new GetChatAccount(uid)).tariffLastPay);
				

				//послан запрос на блокировку
				assertEquals(1, blockReqs.size()); 
				assertTrue(blockReqs.get(0).second);
				blockReqs.clear();
				
				
				//проверка ограничений
				pushToSecurityContext(new User(userId));
				try {
					
					
					//не можем менять тарифы юзером
					try {
						chats.updateAccTariffByUser(accUid, tariffId);
						fail_exception_expected();
					}
					catch(ChatAccountBlockedException e){
						//ok
					}
					
					
					//не можем создавать новые акки
					try {
						chats.createAccByUser("some");
						fail_exception_expected();
					}
					catch(ChatAccountBlockedException e){
						//ok
					}
					
					//не можем паузить
					try {
						chats.pauseAccByUser(accUid);
						fail_exception_expected();
					}
					catch(ChatAccountBlockedException e){
						//ok
					}
					
					
					//не можем анпаузить
					try {
						chats.unpauseAccByUser(accUid);
						fail_exception_expected();
					}
					catch(ChatAccountBlockedException e){
						//ok
					}
					
				}finally {
					popUserFromSecurityContext();
				}
				

				
				//в кеше есть флаги блока для каждого акка
				for(String uid : db.chats.getOwnerAccs(userId)){
					assertEquals("accUid="+uid, true, isAccBlockedFromCache(cacheClient, uid));
				}
				
				
				//если заново стартанем синк сервер, то кеш будет заполнен
				{
					Cache newCache = new CacheImpl(0);
					BillingSyncService otherBillingSync = new BillingSyncService();
					otherBillingSync.setCacheServerContext(new CacheServerContext(props, newCache, db, mailService));
					otherBillingSync.init();
					for(String uid : db.chats.getOwnerAccs(userId)){
						assertEquals(true, isAccBlockedFromCache(newCache, uid));
					}
				}
				
				//так же доступны данные через публичное апи чатов
				pushToSecurityContext(new User(userId));
				try {
					assertTrue(chats.getBlockedAccs().size() > 0);
					for(ChatAccount acc : chats.getAccsForOperator(userId)){
						assertEquals(true, acc.blocked);
					}
				}finally {
					popUserFromSecurityContext();
				}
				
			}
			
			
			
			//прошел месяц -- все еще заблочены -- расчетов по тарифу нет
			{
				assertTrue(findBalance(universal, userId).accsBlocked);
				BigDecimal initBalance = billing.getUserBalance(userId);
				
				Date now = parseStandartDateTime("01.10.2014 3:00:00");
				assertEquals(1, billingSync.doSyncWork(false, now));
				assertEquals("0.00", getDeltaVal(userId, initBalance));
				assertEquals("-0.01", billing.getUserBalance(userId).toString());
				
				//но расчет тарифов обновился
				assertTrue(findBalance(universal, userId).accsBlocked);
				assertEquals(parseStandartDateTime("01.10.2014 00:00:00"), universal.selectOne(new GetChatAccount(accUid)).tariffLastPay);
				
				//старые тарифы не обновили дату оплаты, т.к. она у них позже текущей
				for(String uid : oldAccs) assertEquals(longFuture, universal.selectOne(new GetChatAccount(uid)).tariffLastPay);
				
				
				//не было запроса на блокировку
				assertEquals(0, blockReqs.size()); 
				
				
				//в кеше есть флаги блока для каждого акка
				for(String uid : db.chats.getOwnerAccs(userId)){
					assertEquals(true, BillingOps.isAccBlockedFromCache(cacheClient, uid));
				}
			}
			
			
			//пользователь закинул деньги спустя неделю в ноль и его разблокировали
			{
				Date now = parseStandartDateTime("12.10.2014 15:45:00");
				pushToSecurityContext(new User(userId));
				try {
					paypalClient.payAmount = new BigDecimal("0.01");
					billing.sendPayReq(paypal_key.strDefVal(), paypalClient.payAmount);
					billing.paypal_preparePayConfirm(randomSimpleId(), STUB_TOKEN);
					billing.paypal_finishPayment(now);
				} finally {
					popUserFromSecurityContext();
				}
				assertEquals("0.00", billing.getUserBalance(userId).toString());
				assertFalse(findBalance(universal, userId).accsBlocked);
				assertEquals(now, universal.selectOne(new GetChatAccount(accUid)).tariffLastPay);
				
				//старые тарифы не обновили дату оплаты, т.к. она у них позже текущей
				for(String uid : oldAccs) assertEquals(longFuture, universal.selectOne(new GetChatAccount(uid)).tariffLastPay);
				
				
				//послан запрос на разблокировку
				assertEquals(1, blockReqs.size()); 
				assertFalse(blockReqs.get(0).second);
				blockReqs.clear();
				
				
				//можем менять тарифы юзером
				pushToSecurityContext(new User(userId));
				try {
					chats.updateAccTariffByUser(accUid, tariffId);
				}finally {
					popUserFromSecurityContext();
				}
				
				//в кеше нет флагов блока для каждого акка
				for(String uid : db.chats.getOwnerAccs(userId)){
					assertEquals(false, BillingOps.isAccBlockedFromCache(cacheClient, uid));
				}
				
				
				//если заново стартанем синк сервер, то кеш будет верным
				{
					Cache newCache = new CacheImpl(0);
					BillingSyncService otherBillingSync = new BillingSyncService();
					otherBillingSync.setCacheServerContext(new CacheServerContext(props, newCache, db, mailService));
					otherBillingSync.init();
					for(String uid : db.chats.getOwnerAccs(userId)){
						assertEquals(false, isAccBlockedFromCache(newCache, uid));
					}
				}
				
				
				//так же доступны данные через публичное апи чатов
				pushToSecurityContext(new User(userId));
				try {
					assertTrue(chats.getBlockedAccs().size() == 0);
					for(ChatAccount acc : chats.getAccsForOperator(userId)){
						assertEquals(false, acc.blocked);
					}
				}finally {
					popUserFromSecurityContext();
				}
			}
			
			
			//прошел месяц, снова сняли деньги и заблочили
			{
				assertFalse(findBalance(universal, userId).accsBlocked);
				BigDecimal initBalance = billing.getUserBalance(userId);
				
				Date now = parseStandartDateTime("01.11.2014 5:15:00");
				assertEquals(1, billingSync.doSyncWork(false, now));
				assertEquals("-3.12", getDeltaVal(userId, initBalance));
				assertEquals("-3.12", billing.getUserBalance(userId).toString());
				
				//но расчет тарифов обновился
				assertTrue(findBalance(universal, userId).accsBlocked);
				assertEquals(parseStandartDateTime("01.11.2014 00:00:00"), universal.selectOne(new GetChatAccount(accUid)).tariffLastPay);
				
				//старые тарифы не обновили дату оплаты, т.к. она у них позже текущей
				for(String uid : oldAccs) assertEquals(longFuture, universal.selectOne(new GetChatAccount(uid)).tariffLastPay);
				
				
				//послан запрос на блокировку
				assertEquals(1, blockReqs.size()); 
				assertTrue(blockReqs.get(0).second);
				blockReqs.clear();
				
				
				//не можем менять тарифы юзером
				pushToSecurityContext(new User(userId));
				try {
					chats.updateAccTariffByUser(accUid, tariffId);
					fail_exception_expected();
				}
				catch(ChatAccountBlockedException e){
					//ok
				}finally {
					popUserFromSecurityContext();
				}
			}
			
			
			
			//юзер закинул мало денег и его не разблочили
			{
				assertTrue(findBalance(universal, userId).accsBlocked);
				Date oldLastPay = universal.selectOne(new GetChatAccount(accUid)).tariffLastPay;
				
				Date now = parseStandartDateTime("03.11.2014 13:12:00");
				pushToSecurityContext(new User(userId));
				try {
					paypalClient.payAmount = new BigDecimal("3.11");
					billing.sendPayReq(paypal_key.strDefVal(), paypalClient.payAmount);
					billing.paypal_preparePayConfirm(randomSimpleId(), STUB_TOKEN);
					billing.paypal_finishPayment(now);
				} finally {
					popUserFromSecurityContext();
				}
				
				//баланс изменился, но тариф и блокировка остались прежними
				assertEquals("-0.01", billing.getUserBalance(userId).toString());
				assertTrue(findBalance(universal, userId).accsBlocked);
				assertEquals(oldLastPay, universal.selectOne(new GetChatAccount(accUid)).tariffLastPay);
				
				
				assertEquals(0, blockReqs.size()); 
			}
			
			
			
			//юзер пополнил с ассинхронным подверждением и его снова разблокировали
			{
				assertTrue(findBalance(universal, userId).accsBlocked);
				
				BigDecimal payAmount = new BigDecimal("5.01");
				Date prev = parseStandartDateTime("04.11.2014 11:00:00");
				Date now = parseStandartDateTime("05.11.2014 13:12:00");
				long payId = universal.nextSeqFor(payments);
				universal.update(new CreatePayment(new PaymentExt(payId, userId, PAYPAL, "somePay", WAIT, prev, prev, payAmount)));
				
				paypalClient.payAmount = payAmount;
				paypalClient.paymentHistory = list(new PaymentBase("somePay", COMPLETED));
				paypalClient.paymentId = "somePay";
				paypalClient.payment = new PaymentBase(paypalClient.paymentId, COMPLETED);
				paySync.doSyncWork(now);
				
				//платеж прошел
				assertEquals(COMPLETED, universal.selectOne(new GetPaymentById(payId)).paymentStatus);
				
				//акки разблокировались
				assertEquals("5.00", billing.getUserBalance(userId).toString());
				assertFalse(findBalance(universal, userId).accsBlocked);
				assertEquals(now, universal.selectOne(new GetChatAccount(accUid)).tariffLastPay);
				
				//старые тарифы не обновили дату оплаты, т.к. она у них позже текущей
				for(String uid : oldAccs) assertEquals(longFuture, universal.selectOne(new GetChatAccount(uid)).tariffLastPay);
				
				
				//послан запрос на разблокировку
				assertEquals(1, blockReqs.size()); 
				assertFalse(blockReqs.get(0).second);
				blockReqs.clear();
				
			}
			
			
			
			
			
		}finally {
			popUserFromSecurityContext();
		}
		
		
		//Нормируем акки обратно
		Date now = new Date();
		universal.update(new UpdateAllChatAccounts(new TariffStart(now), new TariffLastPay(now)));
	}
	
	
	
	
	
	
	
	
	
	private void test_billing_callFromCacheFlag() throws Exception {
		
		MailServiceStub mailService = new MailServiceStub(mailSender, props);
		mailSender.tasks.clear();
		
		Date prevMonth = monthStart(addMonths(new Date(), -1));
		

			
		BillingSyncService sync = new BillingSyncService();
		sync.setCacheServerContext(new CacheServerContext(props, cacheSever, db, mailService));
		sync.init();
		
		assertEquals(null, cacheClient.getVal(BILLING_SYNC_REQ));
		assertEquals(null, cacheClient.getVal(BILLING_SYNC_RESP));
		
		universal.update(new UpdateChatAccountByUid("demo", new TariffStart(prevMonth),new TariffLastPay(prevMonth)));
		
		//call
		String reqId = "123";
		cacheClient.putCache(BILLING_SYNC_REQ, reqId);
		sync.checkTasksFromCache();
		
		AdminSyncResp resp = tryParseJson(cacheClient.getVal(BILLING_SYNC_RESP), AdminSyncResp.class);
		assertNotNull(resp);
		assertEquals(reqId, resp.reqId);
		assertEquals(1, resp.updatedCount);
		
	}
	
	
	
	
	private void test_billing_sendErrorsWithEmails() throws Exception {
		
		MailServiceStub mailService = new MailServiceStub(mailSender, props);
		mailSender.tasks.clear();
		
		Date prevMonth = monthStart(addMonths(new Date(), -1));
		

			
		BillingSyncService sync = new BillingSyncService();
		sync.setCacheServerContext(new CacheServerContext(props, cacheSever, db, mailService));
		sync.init();
		
		//no errors
		universal.update(new UpdateChatAccountByUid("demo", new TariffStart(prevMonth),new TariffLastPay(prevMonth)));
		assertEquals(1, sync.doSyncWork(false));
		assertEquals(0, mailSender.tasks.size());
		
		//with errors
		universal.update(new UpdateChatAccountByUid("demo", new TariffStart(prevMonth),new TariffLastPay(prevMonth)));
		sync.syncAccsListener = ()-> {
			throw new TestException();
		};
		assertEquals(1, sync.doSyncWork(false));
		assertEquals(1, mailSender.tasks.size());
		

	}
	
	
	
	
	
	private void test_tariff_billing_concurrent() throws Exception {
		
		ExecutorService async = ExecutorsUtil.newSingleThreadExecutor("billing_concurrent");
		
		MailServiceStub mailService = new MailServiceStub(mailSender, props);
		mailSender.tasks.clear();
		
		//Отключаем все другие акки от апдейта
		Date longFuture = parseStandartDateTime("02.09.2040 3:00:00");
		universal.update(new UpdateAllChatAccounts(new TariffStart(longFuture), new TariffLastPay(longFuture)));
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			int tariffId = 2;
			long userId = userId4;
			String accUid = "tariff_billing_concurrent";
			BigDecimal tariffPrice = new BigDecimal("5.00");
			BigDecimal correctVal = new BigDecimal(100);
			
			//create acc
			chats.createAcc(serverId1, accUid, userId, "billing_concurrent", tariffId);
			chats.setOperatorForAcc(accUid, userId);
			universal.update(new UpdateUserAccsBlocked(userId, false));
			
			BillingSyncService sync = new BillingSyncService();
			sync.setCacheServerContext(new CacheServerContext(props, cacheSever, db, mailService));
			sync.init();
			
			
			//пытаемся менять тариф и в этот момент происходит синх оплаты
			{
				correctBalance(userId, correctVal);
				
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-5.00";
				
				Date pastPay = parseStandartDateTime("01.08.2014 00:00:00");
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date now = parseStandartDateTime("03.09.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(tariffStart), new TariffLastPay(pastPay)));
				
				
				//race
				try {
					chats.updateAccTariff(accUid, 4, new UpdateTariffOps(true, false, tariffStart, pastPay, now, tariffPrice, ()->{
						
						//в транзакции обновления тарифа
						//создаем новую транзакцию в которой успеваем отработать синхр оплат
						async.submit(()->{
							
							pushToSecurityContext_SYSTEM_USER();
							try {
								assertEquals(1, sync.doSyncWork(false, now));
								assertEquals(expAmount, getDeltaVal(userId, initBalance));
								return null;
							}finally {
								popUserFromSecurityContext();
							}
							
						}).get();
						
					}));
					
					fail_exception_expected();
					
				}catch(ConcurrentUpdateSqlException e){
					//ok
				}
				
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			
			//пытаемся синхр оплаты и в этот момент происходит смена тарифа
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-5.34";
				
				Date pastPay = parseStandartDateTime("01.08.2014 00:00:00");
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date now = parseStandartDateTime("03.09.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(tariffStart), new TariffLastPay(pastPay)));
				
				
				//race
				int syncCount =	sync.doSyncWork(false, now, ()->{
						
					//в транзакции синхр цен
					//создаем новую транзакцию в которой успеваем обновить тариф
					async.submit(()->{
						
						pushToSecurityContext_SYSTEM_USER();
						try {
							chats.updateAccTariff(accUid, 4, new UpdateTariffOps(true, false, tariffStart, pastPay, now, tariffPrice));
							assertEquals(expAmount, getDeltaVal(userId, initBalance));
							return null;
						}finally {
							popUserFromSecurityContext();
						}
						
					}).get();
						
				});
				assertEquals(0, syncCount);
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
				
				
				//второй вызов синхра
				Date newNow = parseStandartDateTime("03.09.2014 4:00:00");
				BigDecimal newInitBalance = billing.getUserBalance(userId);
				assertEquals(0, sync.doSyncWork(false, newNow));
				assertEquals("0.00", getDeltaVal(userId, newInitBalance));
			}
			
			
			
		
		}finally {
			popUserFromSecurityContext();
		}
		
		
		//Нормируем акки обратно
		Date now = new Date();
		universal.update(new UpdateAllChatAccounts(new TariffStart(now), new TariffLastPay(now)));
	}
	
	
	
	private void test_billing_monthBill() throws Exception {
		
		MailServiceStub mailService = new MailServiceStub(mailSender, props);
		
		//Отключаем все другие акки от апдейта
		Date longFuture = parseStandartDateTime("02.09.2040 3:00:00");
		universal.update(new UpdateAllChatAccounts(new TariffStart(longFuture), new TariffLastPay(longFuture)));

		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			int tariffId = 2;
			long userId = userId4;
			String accUid = "billing_monthBill";
			BigDecimal correctVal = new BigDecimal(100);
			
			//create acc
			assertEquals(0, chats.getAccsForOperator(userId).size());
			chats.createAcc(serverId1, accUid, userId, "test_monthBill", tariffId);
			assertEquals(1, chats.getAccsForOperator(userId).size());
			assertEquals(0, chats.getAccOperators(accUid).size());

			//set operator
			chats.setOperatorForAcc(accUid, userId);
			assertEquals(1, chats.getAccOperators(accUid).size());
			assertEquals("0.00", billing.getUserBalance(userId).toString());
			
			mailSender.tasks.clear();
			
			BillingSyncService sync = new BillingSyncService();
			sync.setCacheServerContext(new CacheServerContext(props, cacheSever, db, mailService));
			sync.init();
			
			//целый длинный месяц (31)
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-5.00";
				
				Date pastPay = parseStandartDateTime("01.08.2014 00:00:00");
				Date now = parseStandartDateTime("02.09.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			//целый короткий месяц (28)
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-5.00";
				
				Date pastPay = parseStandartDateTime("01.02.2014 00:00:00");
				Date now = parseStandartDateTime("02.03.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			//половина длинного месяца
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-2.72";
				
				Date pastPay = parseStandartDateTime("15.08.2014 3:00:00");
				Date now = parseStandartDateTime("02.09.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			//половина короткого месяца
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-2.24";
				
				Date pastPay = parseStandartDateTime("15.02.2014 3:00:00");
				Date now = parseStandartDateTime("20.03.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			//успели сменить тариф перед подсчетом
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "0.00";
				
				Date pastPay = parseStandartDateTime("01.03.2014 2:00:00");
				Date now = parseStandartDateTime("01.03.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			
			//1,5 месяца (в случае ручного срабатывания синхронизации в середине второго месяца)
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-5.00";
				
				Date pastPay = parseStandartDateTime("01.07.2014 00:00:00");
				Date now = parseStandartDateTime("20.08.2014 12:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			
			//2 полных месяца (в случае не срабатывания синхронизации после первого)
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-10.00";
				
				Date pastPay = parseStandartDateTime("01.07.2014 00:00:00");
				Date now = parseStandartDateTime("02.09.2014 3:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			//3 полных месяца - начинает накапливаться погрешность
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-14.84"; //not 15.00
				
				Date pastPay = parseStandartDateTime("01.07.2014 00:00:00");
				Date now = parseStandartDateTime("15.10.2014 11:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			//ГОД - большая погрешность
			{
				correctBalance(userId, correctVal);
				BigDecimal initBalance = billing.getUserBalance(userId);
				String expAmount = "-58.87"; //12*5 = 60
				
				Date pastPay = parseStandartDateTime("01.07.2014 00:00:00");
				Date now = parseStandartDateTime("03.07.2015 11:00:00");
				universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
				assertEquals(1, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
				//второй вызов -- нет эффекта
				assertEquals(0, sync.doSyncWork(false, now));
				assertEquals(expAmount, getDeltaVal(userId, initBalance));
			}
			
			
			
			//нет ошибок для отправки админу
			assertEquals(0, mailSender.tasks.size());
			
			
			//тест рабочего времени таймера
			{
				//before
				assertEquals(-3, sync.doSyncWork(true, parseStandartDateTime("31.08.2014 12:35:00")));
				assertEquals(-3, sync.doSyncWork(true, parseStandartDateTime("31.08.2014 23:59:00")));
				assertEquals(-2, sync.doSyncWork(true, parseStandartDateTime("01.08.2014 00:00:00")));
				assertEquals(-2, sync.doSyncWork(true, parseStandartDateTime("01.08.2014 05:00:00")));
				assertEquals(-2, sync.doSyncWork(true, parseStandartDateTime("01.08.2014 05:59:59")));
				
				//in
				assertEquals(0, sync.doSyncWork(true, parseStandartDateTime("01.08.2014 06:00:00")));
				assertEquals(0, sync.doSyncWork(true, parseStandartDateTime("02.08.2014 00:00:00")));
				assertEquals(0, sync.doSyncWork(true, parseStandartDateTime("03.08.2014 00:00:00")));
				assertEquals(0, sync.doSyncWork(true, parseStandartDateTime("03.08.2014 23:59:00")));
				assertEquals(0, sync.doSyncWork(true, parseStandartDateTime("03.08.2014 23:59:01")));
				assertEquals(0, sync.doSyncWork(true, parseStandartDateTime("03.08.2014 23:59:59")));
				
				//after
				assertEquals(-3, sync.doSyncWork(true, parseStandartDateTime("04.08.2014 00:00:00")));
			}
			
			
		}finally {
			popUserFromSecurityContext();
		}
		
		
		//Нормируем акки обратно
		Date now = new Date();
		universal.update(new UpdateAllChatAccounts(new TariffStart(now), new TariffLastPay(now)));
		
	}
	
	private String getDeltaVal(long userId, BigDecimal oldVal) throws Exception{
		BigDecimal curBalance = billing.getUserBalance(userId);
		return getDeltaVal(curBalance, oldVal);
	}
	
	private String getDeltaVal(BigDecimal newVal, BigDecimal oldVal) throws Exception{
		return round(newVal.subtract(oldVal)).toString();
	}

	
	private void correctBalance(long userId, BigDecimal val) throws Exception {
		universal.update(new UpdateUserBalanceUnsafe(userId, val));
		billing.updateUserBalanceCache(userId, false);
	}
	
	
	
	
	
	
	
	
	
	private void test_tariff_update_maxInDayLimit() throws Exception {
		
		long ownerId = 100;
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			
			//reset day to now
			chats.updateAccTariff("demo2", 2, true);
			
			long t1 = chats.createTariff(new BigDecimal(1), true, 10);
			long t2 = chats.createTariff(new BigDecimal(2), true, 10);
			long t3 = chats.createTariff(new BigDecimal(3), true, 10);
			
			//day limitations
			pushToSecurityContext(new User(ownerId));
			try {
				assertEquals(0, chats.getAccsForOperator(ownerId).get(1).tariffChangedInDay);
				int maxLimitations = PropKey.tariffs_maxChangedInDay.intDefVal();
				for (int i = 0; i < maxLimitations; i++) {
					chats.updateAccTariffByUser("demo2", i%2==0? t1 : t2);
					assertEquals(i+1, chats.getAccsForOperator(ownerId).get(1).tariffChangedInDay);
				}
				try {
					chats.updateAccTariffByUser("demo2", t3);
					fail_exception_expected();
				}catch(ChangeTariffLimitException e){
					//ok
				}
			}finally {
				popUserFromSecurityContext();
			}
			
			
			//next day
			boolean canUseNotPublic = true;
			boolean checkChangedInDay = true;
			Date nextDay = addDays(new Date(), 1);
			chats.updateAccTariff("demo2", t3, new UpdateTariffOps(canUseNotPublic, checkChangedInDay, null, null, nextDay, null));
			assertEquals(1, chats.getAccsForOperator(ownerId).get(1).tariffChangedInDay);
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	private void test_tariff_update() throws Exception {
		
		//Нормируем акки под текущю дату
		Date createDate = parseStandartDateTime("21.08.2014 12:00:00");
		universal.update(new UpdateAllChatAccounts(new TariffStart(createDate), new TariffLastPay(createDate)));
		
		BigDecimal price = new BigDecimal("5.00");
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			long ownerId = 100;
			{
				List<ChatAccount> accs = chats.getAccsForOperator(ownerId);
				ChatAccount acc = accs.get(0);
				assertEquals("demo", acc.uid);
				assertEquals(1L, acc.tariffId);
				assertEquals(null, acc.tariffPrevId);
			}
			
			
			//смена спустя минуту
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "0.00";
				
				Date tariffStart = createDate;
				Date now = new Date(createDate.getTime() + 1000*60);
				
				chats.updateAccTariff("demo", 3, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
				
				//test cache update
				BigDecimal fromCache = tryParseBigDecimal(cacheClient.tryGetVal(getBalanceCacheKey(ownerId)), null);
				assertEquals(expAmount, getDeltaVal(fromCache, initBalance));
				
				List<ChatAccount> accs = chats.getAccsForOperator(ownerId);
				ChatAccount acc = accs.get(0);
				assertEquals("demo", acc.uid);
				assertEquals(3L, acc.tariffId);
				assertEquals(Long.valueOf(1L), acc.tariffPrevId);
			}
			
			
			//смена спустя час
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-0.01";
				
				Date tariffStart = createDate;
				Date now = new Date(createDate.getTime() + 1000*60*60);
				
				chats.updateAccTariff("demo", 2, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
				
				//test cache update
				BigDecimal fromCache = tryParseBigDecimal(cacheClient.tryGetVal(getBalanceCacheKey(ownerId)), null);
				assertEquals(expAmount, getDeltaVal(fromCache, initBalance));
			}
			
			
			//смена спустя день
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-0.16";
				
				Date tariffStart = createDate;
				Date now = new Date(createDate.getTime() + 1000*60*60*24);
				
				chats.updateAccTariff("demo", 3, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
				
				//test cache update
				BigDecimal fromCache = tryParseBigDecimal(cacheClient.tryGetVal(getBalanceCacheKey(ownerId)), null);
				assertEquals(expAmount, getDeltaVal(fromCache, initBalance));
			}
			
			
			
			//смена спустя почти целый месяц
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-4.53";
				
				Date tariffStart = parseStandartDateTime("01.08.2014 10:45:13");
				Date now = parseStandartDateTime("29.08.2014 12:15:00");
				
				chats.updateAccTariff("demo", 2, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
				
				//test cache update
				BigDecimal fromCache = tryParseBigDecimal(cacheClient.tryGetVal(getBalanceCacheKey(ownerId)), null);
				assertEquals(expAmount, getDeltaVal(fromCache, initBalance));
			}
			
			
			//смена спустя целый длинный месяц
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-5.00";
				
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date now = parseStandartDateTime("31.08.2014 23:59:59");
				
				chats.updateAccTariff("demo", 3, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
			}
			
			
			//смена спустя целый короткий месяц
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-4.52";
				
				Date tariffStart = parseStandartDateTime("01.02.2014 00:00:00");
				Date now = parseStandartDateTime("28.02.2014 23:59:59");
				
				chats.updateAccTariff("demo", 2, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
			}
			
			
			//смена спустя месяц плюс несколько дней (когда еще не успел отработать синх по оплатам)
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-5.24";
				
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date now = parseStandartDateTime("02.09.2014 12:15:00");
				
				chats.updateAccTariff("demo", 3, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
			}
			
			
			
			//смена спустя 2 месяца (сломался синх оплат)
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-10.24";
				
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date now = parseStandartDateTime("03.10.2014 12:15:00");
				
				chats.updateAccTariff("demo", 2, new UpdateTariffOps(true, false, tariffStart, tariffStart, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
			}
			
			
			//смена спустя день после оплаты прошлого месяца
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-0.24";
				
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date lastPay = parseStandartDateTime("01.09.2014 00:00:00");
				Date now = parseStandartDateTime("02.09.2014 12:15:00");
				
				chats.updateAccTariff("demo", 3, new UpdateTariffOps(true, false, tariffStart, lastPay, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
			}
			
			
			//постановка на паузу в середине месяца делает расчет
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "-0.24";
				
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date lastPay = parseStandartDateTime("01.09.2014 00:00:00");
				Date now = parseStandartDateTime("02.09.2014 12:15:00");
				
				chats.pauseAcc("demo", new UpdateTariffOps(true, false, tariffStart, lastPay, now, price));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
			}
			
			
			//нельзя менять тариф у акка на паузе
			pushToSecurityContext(new User(ownerId));
			try {
				chats.updateAccTariffByUser("demo", 3);
				fail_exception_expected();
			}catch(ChatAccountPausedException e){
				//ok
			} finally {
				popUserFromSecurityContext();
			}
			
			
			
			//снятие с паузы не делает расчет
			{
				BigDecimal initBalance = billing.getUserBalance(ownerId);
				String expAmount = "0.00";
				
				Date tariffStart = parseStandartDateTime("01.08.2014 00:00:00");
				Date lastPay = parseStandartDateTime("01.09.2014 00:00:00");
				Date now = parseStandartDateTime("02.09.2014 12:15:00");
				
				chats.unpauseAcc("demo", 3, new UpdateTariffOps(true, false, tariffStart, lastPay, now, null));
				assertEquals(expAmount, getDeltaVal(ownerId, initBalance));
			}
			
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	private void test_tariff_update_invalidInputs_limitations() throws Exception {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			try {
				chats.updateAccTariff("demo-nono", 2, false);
				fail_exception_expected();
			}catch(NoChatAccountException e){
				//ok
			}
			
			
			try {
				chats.updateAccTariff("demo", -99, false);
				fail_exception_expected();
			}catch(TariffNotFoundException e){
				//ok
			}
			
			
			try {
				chats.updateAccTariff("demo", 6, false);
				fail_exception_expected();
			}catch(NotPublicTariffException e){
				//ok
			}
			
			
			//limitations
			long storngTariffId = chats.createTariff(ZERO, true, 1);
			try {
				chats.updateAccTariff("demo", storngTariffId, false);
				fail_exception_expected();
			}catch(UpdateTariffOperatorsLimitException e){
				//ok
			}
			
			
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	
	private void test_tariff_defaultTariffs() throws Exception {
		
		List<Tariff> tariffs = universal.select(new GetAllTariffs());
		assertTrue(tariffs.size() > 1);
		
		assertEquals(Tariff.PAUSE_TARIFF_ID, tariffs.get(0).id);
		assertEquals(0, tariffs.get(0).maxOperators);
		assertEquals(false, tariffs.get(0).isPublic);
		assertEquals("0.00", tariffs.get(0).price.toString());
		
		assertEquals(0, tariffs.get(1).maxOperators);
		assertEquals(false, tariffs.get(1).isPublic);
		
		assertEquals(2, tariffs.get(2).maxOperators);
		assertEquals(true, tariffs.get(2).isPublic);
		
		assertEquals(10, tariffs.get(4).maxOperators);
		assertEquals(false, tariffs.get(4).isPublic);
		
		assertEquals(10, tariffs.get(5).maxOperators);
		assertEquals(true, tariffs.get(5).isPublic);
		
		List<Tariff> publicTariffs = chats.getPublicTariffs();
		assertEquals(2, publicTariffs.size());
		assertEquals(2L, publicTariffs.get(0).id);
		assertEquals(5L, publicTariffs.get(1).id);
		
	}
	
	
	private void test_billing_atomicChangeBalance() throws Exception {
		
		long userId = 101L;
		assertEquals(0, findBalance(universal, userId).balance.intValue());
		
		assertEquals(1, appendBalance(universal, userId, new BigDecimal(1L)).intValue());
		assertEquals(1, findBalance(universal, userId).balance.intValue());
		
		assertEquals(2, appendBalance(universal, userId, new BigDecimal(1L)).intValue());
		assertEquals(2, findBalance(universal, userId).balance.intValue());
		
		assertEquals(1, appendBalance(universal, userId, new BigDecimal(-1L)).intValue());
		assertEquals(1, findBalance(universal, userId).balance.intValue());
		
		//manual check of atomic
		universal.update(new UpdateUserBalance(userId, new BigDecimal(100), new BigDecimal(99)));
		assertEquals(1, findBalance(universal, userId).balance.intValue());
		universal.update(new UpdateUserBalance(userId, new BigDecimal(1), new BigDecimal(99)));
		assertEquals(99, findBalance(universal, userId).balance.intValue());
		
		
		//проверяем, что два конкурентных запроса в разных транзакция 
		//корректно работают с оптимист.блокировкой
		{
			ExecutorService singleExecutor = ExecutorsUtil.newSingleThreadExecutor("test-optimistic-lock");
			Future<?> f = null;
			
			setSingleTxMode();
			try {
				
				final BigDecimal oldVal = new BigDecimal(99);
				
				int updated = universal.updateOne(new UpdateUserBalance(userId, oldVal, new BigDecimal(100)));
				assertEquals(1, updated);
				
				f = singleExecutor.submit(()->{
					
					setSingleTxMode();
					try {
						int updatedInOtherTx = universal.updateOne(new UpdateUserBalance(userId, oldVal, new BigDecimal(98)));
						assertEquals(0, updatedInOtherTx);
					}catch (Exception e) {
						rollbackSingleTx();
						throw e;
					} finally {
						closeSingleTx();
					}
					return null;

				});
				
			}catch (Exception e) {
				rollbackSingleTx();
				throw e;
			} finally {
				closeSingleTx();
			}
			
			//если вызвать f.get внутри тела первой tx, то поток 2 зависнет, до тех пор пока не будет коммит потока 1 (дед лок)
			//если в потоке 2 будет userId+1, то зависания не будет
			//заморозку делает сама БД, проверено под дебагом: если до коммита потока 1 в клиенте к бд вызвать апдейт, то он зависнет
			f.get();
			
			//в итоге прав оказался первый поток
			assertEquals(100, findBalance(universal, userId).balance.intValue());
			
			singleExecutor.shutdown();
		}

		

	}
	
	
	
	
	
	
	
	private void test_billing_payBill() throws Exception {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			BigDecimal balance = billing.getUserBalance(userId1);
			List<PaymentExt> payments = billing.getPayments(userId1, 100, 0);
			
			String desc = "test bill";
			BigDecimal delta = TEN;
			billing.payBill(userId1, delta, new Date(), SYSTEM_OUT_CORRECTION, desc);
			
			BigDecimal newBalance = billing.getUserBalance(userId1);
			assertEquals(balance.subtract(TEN).doubleValue(), newBalance.doubleValue(), 0.01);
			
			List<PaymentExt> newPayments = billing.getPayments(userId1, 100, 0);
			assertEquals(payments.size()+1, newPayments.size());
			
			PaymentExt item = newPayments.get(0);
			assertEquals(desc, item.details);
			assertEquals(PaymentType.SYSTEM_OUT_CORRECTION, item.payType);
			
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	
	
	
	
	
	
	private void test_billing_paySync() throws Exception {
		
		MapProps props = new MapProps();
		props.putVal(admin_Emails, "some@host");
		props.putVal(paypal_sync_debug_DisableTimer, true);
		props.putVal(mail_storeToDisc, false);
		
		MailServiceStub mailService = new MailServiceStub(mailSender, props);
		
		PaypalClientStub clientStub = new PaypalClientStub();
		ArrayList<PaymentExt> syncPaymentsByDate = new ArrayList<PaymentExt>();
		ArrayList<PaymentExt> syncPaymentsById = new ArrayList<PaymentExt>();
		boolean[] canSync = {false};
		
		//service
		PaypalPaymentsSynchService syncService = new PaypalPaymentsSynchService(){
			@Override
			protected void syncPaymentsByDate(List<PaymentExt> list, Date now) {
				syncPaymentsByDate.clear();
				syncPaymentsByDate.addAll(list);
				if(canSync[0]) super.syncPaymentsByDate(list, now);
			}
			@Override
			protected void syncPaymentsByIdAsync(List<PaymentExt> list, Date now) {
				syncPaymentsById.clear();
				syncPaymentsById.addAll(list);
				if(canSync[0]) super.syncPaymentsByIdAsync(list, now);
			}
		};
		syncService.setCacheServerContext(new CacheServerContext(props, cacheSever, db, mailService));
		syncService.setClient(clientStub);
		syncService.init();
		
		Date now = new Date();
		Date minuteAgo = new Date(now.getTime() - DateUtil.ONE_MINUTE);
		Date twoHoursAgo = new Date(now.getTime() - DateUtil.ONE_HOUR*2);
		Date monthAgo = DateUtil.addDays(now, -31);
		
		long userId = 103L;
		//fill db
		universal.update(new CreatePayment(new PaymentExt(universal.nextSeqFor(payments), userId, PAYPAL, "p1", WAIT, minuteAgo, minuteAgo, BigDecimal.ONE)));
		universal.update(new CreatePayment(new PaymentExt(universal.nextSeqFor(payments), userId, PAYPAL, "p2", WAIT, twoHoursAgo, twoHoursAgo, BigDecimal.ONE)));
		universal.update(new CreatePayment(new PaymentExt(universal.nextSeqFor(payments), userId, PAYPAL, "p3", WAIT, monthAgo, monthAgo, BigDecimal.ONE)));
		
		
		//check filter logic
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			assertEquals(0, billing.getUserBalance(userId).intValue());
			
			//first call -- all payments to sync
			{
				syncService.doSyncWork(now);
				assertEquals(2, syncPaymentsByDate.size());
				assertEquals(1, syncPaymentsById.size());
				assertEquals("p1", syncPaymentsByDate.get(0).externalId);
				assertEquals("p2", syncPaymentsByDate.get(1).externalId);
				assertEquals("p3", syncPaymentsById.get(0).externalId);
				syncPaymentsByDate.clear();
				syncPaymentsById.clear();
			}

			
			//second call -- only new
			now = new Date(now.getTime() + 1000);
			{
				syncService.doSyncWork(now);
				assertEquals(1, syncPaymentsByDate.size());
				assertEquals(0, syncPaymentsById.size());
				assertEquals("p1", syncPaymentsByDate.get(0).externalId);
				syncPaymentsByDate.clear();
				syncPaymentsById.clear();
			}
			
			//next call -- same
			now = new Date(now.getTime() + 1000);
			{
				syncService.doSyncWork(now);
				assertEquals(1, syncPaymentsByDate.size());
				assertEquals(0, syncPaymentsById.size());
				assertEquals("p1", syncPaymentsByDate.get(0).externalId);
				syncPaymentsByDate.clear();
				syncPaymentsById.clear();
			}
			
			//after long delta -- all
			now = new Date(now.getTime() + 1000);
			props.putVal(paypal_sync_longUpdateDelta, 0);
			{
				syncService.doSyncWork(now);
				assertEquals(2, syncPaymentsByDate.size());
				assertEquals(1, syncPaymentsById.size());
				syncPaymentsByDate.clear();
				syncPaymentsById.clear();
			}
			
			now = new Date(now.getTime() + 1000);
			props.putVal(paypal_sync_longUpdateDelta, paypal_sync_longUpdateDelta.longDefVal());
			{
				syncService.doSyncWork(now);
				assertEquals(1, syncPaymentsByDate.size());
				assertEquals(0, syncPaymentsById.size());
				assertEquals("p1", syncPaymentsByDate.get(0).externalId);
				syncPaymentsByDate.clear();
				syncPaymentsById.clear();
			}
		}finally {
			popUserFromSecurityContext();
		}
		
		
		//sync by 
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			canSync[0] = true;
			
			//update by date
			now = new Date(now.getTime() + 1000);
			assertEquals(WAIT, universal.selectOne(new GetPaymentByExternalId(PAYPAL, "p1")).paymentStatus);
			clientStub.paymentHistory = list(new PaymentBase("p1", COMPLETED));
			syncService.doSyncWork(now);
			assertEquals(COMPLETED, universal.selectOne(new GetPaymentByExternalId(PAYPAL, "p1")).paymentStatus);
			assertEquals(1, universal.selectOne(new SelectUserBalanceById(userId)).balance.intValue());
			assertEquals(1, billing.getUserBalance(userId).intValue());
			
			//update by ids
			now = new Date(now.getTime() + 1000);
			props.putVal(paypal_sync_longUpdateDelta, 0);
			clientStub.paymentHistory = list(new PaymentBase("p2", COMPLETED));
			clientStub.paymentId = "p3";
			clientStub.payment = new PaymentBase(clientStub.paymentId, COMPLETED);
			syncService.doSyncWork(now);
			assertEquals(COMPLETED, universal.selectOne(new GetPaymentByExternalId(PAYPAL, "p2")).paymentStatus);
			assertEquals(COMPLETED, universal.selectOne(new GetPaymentByExternalId(PAYPAL, "p3")).paymentStatus);
			assertEquals(3, billing.getUserBalance(userId).intValue());
			
			
			now = new Date(now.getTime() + 1000);
			syncPaymentsByDate.clear();
			syncPaymentsById.clear();
			syncService.doSyncWork(now);
			assertEquals(0, syncPaymentsByDate.size());
			assertEquals(0, syncPaymentsById.size());
			
		}finally {
			popUserFromSecurityContext();
		}
		
		
		//test timers
		props.putVal(paypal_sync_debug_DisableTimer, false);
		props.putVal(paypal_sync_timerDelay, 1L);
		props.putVal(paypal_sync_timerDelta, 20L);
		syncService.stop();
		syncService.init();
		pushToSecurityContext_SYSTEM_USER();
		try {
			canSync[0] = true;
			clientStub.paymentHistory = list(new PaymentBase("p4", COMPLETED));
			universal.update(new CreatePayment(new PaymentExt(universal.nextSeqFor(payments), userId, PAYPAL, "p4", WAIT, minuteAgo, minuteAgo, BigDecimal.ONE)));
			
			Thread.sleep(50);
			
			assertEquals(COMPLETED, universal.selectOne(new GetPaymentByExternalId(PAYPAL, "p4")).paymentStatus);
			assertEquals(4, billing.getUserBalance(userId).intValue());
		}finally {
			syncService.stop();
			popUserFromSecurityContext();
		}
		
		
		//test send errors to admin
		props.putVal(paypal_sync_sendErrorsDelay, 1L);
		props.putVal(paypal_sync_sendErrorsDelta, 20L);
		syncService.init();
		try {
			universal.update(new CreatePayment(new PaymentExt(universal.nextSeqFor(payments), userId, PAYPAL, "p5", WAIT, minuteAgo, minuteAgo, BigDecimal.ONE)));
			
			mailSender.tasks.clear();
			clientStub.paymentHistory = null;
			
			Thread.sleep(100);
			assertTrue(mailSender.tasks.size() > 0);
			
			
			//dissable send errors
			props.putVal(paypal_sync_debug_DisableSendErrors, true);
			Thread.sleep(50);
			mailSender.tasks.clear();
			Thread.sleep(50);
			assertTrue(mailSender.tasks.size() == 0);
			
			//enable again
			props.putVal(paypal_sync_debug_DisableSendErrors, false);
			Thread.sleep(50);
			assertTrue(mailSender.tasks.size() > 0);
			
			//set filter
			props.putVal(paypal_sync_skipErrorTerms, "empty history by daysBefore");
			Thread.sleep(50);
			mailSender.tasks.clear();
			Thread.sleep(50);
			assertTrue(mailSender.tasks.size() == 0);
			
			//remove filter
			props.putVal(paypal_sync_skipErrorTerms, (String)null);
			Thread.sleep(50);
			assertTrue(mailSender.tasks.size() > 0);
			
		}finally {
			syncService.stop();
		}
		
		
		
		
	}
	
	
	private void test_billing_paypal_paySteps() throws Exception {
		
		
		User user = new User(100, "some", "dd@dd.dd", ACTIVATED);
		pushToSecurityContext(user);
		try {
			
			//show paypal page
			{
				assertNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
				
				PayData data = billing.sendPayReq(paypal_key.strDefVal(), new BigDecimal(12));
				assertNotNull(data);
				
				assertNotNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
			}
			
			//paypal wrong resp
			try {
				billing.paypal_preparePayConfirm("some", STUB_TOKEN+"123");
			}catch(NoDataToConfirmPaymentException e){
				//ok
			}
			
			//paypal resp
			{
				assertNull(cacheClient.getVal(BillingService.getConfirmPayCacheKey(user.id)));
				
				String userPayId = "some-"+randomSimpleId();
				billing.paypal_preparePayConfirm(userPayId, STUB_TOKEN);
				
				assertNotNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
			}

			//confirm pay
			{
				mailSender.tasks.clear();
				assertEquals(0, billing.getCurUserBalance().intValue());
				
				billing.paypal_finishPayment();
				lastFrom(asyncFutures).get();
				
				assertTrue(billing.getCurUserBalance().intValue() > 0);
				assertNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
				assertNull(cacheClient.getVal(BillingService.getConfirmPayCacheKey(user.id)));
				
				//check db
				List<PaymentExt> payments = billing.getPayments(10, 0);
				assertEquals(1, payments.size());
				
				//check email sended
				assertEquals(1, mailSender.tasks.size());
				
			}
			
			
		} finally {
			popUserFromSecurityContext();
		}
	}
	
	
	
	
	
	private void test_billing_2checkout_paySteps() throws Exception {
		
		User user = new User(100, "some", "dd@dd.dd", ACTIVATED);
		pushToSecurityContext(user);
		try {
			
			int initBalance = billing.getCurUserBalance().intValue();
			int payCount = 12;
			
			String token = null;
			//pay req
			{
				assertNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
				
				PayData data = billing.sendPayReq(toCheckout_key.strDefVal(), new BigDecimal(payCount));
				assertNotNull(data);
				
				assertNotNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
				
				token = data.token;
			}
			
			//wrong resp
			try {
				billing.tochekout_finishPayment("some", "some");
			}catch(NoDataToConfirmPaymentException e){
				//ok
			}
			

			//confirm pay
			{
				assertNotNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
				
				mailSender.tasks.clear();
				assertEquals(initBalance, billing.getCurUserBalance().intValue());
				
				String txId = "some-"+randomSimpleId();
				billing.tochekout_finishPayment(token, txId);
				lastFrom(asyncFutures).get();
				
				assertEquals(initBalance + payCount, billing.getCurUserBalance().intValue());
				assertNull(cacheClient.getVal(BillingService.getPayReqCacheKey(user.id)));
				assertNull(cacheClient.getVal(BillingService.getConfirmPayCacheKey(user.id)));
				
				//check db
				List<PaymentExt> payments = billing.getPayments(10, 0);
				assertEquals(2, payments.size());
				
				PaymentExt payment = payments.get(0);
				assertEquals(payCount, payment.amount.intValue());
				assertEquals(txId, payment.externalId);
				assertEquals(PaymentStatus.COMPLETED, payment.paymentStatus);
				assertEquals(PaymentType.REPLENISHMENT, payment.payType);
				assertEquals(PaymentProvider.TO_CHECKOUT, payment.provider);
				
				//check email sended
				assertEquals(1, mailSender.tasks.size());
				
			}
			
			
		} finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	
	
	
	private void test_billing_get_balance_from_cache_or_db() throws Exception {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			//from cache
			{
				long user = 100;
				assertEquals("0.00", cacheClient.getVal("balance-"+user));
				assertEquals("0.00", billing.getUserBalance(user).toString());
			}

			
			
			//from db and put to cache
			{
				long user = 102;
				assertEquals(null, cacheClient.getVal("balance-"+user));
				assertEquals(0, billing.getUserBalance(user).intValue());
				lastFrom(asyncFutures).get();
				assertEquals("0.00", cacheClient.getVal("balance-"+user));
			}
			
			
			//unknown user
			try {
				billing.getUserBalance(-99);
				fail_exception_expected();
			}catch(UserNotFoundException e){
				//ok
			}
			
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	
	private void test_billing_put_balances_to_cache() throws Exception {
		
		long user1 = 100;
		long user2 = 101;
		
		//in start
		assertEquals("0.00", cacheClient.getVal("balance-"+user1));
		assertEquals(null, cacheClient.getVal("balance-"+user2));
		
		//after creation
		pushToSecurityContext_SYSTEM_USER();
		try {
			String accountId = "demo-balance";
			chats.createAcc(serverId3, accountId, user2, null, 1);
		}finally {
			popUserFromSecurityContext();
		}
		lastFrom(asyncFutures).get();
		assertEquals(null, cacheClient.getVal("balance-"+user2));
		
	}
	
	
	
	private void test_chats_operatorsLimits() throws Exception {
		
		
		long tariffId = 2;
		String name = "operatorsLimits";
		User user = new User(userId1);
		
		pushToSecurityContext(user);
		try {
			
			String accUid = chats.createAccByUser(name, tariffId);
			
			chats.setOperatorForAcc(accUid, userId1);
			chats.setOperatorForAcc(accUid, userId2);
			
			//ограничение тарифа на макс число операторов
			try {
				chats.setOperatorForAcc(accUid, userId3);
				fail_exception_expected();
			}catch(OperatorsLimitException e){
				//ok
			}
			
			chats.removeUserPrivileges(accUid, userId2, set(PrivilegeType.CHAT_OPERATOR));
			chats.setOperatorForAcc(accUid, userId3);
			
			//ограничение тарифа на макс число операторов снова
			try {
				chats.setOperatorForAcc(accUid, userId2);
				fail_exception_expected();
			}catch(OperatorsLimitException e){
				//ok
			}
			
			
			//ограничение при паузе
			{
				chats.removeUserPrivileges(accUid, userId3, set(PrivilegeType.CHAT_OPERATOR));
				chats.pauseAccByUser(accUid);
				
				try {
					chats.setOperatorForAcc(accUid, userId2);
					fail_exception_expected();
				}catch(ChatAccountPausedException e){
					//ok
				}
				
				chats.unpauseAccByUser(accUid);
				chats.setOperatorForAcc(accUid, userId2);
			}
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	private void test_chats_changePrivsForUser() throws Exception {
		
		long tariffId = 5;
		User user1 = new User(userId1);
		User user2 = new User(userId2);
		String name = "changePrivsForUser";
		String accUid = null;
		
		//create acc
		pushToSecurityContext(user1);
		try {
			accUid = chats.createAccByUser(name, tariffId);
		}finally {
			popUserFromSecurityContext();
		}
		assertEquals(set(CHAT_OWNER), chats.getAccPrivilegesForUser(accUid, userId1));
		
		
		//add empty privs
		{
			chats.addUserPrivileges(accUid, userId2, set());
			assertEquals(set(), chats.getAccPrivilegesForUser(accUid, userId2));
		}
		
		//add with no role
		{
			try {
				chats.addUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
				fail_exception_expected();
			}catch(AccessDeniedException e){
				//ok
			}
		}
		
		//add with wrong role
		{
			pushToSecurityContext(user2);
			try {
				chats.addUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
				fail_exception_expected();
			}catch(AccessDeniedException e){
				//ok
			} finally {
				popUserFromSecurityContext();
			}
		}
		
		//add like owner
		{
			pushToSecurityContext(user1);
			try {
				chats.addUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
			}finally {
				popUserFromSecurityContext();
			}
			assertEquals(set(CHAT_OPERATOR), chats.getAccPrivilegesForUser(accUid, userId2));
		}
		
		//remove empty privs
		{
			chats.removeUserPrivileges(accUid, userId2, set());
			assertEquals(set(CHAT_OPERATOR), chats.getAccPrivilegesForUser(accUid, userId2));
		}
		
		//remove with no role
		{
			try {
				chats.removeUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
				fail_exception_expected();
			}catch(AccessDeniedException e){
				//ok
			}
		}
		
		//remove with wrong role
		{
			pushToSecurityContext(user2);
			try {
				chats.removeUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
				fail_exception_expected();
			}catch(AccessDeniedException e){
				//ok
			} finally {
				popUserFromSecurityContext();
			}
		}
		
		//remove like owner
		{
			pushToSecurityContext(user1);
			try {
				chats.removeUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
			}finally {
				popUserFromSecurityContext();
			}
			assertEquals(set(), chats.getAccPrivilegesForUser(accUid, userId2));
		}
		
		//add moder and self operator
		{
			pushToSecurityContext(user1);
			try {
				chats.addUserPrivileges(accUid, userId1, set(CHAT_OPERATOR));
				chats.addUserPrivileges(accUid, userId2, set(CHAT_MODER));
			}finally {
				popUserFromSecurityContext();
			}
			assertEquals(set(CHAT_OWNER, CHAT_OPERATOR), chats.getAccPrivilegesForUser(accUid, userId1));
			assertEquals(set(CHAT_MODER), chats.getAccPrivilegesForUser(accUid, userId2));
		}
		
		//try add moder by other moder
		{
			pushToSecurityContext(user2);
			try {
				chats.addUserPrivileges(accUid, userId3, set(CHAT_MODER));
				fail_exception_expected();
			}catch(AccessDeniedException e){
				//ok
			} finally {
				popUserFromSecurityContext();
			}
		}
		
		//try remove moder by other moder
		{
			pushToSecurityContext(user2);
			try {
				chats.removeUserPrivileges(accUid, userId2, set(CHAT_MODER));
				fail_exception_expected();
			}catch(AccessDeniedException e){
				//ok
			} finally {
				popUserFromSecurityContext();
			}
		}
		
		//add operator by moder
		{
			pushToSecurityContext(user2);
			try {
				chats.addUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
				chats.addUserPrivileges(accUid, userId3, set(CHAT_OPERATOR));
			} finally {
				popUserFromSecurityContext();
			}
			assertEquals(set(CHAT_MODER, CHAT_OPERATOR), chats.getAccPrivilegesForUser(accUid, userId2));
			assertEquals(set(CHAT_OPERATOR), chats.getAccPrivilegesForUser(accUid, userId3));
		}
		
		//remove operator by moder
		{
			pushToSecurityContext(user2);
			try {
				chats.removeUserPrivileges(accUid, userId2, set(CHAT_OPERATOR));
				chats.removeUserPrivileges(accUid, userId3, set(CHAT_OPERATOR));
			} finally {
				popUserFromSecurityContext();
			}
			assertEquals(set(CHAT_MODER), chats.getAccPrivilegesForUser(accUid, userId2));
			assertEquals(set(), chats.getAccPrivilegesForUser(accUid, userId3));
		}
		
		//add op with unexists user
		pushToSecurityContext(user2);
		try {
			chats.addUserPrivileges(accUid, -100, set(CHAT_OPERATOR));
		} finally {
			popUserFromSecurityContext();
		}
	}
	
	
	private void test_chats_renameAccs_BySystem_ByUser() throws Exception {
		
		int userId = 100;
		String uid = null;
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			List<ChatAccount> accs = chats.getAccsForOperator(userId);
			assertTrue(accs.size() > 0);
			uid = accs.get(0).uid;
		}finally {
			popUserFromSecurityContext();
		}
		
		//rename by system
		String newNameA = "newName-123";
		pushToSecurityContext_SYSTEM_USER();
		try {
			chats.putAccConfigByUser(uid, Key.name, newNameA);
			assertEquals(newNameA, chats.getAccsForOperator(userId).get(0).name);
		}finally {
			popUserFromSecurityContext();
		}
		
		//rename by user
		String newNameB = "newName-345";
		pushToSecurityContext(new User(100));
		try {
			chats.putAccConfigByUser(uid, Key.name, newNameB);
			assertEquals(newNameB, chats.getAccsForOperator(userId).get(0).name);
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	
	private void test_chats_createAccs_byUser() throws Exception {
		
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			List<ServerRow> servers = chats.getServers();
			assertEquals(3, servers.size());
			
			String accName = "test_chats_createAccs_byUser";
			User user = new User(100);
			
			//create by System
			try {
				chats.createAccByUser(accName);
				fail_exception_expected();
			}catch(UserNotFoundException e){
				//ok
			}
			
			
			//no servers exception
			{
				for (ServerRow server : servers) chats.setServerFull(server.id, true);
				
				pushToSecurityContext(user);
				try {
					chats.createAccByUser(accName);
					fail_exception_expected();
				}catch(NoAvailableServerException e){
					//ok
				}finally {
					popUserFromSecurityContext();
				}
			}
			
			//set server to acc
			long serverId = servers.get(1).id;
			chats.setServerFull(serverId, false);
			pushToSecurityContext(user);
			try {
				 chats.createAccByUser(accName);
			}finally {
				popUserFromSecurityContext();
			}
			
			List<ChatAccount> accs = chats.getAccsForOperator(user.id);
			ChatAccount acc = find(accs, (c) -> accName.equals(c.name));
			assertNotNull(acc);
			assertNotNull(acc.created);
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	

	
	private void test_chats_createServerAndAcc_BySystem() throws Exception {
		
		mailSender.tasks.clear();
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			//dublicate
			try {
				chats.createServer(server1HttpUrl, server1HttpsUrl);
				fail_exception_expected();
			}catch(SQLException e){
				//ok
			}
			
			assertEquals(0, mailSender.tasks.size());
			
			serverId3 = chats.createServer(server3HttpUrl, server3HttpsUrl);
			
			
			int chatOwnerId = 100;
			
			//dublicate
			try {
				chats.createAcc(serverId3, "demo", chatOwnerId, null, 1);
				fail_exception_expected();
			}catch(SQLException e){
				//ok
			}
			
			assertEquals(0, mailSender.tasks.size());
			
			
			String accountId = "demo3";
			chats.createAcc(serverId3, accountId, chatOwnerId, null, 1);
			assertEquals(1, mailSender.tasks.size());
			
			int chatOperatorId = 2;
			chats.setOperatorForAcc(accountId, chatOperatorId);
			
			//test get
			ServerRow server = chats.getServerByAcc(accountId);
			assertNotNull(server);
			assertEquals(server3HttpUrl, server.httpUrl);
			
		}finally {
			popUserFromSecurityContext();
		}
		
		
	}
	
	
	
	private void test_set_get_roles() throws Exception {
		
		try {
			pushToSecurityContext_SYSTEM_USER();
			assertEquals(emptySet(), users.getUserById(userId1).getRoles());
			
			users.setRoles(userId1, set(ADMIN, MODERATOR));
			assertEquals(set(ADMIN, MODERATOR), users.getUserById(userId1).getRoles());
			
			users.setRoles(userId1, set(ADMIN));
			assertEquals(set(ADMIN), users.getUserById(userId1).getRoles());
			
			users.setRoles(userId1, new HashSet<UserRole>(0));
			assertEquals(emptySet(), users.getUserById(userId1).getRoles());
			
			users.setRoles(userId1, null);
			assertEquals(emptySet(), users.getUserById(userId1).getRoles());
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	private void test_ban_user_runtime_with_exists_session() throws Exception {
		
		LoginUserReq loginReq = new LoginUserReq(mail1, psw1, false);
		
		//login
		security.createUserSession(req, resp, loginReq);
		{
			User user = security.getUserFromSession(req);
			assertNotNull(user);
			assertEquals(login1, user.login);
		}
		
		//ban
		try {
			pushToSecurityContext_SYSTEM_USER();
			users.banUser(userId1);
		} finally {
			popUserFromSecurityContext();
		}
		assertNull(security.getUserFromSession(req));
		
		
		//unban
		try {
			pushToSecurityContext_SYSTEM_USER();
			users.activateUser(userId1);
		} finally {
			popUserFromSecurityContext();
		}
		{
			User user = security.getUserFromSession(req);
			assertNotNull(user);
			assertEquals(login1, user.login);
		}
		
		
		//ban and try login
		try {
			pushToSecurityContext_SYSTEM_USER();
			users.banUser(userId1);
		} finally {
			popUserFromSecurityContext();
		}
		assertNull(security.getUserFromSession(req));
		req.clearSessionAttrs();
		try {
			security.createUserSession(req, resp, loginReq);
			fail_exception_expected();
		}catch (BannedUserException e) {
			//ok
		}
		assertNull(security.getUserFromSession(req));
		
		
		//unban and login
		try {
			pushToSecurityContext_SYSTEM_USER();
			users.activateUser(userId1);
		} finally {
			popUserFromSecurityContext();
		}
		security.createUserSession(req, resp, loginReq);
		assertNotNull(security.getUserFromSession(req));
		
	}
	
	
	
	
	private void test_invalidLoginsCount() throws Exception {
		
		String login = "test";
		LoginUserReq data = new LoginUserReq(login, "123", false);
		String cacheKey = getInvalidLoginsKey(req, data);
		
		//init state
		assertEquals(0, security.getInvalidLoginsCount(req, data, 100));
		assertNull(cacheClient.getVal(cacheKey));
		
		//put-get
		security.setInvalidLoginsCountAsync(req, data, 3).get();
		assertEquals(3, security.getInvalidLoginsCount(req, data, 100));
		assertEquals("3",cacheClient.getVal(cacheKey));
		assertEquals(INVALID_LOGINS_CACHE_LIVETIME_MS, cacheSever.getItemLivetime(cacheKey).intValue());
		
		//remove
		security.setInvalidLoginsCountAsync(req, data, 0).get();
		assertEquals(0, security.getInvalidLoginsCount(req, data, 100));
		assertNull(cacheClient.getVal(cacheKey));
		
	}
	
	
	
	private void test_remMeToken_maxCount() throws Exception {
		
		
		int maxRemCount = security.maxRemCount();
		int deleteRemCount = security.deleteRemCount();
		for (int i = 0; i < maxRemCount + deleteRemCount; i++) {
			resetWeb();
			security.createUserSession(req, resp, new LoginUserReq(mail4, psw4, true));
			asyncFutures.get(0).get();
		}
		
		List<RemToken> stored = db.universal.select(new SelectRemTokensByUser(userId4));
		assertEquals(maxRemCount-deleteRemCount, stored.size());
		
	}



	private void test_user_psw_secure() throws Exception {
		
		UserExt stored = db.universal.selectOne(new SelectUserById(userId1));
		assertEquals(stored.pswHash, WebUtil.getHash(psw1, stored.pswSalt));
		
	}



	private void test_remMeToken_secure() throws Exception {
		
		//login	
		resetWeb();
		security.createUserSession(req, resp, new LoginUserReq(mail2, psw2, true));
		asyncFutures.get(0).get();
		ClientRemToken clientToken = ClientRemToken.decodeFromCookieVal(resp.cookies.get(0).getValue());
		
		//check db secure
		List<RemToken> stored = db.universal.select(new SelectRemTokensByUser(userId2));
		RemToken storedToken = stored.get(0);
		assertEquals(storedToken.tokenHash, WebUtil.getHash(clientToken.random, storedToken.tokenSalt));
		
	}







	private void test_create_without_remMe() throws Exception {
		
		resetWeb();
		
		//login		
		security.createUserSession(req, resp, new LoginUserReq(mail1, psw1, true));
		asyncFutures.get(0).get();
		
		//clear session and login again with FALSE flag
		req.clearSessionAttrs();
		req.setCookies(resp);
		resp.clearCookies();
		security.createUserSession(req, resp, new LoginUserReq(mail1, psw1, false));
		assertEmptyRemCookie();
		
	}



	private void test_logout_remMe() throws Exception {
		
		resetWeb();
		
		
		//login		
		security.createUserSession(req, resp, new LoginUserReq(mail1, psw1, true));
		asyncFutures.get(0).get();
		
		assertNotNull(req.session.getAttribute(SESSION_OBJ_KEY));
		Cookie cookie1 = resp.cookies.get(0);
		
		//logout with no cookie in req
		req.clearCookies();
		resp.clearCookies();
		security.logout(req, resp);
		assertNull(req.session.getAttribute(SESSION_OBJ_KEY));
		assertEmptyRemCookie();
		
		
		//restore
		req.setCookies(cookie1);
		security.restoreUserSession(req, resp);
		assertNotNull(req.session.getAttribute(SESSION_OBJ_KEY));
		
		
		//logout with cookie in req
		req.setCookies(cookie1);
		resp.clearCookies();
		security.logout(req, resp);
		assertNull(req.session.getAttribute(SESSION_OBJ_KEY));
		assertEmptyRemCookie();
		
		
		
	}






	private void test_create_restore_remMe() throws Exception {
		
		resetWeb();
		
		//try restore with no cookie
		assertNull(security.restoreUserSession(req, resp));
		
		//first login
		asyncFutures.clear();
		security.createUserSession(req, resp, new LoginUserReq(mail1, psw1, true));
		asyncFutures.get(0).get();
		
		assertNotNull(req.session.getAttribute(SESSION_OBJ_KEY));
		assertEquals(1, resp.cookies.size());
		Cookie cookie1 = resp.cookies.get(0);
		assertEquals(REM_TOKEN, cookie1.getName());
		
		//try restore with full session and NO cookie
		assertNull(security.restoreUserSession(req, resp));
		
		
		//try restore with empty session and NO cookie
		req.clearSessionAttrs();
		assertNull(security.restoreUserSession(req, resp));
		
		
		//restore session with cookie
		asyncFutures.clear();
		req.clearSessionAttrs();
		req.setCookies(cookie1);
		resp.clearCookies();
		assertNotNull(security.restoreUserSession(req, resp));
		assertNotNull(req.session.getAttribute(SESSION_OBJ_KEY));
		asyncFutures.get(0).get();
		
		//after restored it has new cookie
		assertEquals(1, resp.cookies.size());
		Cookie cookie_AfterResore = resp.cookies.get(0);
		
		//try restore with old cookie
		req.clearSessionAttrs();
		req.clearCookies();
		req.setCookies(cookie1);
		resp.clearCookies();
		assertNull(security.restoreUserSession(req, resp));
		
		//restore with new cookie
		req.clearCookies();
		req.setCookies(cookie_AfterResore);
		resp.clearCookies();
		assertNotNull(security.restoreUserSession(req, resp));
		asyncFutures.get(0).get();
		
		
		cookie1 = resp.cookies.get(0);
		
		
		//create agian with exists session
		assertNotNull(req.session.getAttribute(SESSION_OBJ_KEY));
		try {
			security.createUserSession(req, resp, new LoginUserReq(login1, psw1, true));
			fail_exception_expected();
		}catch (UserSessionAlreadyExistsException e) {
			//ok
		}
		
		//create again with clear inputs
		//it will be TWO tokens in DB
		asyncFutures.clear();
		req.clearSessionAttrs();
		req.clearCookies();
		resp.clearCookies();
		security.createUserSession(req, resp, new LoginUserReq(mail1, psw1, true));
		asyncFutures.get(0).get();
		
		Cookie cookie2 = resp.cookies.get(0);
		assertFalse(cookie1.getValue().equals(cookie2.getValue()));
		//	try to restore with old cookie
		req.clearSessionAttrs();
		req.setCookies(cookie1);
		assertNotNull(security.restoreUserSession(req, resp));
		//	restore with new cookie
		req.setCookies(cookie2);
		req.clearSessionAttrs();
		assertNotNull(security.restoreUserSession(req, resp));
	}



	private void test_user_update() throws Exception {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
		
			//correct
			String newMail = "new@mail.com";
			String newLogin = "'; and 1=1; newUser";
			String newPsw = "newPsw";
			users.updateUser(userId1, psw1, new UpdateUserReq(newMail, newLogin, newPsw));
			
			User updated = users.getUserById(userId1);
			assertEquals(newMail, updated.email);
			assertEquals(newLogin, updated.login);
			assertNotNull(users.checkEmailOrLoginAndPsw(newMail, newPsw));
			
			users.updateUser(userId1, newPsw, new UpdateUserReq(mail1, login1, psw1));
			updated = users.getUserById(userId1);
			assertEquals(mail1, updated.email);
			assertEquals(login1, updated.login);
			assertNotNull(users.checkEmailOrLoginAndPsw(mail1, psw1));
			
			
			//wrong psw
			try {
				users.updateUser(userId1, null, new UpdateUserReq(newMail, newLogin, newPsw));
				fail_exception_expected();
			}catch (ValidationException e) {
				//ok
			}
			try {
				users.updateUser(userId1, psw2, new UpdateUserReq(newMail, newLogin, newPsw));
				fail_exception_expected();
			}catch (InvalidLoginDataForUpdateException e) {
				//ok
			}
			
			//wrong login
			try {
				users.updateUser(userId1, psw1, new UpdateUserReq(newMail, User.INVALID_LOGIN_CHARS, newPsw));
				fail_exception_expected();
			}catch (ValidationException e) {
				//ok
			}
			
			//wrong email
			try {
				users.updateUser(userId1, psw1, new UpdateUserReq("wrongMail", newLogin, newPsw));
				fail_exception_expected();
			}catch (ValidationException e) {
				//ok
			}
		
		} finally {
			popUserFromSecurityContext();
		}
	}



	private void test_user_change_psw() throws Exception {
		
		mailSender.tasks.clear();
		
		//for exists
		assertNotNull(users.checkEmailOrLoginAndPsw(mail3, psw3));
		assertNotNull(users.checkEmailOrLoginAndPsw(login3, psw3));
		users.generateNewPassword(mail3);
		assertNull(users.checkEmailOrLoginAndPsw(mail3, psw3));
		assertNull(users.checkEmailOrLoginAndPsw(login3, psw3));
		
		RestorePswEmailParser fromEmail = new RestorePswEmailParser(mailSender.tasks.get(0).msg.text);
		assertEquals(login3, fromEmail.login);
		assertNotNull(users.checkEmailOrLoginAndPsw(mail3, fromEmail.psw));
		
		//by login
		users.generateNewPassword(login3);
		assertEquals(1, mailSender.tasks.size());
		
		//by unknown
		users.generateNewPassword("unknown data");
		assertEquals(1, mailSender.tasks.size());
		
		
	}



	private void test_user_send_activation_email_again() throws Exception {
		
		try {
			pushToSecurityContext_SYSTEM_USER();
			
			users.sendActivationEmailAgain(mail4+"123");
			assertEquals(4, mailSender.tasks.size());
			
			users.sendActivationEmailAgain(mail3);
			assertEquals(4, mailSender.tasks.size());
			
			props.putVal(users_expiredTime, -1);
			try {
				users.sendActivationEmailAgain(mail4);
				fail_exception_expected();
			}catch (UserActivationExpiredException e) {
				//ok
			}
			props.removeVal(users_expiredTime);
			
			users.sendActivationEmailAgain(mail4);
			assertEquals(5, mailSender.tasks.size());
			ActivationEmailParser fromEmail = new ActivationEmailParser(mailSender.tasks.get(4).msg.text);
			users.activateUser(fromEmail.email, fromEmail.code);
			assertNotNull(users.checkEmailOrLoginAndPsw(mail4, psw4));
		}finally {
			popUserFromSecurityContext();
		}
	}



	private void test_user_invalid_activation_states() throws Exception {
		
		ActivationEmailParser fromEmail = new ActivationEmailParser(mailSender.tasks.get(2).msg.text);
		users.activateUser(fromEmail.email+"123", fromEmail.code);
		checkNotActiveUser(users, login3, mail3, psw3);
		try {
			users.activateUser(fromEmail.email, fromEmail.code+"123");
			fail_exception_expected();
		}catch (InvalidUserActivationCodeException e) {
			//ok
		}
		props.putVal(users_expiredTime, -1);
		try {
			users.activateUser(fromEmail.email, fromEmail.code);
			fail_exception_expected();
		}catch (UserActivationExpiredException e) {
			//ok
		}
		props.removeVal(users_expiredTime);
		users.activateUser(fromEmail.email, fromEmail.code);
		assertNotNull(users.checkEmailOrLoginAndPsw(mail3, psw3));

	}



	private void test_user_activate_from_email_text() throws Exception {
		

		checkNotActiveUser(users, login2, mail2, psw2);
		ActivationEmailParser fromEmail = new ActivationEmailParser(mailSender.tasks.get(1).msg.text);
		users.activateUser(fromEmail.email, fromEmail.code);
		assertNotNull(users.checkEmailOrLoginAndPsw(mail2, psw2));
		assertNotNull(users.checkEmailOrLoginAndPsw(login2, psw2));
		
		//second activation
		users.activateUser(fromEmail.email, fromEmail.code);
		
	}



	private void test_user_unban() throws Exception {
		try {
			pushToSecurityContext_SYSTEM_USER();
			users.activateUser(userId1);
			assertNotNull(users.checkEmailOrLoginAndPsw(mail1, psw1));
		}finally {
			popUserFromSecurityContext();
		}
	}



	private void test_user_ban() throws Exception {
		
		try {
			pushToSecurityContext_SYSTEM_USER();
			users.banUser(userId1);
			try {
				users.checkEmailOrLoginAndPsw(mail1, psw1);
				fail_exception_expected();
			}catch (BannedUserException e) {
				//ok
			}
			try {
				users.checkEmailOrLoginAndPsw(login1, psw1);
				fail_exception_expected();
			}catch (BannedUserException e) {
				//ok
			}
			
			//try ban ADMIN user
			try {
				users.banUser(100);
				fail_exception_expected();
			}catch (UnmodifiableAdminUserException e) {
				//ok
			}
		}finally {
			popUserFromSecurityContext();
		}
	}



	private void test_user_activate() throws Exception {
		test_user_unban();
		assertNull(users.checkEmailOrLoginAndPsw(mail1, psw2));
		assertNull(users.checkEmailOrLoginAndPsw(login1, psw2));
	}



	private void test_user_unexists() throws Exception {
		assertNull(users.checkEmailOrLoginAndPsw("unknown", "unknown"));
		checkNotActiveUser(users, login1, mail1, psw2);
	}



	private void test_user_invalid_inputs() throws Exception {
		createUserWithInvalidData(mail1, "login@", psw1, users);
		createUserWithInvalidData(mail1, "login<", psw1, users);
		createUserWithInvalidData(mail1, "login&", psw1, users);
		createUserWithInvalidData(mail1, "login#", psw1, users);
		createUserWithInvalidData("new-"+login2, "invalid-email", psw1, users);
	}



	private void test_user_duplicates() throws Exception {
		try {
			users.createUser(new User(login1, "new-"+mail1), psw1);
			fail_exception_expected();
		}catch (DuplicateUserDataException e) {
			//ok
		}
		try {
			users.createUser(new User("new-"+login1, mail1), psw1);
			fail_exception_expected();
		}catch (DuplicateUserDataException e) {
			//ok
		}
	}



	private void test_user_get() throws Exception {
		User user1 = users.getUserByLogin(login1);
		assertEquals(NEW, user1.getStatus());
		assertEquals(login1, user1.login);
		assertEquals(mail1, user1.email);
		
		User user2 = users.getUserByLogin(login1);
		assertEquals(NEW, user2.getStatus());
		assertEquals(login1, user2.login);
		assertEquals(mail1, user2.email);
	}
	
	
	private void test_user_create() throws Exception {
		
		int firstId = 104;
		
		mailSender.tasks.clear();
		
		userId1 = users.createUser(new User(login1, mail1), psw1);
		userId2 = users.createUser(new User(login2, mail2), psw2);
		userId3 = users.createUser(new User(login3, mail3), psw3);
		userId4 = users.createUser(new User(login4, mail4), psw4);
		assertEquals(firstId, userId1);
		assertEquals(firstId+1, userId2);
		
		//mail sended
		assertEquals(4, mailSender.tasks.size());
		
		
	}
	
	
	private void createUserWithInvalidData(String login, String mail, String psw, UserService users) throws Exception {
		try {
			users.createUser(new User(login, mail), psw);
			fail_exception_expected();
		}catch (ValidationException e) {
			//ok
		}
	}
	
	
	private void resetWeb() {
		asyncFutures.clear();
		req.clearCookies();
		req.clearSessionAttrs();
		resp.clearCookies();
	}
	
	private void assertEmptyRemCookie() {
		
		boolean foundEmptyRemCookie = false;
		
		for(Cookie cookie : resp.cookies){
			if(cookie.getName().equals(REM_TOKEN)){
				assertEquals("", cookie.getValue());
				assertEquals(0, cookie.getMaxAge());
				foundEmptyRemCookie = true;
				break;
			}
		}
		
		assertTrue(String.valueOf(resp.cookies), foundEmptyRemCookie);
	}
	
	
	private void checkNotActiveUser(UserService users, String login, String email, String psw) throws Exception {
		try {
			users.checkEmailOrLoginAndPsw(email, psw);
			fail_exception_expected();
		}catch (NotActivatedUserException e) {
			//ok
		}
		
		try {
			users.checkEmailOrLoginAndPsw(login, psw);
			fail_exception_expected();
		}catch (NotActivatedUserException e) {
			//ok
		}
	}
	

	
	
	private void test_db_getServers() throws Exception {
		
		long serverId = -1;
		//init
		{
			List<ServerRow> servers = universal.select(new GetAllServers());
			assertEquals(2, servers.size());
			assertFalse(servers.get(0).isFull);
			assertFalse(servers.get(1).isFull);
			
			serverId = servers.get(1).id;
		}
		
		//update
		{
			universal.updateOne(new UpdateServerById(serverId, new IsFull(true)));
			List<ServerRow> servers = universal.select(new GetAllServers());
			assertFalse(servers.get(0).isFull);
			assertTrue(servers.get(1).isFull);
		}
		
		//revert to init
		{
			universal.updateOne(new UpdateServerById(serverId, new IsFull(false)));
			List<ServerRow> servers = universal.select(new GetAllServers());
			assertFalse(servers.get(0).isFull);
			assertFalse(servers.get(1).isFull);
		}
		
	}
	

	private void test_init_db() throws Exception {
		
		//init db check
		{
			List<ServerRow> servers = universal.select(new GetAllServers());
			assertEquals(2, servers.size());
			assertEquals(server1HttpUrl, servers.get(0).httpUrl);
			assertEquals(server1HttpsUrl, servers.get(0).httpsUrl);
			assertEquals(server2HttpUrl, servers.get(1).httpUrl);
			assertEquals(server2HttpsUrl, servers.get(1).httpsUrl);
			
			
			List<ChatAccount> chatsList = universal.select(new GetAllChatAccounts());
			assertEquals(2, chatsList.size());
			assertEquals("demo", chatsList.get(0).uid);
			assertEquals(1, chatsList.get(0).serverId);
			assertNotNull(chatsList.get(0).tariffStart);
			assertEquals(0, chatsList.get(0).tariffChangedInDay);
			
			assertEquals("demo2", chatsList.get(1).uid);
			assertEquals(1, chatsList.get(1).serverId);
			
			//operators
			assertEquals(set(CHAT_OWNER, CHAT_OPERATOR), chats.getAccPrivilegesForUser(chatsList.get(0).id, 100L));
			assertEquals(set(CHAT_OPERATOR), chats.getAccPrivilegesForUser(chatsList.get(0).id, 101L));
		}
		
		
		//check model
		{
			ServerRow server = chats.getServerByAcc("demo");
			assertNotNull(server);
			assertEquals(server1HttpUrl, server.httpUrl);
			assertEquals(server1HttpsUrl, server.httpsUrl);
		}
	}

}
