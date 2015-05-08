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

import static och.api.model.BaseBean.*;
import static och.api.model.chat.account.ChatAccount.*;
import static och.api.model.chat.config.Key.*;
import static och.util.Util.*;
import och.api.model.chat.config.Key;
import och.api.remote.chats.PutAccConfigReq;
import och.comp.db.base.universal.UpdateRows;
import och.comp.db.main.table._f.Feedback_notifyOpsByEmail;
import och.comp.db.main.table._f.Name;
import och.comp.db.main.table.chat.UpdateChatAccountByUid;
import och.util.model.Pair;

public class AccConfigOps {
	
	
	public static Pair<UpdateRows, PutAccConfigReq> validateReqAndGetUpdates(String uid, Key key, Object objVal) {
		
		if(key == null) return null;
		
		
		if(key == name){
			
			String val = objVal == null? null : objVal.toString();
			validateForTextSize(val, "name", 1, MAX_NAME_SIZE);
			
			UpdateChatAccountByUid db = new UpdateChatAccountByUid(uid, new Name(val));
			PutAccConfigReq remote = new PutAccConfigReq(uid, key, val);
			return new Pair<>(db, remote);
		}
		
		 
		if(key == feedback_notifyOpsByEmail){
			
			boolean val = tryParseBool(objVal, true);
			
			UpdateChatAccountByUid db = new UpdateChatAccountByUid(uid, new Feedback_notifyOpsByEmail(val));
			PutAccConfigReq remote = new PutAccConfigReq(uid, key, String.valueOf(val));
			return new Pair<>(db, remote);
		}
		
		return null;

		
	}

}
