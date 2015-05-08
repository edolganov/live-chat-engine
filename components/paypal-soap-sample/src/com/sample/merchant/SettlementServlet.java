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

import urn.ebay.api.PayPalAPI.DoAuthorizationReq;
import urn.ebay.api.PayPalAPI.DoAuthorizationRequestType;
import urn.ebay.api.PayPalAPI.DoAuthorizationResponseType;
import urn.ebay.api.PayPalAPI.DoCaptureReq;
import urn.ebay.api.PayPalAPI.DoCaptureRequestType;
import urn.ebay.api.PayPalAPI.DoCaptureResponseType;
import urn.ebay.api.PayPalAPI.DoNonReferencedCreditReq;
import urn.ebay.api.PayPalAPI.DoNonReferencedCreditRequestType;
import urn.ebay.api.PayPalAPI.DoNonReferencedCreditResponseType;
import urn.ebay.api.PayPalAPI.DoReauthorizationReq;
import urn.ebay.api.PayPalAPI.DoReauthorizationRequestType;
import urn.ebay.api.PayPalAPI.DoReauthorizationResponseType;
import urn.ebay.api.PayPalAPI.DoUATPAuthorizationReq;
import urn.ebay.api.PayPalAPI.DoUATPAuthorizationRequestType;
import urn.ebay.api.PayPalAPI.DoUATPAuthorizationResponseType;
import urn.ebay.api.PayPalAPI.DoVoidReq;
import urn.ebay.api.PayPalAPI.DoVoidRequestType;
import urn.ebay.api.PayPalAPI.DoVoidResponseType;
import urn.ebay.api.PayPalAPI.ManagePendingTransactionStatusReq;
import urn.ebay.api.PayPalAPI.ManagePendingTransactionStatusRequestType;
import urn.ebay.api.PayPalAPI.ManagePendingTransactionStatusResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.RefundTransactionReq;
import urn.ebay.api.PayPalAPI.RefundTransactionRequestType;
import urn.ebay.api.PayPalAPI.RefundTransactionResponseType;
import urn.ebay.api.PayPalAPI.ReverseTransactionReq;
import urn.ebay.api.PayPalAPI.ReverseTransactionRequestType;
import urn.ebay.api.PayPalAPI.ReverseTransactionResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CompleteCodeType;
import urn.ebay.apis.eBLBaseComponents.CreditCardDetailsType;
import urn.ebay.apis.eBLBaseComponents.CreditCardTypeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoNonReferencedCreditRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.FMFPendingTransactionActionType;
import urn.ebay.apis.eBLBaseComponents.MerchantStoreDetailsType;
import urn.ebay.apis.eBLBaseComponents.RefundSourceCodeType;
import urn.ebay.apis.eBLBaseComponents.RefundType;
import urn.ebay.apis.eBLBaseComponents.ReverseTransactionRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.TransactionEntityType;
import urn.ebay.apis.eBLBaseComponents.UATPDetailsType;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.sample.util.Configuration;

/**
 * Servlet implementation class CheckoutServlet
 */
public class SettlementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public SettlementServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getRequestURI().contains("DoAuthorization"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Settlements/DoAuthorization.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("DoReauthorization"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Settlements/DoReauthorization.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("DoVoid"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Settlements/DoVoid.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("DoCapture"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Settlements/DoCapture.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("Refund"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Settlements/Refund.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("DoUATPAuthorization"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/Settlements/DoUATPAuthorization.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("ReverseTransaction"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher("/Settlements/ReverseTransaction.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("DoNonReferencedCredit"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/Settlements/DoNonReferencedCredit.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains(
				"ManagePendingTransactionStatus"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/Settlements/ManagePendingTransactionStatus.jsp")
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
		session.setAttribute(
				"relatedUrl",
				"<ul><li><a href='Settlement/DoAuthorization'>DoAuthorization</a></li><li><a href='Settlement/DoCapture'>DoCapture</a></li><li><a href='Settlement/DoVoid'>DoVoid</a></li><li><a href='Settlement/DoReauthorization'>DoReauthorization</a></li><li><a href='Settlement/Refund'>Refund</a></li><li><a href='Settlement/ReverseTransaction'>ReverseTransaction</a></li><li><a href='Settlement/DoNonReferencedCredit'>DoNonReferencedCredit</a></li><li><a href='Settlement/ManagePendingTransactionStatus'>ManagePendingTransactionStatus</a></li></ul>");
		response.setContentType("text/html");
		try {
			
			// Configuration map containing signature credentials and other required configuration.
			// For a full list of configuration parameters refer in wiki page.
			// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
			Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
			
			// Creating service wrapper object to make an API call by loading configuration map.
			PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configurationMap);
			
			if (request.getRequestURI().contains("DoAuthorization")) {
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
				DoAuthorizationResponseType resp = service.doAuthorization(req);
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
						    Reversed- A payment was reversed due to a chargeback or other type of reversal. PayPal removes the funds from your account balance and returns them to the buyer. The ReasonCode element specifies the reason for the reversal.
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
			} else if (request.getRequestURI().contains("DoReauthorization")) {
				// ## DoAuthorizationReq
				DoReauthorizationReq req = new DoReauthorizationReq();
				// `Amount` to reauthorize which takes mandatory params:
				//
				// * `currencyCode`
				// * `amount`
				BasicAmountType amount = new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyCode")),
						request.getParameter("amt"));
				// `DoReauthorizationRequest` which takes mandatory params:
				//
				// * `Authorization Id` - Value of a previously authorized transaction
				// identification number returned by PayPal.
				// * `amount`
				DoReauthorizationRequestType reqType = new DoReauthorizationRequestType(
						request.getParameter("authID"), amount);

				req.setDoReauthorizationRequest(reqType);
				DoReauthorizationResponseType resp = service.doReauthorization(req);

				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 * New authorization identification number.
						   Character length and limits:19 single-byte characters 
						 */
						map.put("Authorization ID", resp.getAuthorizationID());
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
						    Reversed- A payment was reversed due to a chargeback or other type of reversal. PayPal removes the funds from your account balance and returns them to the buyer. The ReasonCode element specifies the reason for the reversal.
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
			} else if (request.getRequestURI().contains("DoVoid")) {
				DoVoidReq req = new DoVoidReq();
				// DoVoidRequest which takes mandatory params:
				//
				// * `Authorization ID` - Original authorization ID specifying the
				// authorization to void or, to void an order, the order ID.
				// `Important:
				// If you are voiding a transaction that has been reauthorized, use the
				// ID from the original authorization, and not the reauthorization.`
				DoVoidRequestType reqType = new DoVoidRequestType(request.getParameter("authID"));
				req.setDoVoidRequest(reqType);
				DoVoidResponseType resp = service.doVoid(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 * Authorization identification number you specified in the request.
							Character length and limitations: 19 single-byte characters
						 */
						map.put("Authorization ID", resp.getAuthorizationID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {

						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("DoCapture")) {
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
				// DoExpressCheckoutPayment, DoDirectPayment, or CheckOut. For
				// point-of-sale transactions, this is the transaction ID returned by
				// the CheckOut call when the payment action is Authorization.
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
				DoCaptureResponseType resp = service.doCapture(req);
				if (resp != null) {
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
			} else if (request.getRequestURI().contains("Refund")) {
				RefundTransactionReq req = new RefundTransactionReq();
				RefundTransactionRequestType reqType = new RefundTransactionRequestType();
				/*
				 *  (Required) Unique identifier of the transaction to be refunded.
					Note:
					Either the transaction ID or the payer ID must be specified.
					Character length and limitations: 17 single-byte alphanumeric characters
				 */
				reqType.setTransactionID(request.getParameter("transID"));
				
				/*
				 *  Type of refund you are making. It is one of the following values:
    				Full - Full refund (default).
    				Partial - Partial refund.
    				ExternalDispute - External dispute. (Value available since version 82.0)
    				Other - Other type of refund. (Value available since version 82.0)

				 */
				if (request.getParameter("refundType") != "Full"
						& request.getParameter("refundType") != "") {
					reqType.setAmount(new BasicAmountType(CurrencyCodeType
							.fromValue(request.getParameter("currencyID")),
							request.getParameter("amt")));
				}
				if (request.getParameter("refundType") != "")
					reqType.setRefundType(RefundType.fromValue(request
							.getParameter("refundType")));
				/*
				 *  (Optional)Type of PayPal funding source (balance or eCheck) that can be used for auto refund. 
				 *  It is one of the following values:
    				any - The merchant does not have a preference. Use any available funding source.
    				default - Use the merchant's preferred funding source, as configured in the merchant's profile.
    				instant - Use the merchant's balance as the funding source.
    				eCheck - The merchant prefers using the eCheck funding source. 
    				If the merchant's PayPal balance can cover the refund amount, use the PayPal balance.
				 Note:
					This field does not apply to point-of-sale transactions.
					This field is available since version 82.0. 
				 */
				if (request.getParameter("refundSource") != "")
					reqType.setRefundSource(RefundSourceCodeType
							.fromValue(request.getParameter("refundSource")));
				/*
				 * Identifier of the merchant store at which the refund is given. 
				 * This field is required for point-of-sale transactions.
					Character length and limitations: 50 single-byte characters
					This field is available since version 82.0. 
				 */
				if (request.getParameter("storeID") != null) {
					MerchantStoreDetailsType merchantStoreDetails = new MerchantStoreDetailsType(
							request.getParameter("storeID"));
					/*
					 *  (Optional) ID of the terminal.
						Character length and limitations: 50 single-byte characters
						This field is available since version 82.0. 
					 */
					merchantStoreDetails.setTerminalID(request.getParameter("terminalID"));
					reqType.setMerchantStoreDetails(merchantStoreDetails);
				}
				req.setRefundTransactionRequest(reqType);
				RefundTransactionResponseType resp = service
						.refundTransaction(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 * Unique transaction ID of the refund.
						   Character length and limitations:17 single-byte characters
						 */
						map.put("Refund Transaction ID",resp.getRefundTransactionID());
						/*
						 * Total amount refunded so far from the original purchase. Say, for example, 
						 * a buyer makes $100 purchase, the buyer was refunded $20 a week ago and is 
						 * refunded $30 in this transaction. The gross refund amount is $30 
						 * (in this transaction). The total refunded amount is $50.
						   Character length and limitations: Value is a positive number which 
						   cannot exceed $10,000 USD in any currency. It includes no currency symbol. 
						   It must have 2 decimal places, the decimal separator must be a period (.), 
						   and the optional thousands separator must be a comma (,).
						   This field is available since version 67.0. 
						 */
						map.put("Total Refunded Amount", resp.getTotalRefundedAmount().getValue()
								+ " " + resp.getTotalRefundedAmount().getCurrencyID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {

						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}

			} else if (request.getRequestURI().contains("DoUATPAuthorization")) {
				DoUATPAuthorizationReq req = new DoUATPAuthorizationReq();
				UATPDetailsType details = new UATPDetailsType();
				BasicAmountType amount = new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyID")),
						request.getParameter("amt"));
				details.setExpMonth(Integer.parseInt(request
						.getParameter("expMonth")));
				details.setExpYear(Integer.parseInt(request
						.getParameter("expYear")));
				details.setUATPNumber(request.getParameter("UATPNum"));
				DoUATPAuthorizationRequestType reqType = new DoUATPAuthorizationRequestType(
						details, amount);
				reqType.setTransactionEntity(TransactionEntityType
						.fromValue(request.getParameter("transactionEntity")));
				req.setDoUATPAuthorizationRequest(reqType);
				DoUATPAuthorizationResponseType resp = service
						.doUATPAuthorization(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						map.put("Authorization Code",
								resp.getAuthorizationCode());
						map.put("Payment Status", resp.getAuthorizationInfo()
								.getPaymentStatus());
						map.put("Amount", resp.getAmount().getValue() + " "
								+ resp.getAmount().getCurrencyID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("ReverseTransaction")) {
				ReverseTransactionReq req = new ReverseTransactionReq();
				ReverseTransactionRequestDetailsType reqDetails = new ReverseTransactionRequestDetailsType();
				//The transaction ID of the transaction whose payment has been denied or accepted.
				reqDetails.setTransactionID(request.getParameter("transID"));
				ReverseTransactionRequestType reqType = new ReverseTransactionRequestType(reqDetails);
				req.setReverseTransactionRequest(reqType);
				ReverseTransactionResponseType resp = service
						.reverseTransaction(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						map.put("Reverse Transaction ID", resp
								.getReverseTransactionResponseDetails()
								.getReverseTransactionID());
						map.put("Status", resp
								.getReverseTransactionResponseDetails()
								.getStatus());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}

			} else if (request.getRequestURI().contains("DoNonReferencedCredit")) {
				DoNonReferencedCreditReq req = new DoNonReferencedCreditReq();
				DoNonReferencedCreditRequestDetailsType reqDetails = new DoNonReferencedCreditRequestDetailsType();
				CreditCardDetailsType cardDetails = new CreditCardDetailsType();
				/*
				 *  (Optional) Type of credit card. For UK, only Maestro, MasterCard, Discover, and Visa are allowable. For Canada, only MasterCard and Visa are allowable and Interac debit cards are not supported. It is one of the following values:
    			   [Visa,  MasterCard, Discover, Amex, Maestro: See note]
				  Note:
					If the credit card type is Maestro, you must set currencyId to GBP. 
					In addition, you must specify either StartMonth and StartYear or IssueNumber.
					Character length and limitations: Up to 10 single-byte alphabetic characters
				 */
				cardDetails.setCreditCardType(CreditCardTypeType.fromValue(request.getParameter("creditCardType")));
				/*
				 *  (Required) Credit card number.
					Character length and limitations: Numeric characters only with no spaces
					or punctuation. The string must conform with modulo and length required by 
					each credit card type.
				 */
				cardDetails.setCreditCardNumber(request.getParameter("creditCardNumber"));
				/*
				 *  (Required) Credit card expiration month.
					Character length and limitations: 2 single-byte numeric characters, 
					including leading zero
				 */
				cardDetails.setExpMonth(Integer.parseInt(request.getParameter("expMonth")));
				/*
				 *  (Required) Credit card expiration year.
					Character length and limitations: 4 single-byte numeric characters
				 */
				cardDetails.setExpYear(Integer.parseInt(request.getParameter("expYear")));
				/*
				 * Card Verification Value, version 2. Your Merchant Account settings determine
				 * whether this field is required. To comply with credit card processing regulations, 
				 * you must not store this value after a transaction has been completed.
				   Character length and limitations: For Visa, MasterCard, and Discover, 
				   the value is exactly 3 digits. For American Express, the value is exactly 4 digits.
				 */
				cardDetails.setCVV2(request.getParameter("cvv"));
				reqDetails.setCreditCard(cardDetails);
				/*
				 * (Optional) Field used by merchant to record why this credit was issued to a buyer. 
				 * It is similar to a "memo" field (freeform text or string field).
				 */
				reqDetails.setComment(request.getParameter("comment"));
				
				/*
				 *  (Optional) Total amount of all items in this transaction.
					Note:
					The only valid currencies are AUD, CAD, EUR, GBP, JPY, and USD.
					Character length and limitations: Must not exceed $10,000 USD in any currency. 
					No currency symbol. Must have 2 decimal places, decimal separator must be a period (.), 
					and the optional thousands separator must be a comma (,). 
				 */
				reqDetails.setNetAmount(new BasicAmountType(CurrencyCodeType
						.fromValue(request.getParameter("currencyID")), request
						.getParameter("itemAmount")));
				/*
				 *  (Optional) Total shipping costs in this transaction.
					Note:
					The only valid currencies are AUD, CAD, EUR, GBP, JPY, and USD.
					Character length and limitations: Value must be zero or greater and cannot 
					exceed $10,000 USD in any currency. No currency symbol. 
					Must have 2 decimal places, decimal separator must be a period (.), 
					and the optional thousands separator must be a comma (,). 
					The only valid currencies are AUD, CAD, EUR, GBP, JPY, and USD.
				 */
				reqDetails.setShippingAmount(new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyID")), request
								.getParameter("shippingAmount")));
				/*
				 *  (Optional) Sum of tax for all items in this order.
					Note:
					The only valid currencies are AUD, CAD, EUR, GBP, JPY, and USD.
					Character length and limitations: The value must be zero or greater and 
					cannot exceed $10,000 USD in any currency. No currency symbol. 
					Must have 2 decimal places, decimal separator must be a period (.), 
					and the optional thousands separator must be a comma (,).
				 */
				reqDetails.setTaxAmount(new BasicAmountType(CurrencyCodeType
						.fromValue(request.getParameter("currencyID")), request
						.getParameter("taxAmount")));
				/*
				 *  (Required) Total of order, including shipping, handling, and tax. 
				 *  Amount = NetAmount + ShippingAmount + TaxAmount
					Character length and limitations: Must not exceed $10,000 USD in any currency. 
					No currency symbol. Must have 2 decimal places, decimal separator must be a period (.),
					and the optional thousands separator must be a comma (,).
				 */
				double totalAmount = Double.parseDouble(request
						.getParameter("itemAmount"))
						+ Double.parseDouble(request
								.getParameter("shippingAmount"))
						+ Double.parseDouble(request.getParameter("taxAmount"));
				reqDetails.setAmount(new BasicAmountType(CurrencyCodeType
						.fromValue(request.getParameter("currencyID")), String
						.valueOf(totalAmount)));
				DoNonReferencedCreditRequestType reqType = new DoNonReferencedCreditRequestType(
						reqDetails);
				req.setDoNonReferencedCreditRequest(reqType);
				DoNonReferencedCreditResponseType resp = service
						.doNonReferencedCredit(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 * Unique identifier of a transaction.
							Character length and limitations: 17 single-byte alphanumeric characters.
						 */
						map.put("Transaction ID", resp
								.getDoNonReferencedCreditResponseDetails()
								.getTransactionID());
						/*
						 * Total of order, including shipping, handling, and tax.
							Character length and limitations: Must not exceed $10,000 USD in any currency. 
							No currency symbol. Must have 2 decimal places, decimal separator must be a period (.), 
							and the optional thousands separator must be a comma (,).
						 */
						map.put("Amount",
								resp.getDoNonReferencedCreditResponseDetails()
										.getAmount().getValue()
										+ " "
										+ resp.getDoNonReferencedCreditResponseDetails()
												.getAmount().getCurrencyID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("ManagePendingTransactionStatus")) {
				ManagePendingTransactionStatusReq req = new ManagePendingTransactionStatusReq();
				/*
				 * (Required) The transaction ID of the payment transaction.
				 * (Required) The operation you want to perform on the transaction. 
				 * It is one of the following values:
    				Accept - Accepts the payment
    				Deny - Rejects the payment
				 */
				ManagePendingTransactionStatusRequestType reqType = new ManagePendingTransactionStatusRequestType(
						request.getParameter("transactionID"),
						FMFPendingTransactionActionType.fromValue(request
								.getParameter("action")));
				req.setManagePendingTransactionStatusRequest(reqType);
				ManagePendingTransactionStatusResponseType resp = service
						.managePendingTransactionStatus(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						//The transaction ID of the transaction whose payment has been denied or accepted. 
						map.put("Transaction ID", resp.getTransactionID());
						/*
						 *  TransactionStatus is one of the following values:
						    Pending
						    Processing
						    Completed
						    Denied
						    Reversed
						    Display Only
						    Partially Refunded
						    Created Refunded
						 */
						map.put("Status", resp.getStatus());
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
