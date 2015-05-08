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
package och.front.web.servlet.system_api.billing;

import static och.api.model.Const.*;
import static och.util.Util.*;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ExpectedException;
import och.api.model.EmptyReq;
import och.api.model.billing.PaymentExt;
import och.api.model.billing.PaymentResp;
import och.comp.web.annotation.RoleSecured;
import och.front.web.JsonPostServlet;

@RoleSecured
@WebServlet("/system-api/billing/confirmPaypalPay")
@SuppressWarnings("serial")
public class ConfirmPaypalPay extends JsonPostServlet<EmptyReq, GetPaymentsResp> {
	
	public ConfirmPaypalPay() {
		this.checkInputDataForEmpty = false;
	}
	
	@Override
	protected GetPaymentsResp doJsonPost(HttpServletRequest req, HttpServletResponse resp, EmptyReq data) throws Throwable {
		
		billing.paypal_finishPayment();
		
		try {
			
			BigDecimal balance = billing.getCurUserBalance();
			List<PaymentExt> list = billing.getPayments(DEFAULT_PAGE_LIMIT, 0);
			List<PaymentResp> out = convert(list, (p) -> new PaymentResp(p));
			return new GetPaymentsResp(out, balance);
			
		}catch(Throwable t){
			ExpectedException.logError(log, t, "can't get payments");
			return null;
		}
	}
}
