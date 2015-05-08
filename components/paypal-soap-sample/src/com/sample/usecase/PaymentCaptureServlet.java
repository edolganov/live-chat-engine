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

import urn.ebay.api.PayPalAPI.DoAuthorizationReq;
import urn.ebay.api.PayPalAPI.DoAuthorizationRequestType;
import urn.ebay.api.PayPalAPI.DoAuthorizationResponseType;
import urn.ebay.api.PayPalAPI.DoCaptureReq;
import urn.ebay.api.PayPalAPI.DoCaptureRequestType;
import urn.ebay.api.PayPalAPI.DoCaptureResponseType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CompleteCodeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.ItemCategoryType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsItemType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentInfoType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;

public class PaymentCaptureServlet extends HttpServlet{

	private static final long serialVersionUID = 34096986986994L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if(request.getRequestURI().contains("SetExpressCheckoutPaymentAuthorization")){
			getServletConfig().getServletContext()
			.getRequestDispatcher("/usecase_jsp/SetExpressCheckoutPaymentAuthorization.jsp")
			.forward(request, response);
		}else if(request.getRequestURI().contains("SetExpressCheckoutPaymentOrder")){
			getServletConfig().getServletContext()
			.getRequestDispatcher("/usecase_jsp/SetExpressCheckoutPaymentOrder.jsp")
			.forward(request, response);
		}else if(request.getRequestURI().contains("AuthorizedPaymentCapture")){
			getServletConfig().getServletContext()
			.getRequestDispatcher("/usecase_jsp/PaymentCapture.jsp")
			.forward(request, response);
		}else if(request.getRequestURI().contains("OrderPaymentCapture")){
			getServletConfig().getServletContext()
			.getRequestDispatcher("/usecase_jsp/PaymentCapture.jsp")
			.forward(request, response);
		}else if(request.getRequestURI().contains("DoAuthorizationForOrderPayment")){
			getServletConfig().getServletContext()
			.getRequestDispatcher("/usecase_jsp/DoAuthorizationForOrderPayment.jsp")
			.forward(request, response);
		}else if(request.getRequestURI().contains("DoExpressCheckout")){
			getServletConfig().getServletContext()
			.getRequestDispatcher("/usecase_jsp/DoExpressCheckout.jsp")
			.forward(request, response);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.setAttribute("url", request.getRequestURI());
		response.setContentType("text/html");
		
		// Configuration map containing signature credentials and other required configuration.
		// For a full list of configuration parameters refer in wiki page.
		// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
		Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
		
		// Creating service wrapper object to make an API call by loading configuration map.
		PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configurationMap);
		
		//# SetExpressCheckout API
		// The SetExpressCheckout API operation initiates an Express Checkout
		// transaction.
		// This sample code uses Merchant Java SDK to make API call. You can
		// download the SDKs [here](https://github.com/paypal/sdk-packages/tree/gh-pages/merchant-sdk/java)
		
		if(request.getRequestURI().contains("SetExpressCheckoutPaymentAuthorization") || request.getRequestURI().contains("SetExpressCheckoutPaymentOrder")){

			SetExpressCheckoutRequestType setExpressCheckoutReq = new SetExpressCheckoutRequestType();
			SetExpressCheckoutRequestDetailsType details = new SetExpressCheckoutRequestDetailsType();

			StringBuffer url = new StringBuffer();
			url.append("http://");
			url.append(request.getServerName());
			url.append(":");
			url.append(request.getServerPort());
			url.append(request.getContextPath());

			String returnURL = url.toString() + "/DoExpressCheckout";
			String cancelURL = null;
			if(request.getRequestURI().contains("SetExpressCheckoutPaymentAuthorization")){
				cancelURL = url.toString() + "/SetExpressCheckoutPaymentAuthorization";
				
			}else if(request.getRequestURI().contains("SetExpressCheckoutPaymentOrder")){
				cancelURL = url.toString() + "/SetExpressCheckoutPaymentOrder";
			}
			
			
			
			/*
			 *  (Required) URL to which the buyer's browser is returned after choosing 
			 *  to pay with PayPal. For digital goods, you must add JavaScript to this 
			 *  page to close the in-context experience.
			  Note:
				PayPal recommends that the value be the final review page on which the buyer 
				confirms the order and payment or billing agreement.
				Character length and limitations: 2048 single-byte characters
			 */
			details.setReturnURL(returnURL + "?currencyCodeType="
					+ request.getParameter("currencyCode"));

			details.setCancelURL(cancelURL);
			/*
			 *  (Optional) Email address of the buyer as entered during checkout.
			 *  PayPal uses this value to pre-fill the PayPal membership sign-up portion on the PayPal pages.
			 *	Character length and limitations: 127 single-byte alphanumeric characters
			 */
			details.setBuyerEmail(request.getParameter("buyerMail"));
			
			double itemTotal = 0.00;
			double orderTotal = 0.00;
			// populate line item details
			//Cost of item. This field is required when you pass a value for ItemCategory.
			String amountItems = request.getParameter("itemAmount");
			/*
			 * Item quantity. This field is required when you pass a value for ItemCategory. 
			 * For digital goods (ItemCategory=Digital), this field is required.
			   Character length and limitations: Any positive integer
			   This field is introduced in version 53.0. 
			 */
			String qtyItems = request.getParameter("itemQuantity");
			/*
			 * Item name. This field is required when you pass a value for ItemCategory.
				Character length and limitations: 127 single-byte characters
				This field is introduced in version 53.0. 
			 */
			String names = request.getParameter("itemName");

			List<PaymentDetailsItemType> lineItems = new ArrayList<PaymentDetailsItemType>();

			PaymentDetailsItemType item = new PaymentDetailsItemType();
			BasicAmountType amt = new BasicAmountType();
			//PayPal uses 3-character ISO-4217 codes for specifying currencies in fields and variables. 
			amt.setCurrencyID(CurrencyCodeType.fromValue(request.getParameter("currencyCode")));
			amt.setValue(amountItems);
			item.setQuantity(new Integer(qtyItems));
			item.setName(names);
			item.setAmount(amt);
			/*
			 * Indicates whether an item is digital or physical. For digital goods, this field is required and must be set to Digital. It is one of the following values:
			 	1.Digital
				2.Physical
			   This field is available since version 65.1. 
			 */
			item.setItemCategory(ItemCategoryType.fromValue(request.getParameter("itemCategory")));
			/*
			 *  (Optional) Item description.
				Character length and limitations: 127 single-byte characters
				This field is introduced in version 53.0. 
			 */
			item.setDescription(request.getParameter("itemDescription"));
			lineItems.add(item);
			/*
			 * (Optional) Item sales tax.
				Note: You must set the currencyID attribute to one of 
				the 3-character currency codes for any of the supported PayPal currencies.
				Character length and limitations: Value is a positive number which cannot exceed $10,000 USD in any currency.
				It includes no currency symbol. It must have 2 decimal places, the decimal separator must be a period (.), 
				and the optional thousands separator must be a comma (,).
			 */
			if (request.getParameter("salesTax") != "") {
				item.setTax(new BasicAmountType(CurrencyCodeType
						.fromValue(request.getParameter("currencyCode")),
						request.getParameter("salesTax")));					
			}
			
			itemTotal += Double.parseDouble(qtyItems) * Double.parseDouble(amountItems);
			orderTotal += itemTotal;
			
			List<PaymentDetailsType> payDetails = new ArrayList<PaymentDetailsType>();
			PaymentDetailsType paydtl = new PaymentDetailsType();
			/*
			 * How you want to obtain payment. When implementing parallel payments, 
			 * this field is required and must be set to Order.
			 *  When implementing digital goods, this field is required and must be set to Sale.
			 *   If the transaction does not include a one-time purchase, this field is ignored. 
			 *   It is one of the following values:

				Sale - This is a final sale for which you are requesting payment (default).
				Authorization - This payment is a basic authorization subject to settlement with PayPal Authorization and Capture.
				Order - This payment is an order authorization subject to settlement with PayPal Authorization and Capture.
			 */
			paydtl.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentAction")));
			/*
			 *  (Optional) Total shipping costs for this order.
				Note:
				You must set the currencyID attribute to one of the 3-character currency codes 
				for any of the supported PayPal currencies.
				Character length and limitations: 
				Value is a positive number which cannot exceed $10,000 USD in any currency.
				It includes no currency symbol. 
				It must have 2 decimal places, the decimal separator must be a period (.), 
				and the optional thousands separator must be a comma (,)
			 */
			if (request.getParameter("shippingTotal") != "") {
				BasicAmountType shippingTotal = new BasicAmountType();
				shippingTotal.setValue(request
						.getParameter("shippingTotal"));
				shippingTotal.setCurrencyID(CurrencyCodeType
						.fromValue(request.getParameter("currencyCode")));
				orderTotal += Double.parseDouble(request
						.getParameter("shippingTotal"));
				paydtl.setShippingTotal(shippingTotal);
			}
			
			/*
			 *  (Optional) Total shipping insurance costs for this order. 
			 *  The value must be a non-negative currency amount or null if you offer insurance options.
				 Note:
				 You must set the currencyID attribute to one of the 3-character currency 
				 codes for any of the supported PayPal currencies.
				 Character length and limitations: 
				 Value is a positive number which cannot exceed $10,000 USD in any currency. 
				 It includes no currency symbol. It must have 2 decimal places,
				 the decimal separator must be a period (.), 
				 and the optional thousands separator must be a comma (,).
				 InsuranceTotal is available since version 53.0.
			 */
			if (request.getParameter("insuranceTotal") != "") {
				paydtl.setInsuranceTotal(new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyCode")), request
								.getParameter("insuranceTotal")));
				paydtl.setInsuranceOptionOffered("true");
				orderTotal += Double.parseDouble(request
						.getParameter("insuranceTotal"));
			}
			/*
			 *  (Optional) Total handling costs for this order.
				 Note:
				 You must set the currencyID attribute to one of the 3-character currency codes 
				 for any of the supported PayPal currencies.
				 Character length and limitations: Value is a positive number which 
				 cannot exceed $10,000 USD in any currency.
				 It includes no currency symbol. It must have 2 decimal places, 
				 the decimal separator must be a period (.), and the optional 
				 thousands separator must be a comma (,). 
			 */
			if (request.getParameter("handlingTotal") != "") {
				paydtl.setHandlingTotal(new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyCode")), request
								.getParameter("handlingTotal")));
				orderTotal += Double.parseDouble(request
						.getParameter("handlingTotal"));
			}
			
			/*
			 *  (Optional) Sum of tax for all items in this order.
				 Note:
				 You must set the currencyID attribute to one of the 3-character currency codes
				 for any of the supported PayPal currencies.
				 Character length and limitations: Value is a positive number which 
				 cannot exceed $10,000 USD in any currency. It includes no currency symbol.
				 It must have 2 decimal places, the decimal separator must be a period (.),
				 and the optional thousands separator must be a comma (,).
			 */
			if (request.getParameter("taxTotal") != "") {
				paydtl.setTaxTotal(new BasicAmountType(CurrencyCodeType
						.fromValue(request.getParameter("currencyCode")),
						request.getParameter("taxTotal")));
				orderTotal += Double.parseDouble(request
						.getParameter("taxTotal"));
			}
			
			/*
			 *  (Optional) Description of items the buyer is purchasing.
				 Note:
				 The value you specify is available only if the transaction includes a purchase.
				 This field is ignored if you set up a billing agreement for a recurring payment 
				 that is not immediately charged.
				 Character length and limitations: 127 single-byte alphanumeric characters
			 */
			if (request.getParameter("orderDescription") != "") {
				paydtl.setOrderDescription(request.getParameter("orderDescription"));
			}

			
			BasicAmountType itemsTotal = new BasicAmountType();
			itemsTotal.setValue(Double.toString(itemTotal));
			//PayPal uses 3-character ISO-4217 codes for specifying currencies in fields and variables. 
			itemsTotal.setCurrencyID(CurrencyCodeType.fromValue(request.getParameter("currencyCode")));
			paydtl.setOrderTotal(new BasicAmountType(CurrencyCodeType.fromValue(request.getParameter("currencyCode")),
					Double.toString(orderTotal)));
			paydtl.setPaymentDetailsItem(lineItems);

			paydtl.setItemTotal(itemsTotal);
			/*
			 *  (Optional) Your URL for receiving Instant Payment Notification (IPN) 
			 *  about this transaction. If you do not specify this value in the request, 
			 *  the notification URL from your Merchant Profile is used, if one exists.
				Important:
				The notify URL applies only to DoExpressCheckoutPayment. 
				This value is ignored when set in SetExpressCheckout or GetExpressCheckoutDetails.
				Character length and limitations: 2,048 single-byte alphanumeric characters
			 */
			paydtl.setNotifyURL(request.getParameter("notifyURL"));
			payDetails.add(paydtl);
			details.setPaymentDetails(payDetails);
			
			setExpressCheckoutReq.setSetExpressCheckoutRequestDetails(details);

			SetExpressCheckoutReq expressCheckoutReq = new SetExpressCheckoutReq();
			expressCheckoutReq.setSetExpressCheckoutRequest(setExpressCheckoutReq);
			SetExpressCheckoutResponseType setExpressCheckoutResponse = null;
			try{
				 setExpressCheckoutResponse = service.setExpressCheckout(expressCheckoutReq);
			}catch(Exception e){
				e.printStackTrace();
			}

			if (setExpressCheckoutResponse != null) {
				session.setAttribute("lastReq", service.getLastRequest());
				session.setAttribute("lastResp", service.getLastResponse());
				if (setExpressCheckoutResponse.getAck().toString().equalsIgnoreCase("SUCCESS")) {
					response.sendRedirect("https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+setExpressCheckoutResponse.getToken());
				} else {

					session.setAttribute("Error", setExpressCheckoutResponse.getErrors());
					response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
				}
			}
		
		}else if(request.getRequestURI().contains("AuthorizedPaymentCapture") || request.getRequestURI().contains("OrderPaymentCapture")){

			// ## DoCaptureReq
			DoCaptureReq req = new DoCaptureReq();
			// `Amount` to capture which takes mandatory params:
			//
			// * `currencyCode`
			// * `amount`
			BasicAmountType amount = new BasicAmountType(
					CurrencyCodeType.fromValue(request
							.getParameter("currencyCode")),
					request.getParameter("amt"));

			// `DoCaptureRequest` which takes mandatory params:
			//
			// * `Authorization ID` - Authorization identification number of the
			// payment you want to capture. This is the transaction ID returned from
			// DoExpressCheckoutPayment, DoDirectPayment, or Checkout. For
			// point-of-sale transactions, this is the transaction ID returned by
			// the Checkout call when the payment action is Authorization.
			// * `amount` - Amount to capture
			// * `CompleteCode` - Indicates whether or not this is your last capture.
			// It is one of the following values:
			// * Complete - This is the last capture you intend to make.
			// * NotComplete - You intend to make additional captures.
			// `Note:
			// If Complete, any remaining amount of the original authorized
			// transaction is automatically voided and all remaining open
			// authorizations are voided.`
			DoCaptureRequestType reqType = new DoCaptureRequestType(
					request.getParameter("authID"), amount,
					CompleteCodeType.fromValue(request
							.getParameter("completeCodeType")));
			
			req.setDoCaptureRequest(reqType);
			DoCaptureResponseType resp = null;
			try{
			  resp = service.doCapture(req);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if (resp != null) {
				//session.setAttribute("nextDescription","<ul> The payment capture is completed.</ul>");
				session.setAttribute("lastReq", service.getLastRequest());
				session.setAttribute("lastResp", service.getLastResponse());
				if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
					Map<Object, Object> map = new LinkedHashMap<Object, Object>();
					map.put("Ack", resp.getAck());
					/*
					 * Authorization identification number you specified in the request.
					  Character length and limits: 19 single-byte characters maximum
					 */
					map.put("Authorization ID", resp
							.getDoCaptureResponseDetails()
							.getAuthorizationID());
					/*
					 * The final amount charged, including any shipping and taxes from your 
					 * Merchant Profile. Shipping and taxes do not apply to point-of-sale 
					 * transactions.
						Character length and limitations: Value is a positive number which 
						cannot exceed $10,000 USD in any currency. It includes no currency symbol. 
						It must have 2 decimal places, the decimal separator must be a period (.), 
						and the optional thousands separator must be a comma (,). Equivalent to 9 
						characters maximum for USD. 
					 */
					map.put("Gross Amount", resp
							.getDoCaptureResponseDetails().getPaymentInfo()
							.getGrossAmount().getValue()
							+ " "
							+ resp.getDoCaptureResponseDetails()
									.getPaymentInfo().getGrossAmount()
									.getCurrencyID());
					session.setAttribute("map", map);
					response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");

				} else {

					session.setAttribute("Error", resp.getErrors());
					response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
				}
			}
		
		}else if(request.getRequestURI().contains("DoExpressCheckout")){ // *************** DoExpressCheckout api call ************************

			DoExpressCheckoutPaymentRequestType doCheckoutPaymentRequestType = new DoExpressCheckoutPaymentRequestType();
			DoExpressCheckoutPaymentRequestDetailsType details = new DoExpressCheckoutPaymentRequestDetailsType();
			/*
			 * A timestamped token by which you identify to PayPal that you are processing
			 * this payment with Express Checkout. The token expires after three hours. 
			 * If you set the token in the SetExpressCheckout request, the value of the token
			 * in the response is identical to the value in the request.
			   Character length and limitations: 20 single-byte characters
			 */
			details.setToken(request.getParameter("token"));
			/*
			 * Unique PayPal Customer Account identification number.
			   Character length and limitations: 13 single-byte alphanumeric characters
			 */
			details.setPayerID(request.getParameter("payerID"));
			/*
			 *  (Optional) How you want to obtain payment. If the transaction does not include
			 *  a one-time purchase, this field is ignored. 
			 *  It is one of the following values:
					Sale - This is a final sale for which you are requesting payment (default).
					Authorization - This payment is a basic authorization subject to settlement with PayPal Authorization and Capture.
					Order - This payment is an order authorization subject to settlement with PayPal Authorization and Capture.
				Note:
				You cannot set this field to Sale in SetExpressCheckout request and then change 
				this value to Authorization or Order in the DoExpressCheckoutPayment request. 
				If you set the field to Authorization or Order in SetExpressCheckout, 
				you may set the field to Sale.
				Character length and limitations: Up to 13 single-byte alphabetic characters
				This field is deprecated. Use PaymentAction in PaymentDetailsType instead.
			 */
			details.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentAction")));
			double itemTotalAmt = 0.00;
			double orderTotalAmt = 0.00;
			String amt = request.getParameter("amt");
			String quantity = request.getParameter("itemQuantity");
			itemTotalAmt = Double.parseDouble(amt) * Double.parseDouble(quantity);
			orderTotalAmt += itemTotalAmt;
			
			PaymentDetailsType paymentDetails = new PaymentDetailsType();
			BasicAmountType orderTotal = new BasicAmountType();
			orderTotal.setValue(Double.toString(orderTotalAmt));
			//PayPal uses 3-character ISO-4217 codes for specifying currencies in fields and variables.
			orderTotal.setCurrencyID(CurrencyCodeType.fromValue(request.getParameter("currencyCode")));
			/*
			 *  (Required) The total cost of the transaction to the buyer. 
			 *  If shipping cost (not applicable to digital goods) and tax charges are known, 
			 *  include them in this value. If not, this value should be the current sub-total 
			 *  of the order. If the transaction includes one or more one-time purchases, this 
			 *  field must be equal to the sum of the purchases. Set this field to 0 if the 
			 *  transaction does not include a one-time purchase such as when you set up a 
			 *  billing agreement for a recurring payment that is not immediately charged. 
			 *  When the field is set to 0, purchase-specific fields are ignored. 
			 *  For digital goods, the following must be true:
				total cost > 0
				total cost <= total cost passed in the call to SetExpressCheckout
			 Note:
				You must set the currencyID attribute to one of the 3-character currency codes 
				for any of the supported PayPal currencies.
				When multiple payments are passed in one transaction, all of the payments must 
				have the same currency code.
				Character length and limitations: Value is a positive number which cannot 
				exceed $10,000 USD in any currency. It includes no currency symbol. 
				It must have 2 decimal places, the decimal separator must be a period (.), 
				and the optional thousands separator must be a comma (,).
			 */
			paymentDetails.setOrderTotal(orderTotal);

			BasicAmountType itemTotal = new BasicAmountType();
			itemTotal.setValue(Double.toString(itemTotalAmt));
			//PayPal uses 3-character ISO-4217 codes for specifying currencies in fields and variables.
			itemTotal.setCurrencyID(CurrencyCodeType.fromValue(request.getParameter("currencyCode")));
			/*
			 *  Sum of cost of all items in this order. For digital goods, this field is 
			 *  required. PayPal recommends that you pass the same value in the call to 
			 *  DoExpressCheckoutPayment that you passed in the call to SetExpressCheckout.
			 Note:
				You must set the currencyID attribute to one of the 3-character currency 
				codes for any of the supported PayPal currencies.
				Character length and limitations: Value is a positive number which cannot 
				exceed $10,000 USD in any currency. It includes no currency symbol. 
				It must have 2 decimal places, the decimal separator must be a period (.), 
				and the optional thousands separator must be a comma (,).
			 */
			paymentDetails.setItemTotal(itemTotal);

			List<PaymentDetailsItemType> paymentItems = new ArrayList<PaymentDetailsItemType>();
			PaymentDetailsItemType paymentItem = new PaymentDetailsItemType();
			/*
			 * Item name. This field is required when you pass a value for ItemCategory.
			   Character length and limitations: 127 single-byte characters
			   This field is introduced in version 53.0. 
			 */
			paymentItem.setName(request.getParameter("itemName"));
			/*
			 * Item quantity. This field is required when you pass a value for ItemCategory. 
			 * For digital goods (ItemCategory=Digital), this field is required.
				Character length and limitations: Any positive integer
				This field is introduced in version 53.0. 
			 */
			paymentItem.setQuantity(Integer.parseInt(request.getParameter("itemQuantity")));
			BasicAmountType amount = new BasicAmountType();
			/*
			 * Cost of item. This field is required when you pass a value for ItemCategory.
			Note:
			You must set the currencyID attribute to one of the 3-character currency codes for
			any of the supported PayPal currencies.
			Character length and limitations: Value is a positive number which cannot 
			exceed $10,000 USD in any currency. It includes no currency symbol.
			It must have 2 decimal places, the decimal separator must be a period (.), 
			and the optional thousands separator must be a comma (,).
			This field is introduced in version 53.0.
			 */
			amount.setValue(request.getParameter("amt"));
			//PayPal uses 3-character ISO-4217 codes for specifying currencies in fields and variables.
			amount.setCurrencyID(CurrencyCodeType.fromValue(request.getParameter("currencyCode")));
			paymentItem.setAmount(amount);
			paymentItems.add(paymentItem);
			paymentDetails.setPaymentDetailsItem(paymentItems);
			/*
			 *  (Optional) Your URL for receiving Instant Payment Notification (IPN) 
			 *  about this transaction. If you do not specify this value in the request, 
			 *  the notification URL from your Merchant Profile is used, if one exists.
				Important:
				The notify URL applies only to DoExpressCheckoutPayment. 
				This value is ignored when set in SetExpressCheckout or GetExpressCheckoutDetails.
				Character length and limitations: 2,048 single-byte alphanumeric characters
			 */
			paymentDetails.setNotifyURL(request.getParameter("notifyURL"));
			
			List<PaymentDetailsType> payDetailType = new ArrayList<PaymentDetailsType>();
			payDetailType.add(paymentDetails);
			/*
			 * When implementing parallel payments, you can create up to 10 sets of payment 
			 * details type parameter fields, each representing one payment you are hosting 
			 * on your marketplace.
			 */
			details.setPaymentDetails(payDetailType);

			doCheckoutPaymentRequestType
					.setDoExpressCheckoutPaymentRequestDetails(details);
			DoExpressCheckoutPaymentReq doExpressCheckoutPaymentReq = new DoExpressCheckoutPaymentReq();
			doExpressCheckoutPaymentReq.setDoExpressCheckoutPaymentRequest(doCheckoutPaymentRequestType);
			DoExpressCheckoutPaymentResponseType doCheckoutPaymentResponseType = null;
			try{
			 doCheckoutPaymentResponseType = service.doExpressCheckoutPayment(doExpressCheckoutPaymentReq);
			}catch(Exception e){
				e.printStackTrace();
			}
			response.setContentType("text/html");
			
			if (doCheckoutPaymentResponseType != null) {
				session.setAttribute(
						"nextDescription",
						"<ul>If  paymentAction is <b>Authorization</b> .you can capture the payment directly using DoCapture api" +
						" <li><a href='AuthorizedPaymentCapture'>DoCapture</a></li>" +
						"If  paymentAction is <b>Order</b> .you need to call DoAuthorization api, before you can capture the payment using DoCapture api." +
						"<li><a href='DoAuthorizationForOrderPayment'>DoAuthorization</a></li></ul>");
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
					 * Unique transaction ID of the payment.
					 Note:
						If the PaymentAction of the request was Authorization or Order, 
						this value is your AuthorizationID for use with the Authorization 
						& Capture APIs.
						Character length and limitations: 19 single-byte characters
					 */
					while (iterator.hasNext()) {
						PaymentInfoType result = (PaymentInfoType) iterator.next();
						map.put("Transaction ID" + index,
								result.getTransactionID());
						index++;
					}
					session.setAttribute("transactionId", doCheckoutPaymentResponseType.getDoExpressCheckoutPaymentResponseDetails().getPaymentInfo().get(0).getTransactionID());
					session.setAttribute("map", map);
					response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
				} else {

					session.setAttribute("Error",
							doCheckoutPaymentResponseType.getErrors());
					response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
				}
			}

		
		}else if(request.getRequestURI().contains("DoAuthorizationForOrderPayment")){

			// ## DoAuthorizationReq
			DoAuthorizationReq req = new DoAuthorizationReq();

			// `Amount` which takes mandatory params:
			//
			// * `currencyCode`
			// * `amount
			BasicAmountType amount = new BasicAmountType(
					CurrencyCodeType.fromValue(request.getParameter("currencyCode")),
					request.getParameter("amt"));
			
			// `DoAuthorizationRequest` which takes mandatory params:
			//
			// * `Transaction ID` - Value of the order's transaction identification
			// number returned by PayPal.
			// * `Amount` - Amount to authorize.
			DoAuthorizationRequestType reqType = new DoAuthorizationRequestType(
					request.getParameter("transID"), amount);

			req.setDoAuthorizationRequest(reqType);
			DoAuthorizationResponseType resp = null;
			try{
				resp = service.doAuthorization(req);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			session.setAttribute(
					"nextDescription",
					"<ul>The payment order is authorized , now you can capture the payment using DoCapture api" +
					" <li><a href='OrderPaymentCapture'>DoCapture</a></li>" );
			if (resp != null) {
				session.setAttribute("lastReq", service.getLastRequest());
				session.setAttribute("lastResp", service.getLastResponse());
				if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
					Map<Object, Object> map = new LinkedHashMap<Object, Object>();
					map.put("Ack", resp.getAck());
					/*
					 *  Amount you specified in the request.
						Character length and limitations: Value is a positive number which 
						cannot exceed $10,000 USD in any currency. It includes no currency symbol. 
						It must have 2 decimal places, the decimal separator must be a period (.), 
						and the optional thousands separator must be a comma (,).
					 */
					map.put("Amount", resp.getAmount().getValue() + " "
							+ resp.getAmount().getCurrencyID());
					/*
					 * Status of the payment. It is one of the following values:
					    None - No status.
					    Canceled-Reversal - A reversal has been canceled. For example, when you win a dispute, PayPal returns the funds for the reversal to you.
					    Completed - The payment has been completed, and the funds have been added successfully to your account balance.
					    Denied - You denied the payment. This happens only if the payment was previously pending because of possible reasons described for the PendingReason element.
					    Expired - The authorization period for this payment has been reached.
					    Failed - The payment has failed. This happens only if the payment was made from the buyer's bank account.
					    In-Progress - The transaction has not terminated. For example, an authorization may be awaiting completion.
					    Partially-Refunded - The payment has been partially refunded.
					    Pending - The payment is pending. See the PendingReason field for more information.
					    Refunded - You refunded the payment.
					    Reversed - A payment was reversed due to a chargeback or other type of reversal. PayPal removes the funds from your account balance and returns them to the buyer. The ReasonCode element specifies the reason for the reversal.
					    Processed - A payment has been accepted.
					    Voided - An authorization for this transaction has been voided.
					 */
					map.put("Payment Status", resp.getAuthorizationInfo()
							.getPaymentStatus());
					session.setAttribute("map", map);
					response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");

				} else {

					session.setAttribute("Error", resp.getErrors());
					response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
				}
			}
		
		}
	}

}
