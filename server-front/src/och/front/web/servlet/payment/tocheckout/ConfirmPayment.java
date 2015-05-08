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
package och.front.web.servlet.payment.tocheckout;

import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.user.SecurityContext.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.comp.tocheckout.BaseConfirmPaymentServlet;
import och.front.service.FrontApp;
import och.front.web.FrontAppProvider;

@WebServlet(value="/payment/2checkout/confirm")
@SuppressWarnings("serial")
public class ConfirmPayment extends BaseConfirmPaymentServlet{
	
	FrontApp app;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		app = FrontAppProvider.get(getServletContext());
	}

	@Override
	protected String getSecretWord(HttpServletRequest req, HttpServletResponse resp) {
		return app.props.getStrVal(toCheckout_secretWord);
	}

	@Override
	protected String getSellerId(HttpServletRequest req, HttpServletResponse resp) {
		return app.props.getStrVal(toCheckout_accId);
	}

	@Override
	protected String createPayment(
			HttpServletRequest req,
			HttpServletResponse resp, 
			String orderNum, 
			String invoiceId,
			BigDecimal total) throws Exception {
		
		findUserIdFromSecurityContext();
		
		String token = req.getParameter("och_token");
		validateForText(token, "och_token");
		
		app.billing.tochekout_finishPayment(token, orderNum+"-"+invoiceId);
		
		return "/cabinet?confirmPayment";
	}

	@Override
	protected void processErrorEnd(
			HttpServletRequest req,
			HttpServletResponse resp, 
			String msg) throws ServletException, IOException {
		
		resp.sendRedirect("/problem?errorMsg="+urlEncode(msg));
		
	}



}
