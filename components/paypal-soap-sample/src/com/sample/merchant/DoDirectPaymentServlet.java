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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import urn.ebay.api.PayPalAPI.DoDirectPaymentReq;
import urn.ebay.api.PayPalAPI.DoDirectPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoDirectPaymentResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.AddressType;
import urn.ebay.apis.eBLBaseComponents.CountryCodeType;
import urn.ebay.apis.eBLBaseComponents.CreditCardDetailsType;
import urn.ebay.apis.eBLBaseComponents.CreditCardTypeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoDirectPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.PayerInfoType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PersonNameType;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.sample.util.Configuration;

public class DoDirectPaymentServlet extends HttpServlet {

	private static final long serialVersionUID = 12345456723541232L;

	public DoDirectPaymentServlet() {
		super();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		getServletConfig().getServletContext()
				.getRequestDispatcher("/DirectPayment/DoDirectPayment.jsp")
				.forward(req, res);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		HttpSession session = req.getSession();
		session.setAttribute("url", req.getRequestURI());
		session.setAttribute(
				"relatedUrl",
				"<ul><li><a href='DCC/DoDirectPayment'>DoDirectPayment</a></li><li><a href='RT/DoReferenceTransaction'>DoReferenceTransaction</a></li><li><a href='RP/CreateRecurringPaymentsProfile'>CreateRecurringPaymentsProfile</a></li></ul>");
		// ## DoDirectPaymentReq
		DoDirectPaymentReq doPaymentReq = new DoDirectPaymentReq();
		
		DoDirectPaymentRequestType pprequest = new DoDirectPaymentRequestType();
		DoDirectPaymentRequestDetailsType details = new DoDirectPaymentRequestDetailsType();
		PaymentDetailsType paymentDetails = new PaymentDetailsType();
		
		// Total cost of the transaction to the buyer. If shipping cost and tax
		// charges are known, include them in this value. If not, this value
		// should be the current sub-total of the order.
		//
		// If the transaction includes one or more one-time purchases, this
		// field must be equal to
		// the sum of the purchases. Set this field to 0 if the transaction does
		// not include a one-time purchase such as when you set up a billing
		// agreement for a recurring payment that is not immediately charged.
		// When the field is set to 0, purchase-specific fields are ignored.
		//
		// * `Currency Code` - You must set the currencyID attribute to one of
		// the
		// 3-character currency codes for any of the supported PayPal
		// currencies.
		// * `Amount`
		BasicAmountType amount = new BasicAmountType();
		amount.setValue(req.getParameter("amount"));
		amount.setCurrencyID(CurrencyCodeType.fromValue(req.getParameter("currencyCode")));
		paymentDetails.setOrderTotal(amount);
		
		AddressType shipTo = new AddressType();
		shipTo.setName(req.getParameter("firstName") + " "+ req.getParameter("lastName"));
		/*
		 *  (Required) First street address.
			 Character length and limitations: 100 single-byte characters
		 */
		shipTo.setStreet1(req.getParameter("address1"));
		/*
		 *  (Optional) Second street address.
			Character length and limitations: 100 single-byte characters
		 */
		shipTo.setStreet2(req.getParameter("address2"));
		/*
		 *  (Required) Name of city.
			Character length and limitations: 40 single-byte characters
		 */
		shipTo.setCityName(req.getParameter("city"));
		/*
		 *  (Required) State or province.
			Character length and limitations: 40 single-byte characters
		 */
		shipTo.setStateOrProvince(req.getParameter("state"));
		/*
		 *  (Required) Country code.
			Character length and limitations: 2 single-byte characters
		 */
		shipTo.setCountry(CountryCodeType.fromValue(req.getParameter("countryCode")));
		/*
		 *  (Required) U.S. ZIP code or other country-specific postal code.
			Character length and limitations: 20 single-byte characters
		 */
		shipTo.setPostalCode(req.getParameter("zip"));
		paymentDetails.setShipToAddress(shipTo);
		/*
		 *  (Optional) Your URL for receiving Instant Payment Notification (IPN) about this transaction. If you do not specify this value in the request, the notification URL from your Merchant Profile is used, if one exists.
			Important:
            The notify URL applies only to DoExpressCheckoutPayment. This value is ignored when set in SetExpressCheckout or GetExpressCheckoutDetails.
		 */
		paymentDetails.setNotifyURL(req.getParameter("notifyURL"));
		details.setPaymentDetails(paymentDetails);
		
		CreditCardDetailsType cardDetails = new CreditCardDetailsType();
		// Type of credit card. For UK, only Maestro, MasterCard, Discover, and
		// Visa are allowable. For Canada, only MasterCard and Visa are
		// allowable and Interac debit cards are not supported. It is one of the
		// following values:
		//
		// * Visa
		// * MasterCard
		// * Discover
		// * Amex
		// * Solo
		// * Switch
		// * Maestro: See note.
		// `Note:
		// If the credit card type is Maestro, you must set currencyId to GBP.
		// In addition, you must specify either StartMonth and StartYear or
		// IssueNumber.`
		cardDetails.setCreditCardType(CreditCardTypeType.fromValue(req
				.getParameter("creditCardType")));
		// Credit Card number
		cardDetails.setCreditCardNumber(req.getParameter("creditCardNumber"));
		// ExpiryMonth of credit card
		cardDetails.setExpMonth(Integer.parseInt(req.getParameter("expDateMonth")));
		// Expiry Year of credit card
		cardDetails.setExpYear(Integer.parseInt(req.getParameter("expDateYear")));
		//cvv2 number
		cardDetails.setCVV2(req.getParameter("cvv2Number"));
		
		PayerInfoType payer = new PayerInfoType();
		PersonNameType name = new PersonNameType();
		/*
		 *  (Required) Buyer's first name.
			Character length and limitations: 25 single-byte characters
		 */
		name.setFirstName(req.getParameter("firstName"));
		/*
		 *  (Required) Buyer's last name.
			Character length and limitations: 25 single-byte characters
		 */
		name.setLastName(req.getParameter("lastName"));
		payer.setPayerName(name);
		/*
		 *  (Required) Country code.
			Character length and limitations: 2 single-byte characters
		 */
		payer.setPayerCountry(CountryCodeType.fromValue(req.getParameter("countryCode")));
		payer.setAddress(shipTo);

		cardDetails.setCardOwner(payer);

		details.setCreditCard(cardDetails);

		details.setIPAddress("127.0.0.1");
		/*
		 *(Optional) How you want to obtain payment. It is one of the following values:
    		Authorization - This payment is a basic authorization subject to settlement with PayPal Authorization and Capture.
    		Sale - This is a final sale for which you are requesting payment (default).
		  Note:
			Order is not allowed for Direct Payment.
			Character length and limit: Up to 13 single-byte alphabetic characters
		 */
		details.setPaymentAction(PaymentActionCodeType.fromValue(req.getParameter("paymentType")));

		pprequest.setDoDirectPaymentRequestDetails(details);
		doPaymentReq.setDoDirectPaymentRequest(pprequest);

		try {
			// Configuration map containing signature credentials and other required configuration.
			// For a full list of configuration parameters refer in wiki page.
			// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
			Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
			
			// Creating service wrapper object to make an API call by loading configuration map.
			PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configurationMap);
			
			DoDirectPaymentResponseType ddresponse = service
					.doDirectPayment(doPaymentReq);
			res.setContentType("text/html");
			if (ddresponse != null) {
				session.setAttribute("lastReq", service.getLastRequest());
				session.setAttribute("lastResp", service.getLastResponse());
				if (ddresponse.getAck().toString().equalsIgnoreCase("SUCCESS")) {
					Map<Object, Object> map = new LinkedHashMap<Object, Object>();
					map.put("Ack", ddresponse.getAck());
					/*
					 * Unique transaction ID of the payment.
					  Note:
					  If the PaymentAction of the request was Authorization, 
					  the value of TransactionID is your AuthorizationID for use with 
					  the Authorization and Capture APIs.
					  Character length and limitations: 19 single-byte characters
					 */
					map.put("Transaction ID", ddresponse.getTransactionID());
					/*
					 * This value is the amount of the payment as specified by you on 
					 * DoDirectPaymentRequest for reference transactions with direct payments.
					 */
					map.put("Amount", ddresponse.getAmount().getValue() + " "
							+ ddresponse.getAmount().getCurrencyID());
					map.put("Payment Status", ddresponse.getPaymentStatus());
					session.setAttribute("map", map);
					res.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
				} else {
					session.setAttribute("Error", ddresponse.getErrors());
					res.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SSLConfigurationException e) {
			e.printStackTrace();
		} catch (InvalidCredentialException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (HttpErrorException e) {
			e.printStackTrace();
		} catch (InvalidResponseDataException e) {
			e.printStackTrace();
		} catch (ClientActionRequiredException e) {
			e.printStackTrace();
		} catch (MissingCredentialException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
