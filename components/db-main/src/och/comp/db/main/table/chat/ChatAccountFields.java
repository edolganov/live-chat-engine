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

import och.comp.db.base.universal.SelectFields;
import och.comp.db.main.table._f.Created;
import och.comp.db.main.table._f.Feedback_notifyOpsByEmail;
import och.comp.db.main.table._f.Id;
import och.comp.db.main.table._f.Name;
import och.comp.db.main.table._f.ServerId;
import och.comp.db.main.table._f.TariffChangedInDay;
import och.comp.db.main.table._f.TariffId;
import och.comp.db.main.table._f.TariffLastPay;
import och.comp.db.main.table._f.TariffPrevId;
import och.comp.db.main.table._f.TariffStart;
import och.comp.db.main.table._f.Uid;

public class ChatAccountFields extends SelectFields{

	public ChatAccountFields() {
		super(
			Id.class, 
			Uid.class,
			Created.class,
			Name.class,
			ServerId.class,
			TariffId.class,
			TariffStart.class,
			TariffLastPay.class,
			TariffChangedInDay.class,
			TariffPrevId.class,
			Feedback_notifyOpsByEmail.class
			);
	}
	
	

}
