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
package och.api.model.billing;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentResp {
	
	public long id;
	public PaymentStatus status;
	public BigDecimal amount;
	public Date updated;
	public Date created;
	public int payType;
	public String comment;
	
	public PaymentResp(PaymentExt p) {
		this.id = p.id;
		this.status = p.paymentStatus;
		this.amount = p.amount;
		this.updated = p.updated;
		this.created = p.created;
		this.payType = p.payType.code;
		this.comment = p.comment;
	}

}
