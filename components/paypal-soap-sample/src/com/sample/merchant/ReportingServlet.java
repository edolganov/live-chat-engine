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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import urn.ebay.api.PayPalAPI.AddressVerifyReq;
import urn.ebay.api.PayPalAPI.AddressVerifyRequestType;
import urn.ebay.api.PayPalAPI.AddressVerifyResponseType;
import urn.ebay.api.PayPalAPI.GetBalanceReq;
import urn.ebay.api.PayPalAPI.GetBalanceRequestType;
import urn.ebay.api.PayPalAPI.GetBalanceResponseType;
import urn.ebay.api.PayPalAPI.GetPalDetailsReq;
import urn.ebay.api.PayPalAPI.GetPalDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetPalDetailsResponseType;
import urn.ebay.api.PayPalAPI.GetTransactionDetailsReq;
import urn.ebay.api.PayPalAPI.GetTransactionDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetTransactionDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.TransactionSearchReq;
import urn.ebay.api.PayPalAPI.TransactionSearchRequestType;
import urn.ebay.api.PayPalAPI.TransactionSearchResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.PaymentTransactionSearchResultType;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.sample.util.Configuration;

public class ReportingServlet extends HttpServlet {
	private static final long serialVersionUID = 2212442342452L;

	public ReportingServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getRequestURI().contains("TransactionSearch"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Reports/TransactionSearch.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("GetTransactionDetails"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Reports/GetTransactionDetails.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("GetBalance"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Reports/GetBalance.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("GetPalDetails"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Reports/GetPalDetails.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("AddressVerify"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Reports/AddressVerify.jsp")
					.forward(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.setAttribute("url", request.getRequestURI());
		session.setAttribute(
				"relatedUrl",
				"<ul><li><a href='Report/GetTransactionDetails'>GetTransactionDetails</a></li><li><a href='Report/TransactionSearch'>TransactionSearch</a></li><li><a href='Report/GetBalance'>GetBalance</a></li><li><a href='Report/GetPalDetails'>GetPalDetails</a></li><li><a href='Report/AddressVerify'>AddressVerify</a></li></ul>");
		response.setContentType("text/html");
		try {
			
			// Configuration map containing signature credentials and other required configuration.
			// For a full list of configuration parameters refer in wiki page.
			// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
			Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
			
			// Creating service wrapper object to make an API call by loading configuration map.
			PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configurationMap);
			
			if (request.getRequestURI().contains("TransactionSearch")) {
				TransactionSearchReq txnreq = new TransactionSearchReq();
				TransactionSearchRequestType type = new TransactionSearchRequestType();
				/*
				 *  (Required) The earliest transaction date at which to start the search.
					Character length and limitations: No wildcards are allowed. 
					The value must be in UTC/GMT.
				 */
				if ((request.getParameter("startDate") != null)
						&& !request.getParameter("startDate").toString()
								.equals("")) {
					type.setStartDate(request.getParameter("startDate"));
				}
				
				/*
				 *  (Optional) The latest transaction date to be included in the search.
					Character length and limitations: No wildcards are allowed. 
					The value must be in UTC/GMT.
				 */
				if ((request.getParameter("endDate") != null)
						&& !request.getParameter("endDate").toString()
								.equals("")) {
					type.setEndDate(request.getParameter("endDate"));
				}
				/*
				 *  (Optional) Search by the transaction ID. 
				 *  The returned results are from the merchant's transaction records.
					Character length and limitations: 19 single-byte characters maximum
				 */
				if (request.getParameter("transactionID") != "") {
					type.setTransactionID(request.getParameter("transactionID"));
				}

				txnreq.setTransactionSearchRequest(type);
				TransactionSearchResponseType txnresponse = null;
				txnresponse = service.transactionSearch(txnreq);

				if (txnresponse != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (txnresponse.getAck().toString()
							.equalsIgnoreCase("SUCCESS")) {
						if (txnresponse.getPaymentTransactions().size() > 0) {
							Map<Object, Object> map = new LinkedHashMap<Object, Object>();
							map.put("Ack", txnresponse.getAck());
							Iterator<PaymentTransactionSearchResultType> iterator = txnresponse
									.getPaymentTransactions().iterator();
							int index = 1;
							while (iterator.hasNext()) {
								PaymentTransactionSearchResultType result = (PaymentTransactionSearchResultType) iterator
										.next();
								//Merchant's transaction ID.
								map.put("Transaction ID" + index,
										result.getTransactionID());
								//The net amount of the transaction.
								if (result.getNetAmount() != null) {
									map.put("Net Amount" + index, result
											.getNetAmount().getValue()
											+ " "
											+ result.getNetAmount()
													.getCurrencyID());
								}
							/*
							 * Email address of either the buyer or the payment recipient (the "payee").
							 * If the payment amount is positive, this field is the recipient of the funds. 
							 * If the payment is negative, this field is the paying buyer.
							 */
								map.put("Payer" + index, result.getPayer());
							   //The status of the transaction. 	
								map.put("Status" + index, result.getStatus());
								index++;
							}

							session.setAttribute("map", map);
							response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
						}
					} else {
						session.setAttribute("Error", txnresponse.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("GetTransactionDetails")) {
				GetTransactionDetailsReq req = new GetTransactionDetailsReq();
				GetTransactionDetailsRequestType reqType = new GetTransactionDetailsRequestType();
				/*
				 * Unique transaction ID of the payment.
				   Character length and limitations: 17 single-byte characters
				 */
				reqType.setTransactionID(request.getParameter("transID"));
				req.setGetTransactionDetailsRequest(reqType);
				GetTransactionDetailsResponseType resp = service.getTransactionDetails(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						//Email address of buyer.
						map.put("Payer", resp.getPaymentTransactionDetails()
								.getPayerInfo().getPayer());
						/*
						 * The final amount charged, including any shipping and taxes from 
						 * your Merchant Profile.
							Character length and limitations: Value is a positive number 
							which cannot exceed $10,000 USD in any currency. 
							It includes no currency symbol. It must have 2 decimal places, 
							the decimal separator must be a period (.), and the optional 
							thousands separator must be a comma (,). 
						 */
						map.put("Gross Amount", resp
								.getPaymentTransactionDetails()
								.getPaymentInfo().getGrossAmount().getValue()
								+ " "
								+ resp.getPaymentTransactionDetails()
										.getPaymentInfo().getGrossAmount()
										.getCurrencyID());
						/*
						 * Invoice number you set in the original transaction.
							Character length and limitations: 256 single-byte alphanumeric characters
						 */
						map.put("Invoice ID", resp
								.getPaymentTransactionDetails()
								.getPaymentItemInfo().getInvoiceID());
						/*
						 * Primary email address of the payment recipient (the merchant).
						   If you are the recipient of the payment and the payment is sent 
						   to your non-primary email address, the value of Receiver is still 
						   your primary email address.
						   Character length and limitations: 127 single-byte alphanumeric characters
						 */
						map.put("Receiver", resp.getPaymentTransactionDetails()
								.getReceiverInfo().getReceiver());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");

					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("GetBalance")) {
				GetBalanceReq req = new GetBalanceReq();
				GetBalanceRequestType reqType = new GetBalanceRequestType();

				/*
				(Optional) Indicates whether to return all currencies. It is one of the following values:
    				0 - Return only the balance for the primary currency holding.
    				1 - Return the balance for each currency holding.
				Note:
				This field is available since version 51. 
				Prior versions return only the balance for the primary currency holding.

				 */
				reqType.setReturnAllCurrencies(request.getParameter("returnAllCurrencies"));
				req.setGetBalanceRequest(reqType);
				GetBalanceResponseType resp = service.getBalance(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						//Available balance and associated currency code for the primary currency holding. 
						map.put("Balance", resp.getBalance().getValue() + " "
								+ resp.getBalance().getCurrencyID());
						Iterator<BasicAmountType> iterator = resp
								.getBalanceHoldings().iterator();
						//Amount in different currency holding, that constitute the part of available balance 
						int index = 1;
						while (iterator.hasNext()) {
							BasicAmountType amount = (BasicAmountType) iterator
									.next();
							map.put("Amount" + index, amount.getValue() + " "
									+ amount.getCurrencyID());
							index++;
						}
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");

					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("GetPalDetails")) {
				GetPalDetailsReq req = new GetPalDetailsReq();
				GetPalDetailsRequestType reqType = new GetPalDetailsRequestType();
				req.setGetPalDetailsRequest(reqType);
				/*
				 * Obtain your Pal ID, which is the PayPal-assigned merchant account number, 
				 * and other informaton about your account. 
				 * You need the account number when working with dynamic versions 
				 * of PayPalbuttons and logos
				 */
				GetPalDetailsResponseType resp = service.getPalDetails(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						//ThePayPal-assigned merchant account number.
						map.put("Pal ID", resp.getPal());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("AddressVerify")) {
				AddressVerifyReq req = new AddressVerifyReq();
				/*
				 * #. Email: (Required) Email address of a PayPal member to verify.
				 * #. Street: (Required) First line of the billing or shipping postal address to verify. 
				 * To pass verification, the value of Street must match the first 3 single-byte characters of a postal address on file for the PayPal member.
				 * #. Zip: (Required) Postal code to verify. To pass verification, 
				 * the value of Zip must match the first 5 single-byte characters of the postal 
				 * code of the verified postal address for the verified PayPal member. 
				 */
				AddressVerifyRequestType reqType = new AddressVerifyRequestType(
						request.getParameter("mail"),
						request.getParameter("street"),
						request.getParameter("zip"));
				req.setAddressVerifyRequest(reqType);
				AddressVerifyResponseType resp = service.addressVerify(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 * Indicates whether the address is a confirmed address on file at PayPal. 
						 * It is one of the following values:
    						None - The request value of the Email element does not match any email address on file at PayPal.
    						Confirmed - If the response value of the StreetMatch element is Matched, the entire postal address is confirmed.
    						Unconfirmed - PayPal responds that the postal address is unconfirmed.
						 Note:
							The values Confirmed and Unconfirmed both indicate that the member email address passed verification.

						 */
						map.put("Confirmation Code", resp.getConfirmationCode());
						/*
						 * The token contains encrypted information about the member's email address 
						 * and postal address. If you pass the value of the token in the 
						 * HTML variable address_api_token of Buy Now buttons, 
						 * PayPal prevents the buyer from using an email address or 
						 * postal address other than those that PayPal verified with this API call. 
						 * The token is valid for 24 hours.
						 */
						map.put("PayPal Token", resp.getPayPalToken());
						/*
						 * Indicates whether the street address matches address information on file at PayPal. 
						 * It is one of the following values:
    						None - The request value of the Email element does not match any email address on file at PayPal. No comparison of other request values was made.
    						Matched - The request value of the Street element matches the first 3 single-byte characters of a postal address on file for the PayPal member.
    						Unmatched - The request value of the Street element does not match any postal address on file for the PayPal member.
						 */
						map.put("Street Match", resp.getStreetMatch());
						/*
						 * Indicates whether the ZIP address matches address information on file at PayPal. 
						 * It is one of the following values:
    						None - The request value of the Street element was unmatched. No comparison of the Zip element was made.
    						Matched - The request value of the Zip element matches the ZIP code of the postal address on file for the PayPal member.
    						Unmatched - The request value of the Zip element does not match the ZIP code of the postal address on file for the PayPal member.
						 */
						map.put("Zip Match", resp.getZipMatch());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SSLConfigurationException e) {
			e.printStackTrace();
		} catch (InvalidCredentialException e) {
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
