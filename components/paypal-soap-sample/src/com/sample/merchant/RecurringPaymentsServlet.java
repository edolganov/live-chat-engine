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

import urn.ebay.api.PayPalAPI.BAUpdateRequestType;
import urn.ebay.api.PayPalAPI.BAUpdateResponseType;
import urn.ebay.api.PayPalAPI.BillAgreementUpdateReq;
import urn.ebay.api.PayPalAPI.BillOutstandingAmountReq;
import urn.ebay.api.PayPalAPI.BillOutstandingAmountRequestType;
import urn.ebay.api.PayPalAPI.BillOutstandingAmountResponseType;
import urn.ebay.api.PayPalAPI.BillUserReq;
import urn.ebay.api.PayPalAPI.BillUserRequestType;
import urn.ebay.api.PayPalAPI.BillUserResponseType;
import urn.ebay.api.PayPalAPI.CreateRecurringPaymentsProfileReq;
import urn.ebay.api.PayPalAPI.CreateRecurringPaymentsProfileRequestType;
import urn.ebay.api.PayPalAPI.CreateRecurringPaymentsProfileResponseType;
import urn.ebay.api.PayPalAPI.DoReferenceTransactionReq;
import urn.ebay.api.PayPalAPI.DoReferenceTransactionRequestType;
import urn.ebay.api.PayPalAPI.DoReferenceTransactionResponseType;
import urn.ebay.api.PayPalAPI.GetBillingAgreementCustomerDetailsReq;
import urn.ebay.api.PayPalAPI.GetBillingAgreementCustomerDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetBillingAgreementCustomerDetailsResponseType;
import urn.ebay.api.PayPalAPI.GetRecurringPaymentsProfileDetailsReq;
import urn.ebay.api.PayPalAPI.GetRecurringPaymentsProfileDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetRecurringPaymentsProfileDetailsResponseType;
import urn.ebay.api.PayPalAPI.ManageRecurringPaymentsProfileStatusReq;
import urn.ebay.api.PayPalAPI.ManageRecurringPaymentsProfileStatusRequestType;
import urn.ebay.api.PayPalAPI.ManageRecurringPaymentsProfileStatusResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.UpdateRecurringPaymentsProfileReq;
import urn.ebay.api.PayPalAPI.UpdateRecurringPaymentsProfileRequestType;
import urn.ebay.api.PayPalAPI.UpdateRecurringPaymentsProfileResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.ActivationDetailsType;
import urn.ebay.apis.eBLBaseComponents.AddressType;
import urn.ebay.apis.eBLBaseComponents.AutoBillType;
import urn.ebay.apis.eBLBaseComponents.BillOutstandingAmountRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.BillingPeriodDetailsType;
import urn.ebay.apis.eBLBaseComponents.BillingPeriodDetailsType_Update;
import urn.ebay.apis.eBLBaseComponents.BillingPeriodType;
import urn.ebay.apis.eBLBaseComponents.CountryCodeType;
import urn.ebay.apis.eBLBaseComponents.CreateRecurringPaymentsProfileRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.CreditCardDetailsType;
import urn.ebay.apis.eBLBaseComponents.CreditCardNumberTypeType;
import urn.ebay.apis.eBLBaseComponents.CreditCardTypeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoReferenceTransactionRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.FailedPaymentActionType;
import urn.ebay.apis.eBLBaseComponents.ManageRecurringPaymentsProfileStatusRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.MerchantPullPaymentCodeType;
import urn.ebay.apis.eBLBaseComponents.MerchantPullPaymentType;
import urn.ebay.apis.eBLBaseComponents.MerchantPullStatusCodeType;
import urn.ebay.apis.eBLBaseComponents.PayerInfoType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PersonNameType;
import urn.ebay.apis.eBLBaseComponents.RecurringPaymentsProfileDetailsType;
import urn.ebay.apis.eBLBaseComponents.ReferenceCreditCardDetailsType;
import urn.ebay.apis.eBLBaseComponents.ScheduleDetailsType;
import urn.ebay.apis.eBLBaseComponents.StatusChangeActionType;
import urn.ebay.apis.eBLBaseComponents.UpdateRecurringPaymentsProfileRequestDetailsType;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.sample.util.Configuration;

/**
 * Servlet implementation class RecurringPaymentsServlet
 */
public class RecurringPaymentsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public RecurringPaymentsServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getRequestURI().contains("CreateRecurringPaymentsProfile"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/CreateRecurringPaymentsProfile.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains(
				"GetRecurringPaymentsProfileDetails"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/GetRecurringPaymentsProfileDetails.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains(
				"ManageRecurringPaymentsProfileStatus"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/ManageRecurringPaymentsProfileStatus.jsp")
					.forward(request, response);
		if (request.getRequestURI().contains("UpdateRecurringPaymentsProfile"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/UpdateRecurringPaymentsProfile.jsp")
					.forward(request, response);
		if (request.getRequestURI().contains("BillOutstandingAmount"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/BillOutstandingAmount.jsp")
					.forward(request, response);
		if (request.getRequestURI().contains(
				"GetBillingAgreementCustomerDetails")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/GetBillingAgreementCustomerDetails.jsp")
					.forward(request, response);
		} else if (request.getRequestURI().contains("BillAgreementUpdate")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/BillAgreementUpdate.jsp")
					.forward(request, response);
		} else if (request.getRequestURI().contains(
				"SetCustomerBillingAgreement")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/DeprecatedBillingAgreement.jsp")
					.forward(request, response);
		} else if (request.getRequestURI().contains("CreateBillingAgreement")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/DeprecatedBillingAgreement.jsp")
					.forward(request, response);
		} else if (request.getRequestURI().contains("DoReferenceTransaction")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/RecurringPayments/DoReferenceTransaction.jsp")
					.forward(request, response);
		} else if (request.getRequestURI().contains("BillUser")) {
			getServletConfig().getServletContext()
					.getRequestDispatcher("/RecurringPayments/BillUser.jsp")
					.forward(request, response);
		}

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
		session.setAttribute(
				"relatedUrl",
				"<ul><li><a href='RP/CreateRecurringPaymentsProfile'>CreateRecurringPaymentsProfile</a></li><li><a href='RP/GetRecurringPaymentsProfileDetails'>GetRecurringPaymentsProfileDetails</a></li><li><a href='RP/ManageRecurringPaymentsProfileStatus'>ManageRecurringPaymentsProfileStatus</a></li><li><a href='RP/UpdateRecurringPaymentsProfile'>UpdateRecurringPaymentsProfile</a></li><li><a href='RP/BillOutstandingAmount'>BillOutstandingAmount</a></li><li><a href='RT/SetCustomerBillingAgreement'>SetCustomerBillingAgreement</a></li><li><a href='RT/CreateBillingAgreement'>CreateBillingAgreement</a></li><li><a href='RT/GetBillingAgreementCustomerDetails'>GetBillingAgreementCustomerDetails</a></li><li><a href='RT/BillAgreementUpdate'>BillAgreementUpdate</a></li><li><a href='RT/DoReferenceTransaction'>DoReferenceTransaction</a></li></ul>");
		response.setContentType("text/html");
		CurrencyCodeType currency = CurrencyCodeType.fromValue("USD");
		try {
			
			// Configuration map containing signature credentials and other required configuration.
			// For a full list of configuration parameters refer in wiki page.
			// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
			Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
			
			// Creating service wrapper object to make an API call by loading configuration map.
			PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configurationMap);
			
			if (request.getRequestURI().contains("GetBillingAgreementCustomerDetails")) {

				GetBillingAgreementCustomerDetailsReq gReq = new GetBillingAgreementCustomerDetailsReq();
				GetBillingAgreementCustomerDetailsRequestType gRequestType = new GetBillingAgreementCustomerDetailsRequestType();
				/*
				 *  (Required) The time-stamped token returned in the SetCustomerBillingAgreement 
				 *   response.
					Note:
					 The token expires after 3 hours.
					 Character length and limitations: 20 single-byte characters
				 */
				gRequestType.setToken(request.getParameter("token"));
				gReq.setGetBillingAgreementCustomerDetailsRequest(gRequestType);
				GetBillingAgreementCustomerDetailsResponseType txnresponse = service
						.getBillingAgreementCustomerDetails(gReq);

				if (txnresponse != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (txnresponse.getAck().toString()
							.equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", txnresponse.getAck());
						//Email address of buyer.
						map.put("Payer Mail",
								txnresponse
										.getGetBillingAgreementCustomerDetailsResponseDetails()
										.getPayerInfo().getPayer());
						
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", txnresponse.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}

			} else if (request.getRequestURI().contains("BillAgreementUpdate")) {

				BillAgreementUpdateReq bReq = new BillAgreementUpdateReq();
				BAUpdateRequestType baUpdateRequestType = new BAUpdateRequestType();
				baUpdateRequestType.setReferenceID(request
						.getParameter("referenceID"));
				baUpdateRequestType
						.setBillingAgreementStatus(MerchantPullStatusCodeType.fromValue(request
								.getParameter("billingAgreementStatus")));
				baUpdateRequestType.setBillingAgreementDescription(request
						.getParameter("billingAgreementDescription"));
				bReq.setBAUpdateRequest(baUpdateRequestType);
				BAUpdateResponseType txnresponse = service
						.billAgreementUpdate(bReq);

				if (txnresponse != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (txnresponse.getAck().toString()
							.equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", txnresponse.getAck());
						map.put("Billing Agreement ID", txnresponse
								.getBAUpdateResponseDetails()
								.getBillingAgreementID());
						map.put("Billing Agreement Description", txnresponse
								.getBAUpdateResponseDetails()
								.getBillingAgreementDescription());
						map.put("Billing Agreement Status", txnresponse
								.getBAUpdateResponseDetails()
								.getBillingAgreementStatus());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", txnresponse.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("DoReferenceTransaction")) {
				// ## DoReferenceTransactionReq
				DoReferenceTransactionReq doReq = new DoReferenceTransactionReq();
				DoReferenceTransactionRequestType doRequestType = new DoReferenceTransactionRequestType();
				DoReferenceTransactionRequestDetailsType doDetailsType = new DoReferenceTransactionRequestDetailsType();
				/*
				 *  (Optional) How you want to obtain payment. It is one of the following values:
    				  1. Authorization - This payment is a basic authorization subject to settlement 
    					with PayPal Authorization and Capture.
    				  2. Sale - This is a final sale for which you are requesting payment.
				 */
				doDetailsType.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentAction")));
				/*
				 * Indicates whether the payment is instant or delayed. 
				 * It is one of the following values:
    				1.none
    				2.echeck
    				3.instant
					Character length and limitations: 7 single-byte characters
				 */
				String pt = request.getParameter("paymentType");
				doDetailsType.setPaymentType(MerchantPullPaymentCodeType
						.fromValue(pt));

				PaymentDetailsType paymentDetails = new PaymentDetailsType();
				/*
				 *  (Optional) An identification code for use by third-party applications 
				 *  to identify transactions.
					Character length and limitations: 32 single-byte alphanumeric characters
				 */
				paymentDetails.setButtonSource("PayPal_SDK");
				// The total cost of the transaction to the buyer. If shipping cost and
				// tax charges are known, include them in this value. If not, this value
				// should be the current subtotal of the order.

				// If the transaction includes one or more one-time purchases, this field must be equal to
				// the sum of the purchases. Set this field to 0 if the transaction does
				// not include a one-time purchase such as when you set up a billing
				// agreement for a recurring payment that is not immediately charged.
				// When the field is set to 0, purchase-specific fields are ignored
				//
				// * `Currency ID` - You must set the currencyID attribute to one of the
				// 3-character currency codes for any of the supported PayPal
				// currencies.
				// * `Amount`
				BasicAmountType amount = new BasicAmountType();
				amount.setValue(request.getParameter("amount"));
				amount.setCurrencyID(CurrencyCodeType.fromValue(request
						.getParameter("currencyID")));
				paymentDetails.setOrderTotal(amount);

				AddressType shipTo = new AddressType();
				/*
				 * Person's name associated with this shipping address. It is required if using a shipping address.
				   Character length and limitations: 32 single-byte characters
				 */
				shipTo.setName(request.getParameter("firstName") + " "+ request.getParameter("lastName"));
				/*
				 * First street address. It is required if using a shipping address.
				   Character length and limitations: 100 single-byte characters
				 */
				shipTo.setStreet1(request.getParameter("address1"));
				/*
				 *  (Optional) Second street address.
					Character length and limitations: 100 single-byte characters
				 */
				shipTo.setStreet2(request.getParameter("address2"));
				/*
				 * Name of city. It is required if using a shipping address.
				   Character length and limitations: 40 single-byte characters
				 */
				shipTo.setCityName(request.getParameter("city"));
				/*
				 * State or province. It is required if using a shipping address.
				   Character length and limitations: 40 single-byte characters
				 */
				shipTo.setStateOrProvince(request.getParameter("state"));
				/*
				 * Country code. It is required if using a shipping address.
				   Character length and limitations: 2 single-byte characters
				 */
				shipTo.setCountry(CountryCodeType.US);
				/*
				 * U.S. ZIP code or other country-specific postal code. It is required if using a U.S. shipping address;
				 *  may be required for other countries.
					Character length and limitations: 20 single-byte characters
				 */
				shipTo.setPostalCode(request.getParameter("zip"));
				paymentDetails.setShipToAddress(shipTo);
				/*
				 *  (Optional) Your URL for receiving Instant Payment Notification (IPN) about this transaction. 
				 *  If you do not specify this value in the request, the notification URL from your Merchant Profile is used, if one exists.
				Important:
					The notify URL applies only to DoExpressCheckoutPayment. 
					This value is ignored when set in SetExpressCheckout or GetExpressCheckoutDetails.
				 */
				paymentDetails.setNotifyURL(request.getParameter("notifyURL"));
				doDetailsType.setPaymentDetails(paymentDetails);
				
				/*
				 * (Required) A transaction ID from a previous purchase, 
				 * such as a credit card charge using the DoDirectPayment API, or a billing agreement ID.
				 */
				doDetailsType.setReferenceID(request.getParameter("referenceID"));
				if (request.getParameter("ReferenceCreditCardDetails") != null
						&& "ON".equalsIgnoreCase(request.getParameter("ReferenceCreditCardDetails"))) {
					
					ReferenceCreditCardDetailsType rType = new ReferenceCreditCardDetailsType();

					PersonNameType personNameType = new PersonNameType();
					personNameType.setFirstName(request.getParameter("firstName"));
					personNameType.setLastName(request.getParameter("lastName"));
					rType.setCardOwnerName(personNameType);

					CreditCardNumberTypeType crType = new CreditCardNumberTypeType();
					/*
					 *  (Optional) Credit card number.
						Character length and limitations: 
						Numeric characters only with no spaces or punctuation. 
						The string must conform with modulo and length required by each credit card type.
					 */
					crType.setCreditCardNumber(request.getParameter("creditCardNumber"));
					/*
					 *  (Optional) Type of credit card. Is one of the following values:
    					[ Visa, MasterCard, Discover, Amex, Maestro: See note.]
						For UK, only Maestro, MasterCard, Discover, and Visa are allowable. 
						For Canada, only MasterCard and Visa are allowable. Interac debit cards are not supported.
						Note:
						If the credit card type is Maestro, you must set the currencyId to GBP.
						In addition, you must specify either StartMonth and StartYear or IssueNumber.
						Character length and limitations: Up to 10 single-byte alphabetic characters
					 */
					crType.setCreditCardType(CreditCardTypeType.fromValue(request.getParameter("creditCardType")));
					rType.setCreditCardNumberType(crType);

					rType.setCVV2(request.getParameter("CVV2"));
					/*
					 *  (Optional) Credit card expiration month.
						Character length and limitations: 2 single-byte numeric characters, 
						including leading zero
					 */
					rType.setExpMonth(Integer.parseInt(request.getParameter("expMonth")));
					/*
					 *  (Optional) Credit card expiration year.
						Character length and limitations: 4 single-byte numeric characters
					 */
					rType.setExpYear(Integer.parseInt(request.getParameter("expYear")));
					/*
					 *  (Optional) Month that Maestro card was issued.
						Character length and limitations: 2-digit, zero-filled if necessary
					 */
					rType.setStartMonth(Integer.parseInt(request.getParameter("startMonth")));
					/*
					 *  (Optional) Year that Maestro card was issued.
						Character length and limitations: 4 digits
					 */
					rType.setStartYear(Integer.parseInt(request.getParameter("startYear")));

					AddressType billAddr = new AddressType();
					/*
					 * Person's name associated with this shipping address.
					 *  It is required if using a shipping address.
					   Character length and limitations: 32 single-byte characters
					 */
					billAddr.setName(request.getParameter("firstName") + " "+ request.getParameter("lastName"));
					/*
					 * First street address. It is required if using a shipping address.
					   Character length and limitations: 100 single-byte characters
					 */
					billAddr.setStreet1(request.getParameter("address1"));
					/*
					 *  (Optional) Second street address.
						Character length and limitations: 100 single-byte characters
					 */
					billAddr.setStreet2(request.getParameter("address2"));
					/*
					 * Name of city. It is required if using a shipping address.
						Character length and limitations: 40 single-byte characters
					 */
					billAddr.setCityName(request.getParameter("city"));
					/*
					 * State or province. It is required if using a shipping address.
						Character length and limitations: 40 single-byte characters
					 */
					billAddr.setStateOrProvince(request.getParameter("state"));
					/*
					 * Country code. It is required if using a shipping address.
						Character length and limitations: 2 single-byte characters
					 */
					billAddr.setCountry(CountryCodeType.US);
					/*
					 * U.S. ZIP code or other country-specific postal code. 
					 * It is required if using a U.S. shipping address; may be required for other countries.
						Character length and limitations: 20 single-byte characters
					 */
					billAddr.setPostalCode(request.getParameter("zip"));
					rType.setBillingAddress(billAddr);

					doDetailsType.setCreditCard(rType);
				}

				doRequestType
						.setDoReferenceTransactionRequestDetails(doDetailsType);
				doReq.setDoReferenceTransactionRequest(doRequestType);
				DoReferenceTransactionResponseType txnresponse = null;
				txnresponse = service.doReferenceTransaction(doReq);
				response.setContentType("text/html");
				if (txnresponse != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (txnresponse.getAck().toString()
							.equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", txnresponse.getAck());
						/*
						 * Unique transaction ID of the payment.
						   Character length and limitations: 17 single-byte characters
						 */
						map.put("Transaction ID", txnresponse
								.getDoReferenceTransactionResponseDetails()
								.getTransactionID());
						/*
						 * Billing agreement identifier returned 
						 * if the value of ReferenceID in the request is a billing agreement identification number.
						 */
						map.put("Billing Agreement ID", txnresponse
								.getDoReferenceTransactionResponseDetails()
								.getBillingAgreementID());
						
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", txnresponse.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("BillUser")) {
				BillUserReq req = new BillUserReq();
				BillUserRequestType reqType = new BillUserRequestType();
				MerchantPullPaymentType merchantPullPayment = new MerchantPullPaymentType();
				merchantPullPayment.setMpID(request
						.getParameter("billingAgreementID"));
				merchantPullPayment.setPaymentType(MerchantPullPaymentCodeType
						.fromValue(request.getParameter("paymentCodeType")));
				merchantPullPayment.setItemName(request
						.getParameter("itemName"));
				merchantPullPayment.setItemNumber(request
						.getParameter("itemNum"));
				merchantPullPayment.setAmount(new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyID")), request
								.getParameter("amt")));
				merchantPullPayment.setMemo(request.getParameter("memo"));
				merchantPullPayment.setTax(new BasicAmountType(CurrencyCodeType
						.fromValue(request.getParameter("currencyID")), request
						.getParameter("tax")));
				merchantPullPayment.setShipping(new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyID")), request
								.getParameter("shipping")));
				merchantPullPayment.setHandling(new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyID")), request
								.getParameter("handling")));
				merchantPullPayment.setEmailSubject(request
						.getParameter("mailSubject"));
				reqType.setMerchantPullPaymentDetails(merchantPullPayment);
				req.setBillUserRequest(reqType);
				BillUserResponseType resp = service.billUser(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						map.put("Payer Mail", resp.getBillUserResponseDetails()
								.getPayerInfo().getPayer());
						map.put("Merchant Pull Status", resp
								.getBillUserResponseDetails()
								.getMerchantPullInfo().getMpStatus());
						map.put("Transaction ID", resp
								.getBillUserResponseDetails().getPaymentInfo()
								.getTransactionID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			}
			if (request.getRequestURI().contains("CreateRecurringPaymentsProfile")) {

				CreateRecurringPaymentsProfileReq req = new CreateRecurringPaymentsProfileReq();
				CreateRecurringPaymentsProfileRequestType reqType = new CreateRecurringPaymentsProfileRequestType();
				/*
				 *  (Required) The date when billing for this profile begins.
					Note:
					The profile may take up to 24 hours for activation.
					Character length and limitations: Must be a valid date, in UTC/GMT format
				 */
				RecurringPaymentsProfileDetailsType profileDetails = new RecurringPaymentsProfileDetailsType(
						request.getParameter("billingStartDate")
								+ "T00:00:00:000Z");
				/*
				 *  (Optional) Full name of the person receiving the product or service paid for
				 *   by the recurring payment. If not present, the name in the buyer's PayPal
				 *   account is used.
					Character length and limitations: 32 single-byte characters
				 */
				if (request.getParameter("subscriberName") != "") {
					profileDetails.setSubscriberName(request
							.getParameter("subscriberName"));
				} else if (request.getParameter("shippingName") != "") {
					AddressType shippingAddr = new AddressType();
					/*
					 * Person's name associated with this shipping address. 
					 * It is required if using a shipping address.
					   Character length and limitations: 32 single-byte characters
					 */
					shippingAddr.setName(request.getParameter("shippingName"));
					/*
					 * First street address. It is required if using a shipping address.
					   Character length and limitations: 100 single-byte characters
					 */
					shippingAddr.setStreet1(request.getParameter("shippingStreet1"));
					/*
					 *  (Optional) Second street address.
						Character length and limitations: 100 single-byte characters
					 */
					shippingAddr.setStreet2(request.getParameter("shippingStreet2"));
					/*
					 * Optional) Phone number.
					   Character length and limitations: 20 single-byte characters
					 */
					shippingAddr.setPhone(request.getParameter("shippingPhone"));
					/*
					 * Name of city. It is required if using a shipping address.
					   Character length and limitations: 40 single-byte characters
					 */
					shippingAddr.setCityName(request.getParameter("shippingCity"));
					/*
					 * State or province. It is required if using a shipping address.
					   Character length and limitations: 40 single-byte characters
					 */
					shippingAddr.setStateOrProvince(request.getParameter("shippingState"));
					/*
					 * Country code. It is required if using a shipping address.
					  Character length and limitations: 2 single-byte characters
					 */
					shippingAddr.setCountryName(request.getParameter("shippingCountry"));
					/*
					 * U.S. ZIP code or other country-specific postal code. 
					 * It is required if using a U.S. shipping address; may be required 
					 * for other countries.
					   Character length and limitations: 20 single-byte characters
					 */
					shippingAddr.setPostalCode(request.getParameter("shippingPostalCode"));
					profileDetails.setSubscriberShippingAddress(shippingAddr);
				}

				// Populate schedule details
				ScheduleDetailsType scheduleDetails = new ScheduleDetailsType();
				/*
				 *  (Required) Description of the recurring payment.
					Note:
					You must ensure that this field matches the corresponding billing agreement 
					description included in the SetExpressCheckout request.
					Character length and limitations: 127 single-byte alphanumeric characters
				 */
				scheduleDetails.setDescription(request.getParameter("profileDescription"));
				/*
				 *  (Optional) Number of scheduled payments that can fail before the profile 
				 *  is automatically suspended. An IPN message is sent to the merchant when the 
				 *  specified number of failed payments is reached.
					 Character length and limitations: Number string representing an integer
				 */
				if (request.getParameter("maxFailedPayments") != "") {
					scheduleDetails.setMaxFailedPayments(Integer.parseInt(request
									.getParameter("maxFailedPayments")));
				}
				/*
				 *  (Optional) Indicates whether you would like PayPal to automatically bill 
				 *  the outstanding balance amount in the next billing cycle. 
				 *  The outstanding balance is the total amount of any previously failed 
				 *  scheduled payments that have yet to be successfully paid. 
				 *  It is one of the following values:
    				NoAutoBill - PayPal does not automatically bill the outstanding balance.
    				AddToNextBilling - PayPal automatically bills the outstanding balance.
				 */
				if (request.getParameter("autoBillOutstandingAmount") != "") {
					scheduleDetails.setAutoBillOutstandingAmount(AutoBillType.fromValue(request
									.getParameter("autoBillOutstandingAmount")));
				}
				/*
				 *  (Optional) Initial non-recurring payment amount due immediately upon profile creation.
				 *   Use an initial amount for enrolment or set-up fees.
					 Note:
					 All amounts included in the request must have the same currency.
					 Character length and limitations:
					  Value is a positive number which cannot exceed $10,000 USD in any currency.
					  It includes no currency symbol. 
					  It must have 2 decimal places, the decimal separator must be a period (.),
					  and the optional thousands separator must be a comma (,). 
				 */
				if (request.getParameter("initialAmount") != "") {
					ActivationDetailsType activationDetails = new ActivationDetailsType(
							new BasicAmountType(currency,request.getParameter("initialAmount")));
					/*
					 *  (Optional) Action you can specify when a payment fails. 
					 *  It is one of the following values:
    					1. ContinueOnFailure - By default, PayPal suspends the pending profile in the event that
    					 the initial payment amount fails. You can override this default behavior by setting 
    					 this field to ContinueOnFailure. Then, if the initial payment amount fails, 
    					 PayPal adds the failed payment amount to the outstanding balance for this 
    					 recurring payment profile. When you specify ContinueOnFailure, a success code is
    					 returned to you in the CreateRecurringPaymentsProfile response and the recurring
    					 payments profile is activated for scheduled billing immediately. 
    					 You should check your IPN messages or PayPal account for updates of the
    					 payment status.
    					2. CancelOnFailure - If this field is not set or you set it to CancelOnFailure,
    					 PayPal creates the recurring payment profile, but places it into a pending status
    					 until the initial payment completes. If the initial payment clears, 
    					 PayPal notifies you by IPN that the pending profile has been activated. 
    					 If the payment fails, PayPal notifies you by IPN that the pending profile 
    					 has been canceled.

					 */
					if (request.getParameter("failedInitialAmountAction") != "") {
						activationDetails.setFailedInitialAmountAction(FailedPaymentActionType.fromValue(request
										.getParameter("failedInitialAmountAction")));
					}
					scheduleDetails.setActivationDetails(activationDetails);
				}
				if (request.getParameter("trialBillingAmount") != "") {
					/*
					 * Number of billing periods that make up one billing cycle; 
					 * required if you specify an optional trial period.
					   The combination of billing frequency and billing period must be 
					   less than or equal to one year. For example, if the billing cycle is Month,
					   the maximum value for billing frequency is 12. Similarly, 
					   if the billing cycle is Week, the maximum value for billing frequency is 52.
					   Note:
					   If the billing period is SemiMonth, the billing frequency must be 1.

					 */
					int frequency = Integer.parseInt(request.getParameter("trialBillingFrequency"));
					/*
					 * Billing amount for each billing cycle during this payment period; 
					 * required if you specify an optional trial period. 
					 * This amount does not include shipping and tax amounts.
					   Note:
						All amounts in the CreateRecurringPaymentsProfile request must have 
						the same currency.
						Character length and limitations: 
						Value is a positive number which cannot exceed $10,000 USD in any currency. 
						It includes no currency symbol. 
						It must have 2 decimal places, the decimal separator must be a period (.),
						and the optional thousands separator must be a comma (,).
					 */
					BasicAmountType paymentAmount = new BasicAmountType(
							currency,request.getParameter("trialBillingAmount"));
					/*
					 * Unit for billing during this subscription period; 
					 * required if you specify an optional trial period. 
					 * It is one of the following values: [Day, Week, SemiMonth, Month, Year]
					   For SemiMonth, billing is done on the 1st and 15th of each month.
					   Note:
					   The combination of BillingPeriod and BillingFrequency cannot exceed one year.
					 */
					BillingPeriodType period = BillingPeriodType
							.fromValue(request.getParameter("trialBillingPeriod"));
					/*
					 * Number of billing periods that make up one billing cycle; 
					 * required if you specify an optional trial period.
					   The combination of billing frequency and billing period must be 
					   less than or equal to one year. For example, if the billing cycle is Month,
					   the maximum value for billing frequency is 12. Similarly, 
					   if the billing cycle is Week, the maximum value for billing frequency is 52.
					  Note:
						If the billing period is SemiMonth, the billing frequency must be 1.
					 */
					int numCycles = Integer.parseInt(request.getParameter("trialBillingCycles"));

					BillingPeriodDetailsType trialPeriod = new BillingPeriodDetailsType(
							period, frequency, paymentAmount);
					trialPeriod.setTotalBillingCycles(numCycles);
					/*
					 *  (Optional) Shipping amount for each billing cycle during this payment period.
						Note:
						All amounts in the request must have the same currency.
					 */
					if (request.getParameter("trialShippingAmount") != "") {
						trialPeriod.setShippingAmount(new BasicAmountType(
								currency, request
										.getParameter("trialShippingAmount")));
					}
					/*
					 *  (Optional) Tax amount for each billing cycle during this payment period.
						Note:
						All amounts in the request must have the same currency.
						Character length and limitations: 
						Value is a positive number which cannot exceed $10,000 USD in any currency.
						It includes no currency symbol. It must have 2 decimal places, 
						the decimal separator must be a period (.), and the optional 
						thousands separator must be a comma (,).
					 */
					if (request.getParameter("trialTaxAmount") != "") {
						trialPeriod.setTaxAmount(new BasicAmountType(currency,
								request.getParameter("trialTaxAmount")));
					}

					scheduleDetails.setTrialPeriod(trialPeriod);
				}
				
				if (request.getParameter("billingAmount") != "") {
					/*
					 *  (Required) Number of billing periods that make up one billing cycle.
						The combination of billing frequency and billing period must be less than 
						or equal to one year. For example, if the billing cycle is Month, 
						the maximum value for billing frequency is 12. Similarly, 
						if the billing cycle is Week, the maximum value for billing frequency is 52.
						Note:
						If the billing period is SemiMonth, the billing frequency must be 1.
					 */
					int frequency = Integer.parseInt(request.getParameter("billingFrequency"));
					/*
					 *  (Required) Billing amount for each billing cycle during this payment period. 
					 *  This amount does not include shipping and tax amounts.
						Note:
						All amounts in the CreateRecurringPaymentsProfile request must have the same 
						currency.
						Character length and limitations: Value is a positive number which cannot 
						exceed $10,000 USD in any currency. It includes no currency symbol. 
						It must have 2 decimal places, the decimal separator must be a period (.), 
						and the optional thousands separator must be a comma (,). 
					 */
					BasicAmountType paymentAmount = new BasicAmountType(currency, request.getParameter("billingAmount"));
					/*
					 *  (Required) Unit for billing during this subscription period. 
					 *  It is one of the following values:
    					 [Day, Week, SemiMonth, Month, Year]
						For SemiMonth, billing is done on the 1st and 15th of each month.
						Note:
						The combination of BillingPeriod and BillingFrequency cannot exceed one year.
					 */
					BillingPeriodType period = BillingPeriodType.fromValue(request.getParameter("billingPeriod"));
					/*
					 *  (Optional) Number of billing cycles for payment period.
    					For the regular payment period, if no value is specified or the value is 0, 
    					the regular payment period continues until the profile is canceled or deactivated.
    					For the regular payment period, if the value is greater than 0, 
    					the regular payment period will expire after the trial period is 
    					finished and continue at the billing frequency for TotalBillingCycles cycles.
	
					 */
					int numCycles = Integer.parseInt(request.getParameter("totalBillingCycles"));
						
					BillingPeriodDetailsType paymentPeriod = new BillingPeriodDetailsType(
							period, frequency, paymentAmount);
					paymentPeriod.setTotalBillingCycles(numCycles);
					/*
					 *  (Optional) Shipping amount for each billing cycle during this payment period.
						Note:
						All amounts in the request must have the same currency.
					 */
					if (request.getParameter("shippingAmount") != "") {
						paymentPeriod.setShippingAmount(new BasicAmountType(
								currency, request.getParameter("shippingAmount")));
					}
					/*
					 *  (Optional) Tax amount for each billing cycle during this payment period.
						Note:
						All amounts in the request must have the same currency.
						Character length and limitations: 
						Value is a positive number which cannot exceed $10,000 USD in any currency.
						It includes no currency symbol. It must have 2 decimal places, 
						the decimal separator must be a period (.), and the optional 
						thousands separator must be a comma (,).
					 */
					if (request.getParameter("taxAmount") != "") {
						paymentPeriod.setTaxAmount(new BasicAmountType(
								currency, request.getParameter("taxAmount")));
					}
					scheduleDetails.setPaymentPeriod(paymentPeriod);
				}

				CreateRecurringPaymentsProfileRequestDetailsType reqDetails = new CreateRecurringPaymentsProfileRequestDetailsType(
						profileDetails, scheduleDetails);
				/*
				 * A timestamped token, the value of which was returned in the response to the 
				 * first call to SetExpressCheckout. You can also use the token returned in the
				 * SetCustomerBillingAgreement response. Either this token or a credit card number 
				 * is required. If you include both token and credit card number, the token is used 
				 * and credit card number is ignored Call CreateRecurringPaymentsProfile once 
				 * for each billing agreement included in SetExpressCheckout request and use the
				 * same token for each call. Each CreateRecurringPaymentsProfile request creates 
				 * a single recurring payments profile.
					Note:
					Tokens expire after approximately 3 hours.
				 */
				if (request.getParameter("token") != "")
					reqDetails.setToken(request.getParameter("token"));
				else if (request.getParameter("creditCardNumber") != "") {
					CreditCardDetailsType cc = new CreditCardDetailsType();
					/*
					 *  (Required) Credit card number.
						Character length and limitations: Numeric characters only with no spaces 
						or punctuation. The string must conform with modulo and length required 
						by each credit card type.
					 */
					cc.setCreditCardNumber(request.getParameter("creditCardNumber"));
					/*
					 * Card Verification Value, version 2. 
					 * Your Merchant Account settings determine whether this field is required.
					 * To comply with credit card processing regulations, you must not store this 
					 * value after a transaction has been completed.
					   Character length and limitations: 
					   For Visa, MasterCard, and Discover, the value is exactly 3 digits. 
					   For American Express, the value is exactly 4 digits.
					 */
					cc.setCVV2(request.getParameter("cvv"));
					//Expiry Month
					cc.setExpMonth(Integer.parseInt(request.getParameter("expMonth")));
					//Expiry Year
					cc.setExpYear(Integer.parseInt(request.getParameter("expYear")));
					PayerInfoType payerInfo= new PayerInfoType();
					/*
					 *  (Required) Email address of buyer.
						Character length and limitations: 127 single-byte characters
					 */
					payerInfo.setPayer(request.getParameter("BuyerEmailId"));
					cc.setCardOwner(payerInfo);
					/*
					 * (Optional) Type of credit card. 
					 * For UK, only Maestro, MasterCard, Discover, and Visa are allowable. 
					 * For Canada, only MasterCard and Visa are allowable and 
					 * Interac debit cards are not supported. It is one of the following values:
    					[ Visa, MasterCard, Discover, Amex, Maestro: See note.]
					 Note:
					  If the credit card type is Maestro, you must set CURRENCYCODE to GBP. 
					  In addition, you must specify either STARTDATE or ISSUENUMBER.
					 */
					CreditCardTypeType type = CreditCardTypeType.fromValue(request.getParameter("creditCardType"));
					switch(type){
						case AMEX:
							cc.setCreditCardType(CreditCardTypeType.AMEX);
							break;
						case VISA:
							cc.setCreditCardType(CreditCardTypeType.VISA);
							break;
						case DISCOVER:
							cc.setCreditCardType(CreditCardTypeType.DISCOVER);
							break;
						case MASTERCARD:
							cc.setCreditCardType(CreditCardTypeType.MASTERCARD);
							break;
						default:
							break;
					}
					
					reqDetails.setCreditCard(cc);
				}

				reqType.setCreateRecurringPaymentsProfileRequestDetails(reqDetails);
				req.setCreateRecurringPaymentsProfileRequest(reqType);
				CreateRecurringPaymentsProfileResponseType resp = service
						.createRecurringPaymentsProfile(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 *  (Required) Recurring payments profile ID returned in the 
						 *  CreateRecurringPaymentsProfile response.
						    Note: The profile must have a status of either Active or Suspended.
							Character length and limitations: 14 single-byte alphanumeric characters. 
							19 character profile IDs are supported for compatibility with previous versions 
							of the PayPal API.
						 */
						map.put("Profile ID",
								resp.getCreateRecurringPaymentsProfileResponseDetails()
										.getProfileID());
						map.put("Transaction ID",
								resp.getCreateRecurringPaymentsProfileResponseDetails()
										.getTransactionID());
						/*
						 * Status of the recurring payment profile.
    						ActiveProfile - The recurring payment profile has been successfully created and activated for scheduled payments according the billing instructions from the recurring payments profile.
    						PendingProfile - The system is in the process of creating the recurring payment profile. Please check your IPN messages for an update.
						 */
						map.put("Profile Status",
								resp.getCreateRecurringPaymentsProfileResponseDetails()
										.getProfileStatus());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}

			} else if (request.getRequestURI().contains("GetRecurringPaymentsProfileDetails")) {
				GetRecurringPaymentsProfileDetailsReq req = new GetRecurringPaymentsProfileDetailsReq();
				/*
				 *  (Required) Recurring payments profile ID returned in the 
				 *  CreateRecurringPaymentsProfile response. 
				 *  19-character profile IDs are supported for compatibility with 
				 *  previous versions of the PayPal API.
					Character length and limitations: 14 single-byte alphanumeric characters
				 */
				GetRecurringPaymentsProfileDetailsRequestType reqType = new GetRecurringPaymentsProfileDetailsRequestType(
						request.getParameter("profileID"));
				req.setGetRecurringPaymentsProfileDetailsRequest(reqType);
				GetRecurringPaymentsProfileDetailsResponseType resp = service
						.getRecurringPaymentsProfileDetails(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 *  (Required) Recurring payments profile ID returned in the 
						 *  CreateRecurringPaymentsProfile response.
						    Note: The profile must have a status of either Active or Suspended.
							Character length and limitations: 14 single-byte alphanumeric characters. 
							19 character profile IDs are supported for compatibility with previous versions 
							of the PayPal API.
						 */
						map.put("Profile ID",
								resp.getGetRecurringPaymentsProfileDetailsResponseDetails()
										.getProfileID());
						/*
						 * Status of the recurring payment profile. It is one of the following values:
						    Active
						    Pending
						    Cancelled
						    Suspended
						    Expired
						 */
						map.put("Profile Status",
								resp.getGetRecurringPaymentsProfileDetailsResponseDetails()
										.getProfileStatus());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}

			} else if (request.getRequestURI().contains("ManageRecurringPaymentsProfileStatus")) {
				ManageRecurringPaymentsProfileStatusReq req = new ManageRecurringPaymentsProfileStatusReq();
				ManageRecurringPaymentsProfileStatusRequestType reqType = new ManageRecurringPaymentsProfileStatusRequestType();
				/*
				 *  (Required) Recurring payments profile ID returned in the 
				 *  CreateRecurringPaymentsProfile response.
					Character length and limitations: 14 single-byte alphanumeric characters. 
					19 character profile IDs are supported for compatibility with previous 
					versions of the PayPal API.
					------
					(Required) The action to be performed to the recurring payments profile. Must be one of the following:
    				Cancel - Only profiles in Active or Suspended state can be canceled.
    				Suspend - Only profiles in Active state can be suspended.
    				Reactivate - Only profiles in a suspended state can be reactivated.

				 */
				ManageRecurringPaymentsProfileStatusRequestDetailsType reqDetails = new ManageRecurringPaymentsProfileStatusRequestDetailsType(
						request.getParameter("profileID"),
						StatusChangeActionType.fromValue(request.getParameter("action")));
				/*
				 * (Optional) The reason for the change in status. 
				 * For profiles created using Express Checkout, this message is included 
				 * in the email notification to the buyer when the status of the profile is 
				 * successfully changed, and can also be seen by both you and the buyer on 
				 * the Status History page of the PayPal account. 
				 */
				reqDetails.setNote("change");
				reqType.setManageRecurringPaymentsProfileStatusRequestDetails(reqDetails);
				req.setManageRecurringPaymentsProfileStatusRequest(reqType);
				ManageRecurringPaymentsProfileStatusResponseType resp = service
						.manageRecurringPaymentsProfileStatus(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 *  (Required) Recurring payments profile ID returned in the 
						 *  CreateRecurringPaymentsProfile response.
						    Note: The profile must have a status of either Active or Suspended.
							Character length and limitations: 14 single-byte alphanumeric characters. 
							19 character profile IDs are supported for compatibility with previous versions 
							of the PayPal API.
						 */
						map.put("Profile ID",
								resp.getManageRecurringPaymentsProfileStatusResponseDetails()
										.getProfileID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("UpdateRecurringPaymentsProfile")) {
				UpdateRecurringPaymentsProfileReq req = new UpdateRecurringPaymentsProfileReq();
				UpdateRecurringPaymentsProfileRequestType reqType = new UpdateRecurringPaymentsProfileRequestType();
				/*
				 *  (Required) Recurring payments profile ID returned in the 
				 *  CreateRecurringPaymentsProfile response.
					Character length and limitations: 14 single-byte alphanumeric characters. 
					19 character profile IDs are supported for compatibility with previous 
					versions of the PayPal API.
				 */
				UpdateRecurringPaymentsProfileRequestDetailsType reqDetails = new UpdateRecurringPaymentsProfileRequestDetailsType(
						request.getParameter("profileID"));
				/*
				 * (Optional) The reason for the change in status. 
				 * For profiles created using Express Checkout, this message is included 
				 * in the email notification to the buyer when the status of the profile is 
				 * successfully changed, and can also be seen by both you and the buyer on 
				 * the Status History page of the PayPal account. 
				 */
				reqDetails.setNote("change");
				
				if (request.getParameter("creditCardNumber") != "") {
					CreditCardDetailsType cc = new CreditCardDetailsType();
					/*
					 *  (Required) Credit card number.
						Character length and limitations: Numeric characters only with no spaces 
						or punctuation. The string must conform with modulo and length required 
						by each credit card type.
					 */
					cc.setCreditCardNumber(request.getParameter("creditCardNumber"));
					/*
					 * Card Verification Value, version 2. Your Merchant Account settings 
					 * determine whether this field is required. To comply with credit card 
					 * processing regulations, you must not store this value after a transaction 
					 * has been completed.
						Character length and limitations: For Visa, MasterCard, and Discover, 
						the value is exactly 3 digits. For American Express, the value is exactly 
						4 digits.
					 */
					cc.setCVV2(request.getParameter("cvv"));
					//(Required) Credit card expiration month.
					cc.setExpMonth(Integer.parseInt(request.getParameter("expMonth")));
					//(Required) Credit card expiration year.
					cc.setExpYear(Integer.parseInt(request.getParameter("expYear")));
					
					PayerInfoType payerInfo= new PayerInfoType();
					payerInfo.setPayer(request.getParameter("BuyerEmailId"));
					cc.setCardOwner(payerInfo);
					/*
					 *  (Optional) Type of credit card. For UK, only Maestro, MasterCard, Discover, and Visa are allowable. For Canada, only MasterCard and Visa are allowable and Interac debit cards are not supported. It is one of the following values:
	    			   [Visa,  MasterCard, Discover, Amex, Maestro: See note]
					  Note:
						If the credit card type is Maestro, you must set currencyId to GBP. 
						In addition, you must specify either StartMonth and StartYear or IssueNumber.
						Character length and limitations: Up to 10 single-byte alphabetic characters
					 */
					CreditCardTypeType type = CreditCardTypeType.fromValue(request.getParameter("creditCardType"));
					switch(type){
						case AMEX:
							cc.setCreditCardType(CreditCardTypeType.AMEX);
							break;
						case VISA:
							cc.setCreditCardType(CreditCardTypeType.VISA);
							break;
						case DISCOVER:
							cc.setCreditCardType(CreditCardTypeType.DISCOVER);
							break;
						case MASTERCARD:
							cc.setCreditCardType(CreditCardTypeType.MASTERCARD);
							break;
						default:
							break;
					}
					reqDetails.setCreditCard(cc);
				}
				reqDetails.setBillingStartDate(request
						.getParameter("billingStartDate") + "T00:00:00:000Z");
					
				if (request.getParameter("trialBillingAmount") != "") {
					/*
					 * Number of billing periods that make up one billing cycle; 
					 * required if you specify an optional trial period.
					   The combination of billing frequency and billing period must be 
					   less than or equal to one year. For example, if the billing cycle is Month,
					   the maximum value for billing frequency is 12. Similarly, 
					   if the billing cycle is Week, the maximum value for billing frequency is 52.
					   Note:
					   If the billing period is SemiMonth, the billing frequency must be 1.

					 */
					int frequency = Integer.parseInt(request.getParameter("trialBillingFrequency"));
					/*
					 * Number of billing periods that make up one billing cycle; 
					 * required if you specify an optional trial period.
					   The combination of billing frequency and billing period must be 
					   less than or equal to one year. For example, if the billing cycle is Month,
					   the maximum value for billing frequency is 12. Similarly, 
					   if the billing cycle is Week, the maximum value for billing frequency is 52.
					   Note:
					   If the billing period is SemiMonth, the billing frequency must be 1.

					 */
					BasicAmountType paymentAmount = new BasicAmountType(
							currency,request.getParameter("trialBillingAmount"));
					/*
					 * Unit for billing during this subscription period; 
					 * required if you specify an optional trial period. 
					 * It is one of the following values: [Day, Week, SemiMonth, Month, Year]
					   For SemiMonth, billing is done on the 1st and 15th of each month.
					   Note:
					   The combination of BillingPeriod and BillingFrequency cannot exceed one year.
					 */
					BillingPeriodType period = BillingPeriodType
							.fromValue(request
									.getParameter("trialBillingPeriod"));
					/*
					 * Number of billing periods that make up one billing cycle; 
					 * required if you specify an optional trial period.
					   The combination of billing frequency and billing period must be 
					   less than or equal to one year. For example, if the billing cycle is Month,
					   the maximum value for billing frequency is 12. Similarly, 
					   if the billing cycle is Week, the maximum value for billing frequency is 52.
					  Note:
						If the billing period is SemiMonth, the billing frequency must be 1.
					 */
					int numCycles = Integer.parseInt(request.getParameter("trialBillingCycles"));

					BillingPeriodDetailsType_Update trialPeriod = new BillingPeriodDetailsType_Update();
					trialPeriod.setBillingPeriod(period);
					trialPeriod.setBillingFrequency(frequency);
					trialPeriod.setAmount(paymentAmount);
					trialPeriod.setTotalBillingCycles(numCycles);
					/*
					 *  (Optional) Shipping amount for each billing cycle during this payment period.
						Note:
						All amounts in the request must have the same currency.
					 */
					if (request.getParameter("trialShippingAmount") != "") {
						trialPeriod.setShippingAmount(new BasicAmountType(
								currency, request
										.getParameter("trialShippingAmount")));
					}
					/*
					 *  (Optional) Tax amount for each billing cycle during this payment period.
						Note:
						All amounts in the request must have the same currency.
						Character length and limitations: 
						Value is a positive number which cannot exceed $10,000 USD in any currency.
						It includes no currency symbol. It must have 2 decimal places, 
						the decimal separator must be a period (.), and the optional 
						thousands separator must be a comma (,).
					 */
					if (request.getParameter("trialTaxAmount") != "") {
						trialPeriod.setTaxAmount(new BasicAmountType(currency,
								request.getParameter("trialTaxAmount")));
					}

					reqDetails.setTrialPeriod(trialPeriod);
				}
				if (request.getParameter("billingAmount") != "") {
					/*
					 *  (Required) Number of billing periods that make up one billing cycle.
						The combination of billing frequency and billing period must be less than 
						or equal to one year. For example, if the billing cycle is Month, 
						the maximum value for billing frequency is 12. Similarly, 
						if the billing cycle is Week, the maximum value for billing frequency is 52.
						Note:
						If the billing period is SemiMonth, the billing frequency must be 1.
					 */
					int frequency = Integer.parseInt(request.getParameter("billingFrequency"));
					/*
					 *  (Required) Number of billing periods that make up one billing cycle.
						The combination of billing frequency and billing period must be less than 
						or equal to one year. For example, if the billing cycle is Month, 
						the maximum value for billing frequency is 12. Similarly, 
						if the billing cycle is Week, the maximum value for billing frequency is 52.
						Note:
						If the billing period is SemiMonth, the billing frequency must be 1.
					 */
					BasicAmountType paymentAmount = new BasicAmountType(
							currency, request.getParameter("billingAmount"));
					/*
					 *  (Required) Unit for billing during this subscription period. 
					 *  It is one of the following values:
    					 [Day, Week, SemiMonth, Month, Year]
						For SemiMonth, billing is done on the 1st and 15th of each month.
						Note:
						The combination of BillingPeriod and BillingFrequency cannot exceed one year.
					 */
					BillingPeriodType period = BillingPeriodType.fromValue(request.getParameter("billingPeriod"));
					/*
					 *  (Optional) Number of billing cycles for payment period.
    					For the regular payment period, if no value is specified or the value is 0, 
    					the regular payment period continues until the profile is canceled or deactivated.
    					For the regular payment period, if the value is greater than 0, 
    					the regular payment period will expire after the trial period is 
    					finished and continue at the billing frequency for TotalBillingCycles cycles.
	
					 */
					int numCycles = Integer.parseInt(request.getParameter("totalBillingCycles"));

					BillingPeriodDetailsType_Update paymentPeriod = new BillingPeriodDetailsType_Update();
					paymentPeriod.setBillingPeriod(period);
					paymentPeriod.setBillingFrequency(frequency);
					paymentPeriod.setAmount(paymentAmount);
					paymentPeriod.setTotalBillingCycles(numCycles);
					/*
					 *  (Optional) Shipping amount for each billing cycle during this payment period.
					Note:
						All amounts in the request must have the same currency.
						Character length and limitations: Value is a positive number which 
						cannot exceed $10,000 USD in any currency. It includes no currency symbol. 
						It must have 2 decimal places, the decimal separator must be a period (.), 
						and the optional thousands separator must be a comma (,).
					 */
					if (request.getParameter("billingShippingAmount") != "") {
						paymentPeriod.setShippingAmount(new BasicAmountType(
								currency, request
										.getParameter("billingShippingAmount")));
					}
					/*
					 *  (Optional) Tax amount for each billing cycle during the regular payment period.
					Note:
						All amounts in the request must have the same currency.
						Character length and limitations: Value is a positive number which 
						cannot exceed $10,000 USD in any currency. It includes no currency symbol. 
						It must have 2 decimal places, the decimal separator must be a period (.), 
						and the optional thousands separator must be a comma (,). 
					 */
					if (request.getParameter("billingTaxAmount") != "") {
						paymentPeriod.setTaxAmount(new BasicAmountType(
								currency, request
										.getParameter("billingTaxAmount")));
					}
					reqDetails.setPaymentPeriod(paymentPeriod);
				}
				/*
				 *  (Optional) The number of failed payments allowed before the profile is 
				 *  automatically suspended. The specified value cannot be less than the 
				 *  current number of failed payments for this profile.
					Character length and limitations: Number string representing an integer
				 */
				if (request.getParameter("maxFailedPayments") != "") {
					reqDetails.setMaxFailedPayments(Integer.parseInt(request
							.getParameter("maxFailedPayments")));
				}
				/*
				 *  (Optional) Description of the recurring payment.
					Character length and limitations: 127 single-byte alphanumeric characters
				 */
				if (request.getParameter("profileDescription") != "") {
					reqDetails.setDescription(request
							.getParameter("profileDescription"));
				}
				//(Optional) The number of additional billing cycles to add to this profile.
				if (request.getParameter("additionalBillingCycles") != "") {
					reqDetails.setAdditionalBillingCycles(Integer
							.parseInt(request
									.getParameter("additionalBillingCycles")));
				}
				/*
				 *  (Optional) This field indicates whether you would like PayPal to automatically 
				 *  bill the outstanding balance amount in the next billing cycle. 
				 *  It is one of the following values:
    				  NoAutoBill - PayPal does not automatically bill the outstanding balance.
    				  AddToNextBilling - PayPal automatically bills the outstanding balance

				 */
				if (request.getParameter("autoBillOutstandingAmount") != "") {
					reqDetails
							.setAutoBillOutstandingAmount(AutoBillType.fromValue(request
									.getParameter("autoBillOutstandingAmount")));
				}
				/*
				 *  (Optional) Billing amount for each cycle in the subscription period, 
				 *  not including shipping and tax amounts.
				Note:
					For recurring payments with Express Checkout, the payment amount can be 
					increased by no more than 20% every 180 days (starting when the profile is created).
					Character length and limitations: Value is a positive number which cannot 
					exceed $10,000 USD in any currency. It includes no currency symbol. 
					It must have 2 decimal places, the decimal separator must be a period (.), 
					and the optional thousands separator must be a comma (,). 
				 */
				if (request.getParameter("amount") != "") {
					reqDetails.setAmount(new BasicAmountType(currency, request
							.getParameter("amount")));
				}
				/*
				 *  (Optional) Shipping amount for each billing cycle during the regular payment period.
					Note:
						All amounts in the request must have the same currency.
						Character length and limitations: Value is a positive number which cannot 
						exceed $10,000 USD in any currency. It includes no currency symbol. 
						It must have 2 decimal places, the decimal separator must be a period (.), 
						and the optional thousands separator must be a comma (,). 
				 */
				if (request.getParameter("shippingAmount") != "") {
					reqDetails.setShippingAmount(new BasicAmountType(currency, request
							.getParameter("shippingAmount")));
				}
				/*
				 *  (Optional) Tax amount for each billing cycle during the regular payment period.
				 Note:
					All amounts in the request must have the same currency.
					Character length and limitations: Value is a positive number which cannot 
					exceed $10,000 USD in any currency. It includes no currency symbol. 
					It must have 2 decimal places, the decimal separator must be a period (.), 
					and the optional thousands separator must be a comma (,). 
				 */
				if (request.getParameter("taxAmount") != "") {
					reqDetails.setTaxAmount(new BasicAmountType(currency, request
							.getParameter("taxAmount")));
				}
				/*
				 *  (Optional) Full name of the person receiving the product or service 
				 *  paid for by the recurring payment. If not present, the name in the 
				 *  buyer's PayPal account is used.
					Character length and limitations: 32 single-byte characters
				 */
				if (request.getParameter("subscriberName") != "") {
					reqDetails.setSubscriberName(request.getParameter("subscriberName"));
					
				} else if (request.getParameter("shippingName") != "") {
					AddressType shippingAddr = new AddressType();
					/*
					 * Person's name associated with this shipping address. 
					 * It is required if using a shipping address.
						Character length and limitations: 32 single-byte characters
					 */
					shippingAddr.setName(request.getParameter("shippingName"));
					/*
					 * First street address. It is required if using a shipping address.
						Character length and limitations: 100 single-byte characters
					 */
					shippingAddr.setStreet1(request.getParameter("shippingStreet1"));
					/*
					 *  (Optional) Second street address.
						Character length and limitations: 100 single-byte characters
					 */
					shippingAddr.setStreet2(request.getParameter("shippingStreet2"));
					/*
					 *  (Optional) Phone number.
						Character length and limitations: 20 single-byte characters
					 */
					shippingAddr.setPhone(request.getParameter("shippingPhone"));
					/*
					 * Name of city. It is required if using a shipping address.
						Character length and limitations: 40 single-byte characters
					 */
					shippingAddr.setCityName(request.getParameter("shippingCity"));
					/*
					 * State or province. It is required if using a shipping address.
						Character length and limitations: 40 single-byte characters
					 */
					shippingAddr.setStateOrProvince(request.getParameter("shippingState"));
					/*
					 * Country code. It is required if using a shipping address.
						Character length and limitations: 2 single-byte characters
					 */
					shippingAddr.setCountryName(request.getParameter("shippingCountry"));
					/*
					 * U.S. ZIP code or other country-specific postal code. 
					 * It is required if using a U.S. shipping address; 
					 * may be required for other countries.
						Character length and limitations: 20 single-byte characters
					 */
					shippingAddr.setPostalCode(request.getParameter("shippingPostalCode"));
					reqDetails.setSubscriberShippingAddress(shippingAddr);
				}

				reqType.setUpdateRecurringPaymentsProfileRequestDetails(reqDetails);
				req.setUpdateRecurringPaymentsProfileRequest(reqType);
				UpdateRecurringPaymentsProfileResponseType resp = service
						.updateRecurringPaymentsProfile(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 * Recurring payments profile ID returned in the CreateRecurringPaymentsProfile response. 
						 * An error is returned if the profile specified in the BillOutstandingAmount request has 
						 * a status of canceled or expired.
						 */
						map.put("Profile ID",
								resp.getUpdateRecurringPaymentsProfileResponseDetails()
										.getProfileID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("BillOutstandingAmount")) {
				BillOutstandingAmountReq req = new BillOutstandingAmountReq();
				BillOutstandingAmountRequestType reqType = new BillOutstandingAmountRequestType();
				/*
				 *  (Required) Recurring payments profile ID returned in the 
				 *  CreateRecurringPaymentsProfile response.
				    Note: The profile must have a status of either Active or Suspended.
					Character length and limitations: 14 single-byte alphanumeric characters. 
					19 character profile IDs are supported for compatibility with previous versions 
					of the PayPal API.
				 */
				BillOutstandingAmountRequestDetailsType reqDetails = new BillOutstandingAmountRequestDetailsType(
						request.getParameter("profileID"));
				/*
				 *  (Optional) The amount to bill. 
				 *  The amount must be less than or equal to the current outstanding balance 
				 *  of the profile. If no value is specified, PayPal attempts to bill the entire 
				 *  outstanding balance amount.
					Character length and limitations: Value is a positive number which cannot 
					exceed $10,000 USD in any currency. It includes no currency symbol. 
					It must have 2 decimal places, the decimal separator must be a period (.), 
					and the optional thousands separator must be a comma (,). 
				 */
				if (request.getParameter("amt") != "")
					reqDetails.setAmount(new BasicAmountType(currency, request
							.getParameter("amt")));
				reqType.setVersion("84");
				reqType.setBillOutstandingAmountRequestDetails(reqDetails);
				req.setBillOutstandingAmountRequest(reqType);
				BillOutstandingAmountResponseType resp = service
						.billOutstandingAmount(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
					/*
					 * Recurring payments profile ID returned in the CreateRecurringPaymentsProfile response. 
					 * An error is returned if the profile specified in the BillOutstandingAmount request 
					 * has a status of canceled or expired.
					 */
						map.put("Profile ID", resp
								.getBillOutstandingAmountResponseDetails()
								.getProfileID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
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
		}

	}
}
