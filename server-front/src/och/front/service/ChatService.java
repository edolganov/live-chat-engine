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
import static och.api.model.RemoteChats.*;
import static och.api.model.RemoteFront.*;
import static och.api.model.billing.PaymentType.*;
import static och.api.model.chat.account.ChatAccountPrivileges.*;
import static och.api.model.chat.account.PrivilegeType.*;
import static och.api.model.tariff.Tariff.*;
import static och.api.model.tariff.TariffMath.*;
import static och.api.model.user.SecurityContext.*;
import static och.api.model.web.ReqInfo.*;
import static och.api.remote.front.ReloadChatsModelType.*;
import static och.comp.db.main.table.MainTables.*;
import static och.comp.ops.BillingOps.*;
import static och.comp.ops.ChatOps.*;
import static och.comp.ops.RemoteOps.*;
import static och.comp.ops.SecurityOps.*;
import static och.comp.web.JsonOps.*;
import static och.front.service.chat.AccConfigOps.*;
import static och.util.DateUtil.*;
import static och.util.ExceptionUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.concurrent.DoneFuture.*;
import static och.util.servlet.WebUtil.*;
import static och.util.sql.SingleTx.*;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import och.api.annotation.Secured;
import och.api.exception.ExpectedException;
import och.api.exception.InvalidInputException;
import och.api.exception.chat.AccountsLimitException;
import och.api.exception.chat.AddUserReqAlreadyExistsException;
import och.api.exception.chat.ChatAccountBlockedException;
import och.api.exception.chat.ChatAccountNotPausedException;
import och.api.exception.chat.ChatAccountPausedException;
import och.api.exception.chat.HostBlockedException;
import och.api.exception.chat.NoAvailableServerException;
import och.api.exception.chat.NoChatAccountException;
import och.api.exception.chat.UserAlreadyInAccountException;
import och.api.exception.tariff.ChangeTariffLimitException;
import och.api.exception.tariff.InvalidTariffException;
import och.api.exception.tariff.NotPublicTariffException;
import och.api.exception.tariff.OperatorsLimitException;
import och.api.exception.tariff.TariffNotFoundException;
import och.api.exception.tariff.UpdateTariffOperatorsLimitException;
import och.api.exception.user.AccessDeniedException;
import och.api.exception.user.UserNotFoundException;
import och.api.model.billing.UserBalance;
import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.ChatAccountAddReq;
import och.api.model.chat.account.ChatAccountPrivileges;
import och.api.model.chat.account.PauseAccResp;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.chat.config.Key;
import och.api.model.chat.host.ClientHost;
import och.api.model.server.ServerRow;
import och.api.model.tariff.Tariff;
import och.api.model.user.UpdateUserReq;
import och.api.model.user.User;
import och.api.model.user.UserExt;
import och.api.model.user.UserRole;
import och.api.remote.chats.CreateAccountReq;
import och.api.remote.chats.InitUserTokenReq;
import och.api.remote.chats.PutAccConfigReq;
import och.api.remote.chats.PutOperatorReq;
import och.api.remote.chats.RemoveOperatorReq;
import och.api.remote.chats.RemoveUserSessionReq;
import och.api.remote.chats.SetAccsPausedReq;
import och.api.remote.chats.UpdateUserContactReq;
import och.api.remote.chats.UpdateUserSessionsReq;
import och.api.remote.front.ReloadChatsModelReq;
import och.api.remote.front.ReloadChatsModelType;
import och.comp.cache.client.CacheClient;
import och.comp.db.base.universal.UpdateRows;
import och.comp.db.main.table._f.IsFull;
import och.comp.db.main.table._f.TariffChangedInDay;
import och.comp.db.main.table._f.TariffId;
import och.comp.db.main.table._f.TariffLastPay;
import och.comp.db.main.table._f.TariffPrevId;
import och.comp.db.main.table._f.TariffStart;
import och.comp.db.main.table.billing.SelectUserBalanceById;
import och.comp.db.main.table.chat.CreateChatAccount;
import och.comp.db.main.table.chat.GetChatAccount;
import och.comp.db.main.table.chat.UpdateChatAccountByUid;
import och.comp.db.main.table.chat.addreqs.CreateChatAccountAddReqs;
import och.comp.db.main.table.chat.addreqs.DeleteChatAccountAddReq;
import och.comp.db.main.table.chat.addreqs.GetAllChatAccountAddReqsByAcc;
import och.comp.db.main.table.chat.addreqs.GetAllChatAccountAddReqsByUser;
import och.comp.db.main.table.chat.host.CreateClientHost;
import och.comp.db.main.table.chat.host.CreateClientHostAcc;
import och.comp.db.main.table.chat.host.CreateClientHostAccOwner;
import och.comp.db.main.table.chat.host.GetClientHost;
import och.comp.db.main.table.chat.privilege.CreateChatAccountPrivileges;
import och.comp.db.main.table.chat.privilege.DeleteChatAccountPrivilege;
import och.comp.db.main.table.chat.privilege.UpdateChatAccountPrivileges;
import och.comp.db.main.table.chat.privilege.UpdateUserAccNickname;
import och.comp.db.main.table.server.CreateServer;
import och.comp.db.main.table.server.GetServerById;
import och.comp.db.main.table.server.UpdateServerById;
import och.comp.db.main.table.tariff.CreateTariff;
import och.comp.db.main.table.user.SelectUserById;
import och.front.service.chat.HostsStat;
import och.front.service.chat.ReloadOps;
import och.front.service.event.admin.UpdateModelsEvent;
import och.front.service.event.chat.ChatCreatedEvent;
import och.front.service.event.user.UserSessionDesroyedEvent;
import och.front.service.event.user.UserUpdateTxEvent;
import och.front.service.model.ChatsModel;
import och.front.service.model.UserAccInfo;
import och.front.service.model.UserSession;
import och.util.model.CallableVoid;
import och.util.model.Pair;
import och.util.servlet.WebUtil;
import och.util.sql.ConcurrentUpdateSqlException;

public class ChatService extends BaseFrontService {
	
	public static final String ACC_SESSION_TOKEN = "chat.accSessionToken";
	public static final String ACC_INITED_URLS = "chat.accInitedUrls";

	
	private volatile ChatsModel m; 
	private volatile HostsStat hostsStat; 
	
	SecurityService security;
	BillingService billing;
	Random r = new Random();
	CacheClient cache;
	ReloadOps reloadOps; 

	public ChatService(FrontAppContext c) {
		super(c);
	}
	
	@Override
	public void init() throws Exception {
		
		security = c.root.security;
		billing = c.root.billing;
		cache = c.cache;
		reloadOps = new ReloadOps(c);
		
		reloadModel();
		
		//events
		c.events.addListener(UserSessionDesroyedEvent.class, (event)-> 
			sendRemoveUserSessionsAsync(event.userSession));
		
		c.events.addListener(UpdateModelsEvent.class, (event) -> {
			reloadModel();
			sendUpdateOtherFrontsSignalAsync(FULL_MODEL_UPDATED);
		});
		
		c.events.addListener(UserUpdateTxEvent.class, (event)-> 
			updateRemoteContactsOnUserUpdateTx(event));
		
		
		//timer ops
		if(props.getBoolVal(chats_hosts_stat_use)){
			hostsStat = new HostsStat();
			c.async.tryScheduleWithFixedDelay("flush-hosts-statistics", ()-> 
				saveClientsHostsStatImpl(), 
				props.getLongVal(chats_hosts_stat_FlushDelay), 
				props.getLongVal(chats_hosts_stat_FlushDelta));
		}
	}
	
	public void reloadModel() throws Exception{
		reloadModel(null);
	}

	public synchronized void reloadModel(ReloadChatsModelReq req) throws Exception{
		
		ReloadChatsModelType type = req != null? req.type() : FULL_MODEL_UPDATED;
		if(type == FULL_MODEL_UPDATED){
			this.m = reloadOps.loadFullModel();
		}
		else if(type == SERVER_CREATED){
			reloadOps.reloadServer(m, req.getLongParam1());
		}
		else if(type == SERVER_UPDATED){
			reloadOps.reloadServer(m, req.getLongParam1());
		}
		else if(type == ACC_CREATED){
			reloadOps.reloadNewAcc(m, req.getLongParam1(), req.param2);
		}
		else if(type == ACC_UPDATED){
			reloadOps.reloadAcc(m, req.param1);
		}
		else if(type == TARIFF_CREATED){
			reloadOps.reloadTariff(m, req.getLongParam1());
		}
		else if(type == USER_PRIVS_UPDATED){
			reloadOps.reloadUserPrivs(m, req.getLongParam1(), req.getLongParam2());
		}
		
		log.info("model reloaded: type="+type
				+", req="+getReqInfoStr());
	}
	
	
	
	public ServerRow getServerByAcc(String accUid) {
		return m.getServerByAcc(accUid);
	}
	
	
	public Set<PrivilegeType> getAccPrivilegesForUser(long chatId, long userId){
		return m.getPrivilegesForAcc(userId, chatId).privs;
	}
	
	public Set<PrivilegeType> getAccPrivilegesForUser(String accUid, long userId){
		return m.getPrivilegesForAcc(userId, accUid).privs;
	}
	
	
	public Map<String, Set<PrivilegeType>> getAllAccountsPrivilegesForUser(long userId){
		return m.getPrivilegesForAccs(userId);
	}
	
	/**
	 * Список аккаунтов с серверами в которых юзер - оператор или модерадор или админ.
	 * Root роль не дает права получить все аккаунты.
	 */
	public List<ChatAccount> getAccsForOperator(long userId) {
		List<ChatAccount> out = m.getAccountsForOperator(userId);
		for (ChatAccount acc : out) {
			acc.blocked = isAccBlockedFromCache(cache, acc.uid);
		}
		return out;
	}
	
	public List<String> getOwnerAccIds(long userId){
		return new ArrayList<>(m.getAccountsUidsFor(userId, PrivilegeType.CHAT_OWNER));
	}
	
	public boolean checkAllAccExists(long serverId, List<String> accs) {
		
		if(isEmpty(accs)) return false;
		
		for (String accUid : accs) {
			
			if(isEmpty(accUid)) return false;
			
			ChatAccount acc = m.getAccount(accUid, false);
			if(acc == null) return false;
			if(acc.serverId != serverId) return false;
		}
		
		return true;

	}
	
	/**
	 * Послать на сервера чатов токены юзера для их автозаведения сессии при доступе из браузера
	 */
	public String initUserTokenInAccServers(HttpServletRequest req) throws Exception{
		return initUserTokenInAccServers(req, false);
	}
	
	public String initUserTokenInAccServers(HttpServletRequest req, boolean full) throws Exception{
		
		//no session
		User user = security.getUserFromSession(req);
		if(user == null) return null;
		long userId = user.id;
		

		Set<String> reqUrls = getChatServerUrlReqsFor(userId, URL_USER_INIT_TOKEN);
		if(isEmpty(reqUrls)) return null;
		
		
		//get or create token
		String token = security.getUserSessionAttr(req, ACC_SESSION_TOKEN);
		if(token == null){
			token = randomSimpleId();
		}
		
		
		//filter done urls if need
		Map<String, Void> doneUrls = security.getUserSessionAttr(req, ACC_INITED_URLS);
		if(doneUrls == null){
			doneUrls = new HashMap<>();
			security.setUserSessionAttr(req, ACC_INITED_URLS, doneUrls);
		}
		if( ! full){
			for(String doneUrl : doneUrls.keySet()){
				reqUrls.remove(doneUrl);
			}			
		}
		if(isEmpty(reqUrls)) return token;

		
		String clientIp = getClientIp(req);
		String userAgent = getUserAgent(req);
		
		//remote
		if(isUseRemote(props)){
			
			//cur privilages
			Map<String, Set<PrivilegeType>> privilegesByAccount = m.getPrivilegesForAccs(userId);
			
			InitUserTokenReq remoteReq = new InitUserTokenReq(token, userId, clientIp, userAgent, privilegesByAccount);
			Map<String, Void> results = postEncryptedJsonToAny(props, reqUrls, remoteReq);
			doneUrls.putAll(results);
		}
		security.setUserSessionAttr(req, ACC_SESSION_TOKEN, token);
		
		log.info("sended Create session tokens: userId="+user.id
				+", login="+user.login
				+", reqUrls="+reqUrls
				+", req="+getReqInfoStr());
		
		return token;
		
	}



	
	
	private void sendRemoveUserSessionsAsync(UserSession userSession) {
		
		String token = (String)userSession.attrs.get(ACC_SESSION_TOKEN);
		if(token == null) return;
		
		User user = userSession.user;
		Set<String> reqUrls = getChatServerUrlReqsFor(user.id, URL_USER_REMOVE_SESSION);
		if(isEmpty(reqUrls)) return;
		
		//remote
		if(isUseRemote(props)){
			c.async.invoke(()->
				postEncryptedJsonToAny(props, reqUrls, new RemoveUserSessionReq(token))
			);
		}
		
		log.info("sended Remove session tokens: userId="+user.id
				+", login="+user.login
				+", reqUrls="+reqUrls
				+", req="+getReqInfoStr());
	}
	
	

	private Set<String> getChatServerUrlReqsFor(long userId, String req){
		Collection<ChatAccount> accs = m.getAccountsForOperator(userId);
		if(isEmpty(accs)) return null;
		
		HashSet<String> reqUrls = new HashSet<>();
		for(ChatAccount acc : accs){
			reqUrls.add(acc.server.createUrl(req));
		}
		return reqUrls;
	}
	
	
	
	
	
	@Secured
	public long createServer(String httpUrl, String httpsUrl) throws Exception{
		
		checkAccessFor_ADMIN();
		
		
		//update db
		long id = universal.nextSeqFor(servers);
		universal.update(new CreateServer(id, httpUrl, httpsUrl));
		ServerRow server = new ServerRow(id, httpUrl, httpsUrl);
		
		
		//update model
		m.putServer(server);
		
		//update other fronts servers
		sendUpdateOtherFrontsSignalAsync(SERVER_CREATED, id);
		
		log.info("server created: id="+id
				+", httpUrl="+httpUrl
				+", httpsUrl="+httpsUrl
				+", req="+getReqInfoStr());
		
		return id;
		
	}

	
	@Secured
	public void setServerFull(long serverId, boolean val) throws Exception {
		
		checkAccessFor_ADMIN();
		
		//read from model
		ServerRow server = m.getServer(serverId);
		
		if(server == null) return;
		if(server.isFull == val) return;
		
		//update db
		universal.update(new UpdateServerById(serverId, new IsFull(val)));
		
		
		//update model
		m.updateServerFull(serverId, val);
		
		//update other fronts servers
		sendUpdateOtherFrontsSignalAsync(SERVER_UPDATED, serverId);
		
		log.info("server changed full value: id="+server.id
				+", val="+val
				+", req="+getReqInfoStr());
		
	}
	
	
	@Secured
	public long createTariff(BigDecimal price, boolean isPublic, int maxOperators) throws Exception {
		
		checkAccessFor_ADMIN();
		
		return createTariff(price, isPublic, maxOperators, null);
	}
	
	
	@Secured
	public long createTariff(BigDecimal price, boolean isPublic, int maxOperators, Long idPreset) throws Exception {
		
		checkAccessFor_ADMIN();
		
		//update db
		long id = idPreset != null? idPreset : universal.nextSeqFor(tariffs);
		Tariff tariff = new Tariff(id, price, isPublic, maxOperators);
		universal.update(new CreateTariff(tariff));
		
		//update model
		m.putTariff(tariff);
		
		//update other fronts servers
		sendUpdateOtherFrontsSignalAsync(TARIFF_CREATED, id);
		
		log.info("tariff created: id="+id
				+", req="+getReqInfoStr());
		
		return id;
	}
	
	public List<Tariff> getPublicTariffs(){
		return m.getPublicTariffs();
	}
	
	
	@Secured
	public String createAccByUser(String name) throws NoAvailableServerException, UserNotFoundException, Exception{
		return createAccByUser(name, null);
	}
	
	
	@Secured
	public String createAccByUser(String name, Long tariffId) throws NoAvailableServerException, UserNotFoundException, Exception{
		
		if(props.getBoolVal(toolMode)){
			checkAccessFor_ADMIN();
		}
		
		long userId = findUserIdFromSecurityContext();		
		checkUserAccsForBlocked();
		
		Set<String> existsAccs = m.getAccountsUidsFor(userId, CHAT_OWNER);
		
		long maxAccs = props.getVal(chats_maxAccsForUser+"-"+userId, 0);
		if(maxAccs == 0) maxAccs = props.getIntVal(chats_maxAccsForUser);
		if(existsAccs.size() >= maxAccs) throw new AccountsLimitException();
		
		
		if(tariffId == null) tariffId = props.getLongVal(tariffs_default_tariff);
		
		List<Long> serversIds = m.getNotFullServersId();
		int size = serversIds.size();
		if(size == 0) throw new NoAvailableServerException();
		
		//check tariff
		Tariff tariff = m.getTariff(tariffId);
		if(tariff == null) throw new TariffNotFoundException(tariffId);
		if( ! tariff.isPublic) throw new InvalidTariffException(tariffId);
		
		//random server
		Long serverId = serversIds.get(r.nextInt(size));
		String uid = randomUUID();
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			createAcc(serverId, uid, userId, name, tariffId);
			return uid;
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	@Secured
	public ChatAccount getAccByUid(String accUid, boolean withServer){
		
		checkAccessFor_ADMIN();
		
		return m.getAccount(accUid, withServer);
	}

	
	@Secured
	public long createAcc(
			long serverId, 
			String uid, 
			long ownerId, 
			String name, 
			long tariffId) throws UserNotFoundException, Exception{
		return createAcc(serverId, uid, ownerId, name, tariffId, true);
	}

	@Secured
	public long createAcc(
			long serverId, 
			String uid, 
			long ownerId, 
			String name, 
			long tariffId,
			boolean adminNotify) throws UserNotFoundException, Exception{
		
		checkAccessFor_ADMIN();
		
		if(hasText(name) && name.length() > ChatAccount.MAX_NAME_SIZE){
			throw new InvalidInputException("too long name: "+name);
		}
		
		//check tariff
		Tariff tariff = m.getTariff(tariffId);
		if(tariff == null) throw new TariffNotFoundException(tariffId);
		
		Date created = new Date();
		long accId = -1;
		ServerRow server = null;
		ChatAccount acc = null;
		ChatAccountPrivileges ownerPriv = null;
		
		//update db
		setSingleTxMode();
		try {
			
			server = universal.selectOne(new GetServerById(serverId));
			if(server == null) return -1;
			
			accId = universal.nextSeqFor(chat_accounts);
			acc = new ChatAccount(accId, serverId, uid, name, created, tariffId);
			universal.update(new CreateChatAccount(acc));
			
			Set<PrivilegeType> set = singleton(CHAT_OWNER);
			universal.update(new CreateChatAccountPrivileges(accId, ownerId, set));
			ownerPriv = new ChatAccountPrivileges(ownerId, accId, set);
			
			//remote
			if(isUseRemote(props)){
				postEncryptedJson(props, server.createUrl(URL_CHAT_CREATE_ACC), new CreateAccountReq(uid));				
			}
			

		}catch (Exception e) {
			rollbackSingleTx();
			
			String msg = e.getMessage();
			if(hasText(msg) && msg.toLowerCase().contains("userid")){
				throw new UserNotFoundException(ownerId);
			}
			
			throw e;
		} finally {
			closeSingleTx();
		}
		
		
		//update model
		m.putData(server, acc, ownerPriv);
		
		//send remote updates
		sendUpdateChatServerSessions(server, ownerId);
		sendUpdateOtherFrontsSignalAsync(ACC_CREATED, ownerId, uid);
		
		//post event
		c.events.tryFireEvent(new ChatCreatedEvent(acc, ownerId));
		
		String logMsg = "acc created: "
				+ "uid="+acc.uid
				+", name="+acc.name
				+", tariffId="+tariff.id
				+", ownerId="+ownerId
				+", serverId="+server.id
				+", serverAccsCount="+m.getServerAccountsCount(server.id)
				+", serverUrl="+server.httpUrl
				+", req="+getReqInfoStr();
		
		if(adminNotify && props.getBoolVal(admin_emailNotify_AccCreated)){
			c.mails.sendAsyncInfoData("acc created", logMsg);
		}
		log.info(logMsg);
		
		return acc.id;

	}
	
	
	
	@Secured
	public void putAccConfigByUser(String uid, Key key, Object val)throws Exception {
		
		checkPrivilegesForAcc_Owner(uid);
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			putAccConfig(uid, key, val);
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	@Secured
	public void putAccConfig(String uid, Key key, Object val) throws Exception {
		
		checkAccessFor_ADMIN();
		
		ChatAccount acc = m.getAccount(uid, true);
		if(acc == null) return;
		
		Pair<UpdateRows, PutAccConfigReq> data = validateReqAndGetUpdates(uid, key, val);
		if(data == null) return;
		
		//update db and remote acc
		doInSingleTxMode(()->{
			
			universal.update(data.first);
			
			if(isUseRemote(props)){
				PutAccConfigReq req = data.second;
				postEncryptedJson(props, acc.server.createUrl(URL_CHAT_PUT_ACC_CONFIG), req);
			}
			
		});
		
		//update model
		m.putAccConfig(uid, key, val);
		
		//send remote updates
		sendUpdateOtherFrontsSignalAsync(ACC_UPDATED, uid);
		
		log.info("acc config updated: uid="+acc.uid
				+", key="+key
				+", val="+val
				+", req="+getReqInfoStr());
		
	}
	
	
	@Secured
	public PauseAccResp pauseAccByUser(String accUid) throws Exception{
		
		checkPrivilegesForAcc_Owner(accUid);
		checkUserAccsForBlocked();
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			return pauseAcc(accUid, null);
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	@Secured
	public PauseAccResp pauseAcc(String accUid, UpdateTariffOps reqOps) throws Exception{
		
		checkAccessFor_ADMIN();
		
		ChatAccount acc = m.findAccount(accUid, true);

		if(reqOps == null){
			boolean canUseNotPublic = true;
			boolean checkMaxChangedInDay = true;
			reqOps = new UpdateTariffOps(canUseNotPublic, checkMaxChangedInDay);
		}
		
		//extra tx logic
		reqOps.beforeTxCommitListener = ()-> {
			if( ! isUseRemote(props)) return;
			postEncryptedJson(props, acc.server.createUrl(URL_CHAT_PAUSED), new SetAccsPausedReq(accUid, true));
		};
		
		long newTariffId = PAUSE_TARIFF_ID;
		BigDecimal cost = updateAccTariff(accUid, newTariffId, reqOps);
		PauseAccResp out = new PauseAccResp(newTariffId, cost);
		
		log.info("acc paused: uid="+acc.uid
				+", req="+getReqInfoStr());
		
		return out;
	}
	
	@Secured
	public PauseAccResp unpauseAccByUser(String accUid) throws Exception {
		
		checkPrivilegesForAcc_Owner(accUid);
		checkUserAccsForBlocked();
		
		ChatAccount acc = m.findAccount(accUid, false);
		
		if(PAUSE_TARIFF_ID != acc.tariffId) throw new ChatAccountNotPausedException();
		
				
		Long tariffPrevId = acc.tariffPrevId;
		if(tariffPrevId == null){
			log.error("invalid state: tariffPrevId is null for paused acc: "+accUid);
			
			//try to select
			List<Tariff> publicTariffs = m.getPublicTariffs();
			if(isEmpty(publicTariffs)) throw new IllegalStateException("no public tariffs to select");
			tariffPrevId = publicTariffs.get(0).id;
		}
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			return unpauseAcc(accUid, tariffPrevId, null);
		}finally {
			popUserFromSecurityContext();
		}
	}
	
	
	@Secured
	public PauseAccResp unpauseAcc(String accUid, long tariffId, UpdateTariffOps reqOps) throws Exception {
		
		checkAccessFor_ADMIN();
		
		if(tariffId == PAUSE_TARIFF_ID) throw new InvalidInputException("tariff must be not 'paused' for acc: "+accUid);
		
		ChatAccount acc = m.findAccount(accUid, true);
		
		if(reqOps == null){
			boolean canUseNotPublic = true;
			boolean checkMaxChangedInDay = false;
			reqOps = new UpdateTariffOps(canUseNotPublic, checkMaxChangedInDay);
		}
		
		//extra tx logic
		reqOps.beforeTxCommitListener = ()-> {
			if( ! isUseRemote(props)) return;
			postEncryptedJson(props, acc.server.createUrl(URL_CHAT_PAUSED), new SetAccsPausedReq(accUid, false));
		};
		
		BigDecimal cost = updateAccTariff(accUid, tariffId, reqOps);
		PauseAccResp out = new PauseAccResp(tariffId, cost);
		
		log.info("acc unpaused: uid="+acc.uid
				+", tariffId="+tariffId
				+", req="+getReqInfoStr());
		
		return out;
	}
	
	
	
	
	@Secured
	public BigDecimal updateAccTariffByUser(String accUid, long newTariffId) throws Exception{
		
		checkPrivilegesForAcc_Owner(accUid);
		checkUserAccsForBlocked();
		checkAccForPaused(accUid);
		
		if(newTariffId == PAUSE_TARIFF_ID) throw new InvalidInputException("use 'pauseAcc' method for this system tariff");
		
		pushToSecurityContext_SYSTEM_USER();
		try {
			boolean canUseNotPublic = false;
			boolean checkMaxChangedInDay = true;
			return updateAccTariff(accUid, newTariffId, new UpdateTariffOps(canUseNotPublic, checkMaxChangedInDay));
		}finally {
			popUserFromSecurityContext();
		}
	}

	@Secured
	public BigDecimal updateAccTariff(String accUid, long newTariffId, boolean canUseNotPublic) throws Exception {
		
		checkAccessFor_ADMIN();
		
		boolean checkMaxChangedInDay = false;
		return updateAccTariff(accUid, newTariffId, new UpdateTariffOps(canUseNotPublic, checkMaxChangedInDay));
	}

	
	@Secured
	public BigDecimal updateAccTariff(
			String accUid, 
			long newTariffId, 
			UpdateTariffOps reqOps) throws ConcurrentUpdateSqlException, Exception {
		
		checkAccessFor_ADMIN();
		
		UpdateTariffOps ops = reqOps != null? reqOps : new UpdateTariffOps();
		
		//берем из БД 
		ChatAccount acc = universal.selectOne(new GetChatAccount(accUid));
		if(acc == null) throw new NoChatAccountException();
		
		if(acc.tariffId == newTariffId) return ZERO;
		
		//old and new tariffs
		Tariff oldTariff = m.findTariff(acc.tariffId);
		Tariff newTariff = m.findTariff(newTariffId);
		if( ! newTariff.isPublic && ! ops.canUseNotPublic) throw new NotPublicTariffException(newTariffId);
		
		
		//check limitations
		int maxOperators = newTariff.maxOperators;
		if(maxOperators > 0){
			Map<Long, UserAccInfo> curOperators = m.getAccOperators(accUid);
			if( ! props.getBoolVal(toolMode) 
					&& curOperators.size() > maxOperators) throw new UpdateTariffOperatorsLimitException();
		}

		Date now = ops.nowPreset == null? new Date() : ops.nowPreset;
		Date tariffStart = ops.tariffStartPreset == null? acc.tariffStart : ops.tariffStartPreset;
		Date tariffLastPay = ops.tariffLastPayPreset == null? acc.tariffLastPay : ops.tariffLastPayPreset;
		
		//changes in same day
		int changedInDay;
		if( ! isSameDay(tariffStart, now)) changedInDay = 1;
		else {
			if(ops.checkMaxChangedInDay){
				changedInDay = acc.tariffChangedInDay + 1;
				Integer maxChanges = props.getIntVal(tariffs_maxChangedInDay);
				if( ! props.getBoolVal(toolMode) && maxChanges > 0 && changedInDay > maxChanges)
					throw new ChangeTariffLimitException();
			} else {
				changedInDay = acc.tariffChangedInDay;
			}
		}
		
		//calculate bill
		long ownerId = m.findAccOwner(accUid);
		BigDecimal price = ops.pricePreset == null? oldTariff.price : ops.pricePreset;
		BigDecimal minPrice = props.getBigDecimalVal(tariffs_minChangeTariffBill);
		BigDecimal amount = calcForPeriod(price, tariffLastPay, now, minPrice);
		boolean hasBill = ZERO.compareTo(amount) != 0;
		Date prevLastPay = acc.tariffLastPay;

		//update db
		doInSingleTxMode(()->{
			
			//extra tx logic
			if(ops.beforeTxBeginListener != null){
				ops.beforeTxBeginListener.call();
			}
			
			//bill
			if(hasBill) {
				String desc = "accId="+acc.id+", oldTariff="+oldTariff.id+", newTariff="+newTariff.id;
				billing.payBill(ownerId, amount, now, TARIFF_CHANGE_BIll, desc, false);
			}
			
			//set new tariff
			int result = universal.updateOne(new UpdateChatAccountByUid(
					accUid, 
					prevLastPay,
					new TariffId(newTariff.id), 
					new TariffStart(now),
					new TariffLastPay(now),
					new TariffChangedInDay(changedInDay),
					new TariffPrevId(oldTariff.id)));
			//concurrent check
			if(result == 0) throw new ConcurrentUpdateSqlException("UpdateChatAccountByUid: uid="+accUid);
			
			
			//extra tx logic
			if(ops.beforeTxCommitListener != null){
				ops.beforeTxCommitListener.call();
			}
			
		});
		
		//update model
		if(hasBill) {
			billing.updateUserBalanceCache(ownerId, false);
		}
		m.updateAccTariff(accUid, newTariff.id, now, changedInDay, oldTariff.id);
		
		
		//send remote updates
		sendUpdateOtherFrontsSignalAsync(ReloadChatsModelType.ACC_UPDATED, accUid);
		
		log.info("acc tariff updated: uid="+acc.uid
				+", tariffId="+newTariffId 
				+", oldTariffId="+oldTariff
				+", req="+getReqInfoStr());
		
		return amount;
		
	}
	
	
	
	
	@Secured
	public Map<Long, UserAccInfo> getAccUsers(String accUid) throws Exception {
		
		findUserIdFromSecurityContext();
		
		return m.getAccUsers(accUid);
	}
	
	
	@Secured
	public Map<Long, UserAccInfo> getAccOperators(String accUid) throws Exception{
		
		findUserIdFromSecurityContext();
		
		return m.getAccOperators(accUid);
	}
	
	


	@Secured
	public List<ServerRow> getServers() throws Exception {
		
		checkAccessFor_MODERATOR();
		
		return m.getServers();
	}
	
	
	@Secured
	public List<ChatAccount> getServerAccs(long serverId) throws Exception {
		
		checkAccessFor_MODERATOR();
		
		return m.getServerAccounts(serverId);
	}
	
	@Secured
	public Map<String, Long> getAllAccOwners(){
		
		checkAccessFor_MODERATOR();
		
		return m.getAllAccOwners();
	}
	
	
	@Secured
	public void setOperatorForAcc(String accUid, long userId) throws Exception{
		addUserPrivileges(accUid, userId, singleton(CHAT_OPERATOR));
	}
	
	@Secured
	public void addUserPrivileges(String accUid, long userId, Set<PrivilegeType> privsSet) throws Exception{
		
		if(isEmpty(privsSet)) return;
		
		checkAccessForPrivsUpdate(accUid, privsSet);
		
		User owner = findUserFromSecurityContext();
		
		
		UserExt user = universal.selectOne(new SelectUserById(userId));
		if(user == null) return;
		
		
		//read model
		ChatAccount acc = m.getAccount(accUid, true);
		if(acc == null) return;
		
		long accId = acc.id;
		ServerRow server = acc.server;
		

		UserAccInfo info = m.getPrivilegesForAcc(userId, accUid);
		Set<PrivilegeType> privs = info.privs;
		boolean isNew = privs.isEmpty();
		int oldSize = privs.size();
		privs.addAll(privsSet);
		if(privs.size() == oldSize) return;
		
		
		//check max operator limit if need
		if( ! props.getBoolVal(toolMode) && privsSet.contains(CHAT_OPERATOR)){
			
			checkAccForPaused(accUid);
			
			Tariff tariff = m.findTariff(acc.tariffId);
			int maxOperators = tariff.maxOperators;
			if(maxOperators > 0){
				Map<Long, UserAccInfo> curOperators = m.getAccOperators(accUid);
				int newOpsCount = curOperators.size() + 1;
				if(newOpsCount > maxOperators) 
					throw new OperatorsLimitException();
			}
		}
		
		
		//update db
		doInSingleTxMode(()->{
			
			if(isNew){
				try {
					universal.update(new CreateChatAccountPrivileges(accId, userId, privs));
				}catch (Exception e) {
					//already exist
					universal.update(new UpdateChatAccountPrivileges(accId, userId, privs));
				}
			} else {
				universal.update(new UpdateChatAccountPrivileges(accId, userId, privs));
			}
			//remove add reqs
			universal.update(new DeleteChatAccountAddReq(userId, accId));
			
			
			//remote
			if(privsSet.contains(CHAT_OPERATOR) && isUseRemote(props)){
				PutOperatorReq req = new PutOperatorReq(acc.uid, user.id, info.nickname, user.email);
				postEncryptedJson(props, server.createUrl(URL_CHAT_PUT_OP), req);				
			}
			
		});
		
		
		//update model
		m.addPrivileges(userId, accId, privsSet);
		
		
		//send remote updates
		sendUpdateChatServerSessions(server, userId);
		sendUpdateOtherFrontsSignalAsync(USER_PRIVS_UPDATED, userId, accId);
		
		log.info("user privs added: uid="+accUid
				+", userId="+userId
				+", privs="+privsSet
				+", ownerId="+owner.id
				+", ownerLogin="+owner.login
				+", req="+getReqInfoStr());
		
	}

	
	@Secured
	public void removeUserPrivileges(String accUid, long userId, Set<PrivilegeType> privsSet) throws Exception{
		
		if(isEmpty(privsSet)) return;
		
		checkAccessForPrivsUpdate(accUid, privsSet);
		
		User owner = findUserFromSecurityContext();
		
		UserExt user = universal.selectOne(new SelectUserById(userId));
		if(user == null) return;
		
		
		//read model
		ChatAccount acc = m.getAccount(accUid, true);
		if(acc == null) return;
		
		long accId = acc.id;
		ServerRow server = acc.server;
		

		Set<PrivilegeType> privs = getAccPrivilegesForUser(acc.id, userId);
		boolean isNew = privs.isEmpty();
		if(isNew) return;
		int oldSize = privs.size();
		privs.removeAll(privsSet);
		if(privs.size() == oldSize) return;
		
		
		//update db
		doInSingleTxMode(()->{
			
			if( ! privs.isEmpty()){
				universal.update(new UpdateChatAccountPrivileges(accId, userId, privs));
			} else {
				universal.update(new DeleteChatAccountPrivilege(userId, accId));
			}
			
			//remote
			if(privsSet.contains(CHAT_OPERATOR) && isUseRemote(props)){
				RemoveOperatorReq req = new RemoveOperatorReq(acc.uid, user);
				postEncryptedJson(props, server.createUrl(URL_CHAT_REMOVE_OP), req);				
			}
		});
		
		
		//update model
		m.removePrivileges(userId, accId, privsSet);
		
		
		//send remote updates
		sendUpdateChatServerSessions(server, userId);
		sendUpdateOtherFrontsSignalAsync(USER_PRIVS_UPDATED, userId, accId);
		
		log.info("user privs removed: uid="+accUid
				+", userId="+userId
				+", privs="+privsSet
				+(privs.isEmpty()? ", userRemoved" : "")
				+", ownerId="+owner.id
				+", ownerLogin="+owner.login
				+", req="+getReqInfoStr());
		
	}
	
	private void checkAccessForPrivsUpdate(String accUid, Set<PrivilegeType> privsSet) throws AccessDeniedException {		
		if(privsSet.contains(CHAT_OWNER)) checkAccessFor_ADMIN();
		else if(privsSet.contains(CHAT_MODER)) checkPrivilegesForAcc_Owner(accUid);
		else checkPrivilegesForAcc_Owner_Moder(accUid);
	}
	
	
	@Secured
	public List<ChatAccount> getAccsWithUserReqs() throws Exception {
		
		long userId = findUserIdFromSecurityContext();
		
		//db
		List<ChatAccountAddReq> reqs = universal.select(new GetAllChatAccountAddReqsByUser(userId));
		if(isEmpty(reqs)) return list();
		
		ArrayList<ChatAccount> out = new ArrayList<ChatAccount>();
		for(ChatAccountAddReq req : reqs){
			ChatAccount acc = m.getAccount(req.accId, false);
			if(acc != null) {
				acc.params = map("reqDate", req.created);
				out.add(acc);
			}
		}
		return out;
	}
	
	@Secured
	public List<ChatAccountAddReq> getReqsByAcc(String accUid) throws Exception {
		
		checkPrivilegesForAcc_Owner_Moder(accUid);
		
		ChatAccount acc = m.getAccount(accUid, false);
		if(acc == null) return list();
		
		//db
		return universal.select(new GetAllChatAccountAddReqsByAcc(acc.id));
	}
	
	@Secured
	public void addUserReqToAcc(String accUid) throws NoChatAccountException, UserAlreadyInAccountException, AddUserReqAlreadyExistsException, Exception {
		
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		ChatAccount acc = m.findAccount(accUid, false);
		
		UserAccInfo info = m.getPrivilegesForAcc(userId, accUid);
		if( ! isEmpty(info.privs)) throw new UserAlreadyInAccountException();
		
		//db
		try {
			universal.update(new CreateChatAccountAddReqs(acc.id, userId));
		}catch(SQLException e){
			if(containsAnyTextInMessage(e, "userid, accid", "unique index")){
				throw new AddUserReqAlreadyExistsException();
			} else {
				throw e;
			}
		}
		
		log.info("AddUserReq added: accUid="+accUid
				+", userId="+user.id
				+", userLogin="+user.login
				+", req="+getReqInfoStr());
	}
	
	@Secured
	public void removeAccAddReqByUser(String accUid) throws Exception{
		
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		ChatAccount acc = m.getAccount(accUid, false);
		if(acc == null) return;
		
		//db
		universal.update(new DeleteChatAccountAddReq(userId, acc.id));
		
		log.info("AddUserReq removed: accUid="+accUid
				+", userId="+user.id
				+", userLogin="+user.login
				+", req="+getReqInfoStr());
	}
	
	
	@Secured
	public void removeAccAddReqForUser(String accUid, long userId)throws Exception{
		
		checkPrivilegesForAcc_Owner_Moder(accUid);
		
		User owner = findUserFromSecurityContext();
		
		ChatAccount acc = m.getAccount(accUid, false);
		if(acc == null) return;
		
		universal.update(new DeleteChatAccountAddReq(userId, acc.id));
		
		log.info("AddUserReq removed by owner: accUid="+accUid
				+", userId="+userId
				+", ownerId="+owner.id
				+", onwerLogin="+owner.login
				+", req="+getReqInfoStr());
	}
	
	
	@Secured
	public boolean isBlockedAcc(String accUid){
		
		findUserIdFromSecurityContext();
		
		return isAccBlockedFromCache(cache, accUid);
	}
	
	
	public boolean isPausedAcc(String accUid){
		
		findUserIdFromSecurityContext();
		
		ChatAccount acc = m.getAccount(accUid, false);
		if(acc == null) return false;
		return acc.tariffId == PAUSE_TARIFF_ID;
	}

	@Secured
	public List<String> getBlockedAccs() {
		
		long userId = findUserIdFromSecurityContext();
		
		Set<String> accs = m.getAccountsUidsForAnyPriv(userId);
		if(isEmpty(accs)) return list();
		
		ArrayList<String> blocked = new ArrayList<String>();
		for(String uid : accs){
			if(isAccBlockedFromCache(cache, uid)) 
				blocked.add(uid);
		}
		
		return blocked;
	}
	
	@Secured
	public boolean setNickname(String accUid, String nickname) throws Exception {
		
		User user = findUserFromSecurityContext();
		long userId = user.id;
		
		boolean out = setNicknameInternal(accUid, userId, nickname);
		
		log.info("nickname changed: accUid="+accUid
				+", nickname="+nickname
				+", userId="+user.id
				+", userLogin="+user.login
				+", req="+getReqInfoStr());
		
		return out;
	}
	
	
	@Secured
	public boolean setNickname(String accUid, long userId, String nickname) throws Exception {
		
		checkPrivilegesForAcc_Owner_Moder(accUid);
		if(nickname == null) nickname = "";
		
		User owner = findUserFromSecurityContext();
		
		boolean out = setNicknameInternal(accUid, userId, nickname);
		
		log.info("nickname changed by owner: accUid="+accUid
				+", nickname="+nickname
				+", userId="+userId
				+", ownerId="+owner.id
				+", ownerLogin="+owner.login
				+", req="+getReqInfoStr());
		
		return out;
		
	}
	
	
	public Future<?> checkAndLogReferer(HttpServletRequest req, String customRef, String accUid) throws HostBlockedException {
		
		
		ChatAccount acc = m.getAccount(accUid, false);
		if(acc == null) return EMPTY_DONE_FUTURE;
		
		Long accOwner = m.getAccOwner(acc.uid);
		if(accOwner == null) return EMPTY_DONE_FUTURE;
		
		String ref = WebUtil.getReferer(req);
		if( ! hasText(ref)) ref = customRef;
		if( ! hasText(ref)) return EMPTY_DONE_FUTURE;
		
		URL url = null;
		try {
			url = new URL(ref);
		}catch(Exception e){
			return EMPTY_DONE_FUTURE;
		}
		
		String host = url.getHost();
		if( ! hasText(host)) return EMPTY_DONE_FUTURE;
		
		checkBlockedHost(props, host, accOwner, accUid);
		
		//async part
		Future<Object> f = c.async.invoke(()->{
			
			HostsStat curStat = hostsStat;
			if(curStat == null) return null;
			
			curStat.putStat(host, acc.id, accOwner);
			return null;
		});
		return f;
		
		
	}
	
	@Secured
	public boolean saveClientsHostsStat() throws Exception{
		
		checkAccessFor_ADMIN();
		
		return saveClientsHostsStatImpl();
	}
	
	private boolean saveClientsHostsStatImpl() throws Exception {
		
		//empty stat
		if(hostsStat == null) hostsStat = new HostsStat();
		if(hostsStat.isEmpty()) return false;
		
		//create new stat
		HostsStat oldStat = hostsStat;
		hostsStat = new HostsStat();
		
		oldStat.stopUpdating();
		Map<String, Set<Long>> accsOwnerByHost = oldStat.getHostsWithOwners();
		Map<String, Set<Long>> accsByHost = oldStat.getHostsWithAccs();
		
		//update db
		for(Entry<String, Set<Long>> entry : accsOwnerByHost.entrySet()){
			
			//create or get host
			String name = entry.getKey();
			ClientHost host = universal.selectOne(new GetClientHost(name));
			if(host == null){
				try {
					host = new ClientHost(universal.nextSeqFor(client_hosts), name);
					host.important = getHostImportantFlag(props, host.name);
					universal.update(new CreateClientHost(host));
				}catch(Exception e){
					host = universal.selectOne(new GetClientHost(name));
				}
			}
			if(host == null) {
				log.error("can't get client host data by name: "+name);
				continue;
			}
			
			//owners
			for(Long owner : entry.getValue()){
				try {
					universal.update(new CreateClientHostAccOwner(host.id, owner));
				}catch(Exception e){
					//already created
				}
			}
			
			//accs
			Set<Long> accs = accsByHost.get(name);
			if( ! isEmpty(accs)){
				for (Long accId : accs) {
					try {
						universal.update(new CreateClientHostAcc(host.id, accId));
					}catch(Exception e){
						//already created
					}
				}
			}
		}
		
		return true;
			
	}

	private boolean setNicknameInternal(String accUid, long userId, String nickname) throws Exception {
		
		validateForTextSize(nickname, "nickname", 0, MAX_NICKNAME_SIZE);
		
		ChatAccount acc = m.findAccount(accUid, true);
		long accId = acc.id;
		UserAccInfo info = m.getPrivilegesForAcc(userId, accUid);
		
		//update db
		int updateCount[] = {0};
		doInSingleTxMode(()->{
			
			updateCount[0] = universal.updateOne(new UpdateUserAccNickname(acc.id, userId, nickname));
			
			if(updateCount[0] > 0 
					&& info.privs.contains(CHAT_OPERATOR)
					&& isUseRemote(props)){
				PutOperatorReq req = new PutOperatorReq(acc.uid, userId, nickname);
				postEncryptedJson(props, acc.server.createUrl(URL_CHAT_PUT_OP), req);		
			}
			
		});
		if(updateCount[0] == 0) return false;
		
		//update model
		boolean result = m.setNickname(accUid, userId, nickname);
		
		//send remote updates
		sendUpdateOtherFrontsSignalAsync(USER_PRIVS_UPDATED, userId, accId);
		
		return result;
	}
	
	
	private void updateRemoteContactsOnUserUpdateTx(UserUpdateTxEvent event) throws Exception {
		
		if( ! isUseRemote(props)) return;
		
		User user = event.old;
		UpdateUserReq req = event.req;
		
		if( ! isUpdateNotEmptyVal(user.email, req.email)) return;

		//обновляем email на всех чат-серверах где юзер - оператор
		Set<String> accsToUpdate = m.getAccountsUidsFor(user.id, CHAT_OPERATOR);
		if( isEmpty(accsToUpdate)) return;
		
		Set<String> serverUrls = new HashSet<String>();
		for(String accId : accsToUpdate){
			ChatAccount acc = m.getAccount(accId, true);
			if(acc == null) continue;
			serverUrls.add(acc.server.createUrl(URL_CHAT_UPDATE_USER_CONTACT));
		}
		
		postEncryptedJsonToAny(props, serverUrls, new UpdateUserContactReq(user.id, req.email));
		
		log.info("send to accs new user data: "
				+"userId="+user.id
				+", accs="+accsToUpdate
				+", req="+getReqInfoStr());
		
	}
	
	
	
	
	private void checkUserAccsForBlocked() throws ChatAccountBlockedException, SQLException {
		
		long userId = findUserIdFromSecurityContext();
		UserBalance balance = universal.selectOne(new SelectUserBalanceById(userId));
		if(balance == null) throw new UserNotFoundException(userId);
		if(balance.accsBlocked) throw new ChatAccountBlockedException();
	}
	
	private void checkAccForPaused(String accUid) throws ChatAccountPausedException {
		
		ChatAccount acc = m.findAccount(accUid, false);
		if(acc.tariffId == PAUSE_TARIFF_ID) throw new ChatAccountPausedException();
	}
	
	

	
	private void checkPrivilegesForAcc_Owner(String accUid) throws AccessDeniedException {
		checkPrivilegesForAcc(accUid, CHAT_OWNER);
	}
	
	private void checkPrivilegesForAcc_Owner_Moder(String accUid) throws AccessDeniedException {
		checkPrivilegesForAcc(accUid, CHAT_OWNER, CHAT_MODER);
	}
	
	@SuppressWarnings("unused")
	private void checkPrivilegesForAcc_AnyRole(String accUid)throws AccessDeniedException {
		checkPrivilegesForAcc(accUid, CHAT_OWNER, CHAT_MODER, CHAT_OPERATOR);
	}
	
	private void checkPrivilegesForAcc(String accUid, PrivilegeType...privs) throws AccessDeniedException {
		
		if(hasAccessFor(UserRole.ADMIN)) return;
		
		long userId = findUserIdFromSecurityContext();
		
		UserAccInfo info = m.getPrivilegesForAcc(userId, accUid);
		for (PrivilegeType priv : privs) {
			if(info.privs.contains(priv)) return;
		}
		throw new AccessDeniedException("userId="+userId+", accUid="+accUid+", needPrivs="+list(privs));
	}
	
	
	private void sendUpdateOtherFrontsSignalAsync(ReloadChatsModelType type) {
		sendUpdateOtherFrontsSignalAsync(type, null, null);
	}
	
	
	private void sendUpdateOtherFrontsSignalAsync(ReloadChatsModelType type, Object param1) {
		sendUpdateOtherFrontsSignalAsync(type, param1, null);
	}
	
	private void sendUpdateOtherFrontsSignalAsync(ReloadChatsModelType type, Object param1, Object param2) {
		
		String serversUrls = c.props.getVal(frontServerUrls);
		if(isEmpty(serversUrls)) return;
		
		final List<String> reqUrls = new ArrayList<>();
		for(String url : strToList(serversUrls, " ")){
			reqUrls.add(url+URL_SYNC_RELOAD_CHATS_MODELS);
		}
		if(isEmpty(reqUrls)) return;
		
		c.async.invoke(()->
			postEncryptedJsonToAny(props, reqUrls, new ReloadChatsModelReq(c.root.id, type, param1, param2))
		);
	}
	
	
	private void sendUpdateChatServerSessions(ServerRow server, long userId) {
		
		if( ! isUseRemote(props)) return;
		
		Map<String, Set<PrivilegeType>> privilegesByAccount = m.getPrivilegesForAccs(userId);
		try {
			postEncryptedJson(props, server.createUrl(URL_CHAT_UPDATE_SESSIONS), new UpdateUserSessionsReq(userId, privilegesByAccount));
		}catch (Throwable t) {
			ExpectedException.logError(log, t, "can't updateChatServerSessions");
		}
	}
	
	
	public static class UpdateTariffOps {
		
		boolean canUseNotPublic = false;
		boolean checkMaxChangedInDay = true;
		Date tariffStartPreset;
		Date tariffLastPayPreset;
		Date nowPreset;
		BigDecimal pricePreset;
		CallableVoid beforeTxBeginListener;
		CallableVoid beforeTxCommitListener;
		
		public UpdateTariffOps() {}

		public UpdateTariffOps(boolean canUseNotPublic, boolean checkMaxChangedInDay) {
			this.canUseNotPublic = canUseNotPublic;
			this.checkMaxChangedInDay = checkMaxChangedInDay;
		}

		public UpdateTariffOps(boolean canUseNotPublic,
				boolean checkMaxChangedInDay, Date tariffStartPreset,
				Date tariffLastPayPreset, Date nowPreset, BigDecimal pricePreset) {
			this.canUseNotPublic = canUseNotPublic;
			this.checkMaxChangedInDay = checkMaxChangedInDay;
			this.tariffStartPreset = tariffStartPreset;
			this.tariffLastPayPreset = tariffLastPayPreset;
			this.nowPreset = nowPreset;
			this.pricePreset = pricePreset;
		}

		public UpdateTariffOps(boolean canUseNotPublic,
				boolean checkMaxChangedInDay, Date tariffStartPreset,
				Date tariffLastPayPreset, Date nowPreset,
				BigDecimal pricePreset, CallableVoid beforeDbUpdateListener) {
			this.canUseNotPublic = canUseNotPublic;
			this.checkMaxChangedInDay = checkMaxChangedInDay;
			this.tariffStartPreset = tariffStartPreset;
			this.tariffLastPayPreset = tariffLastPayPreset;
			this.nowPreset = nowPreset;
			this.pricePreset = pricePreset;
			this.beforeTxBeginListener = beforeDbUpdateListener;
		}
		
	}


}
