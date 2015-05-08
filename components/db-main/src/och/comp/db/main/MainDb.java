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
package och.comp.db.main;

import static och.api.model.PropKey.*;
import static och.util.Util.*;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import och.api.model.chat.account.ChatAccount;
import och.api.model.chat.host.ClientHost;
import och.comp.db.base.BaseDb;
import och.comp.db.base.mybatis.CommitOnCloseSession;
import och.comp.db.main.mapper.ChatMapper;
import och.comp.db.main.mapper.ClientHostMapper;
import och.comp.db.main.mapper.RemTokenMapper;
import och.service.props.Props;

import org.apache.commons.logging.Log;

public class MainDb extends BaseDb {
	
	public final RemTokenMapperWrapper remTokens = new RemTokenMapperWrapper();
	public final ChatMapperWrapper chats = new ChatMapperWrapper();
	public final ClientHostMapperWrapper clientHosts = new ClientHostMapperWrapper();
	
	private Log log = getLog(getClass());

	public MainDb(DataSource ds, Props props) throws Exception {
		
		super(ds, props);
		
		if(props.getBoolVal(db_skipDbCreation)) {
			log.info("skip DB creation");
			return;
		}
		
		if(props.getBoolVal(db_reinit)){
			reinitDB();
		} else {			
			createTables();
		}
	}
	
	
	public class RemTokenMapperWrapper implements RemTokenMapper {
		
		@Override
		public int deleteOldIfMaxCount(long userId, int maxCount, int deleteCount) {
			try(CommitOnCloseSession s = openCommitOnCloseSession()){
				return s.getMapper(RemTokenMapper.class).deleteOldIfMaxCount(userId, maxCount, deleteCount);
			}
		}
	}
	
	public class ChatMapperWrapper implements ChatMapper {

		@Override
		public List<String> getOwnerAccs(long ownerId) {
			try(CommitOnCloseSession s = openCommitOnCloseSession()){
				return s.getMapper(ChatMapper.class).getOwnerAccs(ownerId);
			}
		}
		
		@Override
		public int updateOwnersAccsLastPay(long ownerId, Date tariffLastPay) {
			try(CommitOnCloseSession s = openCommitOnCloseSession()){
				return s.getMapper(ChatMapper.class).updateOwnersAccsLastPay(ownerId, tariffLastPay);
			}
		}

		@Override
		public List<ChatAccount> getOwnerAccsInfo(long ownerId) {
			try(CommitOnCloseSession s = openCommitOnCloseSession()){
				return s.getMapper(ChatMapper.class).getOwnerAccsInfo(ownerId);
			}
		}
		
	}
	
	public class ClientHostMapperWrapper implements ClientHostMapper {

		@Override
		public List<ClientHost> getHostsWithOwners(boolean important, int minOwnersCount) {
			try(CommitOnCloseSession s = openCommitOnCloseSession()){
				return s.getMapper(ClientHostMapper.class).getHostsWithOwners(important, minOwnersCount);
			}
		}
		
	}

}
