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
package och.comp.ops;

import static och.api.model.RemoteChats.*;
import static och.api.model.tariff.Tariff.*;
import static och.comp.ops.RemoteOps.*;
import static och.comp.ops.ServersOps.*;
import static och.comp.web.JsonOps.*;
import static och.util.Util.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import och.api.exception.ExpectedException;
import och.api.model.billing.UserBalance;
import och.api.model.chat.account.ChatAccount;
import och.api.model.server.ServerRow;
import och.api.remote.chats.SetAccsBlockedReq;
import och.api.remote.chats.SetAccsPausedReq;
import och.comp.cache.Cache;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.MainDb;
import och.comp.db.main.table.billing.GetAllBlockedUsers;
import och.comp.db.main.table.billing.SelectUserBalanceById;
import och.comp.db.main.table.billing.UpdateUserBalance;
import och.comp.db.main.table.chat.GetAllChatAccounts;
import och.service.props.Props;
import och.util.model.Pair;
import och.util.sql.ConcurrentUpdateSqlException;

import org.apache.commons.logging.Log;

public class BillingOps {
	
	static final Log log = getLog(BillingOps.class);
	
	public static SendAccsBlockedListener SEND_ACCS_BLOCKED_LISTENER;
	
	
	public static UserBalance findBalance(UniversalQueries universal, long userId) throws SQLException{
		UserBalance item = universal.selectOne(new SelectUserBalanceById(userId));
		if(item == null) throw new SQLException("User not found by id:" + userId);
		return item;
	}
	
	
	
	public static BigDecimal appendBalance(UniversalQueries universal, long userId, BigDecimal delta) throws ConcurrentUpdateSqlException, SQLException{
		
		int maxTries = 3;
		int curTry = 0;
		BigDecimal curVal;
		
		while(curTry < maxTries){
			
			curVal = findBalance(universal, userId).balance;
			BigDecimal newVal = curVal.add(delta);
			Integer result = universal.updateOne(new UpdateUserBalance(userId, curVal, newVal));
			if(result > 0) return newVal;
			
			//already changed
			curTry++;
		}
		throw new ConcurrentUpdateSqlException("UpdateUserBalance: userId="+userId);
	}
	
	
	
	public static BigDecimal appendBalanceAtomic(UniversalQueries universal, long userId, BigDecimal curVal, BigDecimal delta) throws ConcurrentUpdateSqlException, SQLException{

		BigDecimal newVal = curVal.add(delta);
		Integer result = universal.updateOne(new UpdateUserBalance(userId, curVal, newVal));
		if(result > 0) return newVal;
		
		throw new ConcurrentUpdateSqlException("UpdateUserBalance: userId="+userId);
	}
	
	
	public static boolean isNeedDeblockAccsState(BigDecimal oldBalance, BigDecimal newBalance, BigDecimal minActiveBalance){
		if(oldBalance.compareTo(minActiveBalance) >= 0) return false;
		return newBalance.compareTo(minActiveBalance) >= 0;
	}


	
	public static interface SendAccsBlockedListener {
		void onCall(long ownerId, boolean val);
	}

	/** послать запрос о блокировке и обновить кеш для заданного владельца */
	public static void sendAccsBlocked(Props props, MainDb db, Cache cache, long ownerId, boolean val){
		
		if(SEND_ACCS_BLOCKED_LISTENER != null) {
			try {
				SEND_ACCS_BLOCKED_LISTENER.onCall(ownerId, val); 
			}catch(Exception e){
				log.error("SEND_ACCS_BLOCKED_LISTENER", e);
			}
		}
		
		
		try {
			
			List<ChatAccount> accs = db.chats.getOwnerAccsInfo(ownerId);
			if(isEmpty(accs)) return;
			
			//send reqs
			sendBlockedReqs(props, accs, val);
			
			//update cache
			for (ChatAccount acc : accs) {
				String key = getBlockedAccFlag(acc.uid);
				if(val) cache.tryPutCache(key, "true");
				else cache.tryRemoveCache(key);
			}
			
		}catch(Throwable t){
			ExpectedException.logError(log, t, "can't sendAccsBlocked: ownerId="+ownerId+", val="+val);
		}
		
	}
	
	public static boolean isAccBlockedFromCache(Cache cache, String uid){
		return tryParseBool(cache.tryGetVal(getBlockedAccFlag(uid)), false);
	}
	
	
	/** послать запросы на сервера чатов о блокировке и обновить кеш (для всех чатов) */
	public static Pair<Integer, Integer> reinitAccsBlocked(Props props, MainDb db, Cache cache){
		try {
			
			UniversalQueries universal = db.universal;
			
			log.info("reinitAccsBlockedCache...");
			
			HashSet<ChatAccount> blockedAccs = new HashSet<>();
			HashSet<ChatAccount> unblockedAccs = new HashSet<>();
			
			List<ChatAccount> accs = universal.select(new GetAllChatAccounts());
			
			HashSet<String> blockedUids = new HashSet<>();
			List<UserBalance> blockedUsers = universal.select(new GetAllBlockedUsers());
			for (UserBalance user : blockedUsers) {
				List<String> blocked = db.chats.getOwnerAccs(user.userId);
				blockedUids.addAll(blocked);
			}
			
			for (ChatAccount acc : accs) {
				if( blockedUids.contains(acc.uid)) blockedAccs.add(acc);
				else unblockedAccs.add(acc);
			}
			
			int unblockedCount = unblockedAccs.size();
			int blockedCount = blockedAccs.size();
			log.info("found blocked accs: "+blockedCount+", unblocked accs: "+unblockedCount);
			
			Map<Long, ServerRow> servers = getServersMap(universal);
			
			//send reqs
			sendBlockedReqs(props, blockedAccs, servers, true);
			sendBlockedReqs(props, unblockedAccs, servers, false);
			
			//update cache
			for(ChatAccount acc : blockedAccs) {
				cache.tryPutCache(getBlockedAccFlag(acc.uid), "true");
			}
			for(ChatAccount acc : unblockedAccs){
				cache.tryRemoveCache(getBlockedAccFlag(acc.uid));
			}
			
			
			log.info("done");
			
			return new Pair<>(unblockedCount, blockedCount);
			
		}catch(Exception e){
			log.error("can't reinitAccsBlockedCache: "+ e);
			return null;
		}
	}

	
	
	
	private static void sendBlockedReqs(Props props, Collection<ChatAccount> accsWithServers, boolean val){
		HashSet<String> serverUrls = new HashSet<String>();
		HashSet<String> uids = new HashSet<String>();
		for (ChatAccount acc : accsWithServers) {
			uids.add(acc.uid);
			serverUrls.add(acc.server.createUrl(URL_CHAT_BLOKED));
		}
		
		if( isUseRemote(props)) {
			postEncryptedJsonToAny(props, serverUrls, new SetAccsBlockedReq(uids, val));
		}
	}
	
	
	private static void sendBlockedReqs(Props props, Collection<ChatAccount> accs, Map<Long, ServerRow> servers, boolean val){
		HashSet<String> serverUrls = new HashSet<String>();
		HashSet<String> uids = new HashSet<String>();
		for(ChatAccount acc : accs) {
			ServerRow server = servers.get(acc.serverId);
			if(server == null) continue;
			uids.add(acc.uid);
			serverUrls.add(server.createUrl(URL_CHAT_BLOKED));
		}
		if( isUseRemote(props)) {
			postEncryptedJsonToAny(props, serverUrls, new SetAccsBlockedReq(uids, val));
		}
	}


	public static String getBlockedAccFlag(String uid) {
		return "acc-blocked-"+uid;
	}
	
	
	
	/** послать запросы на сервера чатов о паузе акков */
	public static Pair<Integer, Integer> reinitAccsPaused(Props props, MainDb db){
		try {
			
			UniversalQueries universal = db.universal;
			
			log.info("reinitAccsPaused...");
			
			PausedStateResp state = getPausedState(universal);
			

			int pausedCount = state.pausedAccs.size();
			int unpausedCount = state.unpausedAccs.size();
			log.info("found paused accs: "+pausedCount+", unpaused accs: "+unpausedCount);
			
			Map<Long, ServerRow> servers = getServersMap(universal);
			
			//send reqs
			sendPausedReqs(props, state.pausedAccs, servers, true);
			sendPausedReqs(props, state.unpausedAccs, servers, false);
			
			log.info("done");
			
			return new Pair<>(unpausedCount, pausedCount);
			
		}catch(Exception e){
			log.error("can't reinitAccsBlockedCache: "+ e);
			return null;
		}
	}
	
	private static void sendPausedReqs(Props props, Collection<ChatAccount> accs, Map<Long, ServerRow> servers, boolean val){
		HashSet<String> serverUrls = new HashSet<String>();
		HashSet<String> uids = new HashSet<String>();
		for(ChatAccount acc : accs) {
			ServerRow server = servers.get(acc.serverId);
			if(server == null) continue;
			uids.add(acc.uid);
			serverUrls.add(server.createUrl(URL_CHAT_PAUSED));
		}
		if( isUseRemote(props)) {
			postEncryptedJsonToAny(props, serverUrls, new SetAccsPausedReq(uids, val));
		}
	}
	
	
	public static class PausedStateResp {
		
		public Set<ChatAccount> pausedAccs;
		public Set<ChatAccount> unpausedAccs;
		
		public PausedStateResp(Set<ChatAccount> pausedAccs, Set<ChatAccount> unpausedAccs) {
			this.pausedAccs = pausedAccs;
			this.unpausedAccs = unpausedAccs;
		}
		
		
	}
	
	public static PausedStateResp getPausedState(UniversalQueries universal) throws SQLException{
		
		HashSet<ChatAccount> pausedAccs = new HashSet<>();
		HashSet<ChatAccount> unpausedAccs = new HashSet<>();
		
		List<ChatAccount> accs = universal.select(new GetAllChatAccounts());
		
		for (ChatAccount acc : accs) {
			if(acc.tariffId == PAUSE_TARIFF_ID) pausedAccs.add(acc);
			else unpausedAccs.add(acc);
		}
		
		return new PausedStateResp(pausedAccs, unpausedAccs);
		
	}
	


}
