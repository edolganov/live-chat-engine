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
package och.front.service.chat;

import static och.util.sql.SingleTx.*;

import java.util.List;

import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.account.ChatAccountPrivileges;
import och.api.model.server.ServerRow;
import och.api.model.tariff.Tariff;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.table.chat.GetAllChatAccounts;
import och.comp.db.main.table.chat.GetChatAccount;
import och.comp.db.main.table.chat.privilege.GetAllChatAccountPrivileges;
import och.comp.db.main.table.chat.privilege.GetChatAccountPrivileges;
import och.comp.db.main.table.server.GetAllServers;
import och.comp.db.main.table.server.GetServerById;
import och.comp.db.main.table.tariff.GetAllTariffs;
import och.comp.db.main.table.tariff.GetTariffById;
import och.front.service.FrontAppContext;
import och.front.service.model.ChatsModel;

public class ReloadOps {
	
	FrontAppContext c;
	UniversalQueries universal;

	public ReloadOps(FrontAppContext c) {
		super();
		this.c = c;
		
		universal = c.db.universal;
	}

	public ChatsModel loadFullModel() throws Exception {
		
		List<ServerRow> servers = null;
		List<ChatAccount> accounts  = null;
		List<ChatAccountPrivileges> privileges = null;
		List<Tariff> tariffs = null;
		
		
		setSingleTxMode();
		try {
			
			servers = universal.select(new GetAllServers());
			tariffs = universal.select(new GetAllTariffs());
			accounts = universal.select(new GetAllChatAccounts());
			privileges = universal.select(new GetAllChatAccountPrivileges());
			
		}finally {
			closeSingleTx();
		}
		
		//fill data
		ChatsModel newM = new ChatsModel();
		newM.init(servers, accounts, privileges, tariffs);
		return newM;
	}

	public void reloadServer(ChatsModel m, Long id) throws Exception {
		
		if(id == null) return;
		
		ServerRow server = universal.selectOne(new GetServerById(id));
		if(server == null) m.removeServer(id);
		else m.putServer(server);
		
	}
	
	public void reloadNewAcc(ChatsModel m, Long ownerId, String uid)throws Exception  {
		if(ownerId == null) return;
		if(uid == null) return;
		
		ChatAccount acc = reloadAcc(m, uid);
		if(acc == null) return;
		reloadUserPrivs(m, ownerId, acc.id);
	}

	public ChatAccount reloadAcc(ChatsModel m, String uid)throws Exception  {
		
		if(uid == null) return null;
		
		ChatAccount acc = universal.selectOne(new GetChatAccount(uid));
		if(acc == null) m.removeAcc(uid);
		else m.putAcc(acc);
		return acc;
		
	}

	public void reloadTariff(ChatsModel m, Long id)throws Exception {
		
		if(id == null) return;
		
		Tariff tariff = universal.selectOne(new GetTariffById(id));
		if(tariff == null) return;
		m.putTariff(tariff);
	}

	public void reloadUserPrivs(ChatsModel m, Long userId, Long accId)throws Exception  {
		if(userId == null) return;
		if(accId == null) return;
		
		ChatAccountPrivileges privs = universal.selectOne(new GetChatAccountPrivileges(accId, userId));
		if(privs == null) m.removePrivileges(userId, accId);
		else m.putPrivs(privs);
	}
	
	

}
