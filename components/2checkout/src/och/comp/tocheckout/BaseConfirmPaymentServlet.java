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

import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ValidationException;

import org.apache.commons.logging.Log;

public abstract class BaseConfirmPaymentServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	Log log = getLog(getClass());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String key = req.getParameter("key");
		if(isEmpty(key)) {
			processErrorEnd(req, resp, "validation error: empty key");
			return;
		}
		
		boolean cardProcessed = "Y".equals(req.getParameter("credit_card_processed"));
		if(!cardProcessed) {
			processErrorEnd(req, resp, "validation error: credit card not processed");
			return;
		}
		
		String total = req.getParameter("total");
		BigDecimal totalVal = tryParseBigDecimal(total, null);
		if(totalVal == null) {
			processErrorEnd(req, resp, "validation error: invalid total val: "+total);
			return;
		}
		
		String orderNum = req.getParameter("order_number");
		String invoiceId = req.getParameter("invoice_id");
		
		
		//check inputs
		String secretWord = getSecretWord(req, resp);
		String sellerId = getSellerId(req, resp);
		String reqKey = md5HashStr(secretWord + sellerId + orderNum + total);
		if(isEmpty(reqKey)) return;
		if( ! reqKey.toUpperCase().equals(key.toUpperCase())) {
			processErrorEnd(req, resp, "validation error: invalid key: "+key);
			return;
		}
	
		try {
			String redirectUrl = createPayment(req, resp, orderNum, invoiceId, new BigDecimal(total));
			resp.sendRedirect(redirectUrl);
		}
		catch(ValidationException e){
			processErrorEnd(req, resp, "validation error: can't create payment: "+e);
		}
		catch(Exception e){
			log.error("can't createPayment", e);
			processErrorEnd(req, resp, "server error: can't create payment: "+e);
		}
		
		
	}
	
	protected abstract String getSecretWord(HttpServletRequest req, HttpServletResponse resp);
	
	protected abstract String getSellerId(HttpServletRequest req, HttpServletResponse resp);
	
	protected abstract String createPayment(
			HttpServletRequest req, 
			HttpServletResponse resp,
			String orderNum,
			String invoiceId,
			BigDecimal total) throws Exception;
	
	protected abstract void processErrorEnd(
			HttpServletRequest req, 
			HttpServletResponse resp,
			String msg) throws ServletException, IOException;

}
