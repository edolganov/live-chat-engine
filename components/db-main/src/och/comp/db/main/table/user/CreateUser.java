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
package och.comp.db.main.table.user;

import static och.comp.db.main.table.MainTables.*;
import och.api.model.user.UserExt;
import och.comp.db.base.universal.CreateRow;
import och.comp.db.main.table._f.ActivationCode;
import och.comp.db.main.table._f.ActivationStateDate;
import och.comp.db.main.table._f.Email;
import och.comp.db.main.table._f.Id;
import och.comp.db.main.table._f.Login;
import och.comp.db.main.table._f.PswHash;
import och.comp.db.main.table._f.PswSalt;
import och.comp.db.main.table._f.StatusCode;

public class CreateUser extends CreateRow {

	public CreateUser(UserExt user) {
		super(users, 
			new Id(user.id),
			new Login(user.login),
			new Email(user.email),
			new PswHash(user.pswHash),
			new PswSalt(user.pswSalt),
			new StatusCode(user.getStatusCode()),
			new ActivationStateDate(user.activationStateDate),
			new ActivationCode(user.activationCode));
	}
	
	

}
