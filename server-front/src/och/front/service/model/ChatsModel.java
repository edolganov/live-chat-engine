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
package och.front.service.model;

import static java.util.Collections.*;
import static och.api.model.chat.account.PrivilegeType.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import och.api.exception.chat.ChatOwnerNotFoundException;
import och.api.exception.chat.NoChatAccountException;
import och.api.exception.tariff.TariffNotFoundException;
import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.ChatAccountPrivileges;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.chat.config.Key;
import och.api.model.server.ServerRow;
import och.api.model.tariff.Tariff;

import org.apache.commons.logging.Log;

public class ChatsModel {
	
	private Log log = getLog(getClass());
	private HashMap<Long, ServerRow> serversById = new HashMap<>();
	private HashMap<Long, ChatAccount> accountsById = new HashMap<>();
	private HashMap<String, ChatAccount> accountsByUid = new HashMap<>();
	private HashMap<Long, List<ChatAccountPrivileges>> privilegesByUser = new HashMap<>();
	private HashMap<Long, Tariff> tariffsById = new HashMap<Long, Tariff>();
	
	private ReadWriteLock rw = new ReentrantReadWriteLock();
	private Lock read = rw.readLock();
	private Lock write = rw.writeLock();
	
	
	public void init(
			List<ServerRow> servers, 
			List<ChatAccount> accounts, 
			List<ChatAccountPrivileges> privileges,
			List<Tariff> tariffs){
		
		write.lock();
		try {
			
			for(ServerRow server : servers){
				serversById.put(server.id, server);
			}
			for(ChatAccount account : accounts){
				accountsById.put(account.id, account);
				accountsByUid.put(account.uid, account);
			}
			for(ChatAccountPrivileges priv : privileges){
				putToListMap(privilegesByUser, priv.userId, priv);
			}
			for(Tariff t : tariffs){
				tariffsById.put(t.id, t);
			}
			
		} finally {
			write.unlock();
		}
		
	}
	
	public void putData(ServerRow server, ChatAccount account, ChatAccountPrivileges ownerPriv){
		write.lock();
		try {
			serversById.put(server.id, server);
			accountsById.put(account.id, account);
			accountsByUid.put(account.uid, account);
			putToListMap(privilegesByUser, ownerPriv.userId, ownerPriv);
		}finally {
			write.unlock();
		}
	}
	
	
	public void putServer(ServerRow server){
		write.lock();
		try {
			ServerRow clone = server.clone();
			serversById.put(server.id, clone);			
		} finally {
			write.unlock();
		}
	}
	
	public void removeServer(long serverId){
		write.lock();
		try {
			serversById.remove(serverId);
		} finally {
			write.unlock();
		}
	}
	
	
	public void putAcc(ChatAccount acc){
		write.lock();
		try {
			ChatAccount clone = acc.clone();
			accountsById.put(clone.id, clone);
			accountsByUid.put(clone.uid, clone);
		} finally {
			write.unlock();
		}
	}
	
	public void removeAcc(String accUid){
		write.lock();
		try {
			ChatAccount removed = accountsByUid.remove(accUid);
			if(removed != null){
				accountsById.remove(removed.id);
			}
		} finally {
			write.unlock();
		}
	}
	
	public void putTariff(Tariff tariff){
		write.lock();
		try {
			Tariff clone = tariff.clone();
			tariffsById.put(clone.id, clone);			
		} finally {
			write.unlock();
		}
	}
	
	
	public void updateServerFull(long serverId, boolean val){
		write.lock();
		try {
			ServerRow server = serversById.get(serverId);
			if(server == null) return;
			server.setFull(val);		
		} finally {
			write.unlock();
		}
	}
	
	
	public void putAccConfig(String accUid, Key key, Object val){
		write.lock();
		try {
			
			ChatAccount acc = accountsByUid.get(accUid);
			if(acc == null) return;
			
			if(key == Key.name){
				acc.name = val == null? null : val.toString();
			}
			else if(key == Key.feedback_notifyOpsByEmail){
				acc.setFeedback_notifyOpsByEmail(tryParseBool(val, null));
			}
			
		}finally {
			write.unlock();
		}
	}
	
	
	
	public void addPrivileges(long userId, long accId, Set<PrivilegeType> set){
		write.lock();
		try {
			ChatAccountPrivileges priv = getPrivsForUser(userId, accId, true);
			priv.privileges.addAll(set);
		}finally {
			write.unlock();
		}
	}
	
	public void putPrivs(ChatAccountPrivileges privs){
		write.lock();
		try {
			
			List<ChatAccountPrivileges> list = privilegesByUser.get(privs.userId);
			if(list == null){
				list = new ArrayList<>();
				privilegesByUser.put(privs.userId, list);
			}
			int oldIndex = getPrivsIndex(list, privs.userId, privs.accId);
			if(oldIndex > -1) list.remove(oldIndex);
			list.add(privs);
			
		}finally {
			write.unlock();
		}
	}
	
	public void removePrivileges(long userId, long accId, Set<PrivilegeType> set){
		
		write.lock();
		try {
			ChatAccountPrivileges priv = getPrivsForUser(userId, accId, false);
			if(priv == null) return;
			priv.privileges.removeAll(set);
			
			if(priv.privileges.isEmpty()) {
				removePrivilegeUnsafe(userId, accId);
			}
			
		}finally {
			write.unlock();
		}
		

	}
	
	public void removePrivileges(long userId, long accId){
		write.lock();
		try {
			removePrivilegeUnsafe(userId, accId);
		}finally {
			write.unlock();
		}
	}
	
	private void removePrivilegeUnsafe(long userId, long accId) {
		
		List<ChatAccountPrivileges> list = privilegesByUser.get(userId);
		int index = getPrivsIndex(list, userId, accId);
		if(index == -1) return;
		
		list.remove(index);
		if(list.isEmpty()) privilegesByUser.remove(userId);
	}
	
	private int getPrivsIndex(List<ChatAccountPrivileges> list, long userId, long accId){
		if(isEmpty(list)) return -1;
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i).accId == accId)
				return i;
		}
		return -1;
	}
	
	
	public void updateAccTariff(String accUid, long tariffId, Date tariffStart, int changedInDay, Long tariffPrevId){
		write.lock();
		try {
			
			ChatAccount acc = accountsByUid.get(accUid);
			if(acc == null) return;
			
			acc.tariffId = tariffId;
			acc.tariffStart = tariffStart;
			acc.tariffLastPay = tariffStart;
			acc.tariffChangedInDay = changedInDay;
			acc.tariffPrevId = tariffPrevId;
			
		}finally {
			write.unlock();
		}
	}
	
	
	public boolean setNickname(String accUid, long userId, String nickname) {
		write.lock();
		try {
			
			ChatAccount acc = accountsByUid.get(accUid);
			if(acc == null) return false;
			
			List<ChatAccountPrivileges> privs = privilegesByUser.get(userId);
			if(isEmpty(privs)) return false;
			
			ChatAccountPrivileges priv = null;
			for (ChatAccountPrivileges curPriv : privs) {
				if(curPriv.accId == acc.id){
					priv = curPriv;
					break;
				}
			}
			if(priv == null) return false;
			
			priv.nickname = nickname;
			return true;
			
			
		}finally {
			write.unlock();
		}
	}
	
	


	public ServerRow getServerByAcc(String accUid) {
		
		read.lock();
		try {
			
			ChatAccount chatAccount = accountsByUid.get(accUid);
			if(chatAccount == null) return null;
			ServerRow server = serversById.get(chatAccount.serverId);
			return server == null? null : server.clone();
			
		} finally {
			read.unlock();
		}
	}

	
	public UserAccInfo getPrivilegesForAcc(long userId, long accId) {
		read.lock();
		try {
			
			List<ChatAccountPrivileges> allUserPrivs = privilegesByUser.get(userId);
			if(allUserPrivs == null) return new UserAccInfo();
			
			for (ChatAccountPrivileges privs : allUserPrivs) {
				if(privs.accId == accId){
					return new UserAccInfo(privs);
				}
			}
			return new UserAccInfo();
			
		} finally {
			read.unlock();
		}
	}
	
	
	public UserAccInfo getPrivilegesForAcc(long userId, String accUid) {
		read.lock();
		try {
			
			List<ChatAccountPrivileges> allUserPrivs = privilegesByUser.get(userId);
			if(allUserPrivs == null) return new UserAccInfo();
			
			for (ChatAccountPrivileges privs : allUserPrivs) {
				ChatAccount acc = accountsById.get(privs.accId);
				if(acc == null) continue;
				if(acc.uid.equals(accUid)){
					return new UserAccInfo(privs);
				}
			}
			return new UserAccInfo();
			
		} finally {
			read.unlock();
		}
	}
	
	public Tariff getTariff(long tariffId){
		read.lock();
		try {
			Tariff t = tariffsById.get(tariffId);
			Tariff clone = t == null? null : t.clone();
			return clone;
		}finally {
			read.unlock();
		}
	}
	
	public Tariff findTariff(long tariffId) throws TariffNotFoundException {
		Tariff t = getTariff(tariffId);
		if(t == null) throw new TariffNotFoundException(tariffId);
		return t;
	}
	

	public List<Tariff> getPublicTariffs() {
		read.lock();
		try {
			ArrayList<Tariff> out = new ArrayList<Tariff>();
			for (Tariff t : tariffsById.values()) {
				if(t.isPublic) {
					Tariff clone = t.clone();
					out.add(clone);
				}
			}
			return out;
		}finally {
			read.unlock();
		}
	}

	
	public Map<String, Set<PrivilegeType>> getPrivilegesForAccs(long userId) {
		read.lock();
		try {
			
			List<ChatAccountPrivileges> allUserPrivs = privilegesByUser.get(userId);
			if(allUserPrivs == null) return emptyMap();
			
			Map<String, Set<PrivilegeType>> out = new HashMap<>();
			for (ChatAccountPrivileges privs : allUserPrivs) {
				ChatAccount acc = accountsById.get(privs.accId);
				if(acc == null) continue;
				out.put(acc.uid, privs.clonePrivileges());
			}
			return out;
			
		} finally {
			read.unlock();
		}
	}
	
	
	
	/**
	 * Список аккаунтов с серверами в которых юзер - оператор или модерадор или админ.
	 * Root роль не дает права получить все аккаунты.
	 */
	public List<ChatAccount> getAccountsForOperator(long userId) {
		
		read.lock();
		try {
			
			List<ChatAccountPrivileges> privs = privilegesByUser.get(userId);
			if(isEmpty(privs)) return emptyList();
			

			HashSet<ChatAccount> set = new HashSet<>();
			for (ChatAccountPrivileges priv : privs) {
				
				if( priv.privileges.isEmpty()) continue;
				
				ChatAccount acc = accountsById.get(priv.accId);
				if(acc == null) continue;
				if(set.contains(acc)) continue;
				
				ServerRow server = serversById.get(acc.serverId);
				if(server == null) continue;
				
				ChatAccount clone = acc.clone();
				clone.server = server.clone();
				set.add(clone);
			}
			
			ArrayList<ChatAccount> out = new ArrayList<>(set);
			sort(out);
			return out;
			
		}finally {
			read.unlock();
		}
	}
	
	
	/**
	 * Список uid аккаунтов в которых юзер - оператор или модерадор или админ.
	 * Root роль не дает права получить все аккаунты.
	 */
	public Set<String> getAccountsUidsForAnyPriv(long userId) {
		return getAccountsUidsFor(userId);
	}
	
	/**
	 * Список uid аккаунтов в которых юзер - админ.
	 * Root роль не дает права получить все аккаунты.
	 */
	public Set<String> getAccountsUidsFor(long userId, PrivilegeType... targetPrivs) {
		
		read.lock();
		try {
			
			List<ChatAccountPrivileges> privs = privilegesByUser.get(userId);
			if(isEmpty(privs)) return set();
			

			HashSet<String> set = new HashSet<>();
			for (ChatAccountPrivileges priv : privs) {
				
				Set<PrivilegeType> curAccPrivs = priv.privileges;
				if( curAccPrivs.isEmpty()) continue;
				
				boolean validAcc = targetPrivs.length == 0? true : false;
				for(PrivilegeType targetPriv : targetPrivs){
					if(curAccPrivs.contains(targetPriv)){
						validAcc = true;
						break;
					}
				}
				if( ! validAcc) continue;
				
				ChatAccount acc = accountsById.get(priv.accId);
				if(acc == null) continue;
				if(set.contains(acc)) continue;
				
				ServerRow server = serversById.get(acc.serverId);
				if(server == null) continue;
				
				set.add(acc.uid);
			}
			
			return set;
			
		}finally {
			read.unlock();
		}
	}
	
	

	
	public ServerRow getServer(long serverId){
		read.lock();
		try {
			ServerRow server = serversById.get(serverId);
			return server == null? null : server.clone();
		} finally {
			read.unlock();
		}
	}
	
	public List<ServerRow> getServers(){
		read.lock();
		try {
			
			ArrayList<ServerRow> out = new ArrayList<>();
			for (ServerRow s : serversById.values()) {
				ServerRow clone = s.clone();
				out.add(clone);
			}
			return out;
			
		}finally {
			read.unlock();
		}
	}

	
	public List<Long> getNotFullServersId(){
		
		read.lock();
		try {
			ArrayList<Long> out = new ArrayList<Long>();
			for (ServerRow server : serversById.values()) {
				if( ! server.isFull) out.add(server.id);
			}
			return out;
		}finally {
			read.unlock();
		}
	}
	
	public int getServerAccountsCount(long serverId){
		read.lock();
		try {
			
			int count = 0;
			for (ChatAccount acc : accountsById.values()) {
				if(acc.serverId == serverId){
					count++;
				}
			}
			return count;
			
		}finally {
			read.unlock();
		}
	}
	
	
	public List<ChatAccount> getServerAccounts(long serverId){
		read.lock();
		try {
			
			ArrayList<ChatAccount> out = new ArrayList<>();
			for (ChatAccount acc : accountsById.values()) {
				if(acc.serverId == serverId){
					ChatAccount clone = acc.clone();
					out.add(clone);
				}
			}
			return out;
			
		}finally {
			read.unlock();
		}
	}
	
	public ChatAccount findAccount(String accUid, boolean withServer) throws NoChatAccountException {
		ChatAccount acc = getAccount(accUid, withServer);
		if(acc == null) throw new NoChatAccountException();
		return acc;
	}
	
	public ChatAccount getAccount(String accUid, boolean withServer){
		read.lock();
		try {
			ChatAccount acc = accountsByUid.get(accUid);
			if(acc == null) return null;
			ServerRow server = serversById.get(acc.serverId);
			if(server == null) return null;
			
			ChatAccount clone = acc.clone();
			if(withServer) clone.server = server.clone();
			return clone;
			
		}finally {
			read.unlock();
		}
	}
	
	public ChatAccount getAccount(long accId, boolean withServer){
		read.lock();
		try {
			ChatAccount acc = accountsById.get(accId);
			if(acc == null) return null;
			ServerRow server = serversById.get(acc.serverId);
			if(server == null) return null;
			
			ChatAccount clone = acc.clone();
			if(withServer) clone.server = server.clone();
			return clone;
			
		}finally {
			read.unlock();
		}
	}
	
	public Map<Long, UserAccInfo> getAccUsers(String accUid){
		return getAccUsersWithPrivs(accUid);
	}
	
	public Map<Long, UserAccInfo> getAccOperators(String accUid){
		return getAccUsersWithPrivs(accUid, CHAT_OPERATOR);
	}
	
	public Long getAccOwner(String accUid){
		Map<Long, UserAccInfo> users = getAccUsersWithPrivs(accUid, CHAT_OWNER);
		if(isEmpty(users)) return null;
		if(users.size() > 1) log.warn("account "+accUid+" has many owners: "+users.keySet());
		return firstFrom(users.keySet());
	}
	
	public long findAccOwner(String accUid) throws ChatOwnerNotFoundException {
		Long ownerId = getAccOwner(accUid);
		if(ownerId == null) throw new ChatOwnerNotFoundException();
		return ownerId;
	}
	
	
	public Map<Long, UserAccInfo> getAccUsersWithPrivs(String accUid, PrivilegeType... validPrivs){
		read.lock();
		try {
			
			ChatAccount acc = accountsByUid.get(accUid);
			if(acc == null) return emptyMap();
			
			HashMap<Long, UserAccInfo> out = new HashMap<>();
			
			nextUser: for (Entry<Long, List<ChatAccountPrivileges>> entry : privilegesByUser.entrySet()) {
				List<ChatAccountPrivileges> privs = entry.getValue();
				if(isEmpty(privs)) continue;
				for (ChatAccountPrivileges priv : privs) {
					
					//user of acc
					if(priv.accId == acc.id){
						
						//filter by priv
						if( ! isEmpty(validPrivs)){
							boolean validUser = false;
							for(PrivilegeType validPriv : validPrivs){
								if(priv.privileges.contains(validPriv)){
									validUser = true;
									break;
								}
							}
							if(!validUser) continue nextUser;
						}
						
						Long userId = entry.getKey();
						UserAccInfo userAccInfo = new UserAccInfo(priv);
						out.put(userId, userAccInfo);
						
						continue nextUser;
					}
				}
			}
			
			return out;
			
			
		}finally {
			read.unlock();
		}
	}
	
	
	
	public Map<String, Long> getAllAccOwners(){
		read.lock();
		try {
			
			HashMap<String, Long> accOwners = new HashMap<String, Long>();
			
			nextUser: for (Entry<Long, List<ChatAccountPrivileges>> entry : privilegesByUser.entrySet()) {
				List<ChatAccountPrivileges> privs = entry.getValue();
				if(isEmpty(privs)) continue;
				for (ChatAccountPrivileges priv : privs) {
					if(priv.privileges.contains(CHAT_OWNER)){
						Long accId = priv.accId;
						ChatAccount acc = accountsById.get(accId);
						if(acc != null){
							accOwners.put(acc.uid, priv.userId);
							continue nextUser;
						}
					}
				}
			}
			
			return accOwners;
		}finally {
			read.unlock();
		}
	}

	
	
	
	
	private ChatAccountPrivileges getPrivsForUser(long userId, long accId, boolean createIfNull) {
		
		List<ChatAccountPrivileges> list = privilegesByUser.get(userId);
		if(isEmpty(list)){
			if( ! createIfNull) return null;
			list = new ArrayList<>();
			privilegesByUser.put(userId, list);
		}
		
		ChatAccountPrivileges found = null;
		for(ChatAccountPrivileges candidat : list){
			if(candidat.accId == accId){
				found = candidat;
				break;
			}
		}
		
		if(found == null && createIfNull){
			found = new ChatAccountPrivileges(userId, accId);
			list.add(found);
		}
		
		return found;
	}



	


}
