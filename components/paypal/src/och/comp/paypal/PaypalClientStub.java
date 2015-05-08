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
package och.comp.paypal;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import och.api.model.PropKey;
import och.api.model.billing.PayData;
import och.api.model.billing.PaymentBase;
import och.api.model.billing.PaymentProvider;
import och.api.model.billing.PaymentStatus;

public class PaypalClientStub implements PaypalClient {

	public static final String STUB_TOKEN = "stub";
	public static final BigDecimal defPayAmount = new BigDecimal(12);
	
	public volatile List<PaymentBase> paymentHistory = null;
	public volatile PaymentBase payment = null;
	public volatile String paymentId = null;
	public BigDecimal payAmount = defPayAmount;
	
	@Override
	public String getKey() {
		return PropKey.paypal_key.strDefVal();
	}

	@Override
	public PayData payWithAccReq(BigDecimal val, long userId) throws Exception {
		return new PayData(STUB_TOKEN, "http://ya.ru");
	}

	@Override
	public PaymentBase finishPayment(String token, String payerId, BigDecimal val, long userId)
			throws Exception {
		
		PaymentBase out = new PaymentBase();
		out.paymentStatus = PaymentStatus.COMPLETED;
		out.provider = PaymentProvider.SYSTEM;
		out.externalId = "stub";
		out.created = new Date();
		out.amount = payAmount;
		
		return out;
	}

	@Override
	public void addListener(PaypalClientListener l) {}

	@Override
	public List<PaymentBase> getPaymentHistory(int daysBefore) throws Exception {
		return paymentHistory;
	}

	@Override
	public PaymentBase getPayment(Date startDate, String paymentId) throws Exception {
		if(this.paymentId != null && ! this.paymentId.equals(paymentId)) return null;
		return payment;
	}
	
	public void clearStub(){
		paymentHistory = null;
		payment = null;
	}



}
