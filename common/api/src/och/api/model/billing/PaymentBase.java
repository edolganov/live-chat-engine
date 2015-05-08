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

import static och.util.Util.*;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentBase {
	
	public PaymentProvider provider;
	public String externalId;
	public PaymentStatus paymentStatus;
	public BigDecimal amount;
	public Date created;
	

	public PaymentBase() {
		super();
	}
	
	public PaymentBase(String externalId, PaymentStatus paymentStatus) {
		this.externalId = externalId;
		this.paymentStatus = paymentStatus;
	}
	
	public PaymentBase(PaymentProvider provider, String externalId,
			PaymentStatus paymentStatus, BigDecimal amount, Date created) {
		super();
		this.provider = provider;
		this.externalId = externalId;
		this.paymentStatus = paymentStatus;
		this.amount = amount;
		this.created = created;
	}




	public void setPayProvider(int code) {
		setPayProvider(tryGetEnumByCode(code, PaymentProvider.class, PaymentProvider.SYSTEM));
	}
	
	public void setPayProvider(PaymentProvider provider) {
		this.provider = provider;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	public void setPayStatus(int code) {
		setPayStatus(tryGetEnumByCode(code, PaymentStatus.class, PaymentStatus.ERROR));
	}

	public void setPayStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setCreated(Date created) {
		this.created = created;
	}


	public static String getBalanceCacheKey(long userId) {
		return "balance-"+userId;
	}

	@Override
	public String toString() {
		return "PaymentTransaction [provider=" + provider + ", externalId="
				+ externalId + ", paymentStatus=" + paymentStatus + ", amount="
				+ amount + ", created=" + created + "]";
	}
	
	

}
