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

import static och.api.model.billing.PaymentStatus.*;
import static och.util.Util.*;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentExt extends PaymentBase {
	
	public long id;
	public long userId;
	public PaymentType payType;
	public Date updated;
	public String details;
	public String comment;
	
	public PaymentExt() {
		super();
	}
	
	public PaymentExt(PaymentBase base) {
		this.provider = base.provider;
		this.externalId = base.externalId;
		this.paymentStatus = base.paymentStatus;
		this.amount = base.amount;
		this.created = base.created;
	}
	
	
	public static PaymentExt createSystemBill(long id, long userId, BigDecimal amount, Date created, PaymentType payType, String details){
		BigDecimal negate = amount.abs().negate();
		PaymentExt out = createSystemPayment(id, userId, negate, created, payType, details);
		return out;
	}
	
	public static PaymentExt createSystemPayment(long id, long userId, BigDecimal amount, Date created, PaymentType payType, String details){
		
		PaymentExt payment = new PaymentExt();
		payment.id = id;
		payment.provider = PaymentProvider.SYSTEM;
		payment.paymentStatus = COMPLETED;
		payment.amount = amount;
		payment.created = created;
		payment.userId = userId;
		payment.payType = payType;
		payment.updated = created;
		payment.details = details;
		
		return payment;
	}
	

	public PaymentExt(
			long id, 
			long userId,
			PaymentProvider provider, 
			String externalId, 
			PaymentStatus paymentStatus,
			Date created, 
			Date updated, 
			BigDecimal amount) {
		super(provider, externalId, paymentStatus, amount, created);
		this.id = id;
		this.userId = userId;
		this.updated = updated;
		this.payType = PaymentType.REPLENISHMENT;
	}
	
	

	public void setId(long id) {
		this.id = id;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public void setPayType(int code) {
		setPayType(tryGetEnumByCode(code, PaymentType.class, PaymentType.UNKNOWN));
	}
	
	public void setPayType(PaymentType payType) {
		this.payType = payType;
	}
	
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "PaymentExt [id=" + id + ", userId=" + userId + ", payType="
				+ payType + ", updated=" + updated + ", details=" + details
				+ ", comment=" + comment + ", provider=" + provider
				+ ", externalId=" + externalId + ", paymentStatus="
				+ paymentStatus + ", amount=" + amount + ", created=" + created
				+ "]";
	}


	
	
	
	

}
