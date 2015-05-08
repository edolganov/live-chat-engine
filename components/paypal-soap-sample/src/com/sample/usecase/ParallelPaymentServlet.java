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
package com.sample.usecase;

import com.sample.util.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentInfoType;
import urn.ebay.apis.eBLBaseComponents.SellerDetailsType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;

public class ParallelPaymentServlet extends HttpServlet {

	private static final long serialVersionUID = 1239098098123L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		
		if(request.getRequestURI().contains("SetExpressCheckoutForParallelPayment")){
			getServletConfig().getServletContext().getRequestDispatcher("/usecase_jsp/SetExpressCheckoutForParallelPayment.jsp")
			.forward(request, response);
		}else if(request.getRequestURI().contains("DoExpressCheckoutForParallelPayment")){
			getServletConfig().getServletContext().getRequestDispatcher("/usecase_jsp/DoExpressCheckoutForParallelPayment.jsp")
			.forward(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		session.setAttribute("url", request.getRequestURI());
		response.setContentType("text/html");

		// Configuration map containing signature credentials and other required
		// configuration.
		// For a full list of configuration parameters refer at
		// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
		Map<String, String> configurationMap = Configuration
				.getAcctAndConfig();

		// Creating service wrapper object to make an API call by loading
		// configuration map.
		PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(
				configurationMap);

		// # SetExpressCheckout API
		// The SetExpressCheckout API operation initiates an Express Checkout
		// transaction.
		// This sample code uses Merchant Java SDK to make API call. You can
		// download the SDKs
		// [here](https://github.com/paypal/sdk-packages/tree/gh-pages/merchant-sdk/java)
		if (request.getRequestURI().contains("SetExpressCheckoutForParallelPayment")) { // *************** SetExpressCheckout for parallel payment ************************
			SetExpressCheckoutRequestType setExpressCheckoutReq = new SetExpressCheckoutRequestType();
			SetExpressCheckoutRequestDetailsType details = new SetExpressCheckoutRequestDetailsType();

			StringBuffer url = new StringBuffer();
			url.append("http://");
			url.append(request.getServerName());
			url.append(":");
			url.append(request.getServerPort());
			url.append(request.getContextPath());

			String returnURL = url.toString() + "/DoExpressCheckoutForParallelPayment";
			String cancelURL = url.toString() + "/SetExpressCheckoutForParallelPayment";

			/*
			 * (Required) URL to which the buyer's browser is returned after
			 * choosing to pay with PayPal. For digital goods, you must add
			 * JavaScript to this page to close the in-context experience. Note:
			 * PayPal recommends that the value be the final review page on
			 * which the buyer confirms the order and payment or billing
			 * agreement. Character length and limitations: 2048 single-byte
			 * characters
			 */
			details.setReturnURL(returnURL + "?currencyCodeType="
					+ request.getParameter("currencyCode"));

			details.setCancelURL(cancelURL);
			/*
			 * (Optional) Email address of the buyer as entered during checkout.
			 * PayPal uses this value to pre-fill the PayPal membership sign-up
			 * portion on the PayPal pages. Character length and limitations:
			 * 127 single-byte alphanumeric characters
			 */
			details.setBuyerEmail(request.getParameter("buyerEmail"));

			SellerDetailsType seller_1 = new SellerDetailsType();
			seller_1.setPayPalAccountID(request.getParameter("receiverEmail_0"));
			PaymentDetailsType paymentDetails_1 = new PaymentDetailsType();
			paymentDetails_1.setSellerDetails(seller_1);
			paymentDetails_1.setPaymentRequestID(request
					.getParameter("requestId_0"));
			BasicAmountType orderTotal_1 = new BasicAmountType();
			orderTotal_1.setCurrencyID(CurrencyCodeType.fromValue(request
					.getParameter("currencyCode")));
			orderTotal_1.setValue(request.getParameter("orderTotal"));
			paymentDetails_1.setOrderTotal(orderTotal_1);
			paymentDetails_1.setPaymentAction(PaymentActionCodeType
					.fromValue(request.getParameter("paymentAction")));

			SellerDetailsType seller_2 = new SellerDetailsType();
			seller_2.setPayPalAccountID(request.getParameter("receiverEmail_1"));
			PaymentDetailsType paymentDetails_2 = new PaymentDetailsType();
			paymentDetails_2.setSellerDetails(seller_2);
			paymentDetails_2.setPaymentRequestID(request
					.getParameter("requestId_1"));
			BasicAmountType orderTotal_2 = new BasicAmountType();
			orderTotal_2.setCurrencyID(CurrencyCodeType.fromValue(request
					.getParameter("currencyCode")));
			orderTotal_2.setValue(request.getParameter("orderTotal"));
			paymentDetails_2.setOrderTotal(orderTotal_2);
			paymentDetails_2.setPaymentAction(PaymentActionCodeType
					.fromValue(request.getParameter("paymentAction")));

			List<PaymentDetailsType> payDetails = new ArrayList<PaymentDetailsType>();
			payDetails.add(paymentDetails_1);
			payDetails.add(paymentDetails_2);

			details.setPaymentDetails(payDetails);

			setExpressCheckoutReq.setSetExpressCheckoutRequestDetails(details);

			SetExpressCheckoutReq expressCheckoutReq = new SetExpressCheckoutReq();
			expressCheckoutReq
					.setSetExpressCheckoutRequest(setExpressCheckoutReq);
			SetExpressCheckoutResponseType setExpressCheckoutResponse = null;

			try {
				setExpressCheckoutResponse = service
						.setExpressCheckout(expressCheckoutReq);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (setExpressCheckoutResponse != null) {
				session.setAttribute("lastReq", service.getLastRequest());
				session.setAttribute("lastResp", service.getLastResponse());

				if (setExpressCheckoutResponse.getAck().toString()
						.equalsIgnoreCase("SUCCESS")) {
					session.setAttribute("ecToken",
							setExpressCheckoutResponse.getToken());
					response.sendRedirect("https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="
							+ setExpressCheckoutResponse.getToken());
				} else {

					session.setAttribute("Error",
							setExpressCheckoutResponse.getErrors());
					response.sendRedirect(this.getServletContext()
							.getContextPath() + "/Error.jsp");
				}

			}
		}else if (request.getRequestURI().contains("DoExpressCheckoutForParallelPayment")) {  // *************** DoExpressCheckout for parallel payment ************************
				

				DoExpressCheckoutPaymentRequestType doCheckoutPaymentRequestType = new DoExpressCheckoutPaymentRequestType();
				DoExpressCheckoutPaymentRequestDetailsType doEcdetails = new DoExpressCheckoutPaymentRequestDetailsType();
				/*
				 * A timestamped token by which you identify to PayPal that you
				 * are processing this payment with Express Checkout. The token
				 * expires after three hours. If you set the token in the
				 * SetExpressCheckout request, the value of the token in the
				 * response is identical to the value in the request. Character
				 * length and limitations: 20 single-byte characters
				 */
				doEcdetails.setToken(request.getParameter("token"));
				/*
				 * Unique PayPal Customer Account identification number.
				 * Character length and limitations: 13 single-byte alphanumeric
				 * characters
				 */
				doEcdetails.setPayerID(request.getParameter("payerID"));
				/*
				 * (Optional) How you want to obtain payment. If the transaction
				 * does not include a one-time purchase, this field is ignored.
				 * It is one of the following values: Sale - This is a final
				 * sale for which you are requesting payment (default).
				 * Authorization - This payment is a basic authorization subject
				 * to settlement with PayPal Authorization and Capture. Order -
				 * This payment is an order authorization subject to settlement
				 * with PayPal Authorization and Capture. Note: You cannot set
				 * this field to Sale in SetExpressCheckout request and then
				 * change this value to Authorization or Order in the
				 * DoExpressCheckoutPayment request. If you set the field to
				 * Authorization or Order in SetExpressCheckout, you may set the
				 * field to Sale. Character length and limitations: Up to 13
				 * single-byte alphabetic characters This field is deprecated.
				 * Use PaymentAction in PaymentDetailsType instead.
				 */
				doEcdetails.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentAction")));
				
				SellerDetailsType seller_1 = new SellerDetailsType();
				seller_1.setPayPalAccountID(request.getParameter("receiverEmail_0"));
				PaymentDetailsType paymentDetails_1 = new PaymentDetailsType();
				paymentDetails_1.setSellerDetails(seller_1);
				paymentDetails_1.setPaymentRequestID(request.getParameter("requestId_0"));
				BasicAmountType orderTotal_1 = new BasicAmountType();
				orderTotal_1.setCurrencyID(CurrencyCodeType.fromValue(request.getParameter("currencyCode")));
				orderTotal_1.setValue(request.getParameter("orderTotal"));
				paymentDetails_1.setOrderTotal(orderTotal_1);
				paymentDetails_1.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentAction")));

				SellerDetailsType seller_2 = new SellerDetailsType();
				seller_2.setPayPalAccountID(request.getParameter("receiverEmail_1"));
				PaymentDetailsType paymentDetails_2 = new PaymentDetailsType();
				paymentDetails_2.setSellerDetails(seller_2);
				paymentDetails_2.setPaymentRequestID(request.getParameter("requestId_1"));
				BasicAmountType orderTotal_2 = new BasicAmountType();
				orderTotal_2.setCurrencyID(CurrencyCodeType.fromValue(request.getParameter("currencyCode")));
				orderTotal_2.setValue(request.getParameter("orderTotal"));
				paymentDetails_2.setOrderTotal(orderTotal_2);
				paymentDetails_2.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentAction")));

				List<PaymentDetailsType> payDetails = new ArrayList<PaymentDetailsType>();
				payDetails.add(paymentDetails_1);
				payDetails.add(paymentDetails_2);
				
				doEcdetails.setPaymentDetails(payDetails);
				doCheckoutPaymentRequestType.setDoExpressCheckoutPaymentRequestDetails(doEcdetails);
				DoExpressCheckoutPaymentReq doExpressCheckoutPaymentReq = new DoExpressCheckoutPaymentReq();
				doExpressCheckoutPaymentReq.setDoExpressCheckoutPaymentRequest(doCheckoutPaymentRequestType);
				DoExpressCheckoutPaymentResponseType doCheckoutPaymentResponseType = null;
				
				try {
					doCheckoutPaymentResponseType = service
							.doExpressCheckoutPayment(doExpressCheckoutPaymentReq);
				} catch (Exception e) {
					e.printStackTrace();
				}
				response.setContentType("text/html");

				if (doCheckoutPaymentResponseType != null) {
					//session.setAttribute("nextDescription"," \n Parallel Payment completed");
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (doCheckoutPaymentResponseType.getAck().toString()
							.equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", doCheckoutPaymentResponseType.getAck());
						Iterator<PaymentInfoType> iterator = doCheckoutPaymentResponseType
								.getDoExpressCheckoutPaymentResponseDetails()
								.getPaymentInfo().iterator();
						int index = 1;
						/*
						 * Unique transaction ID of the payment. Note: If the
						 * PaymentAction of the request was Authorization or
						 * Order, this value is your AuthorizationID for use
						 * with the Authorization & Capture APIs. Character
						 * length and limitations: 19 single-byte characters
						 */
						while (iterator.hasNext()) {
							PaymentInfoType result = (PaymentInfoType) iterator
									.next();
							map.put("Transaction ID" + index,
									result.getTransactionID());
							index++;
						}
						session.setAttribute(
								"transactionId",
								doCheckoutPaymentResponseType
										.getDoExpressCheckoutPaymentResponseDetails()
										.getPaymentInfo().get(0)
										.getTransactionID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext()
								.getContextPath() + "/Response.jsp");
					} else {

						session.setAttribute("Error",
								doCheckoutPaymentResponseType.getErrors());
						response.sendRedirect(this.getServletContext()
								.getContextPath() + "/Error.jsp");
					}
				}

			}
	}
}
