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
package och.comp.tocheckout;

import static och.api.model.PropKey.*;
import static och.api.model.billing.PaymentProvider.*;

import java.math.BigDecimal;
import java.util.Date;

import och.api.model.billing.PayData;
import och.api.model.billing.PaymentBase;
import och.api.model.billing.PaymentStatus;
import och.service.billing.BillingProvider;
import och.service.props.Props;

public class ToCheckoutProvider implements BillingProvider {
	
	Props props;
	
	public ToCheckoutProvider(Props props) {
		this.props = props;
	}

	@Override
	public String getKey() {
		return props.getStrVal(toCheckout_key);
	}

	@Override
	public PayData payWithAccReq(BigDecimal val, long userId) throws Exception {
			
		PayData out = new PayData();
		out.accId = props.getStrVal(toCheckout_accId);
		out.redirectUrl = props.getBoolVal(toCheckout_demoMode)? 
				props.getStrVal(toCheckout_redirectUrl_demo) 
				: props.getStrVal(toCheckout_redirectUrl_prod);
		
		return out;
	}
	
	public PaymentBase finishPayment(BigDecimal val, long userId, String txId) {
		PaymentBase out = new PaymentBase();
		out.provider = TO_CHECKOUT;
		out.externalId = txId;
		out.created = new Date();
		out.paymentStatus = PaymentStatus.COMPLETED;
		out.amount = val;
		return out;
	}
	

}
