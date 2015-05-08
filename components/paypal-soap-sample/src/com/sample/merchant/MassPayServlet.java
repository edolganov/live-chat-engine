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
package com.sample.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import urn.ebay.api.PayPalAPI.MassPayReq;
import urn.ebay.api.PayPalAPI.MassPayRequestItemType;
import urn.ebay.api.PayPalAPI.MassPayRequestType;
import urn.ebay.api.PayPalAPI.MassPayResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.ReceiverInfoCodeType;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.sample.util.Configuration;

/**
 * Servlet implementation class MassPayServlet
 */
public class MassPayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MassPayServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		getServletConfig().getServletContext()
				.getRequestDispatcher("/MassPay/MassPay.jsp")
				.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		session.setAttribute("url", request.getRequestURI());
		session.setAttribute("relatedUrl",
				"<ul><li><a href='MassPay'>MassPay</a></li></ul>");
		MassPayReq req = new MassPayReq();

		List<MassPayRequestItemType> massPayItem = new ArrayList<MassPayRequestItemType>();
		BasicAmountType amount1 = new BasicAmountType(
				CurrencyCodeType.fromValue(request
						.getParameter("currencyCode1")),
				request.getParameter("amount1"));
		BasicAmountType amount2 = new BasicAmountType(
				CurrencyCodeType.fromValue(request
						.getParameter("currencyCode2")),
				request.getParameter("amount2"));
		BasicAmountType amount3 = new BasicAmountType(
				CurrencyCodeType.fromValue(request
						.getParameter("currencyCode3")),
				request.getParameter("amount3"));
		MassPayRequestItemType item1 = new MassPayRequestItemType(amount1);
		MassPayRequestItemType item2 = new MassPayRequestItemType(amount2);
		MassPayRequestItemType item3 = new MassPayRequestItemType(amount3);
		/*
		 *  (Optional) How you identify the recipients of payments in this call to MassPay. 
		 *   It is one of the following values:
    			EmailAddress
    			UserID
    			PhoneNumber
		 */
		if (request.getParameter("receiverInfoCode").equalsIgnoreCase(
				"EmailAddress")) {
			item1.setReceiverEmail(request.getParameter("mail1"));
			item2.setReceiverEmail(request.getParameter("mail2"));
			item3.setReceiverEmail(request.getParameter("mail3"));
		} else if (request.getParameter("receiverInfoCode").equalsIgnoreCase(
				"PhoneNumber")) {
			item1.setReceiverPhone(request.getParameter("phone1"));
			item2.setReceiverPhone(request.getParameter("phone2"));
			item3.setReceiverPhone(request.getParameter("phone3"));
		} else if (request.getParameter("receiverInfoCode").equalsIgnoreCase(
				"UserID")) {
			item1.setReceiverID(request.getParameter("id1"));
			item2.setReceiverID(request.getParameter("id2"));
			item3.setReceiverID(request.getParameter("id3"));
		}
		massPayItem.add(item1);
		massPayItem.add(item2);
		massPayItem.add(item3);
		MassPayRequestType reqType = new MassPayRequestType(massPayItem);
		reqType.setReceiverType(ReceiverInfoCodeType.fromValue(request
				.getParameter("receiverInfoCode")));
		req.setMassPayRequest(reqType);
		
		// Configuration map containing signature credentials and other required configuration.
		// For a full list of configuration parameters refer in wiki page.
		// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
		Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
		
		// Creating service wrapper object to make an API call by loading configuration map.
		PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configurationMap);
		
		try {
			response.setContentType("text/html");
			MassPayResponseType resp = service.massPay(req);
			if (resp != null) {
				session.setAttribute("lastReq", service.getLastRequest());
				session.setAttribute("lastResp", service.getLastResponse());
				if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
					Map<Object, Object> map = new LinkedHashMap<Object, Object>();
					map.put("Ack", resp.getAck());
					session.setAttribute("map", map);
					response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
				} else {
					session.setAttribute("Error", resp.getErrors());
					response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
				}
			}

		} catch (SSLConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidResponseDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientActionRequiredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingCredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
