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
package och.comp.db.main.table.remtoken;

import static och.comp.db.main.table.MainTables.*;

import java.util.Date;

import och.comp.db.base.universal.CreateRow;
import och.comp.db.main.table._f.LastVisited;
import och.comp.db.main.table._f.TokenHash;
import och.comp.db.main.table._f.TokenSalt;
import och.comp.db.main.table._f.Uid;
import och.comp.db.main.table._f.UserId;

public class CreateRemToken extends CreateRow {

	public CreateRemToken(String uid, byte[] hash, String salt, long userId) {
		super(rem_tokens, 
				new Uid(uid),
				new TokenHash(hash),
				new TokenSalt(salt),
				new UserId(userId),
				new LastVisited(new Date()));
	}

}
