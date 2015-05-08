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
package och;

import static java.util.Collections.*;
import static och.api.model.PropKey.*;
import static och.api.model.RemoteChats.*;
import static och.api.model.chat.account.PrivilegeType.*;
import static och.api.model.tariff.Tariff.*;
import static och.api.model.tariff.TariffMath.*;
import static och.api.model.user.SecurityContext.*;
import static och.comp.ops.BillingOps.*;
import static och.comp.paypal.PaypalClientStub.*;
import static och.comp.web.JsonOps.*;
import static och.front.service.ChatService.*;
import static och.front.service.FrontApp.*;
import static och.util.ConcurrentUtil.*;
import static och.util.DateUtil.*;
import static och.util.NetUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import javax.servlet.annotation.WebServlet;

import och.api.model.chat.ChatOperator;
import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.chat.config.Key;
import och.api.model.server.ServerRow;
import och.api.model.user.LoginUserReq;
import och.api.model.user.UpdateUserReq;
import och.api.model.user.User;
import och.api.remote.chats.InitUserTokenReq;
import och.api.remote.chats.UpdateUserSessionsReq;
import och.chat.service.ChatsApp;
import och.chat.web.ChatsAppProvider;
import och.chat.web.servlet.remote.chat.CreateAccount;
import och.chat.web.servlet.remote.chat.GetPausedState;
import och.chat.web.servlet.remote.chat.GetUnblockedAccs;
import och.chat.web.servlet.remote.chat.PutAccConfig;
import och.chat.web.servlet.remote.chat.PutOperator;
import och.chat.web.servlet.remote.chat.RemoveOperator;
import och.chat.web.servlet.remote.chat.SetAccsBlocked;
import och.chat.web.servlet.remote.chat.SetAccsPaused;
import och.chat.web.servlet.remote.chat.UpdateUserContact;
import och.chat.web.servlet.remote.user.InitUserToken;
import och.chat.web.servlet.remote.user.RemoveUserSession;
import och.chat.web.servlet.remote.user.UpdateUserSessions;
import och.comp.billing.standalone.BillingSyncService;
import och.comp.cache.Cache;
import och.comp.cache.impl.CacheImpl;
import och.comp.cache.server.CacheServerContext;
import och.comp.cache.server.CacheSever;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.MainDb;
import och.comp.db.main.table._f.TariffLastPay;
import och.comp.db.main.table._f.TariffStart;
import och.comp.db.main.table.billing.GetAllBlockedUsers;
import och.comp.db.main.table.billing.UpdateUserBalanceUnsafe;
import och.comp.db.main.table.chat.UpdateAllChatAccounts;
import och.comp.db.main.table.chat.UpdateChatAccountByUid;
import och.comp.mail.stub.MailServiceStub;
import och.comp.mail.stub.SenderStub;
import och.comp.ops.BillingOps;
import och.comp.web.JsonOps;
import och.front.service.BillingService;
import och.front.service.ChatService;
import och.front.service.FrontApp;
import och.front.service.FrontAppTest;
import och.front.service.SecurityService;
import och.front.web.FrontAppProvider;
import och.front.web.servlet.api.GetStatus;
import och.front.web.servlet.remote.ReloadChatsModel;
import och.service.props.impl.MapProps;
import och.util.concurrent.AsyncListener;
import och.util.model.Pair;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.BaseTest;
import web.MockHttpServletRequest;

import com.google.gson.JsonObject;

public class FrontAndChats_IntegrationTest extends BaseTest {
	
	
	private Server chatsServer1;
	private int chatsPort1 = 30280;
	private String chatsUrl1 = "http://127.0.0.1:"+chatsPort1;
	File accountsDir1;
	MapProps chatProps1;
	
	private Server chatsServer2;
	private int chatsPort2 = 30281;
	private String chatsUrl2 = "http://127.0.0.1:"+chatsPort2;
	File accountsDir2;
	MapProps chatProps2;
	
	
	private Server frontServer1;
	private int frontPort1 = 30282;
	private String frontUrl1 = "http://127.0.0.1:"+frontPort1;
	
	private Server frontServer2;
	private int frontPort2 = 30283;
	private String frontUrl2 = "http://127.0.0.1:"+frontPort2;
	
	
	String remoteCryptKey = "test123";
	
	MapProps frontProps1;
	FrontApp frontApp1;
	CopyOnWriteArrayList<Future<?>> frontApp1Asyncs = new CopyOnWriteArrayList<>();
	
	FrontApp frontApp2;
	CopyOnWriteArrayList<Future<?>> frontApp2Asyncs = new CopyOnWriteArrayList<>();
	
	ChatsApp chatApp1;
	ChatsApp chatApp2;
	
	int cachePort = 12158;
	CacheSever cacheSever;
	String cacheSecureKey = "dd@#$#%sdfd";
	
	MapProps syncServerProps;
	SenderStub syncMailSender;
	MailServiceStub syncMailService;
	MainDb syncDb;
	
	
	
	public FrontAndChats_IntegrationTest() {
		this.createDir = true;
	}
	
	@Before
    public void startServer() throws Exception {
		
		MapProps cacheProps = new MapProps();
		FrontAppTest.putDbProps(cacheProps, TEST_DIR);
		cacheProps.putVal(db_skipDbCreation, true);
		cacheProps.putVal(mail_storeDir, "./test-out/mails-itests");
		
		cacheSever = new CacheSever(cachePort, 2, 10000, cacheSecureKey,cacheProps);
		cacheSever.runAsync();
		
		accountsDir1 = new File(TEST_DIR, "accounts1");
		accountsDir2 = new File(TEST_DIR, "accounts2");
		
		
		
		//chats1
		{
			chatProps1 = createChatProps();
			chatProps1.putVal(chatApp_id, "chats1");
			chatProps1.putVal(chats_rootDir, accountsDir1.getPath());
			ChatsAppProvider.directProps = chatProps1;
			
			chatsServer1 = new Server(chatsPort1);
			chatsServer1.setStopAtShutdown(true);
			chatsServer1.setHandler(createChatsServlets());
			chatsServer1.start();
		}
		
		
		
		
		
		//front1
		{	
			MapProps props = createFrontProps();
			props.putVal(httpServerUrl, frontUrl1);
			props.putVal(frontApp_id, "front1");
			props.putVal(db_reinit, true);
			props.putVal(paypal_clientStub, true);
			
			FrontAppProvider.directProps = props;			
			frontServer1 = new Server(frontPort1);
			frontServer1.setStopAtShutdown(true);
			frontServer1.setHandler(createFrontServlets());
			frontServer1.start();
			
			frontProps1 = props;
		}
		
		//start front1 and init db
		sendPost(frontUrl1+"/api/status");
		chatApp1 = ChatsAppProvider.lastCreated;
		frontApp1 = FrontAppProvider.lastCreated;
		frontApp1.addAsyncListener(new AsyncListener() {
			@Override
			public void onFutureEvent(Future<?> future) {
				frontApp1Asyncs.add(future);
			}
		});
		
		
		
		//front2
		{
			
			MapProps props = createFrontProps();
			props.putVal(httpServerUrl, frontUrl2);
			props.putVal(frontApp_id, "front2");
			props.putVal(db_reinit, false);
			props.putVal(paypal_clientStub, true);
			
			FrontAppProvider.directProps = props;	
			frontServer2 = new Server(frontPort2);
			frontServer2.setStopAtShutdown(true);
			frontServer2.setHandler(createFrontServlets());
			frontServer2.start();
		}
		//start front2
		sendPost(frontUrl2+"/api/status");
		frontApp2 = FrontAppProvider.lastCreated;
		frontApp2.addAsyncListener(new AsyncListener() {
			@Override
			public void onFutureEvent(Future<?> future) {
				frontApp2Asyncs.add(future);
			}
		});
		
		
		//chats2
		{
			chatProps2 = createChatProps();
			chatProps2.putVal(chatApp_id, "chats2");
			chatProps2.putVal(chats_rootDir, accountsDir2.getPath());
			ChatsAppProvider.directProps = chatProps2;
			
			chatsServer2 = new Server(chatsPort2);
			chatsServer2.setStopAtShutdown(true);
			chatsServer2.setHandler(createChatsServlets());
			chatsServer2.start();
		}
		//start chats2
		sendPost(chatsUrl2+URL_CHAT_CREATE_ACC);
		chatApp2 = ChatsAppProvider.lastCreated;
		
		
		syncServerProps = new MapProps(frontProps1.toMap());
		syncServerProps.putVal(db_skipDbCreation, true);
		syncServerProps.putVal(billing_sync_debug_DisableTimer, true);
		syncServerProps.putVal(billing_sync_fillBlockedCacheOnStartDelay, 0);
		syncServerProps.putVal(billing_sync_lastSyncStore, false);
		
		syncMailSender = new SenderStub();
		syncMailService = new MailServiceStub(syncMailSender, syncServerProps);
		
		syncDb = new MainDb(MainDb.createDataSource(syncServerProps), syncServerProps);

    }



	public MapProps createFrontProps() throws Exception {
		MapProps props = FrontAppTest.baseFrontProps(TEST_DIR);
		props.putVal(remote_encyptedKey, remoteCryptKey);
		props.putVal(chats_server_init_urls, chatsUrl1+" "+toHttps(chatsUrl1));
		props.putVal(frontServerUrls, collectionToStr(list(frontUrl1,frontUrl2), ' '));
		props.putVal(cache_remote_port, cachePort);
		props.putVal(mail_storeDir, "./test-out/mails-itests");
		props.putVal(cache_encyptedKey, cacheSecureKey);
		return props;
	}
	
	public MapProps createChatProps() {
		MapProps props = new MapProps();
		props.putVal(remote_encyptedKey, remoteCryptKey);
		props.putVal(templates_path, "./server-chat/web/WEB-INF/templates");
		return props;
	}
	
	

	public static ServletHandler createChatsServlets() {
		ServletHandler sh = new ServletHandler();
		addServletWithMapping(sh, CreateAccount.class);
		addServletWithMapping(sh, PutOperator.class);
		addServletWithMapping(sh, RemoveOperator.class);
		addServletWithMapping(sh, InitUserToken.class);
		addServletWithMapping(sh, RemoveUserSession.class);
		addServletWithMapping(sh, UpdateUserSessions.class);
		addServletWithMapping(sh, SetAccsBlocked.class);
		addServletWithMapping(sh, GetUnblockedAccs.class);
		addServletWithMapping(sh, SetAccsPaused.class);
		addServletWithMapping(sh, GetPausedState.class);
		addServletWithMapping(sh, UpdateUserContact.class);
		addServletWithMapping(sh, PutAccConfig.class);
		return sh;
	}

	public static ServletHandler createFrontServlets() {
		ServletHandler sh = new ServletHandler();
		addServletWithMapping(sh, GetStatus.class);
		addServletWithMapping(sh, ReloadChatsModel.class);
		return sh;
	}
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void addServletWithMapping(ServletHandler sh, Class type){
		WebServlet annotation = (WebServlet)type.getAnnotation(WebServlet.class);
		String[] paths = annotation.value();
		for (String path : paths) {
			sh.addServletWithMapping(type, path);			
		}
	}
	
	
    @After
    public void shutdownServer() throws Exception {
    	FrontAppProvider.directProps = null;
    	frontServer1.stop();
    }
    
    
    
    
    
    
	
	@Test
	public void test_all() throws Exception {
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			//chats
			test_chats_Exists_in_chat_server_after_front_init();
			test_chats_Get_status_api();
			test_chats_Fronts_models_sync_by_cache();
			test_chats_InitUserSessions();
			test_chats_SendUpdateSessionsReq();
			test_chats_CreateAcc_byUser();
			test_chats_putAccConfig_byUser();
			test_chats_ChangeUserPrivs();
			test_chats_ChangeUserNickname();
			test_chats_ChangeOperatorEmail();
			
			//tariffs
			test_tariffs_changeSync();
			
			//pause sync
			test_chats_pauseSync();
			
			//blocks sync
			test_chats_blockedSync();
			
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	
	private void test_chats_ChangeOperatorEmail() throws Exception {
		
		CopyOnWriteArrayList<Future<?>> asyncFutures = new CopyOnWriteArrayList<>();
		frontApp1.addAsyncListener((f) -> asyncFutures.add(f));
		
		long userId = 100;
		
		String accUid = "demo";
		ChatAccount acc = frontApp1.chats.getAccByUid(accUid, true);
		ChatsApp chatApp = acc.server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
		ChatOperator initOp = chatApp.chats.getOperator(accUid, userId);
		String initEmail = initOp.email;
		assertNotNull(initEmail);
		
		String psw = "admin";
		String newEmail = "someNew@system";
		frontApp1.users.updateUser(userId, psw, new UpdateUserReq(newEmail, null, null));
		
		//check chats
		{
			ChatOperator op = chatApp.chats.getOperator(accUid, userId);
			assertEquals(newEmail, op.email);
		}
		
	}
	
	
	private void test_chats_ChangeUserNickname() throws Exception {
		
		ChatService chats1 = frontApp1.chats;
		ChatService chats2 = frontApp2.chats;
		
		CopyOnWriteArrayList<Future<?>> asyncFutures = new CopyOnWriteArrayList<>();
		frontApp1.addAsyncListener((f) -> asyncFutures.add(f));
		
		long userId = 100;
		String nick = "test ChangeUserNickname";
		String accUid = "demo";
		
		ChatAccount acc = chats1.getAccByUid(accUid, true);
		ChatsApp chatApp = acc.server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
		ChatOperator initOp = chatApp.chats.getOperator(accUid, userId);
		String initEmail = initOp.email;
		assertNotNull(initEmail);
		
		chats1.setNickname(accUid, userId, nick);
		assertTrue(asyncFutures.size() > 0);
		lastFrom(asyncFutures).get();
		
		//check front 2
		{
			assertEquals(nick, chats2.getAccOperators(accUid).get(userId).nickname);
		}
		
		//check chats
		{
			ChatOperator op = chatApp.chats.getOperator(accUid, userId);
			assertEquals(nick, op.name);
			assertEquals(initEmail, op.email);
		}
		
	}
	
	
	private void test_chats_blockedSync() throws Exception {
		
		ChatService frontChats1 = frontApp1.chats;
		BillingService billing1 = frontApp1.billing;
		BillingService billing2 = frontApp2.billing;
		
		UniversalQueries universal = syncDb.universal;
		
		BillingSyncService billingSync = new BillingSyncService();
		billingSync.setCacheServerContext(new CacheServerContext(syncServerProps, cacheSever, syncDb, syncMailService));
		billingSync.init();
		
		ArrayList<Pair<Long, Boolean>> blockReqs = new ArrayList<>();
		BillingOps.SEND_ACCS_BLOCKED_LISTENER = (ownerId, val) -> blockReqs.add(new Pair<Long, Boolean>(ownerId, val));
		
		
		long userId = 100;
		int tariffId = 2;
		String accUid = "blockedSync";
		
		List<String> oldAccs = syncDb.chats.getOwnerAccs(userId);
		assertTrue(oldAccs.size() > 0);
		
		//Отключаем все другие акки от апдейта
		Date longFuture = parseStandartDateTime("02.09.2040 3:00:00");
		universal.update(new UpdateAllChatAccounts(new TariffStart(longFuture), new TariffLastPay(longFuture)));
		
		//create acc
		frontChats1.createAcc(1, accUid, userId, "test_monthBill", tariffId);
		frontChats1.setOperatorForAcc(accUid, userId);
		assertEquals(1, frontChats1.getAccOperators(accUid).size());
		
		
		//ушли в минус - заблокированы все акки
		{
			correctBalance(syncDb, userId, new BigDecimal(4.99d));
			assertEquals("4.99", frontApp1.billing.getUserBalance(userId).toString());
			
			BigDecimal initBalance = billing1.getUserBalance(userId);
			String expAmount = "-5.00";
			assertFalse(findBalance(universal, userId).accsBlocked);
			
			Date pastPay = parseStandartDateTime("01.08.2014 00:00:00");
			Date now = parseStandartDateTime("02.09.2014 3:00:00");
			universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
			assertEquals(1, billingSync.doSyncWork(false, now));
			assertEquals(expAmount, getDeltaVal(userId, initBalance));
			
			//заблокировано в бд
			assertEquals("-0.01", billing1.getUserBalance(userId).toString());
			assertEquals("-0.01", billing2.getUserBalance(userId).toString());
			assertTrue(findBalance(universal, userId).accsBlocked);
			
			
			List<ChatAccount> ownerAccs = syncDb.chats.getOwnerAccsInfo(userId);
			
			//все акки заблокированы
			HashSet<String> serverUrls = new HashSet<>();
			for (ChatAccount acc : ownerAccs) {
				ChatsApp chatApp = acc.server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
				assertEquals(true, chatApp.chats.isAccBlocked(acc.uid));
				serverUrls.add(acc.server.httpUrl);
			}
			//тест проводился на разных серверах
			assertTrue(serverUrls.size() > 1);
			
			
			//в кеше есть флаги блока для каждого акка
			for(ChatAccount acc : ownerAccs){
				assertEquals(true, BillingOps.isAccBlockedFromCache(cacheSever, acc.uid));
			}
			
			
			//явно разблокировываем акки на серверах, чтобы проверить синхронизацию блокировок
			for (ChatAccount acc : ownerAccs) {
				ChatsApp chatApp = acc.server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
				chatApp.chats.setAccBlocked(acc.uid, false);
				assertEquals(false, chatApp.chats.isAccBlocked(acc.uid));
			}
			//запускаем синхронизацию
			{
				Cache newCache = new CacheImpl(0);
				BillingSyncService otherBillingSync = new BillingSyncService();
				otherBillingSync.setCacheServerContext(new CacheServerContext(syncServerProps, newCache, syncDb, syncMailService));
				otherBillingSync.init();
			}
			//проверяем блокировки после синхронизации
			for (ChatAccount acc : ownerAccs) {
				ChatsApp chatApp = acc.server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
				assertEquals(true, chatApp.chats.isAccBlocked(acc.uid));
			}
			//в кеше тоже все норм
			for(ChatAccount acc : ownerAccs){
				assertEquals(true, BillingOps.isAccBlockedFromCache(cacheSever, acc.uid));
			}
			
		}
		
		
		//пользователь закинул деньги спустя неделю в ноль и его разблокировали
		{
			Date now = parseStandartDateTime("12.09.2014 15:45:00");
			pushToSecurityContext(new User(userId));
			try {
				
				BillingService.PayConfirmCacheData data = new BillingService.PayConfirmCacheData(STUB_TOKEN, defPayAmount, ""+userId);
				cacheSever.putVal("pay-confirm-"+userId, toJson(data));
				billing2.paypal_finishPayment(now);
			} finally {
				popUserFromSecurityContext();
			}
			assertEquals("11.99", billing2.getUserBalance(userId).toString());
			assertEquals("11.99", billing1.getUserBalance(userId).toString());
			assertFalse(findBalance(universal, userId).accsBlocked);
			
			//все акки разблокированы
			for (ChatAccount acc : syncDb.chats.getOwnerAccsInfo(userId)) {
				ChatsApp chatApp = acc.server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
				assertFalse(chatApp.chats.isAccBlocked(acc.uid));
			}
			
			//в кеше нет флагов блока для каждого акка
			for(String uid : syncDb.chats.getOwnerAccs(userId)){
				assertEquals(false, BillingOps.isAccBlockedFromCache(cacheSever, uid));
			}
		}
		
		
		//снова заблокировали акк
		{
			correctBalance(syncDb, userId, new BigDecimal(4.99d));
			assertEquals("4.99", frontApp1.billing.getUserBalance(userId).toString());
			assertFalse(findBalance(universal, userId).accsBlocked);
			
			Date pastPay = parseStandartDateTime("01.08.2014 00:00:00");
			Date now = parseStandartDateTime("02.09.2014 3:00:00");
			universal.update(new UpdateChatAccountByUid(accUid, new TariffStart(pastPay), new TariffLastPay(pastPay)));
			assertEquals(1, billingSync.doSyncWork(false, now));
			
			//заблокировано в бд
			assertTrue(findBalance(universal, userId).accsBlocked);
			
			
			//все акки заблокированы
			for (ChatAccount acc : syncDb.chats.getOwnerAccsInfo(userId)) {
				ChatsApp chatApp = acc.server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
				assertTrue(chatApp.chats.isAccBlocked(acc.uid));
			}
		}
		
		
		//проверка заблокированных чатов
		{
			assertEquals(1, syncDb.universal.select(new GetAllBlockedUsers()).size());
			
			syncMailSender.tasks.clear();
			billingSync.checkAccBlocks();
			assertEquals(0, syncMailSender.tasks.size());
			
			
			//разблокировали чат
			chatApp1.chats.setAccBlocked(accUid, false);
			chatApp2.chats.setAccBlocked(accUid, false);
			
			
			//проверка отправила письмо админу про рассинхрон
			billingSync.checkAccBlocks();
			assertEquals(1, syncMailSender.tasks.size());
			
			
			//заблокировали -- проверка прошла нормально
			syncMailSender.tasks.clear();
			chatApp1.chats.setAccBlocked(accUid, true);
			chatApp2.chats.setAccBlocked(accUid, true);
			billingSync.checkAccBlocks();
			assertEquals(0, syncMailSender.tasks.size());
		}

		
		
		//Нормируем акки обратно
		Date now = new Date();
		universal.update(new UpdateAllChatAccounts(new TariffStart(now), new TariffLastPay(now)));
		
	}
	
	private String getDeltaVal(long userId, BigDecimal oldVal) throws Exception{
		BigDecimal curBalance = frontApp1.billing.getUserBalance(userId);
		return getDeltaVal(curBalance, oldVal);
	}
	
	private String getDeltaVal(BigDecimal newVal, BigDecimal oldVal) throws Exception{
		return round(newVal.subtract(oldVal)).toString();
	}

	
	private void correctBalance(MainDb db, long userId, BigDecimal val) throws Exception {
		db.universal.update(new UpdateUserBalanceUnsafe(userId, val));
		frontApp1.billing.updateUserBalanceCache(userId, false);
		frontApp2.billing.updateUserBalanceCache(userId, false);
	}
	
	
	
	private void test_chats_pauseSync() throws Exception {
		
		ChatService chats1 = frontApp1.chats;
		ChatService chats2 = frontApp2.chats;
		
		CopyOnWriteArrayList<Future<?>> asyncFutures = new CopyOnWriteArrayList<>();
		frontApp1.addAsyncListener((f) -> asyncFutures.add(f));
		frontApp2.addAsyncListener((f) -> asyncFutures.add(f));
		
		long ownerId = 100;
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			//init
			ChatAccount initAcc = chats1.getAccsForOperator(ownerId).get(0);
			String uid = initAcc.uid;
			long initTariff = initAcc.tariffId;
			assertNotEquals(PAUSE_TARIFF_ID, initAcc.tariffId);
			assertEquals(initTariff, chats2.getAccsForOperator(ownerId).get(0).tariffId);
			assertEquals(false, chats1.isPausedAcc(uid));
			assertEquals(false, chats2.isPausedAcc(uid));
			
			//пауза во фронте 1
			pushToSecurityContext(new User(ownerId));
			try {
				chats1.pauseAccByUser(uid);
			} finally {
				popUserFromSecurityContext();
			}
			
			//sync
			lastFrom(asyncFutures).get();
			
			//изменения подхватились во фронте 2
			assertEquals(PAUSE_TARIFF_ID, chats2.getAccByUid(uid, false).tariffId);
			assertEquals(true, chats1.isPausedAcc(uid));
			assertEquals(true, chats2.isPausedAcc(uid));
			
			//и на сервере чатов
			ServerRow server = chats1.getAccByUid(uid, true).server;
			ChatsApp chatApp = server.httpUrl.equals(chatsUrl1) ? chatApp1 : chatApp2;
			assertEquals(true, chatApp.chats.isAccPaused(uid));
			
			
			
			//тесты отправки писем админу про рассинхрон
			test_billing_checkAccPaused(chatApp, uid, true);
			
			
			
			//синхронизация билинга фиксит расхождения бд и серверов
			{
				//явно отпаузили
				chatApp.chats.setAccPaused(uid, false);
				assertEquals(false, chatApp.chats.isAccPaused(uid));
				
				//если заново стартанем синк сервер, то будет фикс
				{
					Cache newCache = new CacheImpl(0);
					BillingSyncService otherBillingSync = new BillingSyncService();
					otherBillingSync.setCacheServerContext(new CacheServerContext(syncServerProps, newCache, syncDb, syncMailService));
					otherBillingSync.init();
					assertEquals(true, chatApp.chats.isAccPaused(uid));
				}
			}
			
			
			
			
			
			//анпаузили во фронте 2
			pushToSecurityContext(new User(ownerId));
			try {
				chats2.unpauseAccByUser(uid);
			} finally {
				popUserFromSecurityContext();
			}
			
			//sync
			lastFrom(asyncFutures).get();
			
			
			//изменения подхватились повсюду
			assertEquals(false, chats1.isPausedAcc(uid));
			assertEquals(false, chats2.isPausedAcc(uid));
			assertEquals(false, chatApp.chats.isAccPaused(uid));
			
			
			//тесты отправки писем админу про рассинхрон
			test_billing_checkAccPaused(chatApp, uid, false);
			
			
			//синхронизация билинга фиксит расхождения паузы
			{
				//явно отпаузили
				chatApp.chats.setAccPaused(uid, true);
				assertEquals(true, chatApp.chats.isAccPaused(uid));
				
				//если заново стартанем синк сервер, то будет фикс
				{
					Cache newCache = new CacheImpl(0);
					BillingSyncService otherBillingSync = new BillingSyncService();
					otherBillingSync.setCacheServerContext(new CacheServerContext(syncServerProps, newCache, syncDb, syncMailService));
					otherBillingSync.init();
					assertEquals(false, chatApp.chats.isAccPaused(uid));
				}
			}
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	private void test_billing_checkAccPaused(ChatsApp chatApp, String uid, boolean expectedVal) throws Exception{
		
		Cache newCache = new CacheImpl(0);
		BillingSyncService billingSync = new BillingSyncService();
		billingSync.setCacheServerContext(new CacheServerContext(syncServerProps, newCache, syncDb, syncMailService));
		billingSync.init();
		
		//рассинхрона нет
		syncMailSender.tasks.clear();
		billingSync.checkAccPaused();
		assertEquals(0, syncMailSender.tasks.size());
		
		//рассинхрон
		assertEquals(expectedVal, chatApp.chats.isAccPaused(uid));
		chatApp.chats.setAccPaused(uid, !expectedVal);
		assertEquals(!expectedVal, chatApp.chats.isAccPaused(uid));
		
		//отправлено письмо админу
		billingSync.checkAccPaused();
		assertEquals(1, syncMailSender.tasks.size());
		
		
		//снова синхрон -- нет писем
		chatApp.chats.setAccPaused(uid, expectedVal);
		syncMailSender.tasks.clear();
		billingSync.checkAccPaused();
		assertEquals(0, syncMailSender.tasks.size());
	}
	
	
	private void test_tariffs_changeSync() throws Exception {
		
		ChatService chats1 = frontApp1.chats;
		ChatService chats2 = frontApp2.chats;
		
		CopyOnWriteArrayList<Future<?>> asyncFutures = new CopyOnWriteArrayList<>();
		frontApp1.addAsyncListener((f) -> asyncFutures.add(f));
		
		long ownerId = 100;
		long newTariffId = 2;
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			
			//init data
			ChatAccount initAcc = chats1.getAccsForOperator(ownerId).get(0);
			long initTariff = initAcc.tariffId;
			assertNotEquals(initTariff, newTariffId);
			assertEquals(null, initAcc.tariffPrevId);
			assertEquals(initTariff, chats2.getAccsForOperator(ownerId).get(0).tariffId);
			
			//app1
			chats1.updateAccTariff("demo", newTariffId, false);
			
			//sync
			lastFrom(asyncFutures).get();
			
			//app2
			List<ChatAccount> accs = chats2.getAccsForOperator(ownerId);
			ChatAccount acc = accs.get(0);
			assertEquals("demo", acc.uid);
			assertEquals(newTariffId, acc.tariffId);
			assertEquals(Long.valueOf(initTariff), acc.tariffPrevId);
			
		}finally {
			popUserFromSecurityContext();
		}
		
	}
	
	
	private void test_chats_ChangeUserPrivs() throws Exception {
		
		ChatService chats1 = frontApp1.chats;
		ChatService chats2 = frontApp2.chats;
		
		CopyOnWriteArrayList<Future<?>> asyncFutures = new CopyOnWriteArrayList<>();
		frontApp1.addAsyncListener((f) -> asyncFutures.add(f));
		
		long userId1 = 100;
		long userId2 = 101;
		User user1 = new User(userId1);
		String uid = null;
		String userEmail2 = "root@system";
		
		//create acc
		pushToSecurityContext(user1);
		try {
			uid = chats1.createAccByUser("changeUserPrivs");
		}finally {
			popUserFromSecurityContext();
		}
		lastFrom(asyncFutures).get();
		asyncFutures.clear();
		String serverUrl = chats1.getServerByAcc(uid).httpUrl;
		ChatsApp chatApp = serverUrl.equals(chatsUrl1)? chatApp1 : chatApp2;
		
		assertEquals(set(CHAT_OWNER), chats2.getAccPrivilegesForUser(uid, userId1));
		
		
		//create op
		{
			pushToSecurityContext(user1);
			try {
				chats1.addUserPrivileges(uid, userId2, set(CHAT_OPERATOR));
			}finally {
				popUserFromSecurityContext();
			}
			
			assertTrue(asyncFutures.size() > 0);
			lastFrom(asyncFutures).get();
			
			//оператор появился во чатах
			pushToSecurityContext_SYSTEM_USER();
			try {
				assertEquals(set(CHAT_OPERATOR), chats2.getAccPrivilegesForUser(uid, userId2));
				ChatOperator op = chatApp.chats.getOperator(uid, userId2);
				assertNotNull(op);
				assertEquals(userEmail2, op.email);
			}finally {
				popUserFromSecurityContext();
			}


		}
		
		//remove op
		{
			pushToSecurityContext(user1);
			try {
				chats1.removeUserPrivileges(uid, userId2, set(CHAT_OPERATOR));
			}finally {
				popUserFromSecurityContext();
			}
			
			assertTrue(asyncFutures.size() > 0);
			lastFrom(asyncFutures).get();
			
			pushToSecurityContext_SYSTEM_USER();
			try {
				assertEquals(set(), chats2.getAccPrivilegesForUser(uid, userId2));
				assertNull(chatApp.chats.getOperator(uid, userId2));
			}finally {
				popUserFromSecurityContext();
			}
		}
		
		
		
	}
	
	
	
	private void test_chats_putAccConfig_byUser() throws Exception {
		
		ChatService chats1 = frontApp1.chats;
		ChatService chats2 = frontApp2.chats;
		
		CopyOnWriteArrayList<Future<?>> asyncFutures = new CopyOnWriteArrayList<>();
		frontApp1.addAsyncListener((f) -> asyncFutures.add(f));
		
		User user = new User(100);
		String uid = null;
		ChatAccount acc;
		ChatsApp chatApp;
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			uid = chats1.getAccsForOperator(user.id).get(0).uid;
			acc = chats1.getAccByUid(uid, false);
		}finally {
			popUserFromSecurityContext();
		}
		
		//name
		{
			String newName = "newName-integrTest";
			
			//front1
			pushToSecurityContext(user);
			try {
				chats1.putAccConfigByUser(uid, Key.name, newName);
			}finally {
				popUserFromSecurityContext();
			}
			
			String serverUrl = chats1.getServerByAcc(uid).httpUrl;
			chatApp = serverUrl.equals(chatsUrl1)? chatApp1 : chatApp2;
			chatApp.chats.setAsyncListener((f) -> asyncFutures.add(f));
			
			assertTrue(asyncFutures.size() > 0);
			getAndClearAllFutures(asyncFutures);
			
			//check in front2
			assertEquals(newName, chats2.getAccsForOperator(user.id).get(0).name);
			
			//check in remote acc
			assertEquals(newName, chatApp.chats.getAccConfig(uid, Key.name));
		}
		
		
		//feedback_notifyOpsByEmail
		{
			boolean val = acc.feedback_notifyOpsByEmail;
			
			//front1
			pushToSecurityContext(user);
			try {
				chats1.putAccConfigByUser(uid, Key.feedback_notifyOpsByEmail, !val);
			}finally {
				popUserFromSecurityContext();
			}
			
			getAndClearAllFutures(asyncFutures);
			
			//check in front2
			assertEquals( ! val, chats2.getAccsForOperator(user.id).get(0).feedback_notifyOpsByEmail);
			
			//check in remote acc
			assertEquals( String.valueOf(!val), chatApp.chats.getAccConfig(uid, Key.feedback_notifyOpsByEmail));
		}
	}
	
	
	private void test_chats_CreateAcc_byUser() throws Exception {
		
		ChatService chats1 = frontApp1.chats;
		ChatService chats2 = frontApp2.chats;
		
		CopyOnWriteArrayList<Future<?>> asyncFutures = new CopyOnWriteArrayList<>();
		frontApp1.addAsyncListener((f) -> asyncFutures.add(f));
		

		User user = new User(100);
		String accName = "test_chats_CreateAcc_byUser";
		
		//create in front1
		pushToSecurityContext(user);
		try {
			chats1.createAccByUser(accName);
		}finally {
			popUserFromSecurityContext();
		}
		
		assertTrue(asyncFutures.size() > 0);
		lastFrom(asyncFutures).get();
		
		//check in front2
		List<ChatAccount> accs2 = chats2.getAccsForOperator(user.id);
		assertTrue(accs2.size() > 0);
		assertEquals(accName, lastFrom(accs2).name);
		
		
	}
	
	
	
	private void test_chats_SendUpdateSessionsReq() throws Exception {
		
		final CopyOnWriteArrayList<RemoteReqData> reqCounter = new CopyOnWriteArrayList<>();
		JsonOps.addPostEncryptedJsonListener((url, req)-> {
			if(url.endsWith(URL_CHAT_UPDATE_SESSIONS)) reqCounter.add(new RemoteReqData(url, req));
		});
		
		long serverId = 1;
		String uid = "demo"+randomSimpleId();
		ChatService chats = frontApp1.chats;
		long userId = 100;
		
		//create acc
		chats.createAcc(serverId, uid, userId, null, 1);
		assertEquals(1, reqCounter.size());
		{
			RemoteReqData data = lastFrom(reqCounter);
			assertTrue(data.url.startsWith(chatsUrl1));
			UpdateUserSessionsReq req = (UpdateUserSessionsReq) data.req;
			assertEquals(userId, req.userId);
			assertEquals(set(CHAT_OWNER), req.privilegesByAccount.get(uid));
		}
		
		//set operator
		chats.setOperatorForAcc(uid, userId);
		assertEquals(2, reqCounter.size());
		{
			RemoteReqData data = lastFrom(reqCounter);
			assertTrue(data.url.startsWith(chatsUrl1));		
			UpdateUserSessionsReq req = (UpdateUserSessionsReq) data.req;
			assertEquals(userId, req.userId);
			assertEquals(set(CHAT_OWNER, CHAT_OPERATOR), req.privilegesByAccount.get(uid));
		}
		
	}
	
	
	
	private void test_chats_InitUserSessions() throws Exception {
		
		//init token req counter
		final CopyOnWriteArrayList<String> reqCounter = new CopyOnWriteArrayList<>();
		JsonOps.addPostEncryptedJsonListener((url, req) -> {
			if(url.endsWith(URL_USER_INIT_TOKEN)) reqCounter.add(url);
		});
		
		SecurityService security = frontApp1.security;
		ChatService chats = frontApp1.chats;
		
		MockHttpServletRequest req = mockReq();
		req.setUserAgent("browserAgent-1");
		
		//session in front1
		security.createUserSession(req, mockResp(), new LoginUserReq("root", "root", false));
		
		//exception in chat2
		chatProps2.putVal(chatApp_debug_failInitToken, true);
		String token1 = chats.initUserTokenInAccServers(req);
		chatProps2.putVal(chatApp_debug_failInitToken, false);
		assertEquals(token1, security.getUserSessionAttr(req, ACC_SESSION_TOKEN));
		assertNotNull(chatApp1.security.getUserTokenLivetime(token1));
		assertNull(chatApp2.security.getUserTokenLivetime(token1));
		
		//check remote reqs
		assertEquals(1, reqCounter.size());
		assertTrue(reqCounter.get(0).startsWith(chatsUrl1));
		reqCounter.clear();
		
		
		//token in front1 and chat1, chat2
		String token2 = chats.initUserTokenInAccServers(req);
		assertEquals(token1, token2);
		assertEquals(token2, security.getUserSessionAttr(req, ACC_SESSION_TOKEN));
		//token livetime in chats
		assertNotNull(chatApp1.security.getUserTokenLivetime(token2));
		assertNotNull(chatApp2.security.getUserTokenLivetime(token2));
		//user privs in token
		{
			InitUserTokenReq tokenData1 = chatApp1.security.getUserTokenDataFromCache(token2);
			InitUserTokenReq tokenData2 = chatApp2.security.getUserTokenDataFromCache(token2);
			assertNotNull(tokenData1);
			assertNotNull(tokenData2);
			Set<PrivilegeType> privsDemo = tokenData1.privsByAcc.get("demo");
			assertEquals(set(CHAT_OPERATOR), privsDemo);
			assertEquals(privsDemo, tokenData1.privsByAcc.get("demo2"));
			assertEquals(privsDemo, tokenData1.privsByAcc.get("demo3"));
			assertEquals(tokenData1.privsByAcc, tokenData2.privsByAcc);
		}
		
		//check remote reqs
		assertEquals(1, reqCounter.size());
		assertTrue(reqCounter.get(0).startsWith(chatsUrl2));
		reqCounter.clear();
		
		
		//no reqs if alredy done
		String token3 = chats.initUserTokenInAccServers(req);
		assertEquals(token1, token3);
		assertEquals(0, reqCounter.size());
		
		
		//logout in front
		security.logout(req, mockResp());
		lastFrom(frontApp1Asyncs).get();
		assertNull(chatApp1.security.getUserTokenLivetime(token2));
		assertNull(chatApp2.security.getUserTokenLivetime(token2));
		
	}
	
	
	
	
	private void test_chats_Fronts_models_sync_by_cache() throws Exception {
		
		ChatService chats1 = frontApp1.chats;
		ChatService chats2 = frontApp2.chats;
		
		assertEquals(chats1.getServers(), chats2.getServers());
		
		//front1 -> front2
		long serverId = chats1.createServer(chatsUrl2, chatsUrl2);
		long accId = chats1.createAcc(serverId, "demo3", 100, null, 1);
		assertTrue(frontApp1Asyncs.size() > 0);
		lastFrom(frontApp1Asyncs).get();
		
		//check
		{
			List<ChatAccount> list = chats2.getServerAccs(serverId);
			assertTrue(list.size() == 1);
			assertEquals("demo3", list.get(0).uid);
		}
		
		
		//front2 -> front1
		assertEquals(emptySet(), chats1.getAccPrivilegesForUser(accId, 101));
		assertEquals(emptySet(), chats1.getAccPrivilegesForUser(accId, 101));
		
		int userId = 101;
		chats2.setOperatorForAcc("demo3", userId);
		lastFrom(frontApp2Asyncs).get();
		
		//check
		{
			assertEquals(set(CHAT_OPERATOR), chats1.getAccPrivilegesForUser(accId, 101));
			assertEquals(set(CHAT_OPERATOR), chats2.getAccPrivilegesForUser(accId, 101));
		}
		
		//has new account
		{
			ServerRow server = frontApp1.chats.getServerByAcc("demo3");
			assertNotNull(server);
			assertEquals(chatsUrl2, server.httpUrl);
		}
		{
			ServerRow server = frontApp2.chats.getServerByAcc("demo3");
			assertNotNull(server);
			assertEquals(chatsUrl2, server.httpUrl);
		}
		
		assertTrue(chatApp2.chats.getRegistredOperators("demo3").size() > 0);
		
	}
	
	
	private void test_chats_Exists_in_chat_server_after_front_init() throws Exception {
		
		assertEquals(list(DEFAULT_INIT_CHAT_ACC_1, DEFAULT_INIT_CHAT_ACC_2), chatApp1.chats.getAccIds());
		assertTrue(chatApp1.chats.getRegistredOperators(DEFAULT_INIT_CHAT_ACC_1).size() > 0);
		assertTrue(chatApp1.chats.getRegistredOperators(DEFAULT_INIT_CHAT_ACC_2).size() > 0);
		
	}
	
	

	private void test_chats_Get_status_api() throws Exception {
		
		//invalid req
		{
			JsonObject resp = parseJsonResp(sendPost(frontUrl1+"/api/status"));
			assertNotNull(resp);
			assertFalse(isOkStatus(resp));
		}
		//invalid req
		{
			String invalidAccount = DEFAULT_INIT_CHAT_ACC_1+"-no";
			JsonObject resp = parseJsonResp(sendPost(frontUrl1+"/api/status", map("data", "{id:'" + invalidAccount + "'}")));
			assertNotNull(resp);
			assertFalse(isOkStatus(resp));
		}
		//valid acc1
		{
			JsonObject resp = parseJsonResp(sendPost(frontUrl1+"/api/status", map("data", "{id:'"+DEFAULT_INIT_CHAT_ACC_1+"'}")));
			assertNotNull(resp);
			assertTrue(isOkStatus(resp));
		}
		//valid acc2
		{
			JsonObject resp = parseJsonResp(sendPost(frontUrl1+"/api/status", map("data", "{id:'"+DEFAULT_INIT_CHAT_ACC_2+"'}")));
			assertNotNull(resp);
			assertTrue(isOkStatus(resp));
		}
	}
	
	
	static class RemoteReqData {
		public String url;
		public Object req;
		public RemoteReqData(String url, Object req) {
			super();
			this.url = url;
			this.req = req;
		}
	}

	
}
