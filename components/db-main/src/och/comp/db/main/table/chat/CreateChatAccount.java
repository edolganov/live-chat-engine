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
package och.comp.db.main.table.chat;

import static och.comp.db.main.table.MainTables.*;

import java.util.Date;

import och.api.model.chat.account.ChatAccount;
import och.comp.db.base.universal.CreateRow;
import och.comp.db.main.table._f.Created;
import och.comp.db.main.table._f.Id;
import och.comp.db.main.table._f.Name;
import och.comp.db.main.table._f.ServerId;
import och.comp.db.main.table._f.TariffId;
import och.comp.db.main.table._f.TariffLastPay;
import och.comp.db.main.table._f.TariffStart;
import och.comp.db.main.table._f.Uid;

public class CreateChatAccount extends CreateRow {
	
	public CreateChatAccount(ChatAccount acc){
		this(acc.id, 
			acc.uid, 
			acc.serverId,
			acc.name, 
			acc.created, 
			acc.tariffId, 
			acc.tariffStart,
			acc.tariffLastPay);
	}

	public CreateChatAccount(
			long id, 
			String uid, 
			long serverId, 
			String name, 
			Date created, 
			long tariffId, 
			Date tarriffStart, 
			Date tariffLastPay) {
		super(chat_accounts, 
			new Id(id),
			new Uid(uid),
			new Created(created),
			new ServerId(serverId),
			new Name(name),
			new TariffId(tariffId),
			new TariffStart(tarriffStart),
			new TariffLastPay(tariffLastPay));
	}
	
	

}
