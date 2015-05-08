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
package och.front.web.servlet.payment.paypal;

import static och.api.model.BaseBean.*;
import static och.api.model.user.SecurityContext.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.front.web.SimpleFrontServlet;

@WebServlet(value="/payment/paypal/ipn")
@SuppressWarnings("serial")
public class Paypal_IPN extends SimpleFrontServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//check for user
		findUserIdFromSecurityContext();
		
		String token = req.getParameter("token");
		String userPayId = req.getParameter("PayerID");
		
		validateForText(token, "token");
		validateForText(userPayId, "userPayId");
		
		billing.paypal_preparePayConfirm(userPayId, token);

		resp.sendRedirect("/cabinet?confirmPayment");
	}

}
