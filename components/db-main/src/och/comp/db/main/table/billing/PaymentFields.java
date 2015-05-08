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
package och.comp.db.main.table.billing;

import och.comp.db.base.universal.SelectFields;
import och.comp.db.main.table._f.Amount;
import och.comp.db.main.table._f.Comment;
import och.comp.db.main.table._f.Created;
import och.comp.db.main.table._f.Details;
import och.comp.db.main.table._f.ExternalId;
import och.comp.db.main.table._f.Id;
import och.comp.db.main.table._f.PayProvider;
import och.comp.db.main.table._f.PayStatus;
import och.comp.db.main.table._f.PayType;
import och.comp.db.main.table._f.Updated;
import och.comp.db.main.table._f.UserId;

public class PaymentFields extends SelectFields {
	
	public PaymentFields() {
		super(
			Id.class, 
			UserId.class,
			PayProvider.class,
			PayType.class,
			PayStatus.class,
			Amount.class,
			Created.class,
			Updated.class,
			ExternalId.class,
			Details.class,
			Comment.class
		);
	}

}
