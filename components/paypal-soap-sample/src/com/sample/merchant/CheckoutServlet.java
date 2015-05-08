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
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.DoUATPExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoUATPExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoUATPExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.ExecuteCheckoutOperationsReq;
import urn.ebay.api.PayPalAPI.ExecuteCheckoutOperationsRequestType;
import urn.ebay.api.PayPalAPI.ExecuteCheckoutOperationsResponseType;
import urn.ebay.api.PayPalAPI.ExternalRememberMeOptOutReq;
import urn.ebay.api.PayPalAPI.ExternalRememberMeOptOutRequestType;
import urn.ebay.api.PayPalAPI.ExternalRememberMeOptOutResponseType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsReq;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.AddressType;
import urn.ebay.apis.eBLBaseComponents.ApprovalSubTypeType;
import urn.ebay.apis.eBLBaseComponents.ApprovalTypeType;
import urn.ebay.apis.eBLBaseComponents.AuthorizationRequestType;
import urn.ebay.apis.eBLBaseComponents.BillingAgreementDetailsType;
import urn.ebay.apis.eBLBaseComponents.BillingApprovalDetailsType;
import urn.ebay.apis.eBLBaseComponents.BillingCodeType;
import urn.ebay.apis.eBLBaseComponents.BuyerDetailType;
import urn.ebay.apis.eBLBaseComponents.CountryCodeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.ExecuteCheckoutOperationsRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.ExternalRememberMeOwnerDetailsType;
import urn.ebay.apis.eBLBaseComponents.IdentificationInfoType;
import urn.ebay.apis.eBLBaseComponents.InfoSharingDirectivesType;
import urn.ebay.apis.eBLBaseComponents.ItemCategoryType;
import urn.ebay.apis.eBLBaseComponents.MerchantPullPaymentCodeType;
import urn.ebay.apis.eBLBaseComponents.MobileIDInfoType;
import urn.ebay.apis.eBLBaseComponents.OrderDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsItemType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentDirectivesType;
import urn.ebay.apis.eBLBaseComponents.PaymentInfoType;
import urn.ebay.apis.eBLBaseComponents.RememberMeIDInfoType;
import urn.ebay.apis.eBLBaseComponents.SetDataRequestType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;

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
public class CheckoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public CheckoutServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getRequestURI().contains("SetExpressCheckout"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Checkout/SetExpressCheckout.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("GetExpressCheckout"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Checkout/GetExpressCheckout.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("DoExpressCheckout"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Checkout/DoExpressCheckout.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains(
				"DoUATPExpressCheckoutPayment"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/Checkout/DoUATPExpressCheckoutPayment.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("ExternalRememberMeOptOut"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/Checkout/ExternalRememberMeOptOut.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("ExecuteCheckoutOperations"))
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/Checkout/ExecuteCheckoutOperations.jsp")
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
				"<ul><li><a href='EC/SetExpressCheckout'>SetExpressCheckout</a></li><li><a href='EC/GetExpressCheckout'>GetExpressCheckout</a></li><li><a href='EC/DoExpressCheckout'>DoExpressCheckout</a></li></ul>");
		response.setContentType("text/html");
		try {
			
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
			if (request.getRequestURI().contains("SetExpressCheckout")) {
				SetExpressCheckoutRequestType setExpressCheckoutReq = new SetExpressCheckoutRequestType();
				SetExpressCheckoutRequestDetailsType details = new SetExpressCheckoutRequestDetailsType();

				StringBuffer url = new StringBuffer();
				url.append("http://");
				url.append(request.getServerName());
				url.append(":");
				url.append(request.getServerPort());
				url.append(request.getContextPath());

				String returnURL = url.toString() + "/EC/GetExpressCheckout";
				String cancelURL = url.toString() + "/index.html";
				
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
				request.getSession().setAttribute("paymentType",request.getParameter("paymentType"));

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
				paydtl.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentType")));
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
						AddressType shipToAddress=new AddressType();
				/*
				 * Person's name associated with this shipping address.
				 *  It is required if using a shipping address.
				   Character length and limitations: 32 single-byte characters
				 */
				shipToAddress.setName(request.getParameter("name"));
				/*
				 * First street address. It is required if using a shipping address.
				   Character length and limitations: 100 single-byte characters
				 */
				shipToAddress.setStreet1(request.getParameter("street1"));
				/*
				 *  (Optional) Second street address.
					Character length and limitations: 100 single-byte characters
				 */
				shipToAddress.setStreet2(request.getParameter("street2"));
				/*
				 * Name of city. It is required if using a shipping address.
				   Character length and limitations: 40 single-byte characters
				 */
				shipToAddress.setCityName(request.getParameter("city"));
				/*
				 * State or province. It is required if using a shipping address.
				   Character length and limitations: 40 single-byte characters
				 */
				shipToAddress.setStateOrProvince(request.getParameter("state"));
				/*
				 * U.S. ZIP code or other country-specific postal code. 
				 * It is required if using a U.S. shipping address and may be
				 *  required for other countries.
				   Character length and limitations: 20 single-byte characters
				 */
				shipToAddress.setPostalCode(request.getParameter("postalCode"));
				/*
				 * Country code. It is required if using a shipping address.
				   Character length and limitations: 2 single-byte characters
				 */
				shipToAddress.setCountry(CountryCodeType.fromValue(request.getParameter("countryCode")));
				paydtl.setShipToAddress(shipToAddress);
				payDetails.add(paydtl);
				details.setPaymentDetails(payDetails);
				if (request.getParameter("billingAgreementText") != "") {
					/*
					 *  (Required) Type of billing agreement. For recurring payments,
					 *   this field must be set to RecurringPayments. 
					 *   In this case, you can specify up to ten billing agreements. 
					 *   Other defined values are not valid.
						 Type of billing agreement for reference transactions. 
						 You must have permission from PayPal to use this field. 
						 This field must be set to one of the following values:
    						1. MerchantInitiatedBilling - PayPal creates a billing agreement 
    						   for each transaction associated with buyer.You must specify 
    						   version 54.0 or higher to use this option.
    						2. MerchantInitiatedBillingSingleAgreement - PayPal creates a 
    						   single billing agreement for all transactions associated with buyer.
    						   Use this value unless you need per-transaction billing agreements. 
    						   You must specify version 58.0 or higher to use this option.

					 */
					BillingAgreementDetailsType billingAgreement = new BillingAgreementDetailsType(
							BillingCodeType.fromValue(request.getParameter("billingType")));
					/*
					 * Description of goods or services associated with the billing agreement. 
					 * This field is required for each recurring payment billing agreement.
					 *  PayPal recommends that the description contain a brief summary of 
					 *  the billing agreement terms and conditions. For example,
					 *   buyer is billed at "9.99 per month for 2 years".
					   Character length and limitations: 127 single-byte alphanumeric characters
					 */
					billingAgreement.setBillingAgreementDescription(request.getParameter("billingAgreementText"));
					List<BillingAgreementDetailsType> billList = new ArrayList<BillingAgreementDetailsType>();
					billList.add(billingAgreement);
					details.setBillingAgreementDetails(billList);
				}
				
				//shipping address
				/*
				 * Indicates whether or not you require the buyer's shipping address on 
				 * file with PayPal be a confirmed address. For digital goods, 
				 * this field is required, and you must set it to 0. It is one of the following values:
    				0 - You do not require the buyer's shipping address be a confirmed address.
    				1 - You require the buyer's shipping address be a confirmed address.
					Note:
					Setting this field overrides the setting you specified in your Merchant Account Profile.
					Character length and limitations: 1 single-byte numeric character
				 */
				details.setReqConfirmShipping(request.getParameter("reqConfirmShipping"));
				/*
				 *  (Optional) Determines whether or not the PayPal pages should 
				 *  display the shipping address set by you in this SetExpressCheckout request,
				 *   not the shipping address on file with PayPal for this buyer. Displaying 
				 *   the PayPal street address on file does not allow the buyer to edit that address. 
				 *   It is one of the following values:
    				 0 - The PayPal pages should not display the shipping address.
    				 1 - The PayPal pages should display the shipping address.
					Character length and limitations: 1 single-byte numeric character
				 */
				details.setAddressOverride(request.getParameter("addressoverride"));
		
				
				/*
				 * Determines where or not PayPal displays shipping address fields on the PayPal pages. 
				 * For digital goods, this field is required, and you must set it to 1. It is one of the
				 *  following values:
    				0 - PayPal displays the shipping address on the PayPal pages.
    				1 - PayPal does not display shipping address fields whatsoever.
    				2 - If you do not pass the shipping address, PayPal obtains it from the 
    				buyer's account profile.
					Character length and limitations: 4 single-byte numeric characters
				 */
				details.setNoShipping(request.getParameter("noShipping"));
				
				// PayPal page styling attributes
				/*
				 *  (Optional) A label that overrides the business name in the PayPal account on 
				 *   the PayPal hosted checkout pages.
					 Character length and limitations: 127 single-byte alphanumeric characters
				 */
				details.setBrandName(request.getParameter("brandName"));
				/*
				 *  (Optional) Name of the Custom Payment Page Style for payment pages associated with
				 *  this button or link. It corresponds to the HTML variable page_style for customizing
				 *  payment pages. It is the same name as the Page Style Name you chose to add or edit 
				 *  the page style in your PayPal Account profile.
					Character length and limitations: 30 single-byte alphabetic characters
				 */
				details.setCustom(request.getParameter("pageStyle"));
				/*
				 * (Optional) URL for the image you want to appear at the top left of the payment page. 
				 * The image has a maximum size of 750 pixels wide by 90 pixels high. PayPal recommends 
				 * that you provide an image that is stored on a secure (https) server. 
				 * If you do not specify an image, the business name displays.
				 */
				details.setCppHeaderImage(request.getParameter("cppheaderimage"));
				/*
				 *  (Optional) Sets the border color around the header of the payment page. 
				 *  The border is a 2-pixel perimeter around the header space, which is 750 pixels
				 *  wide by 90 pixels high. By default, the color is black.
					Character length and limitations: 6-character HTML hexadecimal ASCII color code
				 */
				details.setCppHeaderBorderColor(request.getParameter("cppheaderbordercolor"));
				/*
				 *  (Optional) Sets the background color for the header of the payment page. 
				 *   By default, the color is white.
					 Character length and limitations: 6-character HTML hexadecimal ASCII color code 
				 */
				details.setCppHeaderBackColor(request.getParameter("cppheaderbackcolor"));
				/*
				 *  (Optional) Sets the background color for the payment page. 
				 *   By default, the color is white.
					 Character length and limitations: 6-character HTML hexadecimal ASCII color code 
				 */
				details.setCppPayflowColor(request.getParameter("cpppayflowcolor"));
				/*
				 *  (Optional) Enables the buyer to enter a note to the merchant on the 
				 *  PayPal page during checkout. The note is returned in the 
				 *  GetExpressCheckoutDetails response and the DoExpressCheckoutPayment response.
				 *  It is one of the following values:
    				0 - The buyer is unable to enter a note to the merchant.
    				1 - The buyer is able to enter a note to the merchant.
					Character length and limitations: 1 single-byte numeric character
					This field is available since version 53.0. 
				 */
				details.setAllowNote(request.getParameter("allowNote"));

				setExpressCheckoutReq.setSetExpressCheckoutRequestDetails(details);

				SetExpressCheckoutReq expressCheckoutReq = new SetExpressCheckoutReq();
				expressCheckoutReq
						.setSetExpressCheckoutRequest(setExpressCheckoutReq);

				SetExpressCheckoutResponseType setExpressCheckoutResponse = service
						.setExpressCheckout(expressCheckoutReq);

				if (setExpressCheckoutResponse != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (setExpressCheckoutResponse.getAck().toString()
							.equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", setExpressCheckoutResponse.getAck());
						/*
						 * A time stamped token by which you identify to PayPal that you are processing 
						 * this payment with Express Checkout. The token expires after three hours. 
						 * If you set the token in the SetExpressCheckout request, the value of the 
						 * token in the response is identical to the value in the request.
							Character length and limitations: 20 single-byte characters
						 */
						map.put("Token", setExpressCheckoutResponse.getToken());
						map.put("Redirect URL",
								"<a href=https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="
										+ setExpressCheckoutResponse.getToken()
										+ ">Redirect To PayPal</a>");
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {

						session.setAttribute("Error",
								setExpressCheckoutResponse.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			}
			//# GetExpressCheckout API
			// The GetExpressCheckoutDetails API operation obtains information about
			// an Express Checkout transaction.
			// This sample code uses Merchant Java SDK to make API call. You can
			// download the SDKs [here](https://github.com/paypal/sdk-packages/tree/gh-pages/merchant-sdk/java)
			if (request.getRequestURI().contains("GetExpressCheckout")) {
				GetExpressCheckoutDetailsReq req = new GetExpressCheckoutDetailsReq();
				/*
				 * A timestamped token by which you identify to PayPal that you are processing
				 * this payment with Express Checkout. The token expires after three hours. 
				 * If you set the token in the SetExpressCheckout request, the value of the token
				 * in the response is identical to the value in the request.
				   Character length and limitations: 20 single-byte characters
				 */
				GetExpressCheckoutDetailsRequestType reqType = new GetExpressCheckoutDetailsRequestType(
						request.getParameter("token"));
				req.setGetExpressCheckoutDetailsRequest(reqType);
				GetExpressCheckoutDetailsResponseType resp = service
						.getExpressCheckoutDetails(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						/*
						 * The time stamped token value that was returned by SetExpressCheckout 
						 * response and passed on GetExpressCheckoutDetails request.
							Character length and limitations: 20 single-byte characters
						 */
						map.put("Token", resp.getGetExpressCheckoutDetailsResponseDetails().getToken());
						/*
						 * Unique PayPal Customer Account identification number.
							Character length and limitations: 13 single-byte alphanumeric characters
						 */
						map.put("Payer ID", resp.getGetExpressCheckoutDetailsResponseDetails()
								.getPayerInfo().getPayerID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {

						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			}
			//# DoExpressCheckout API
			// The DoExpressCheckoutPayment API operation completes an Express Checkout
			// transaction. If you set up a billing agreement in your SetExpressCheckout
			// API call, the billing agreement is created when you call the
			// DoExpressCheckoutPayment API operation.
			// This sample code uses Merchant Java SDK to make API call. You can
			// download the SDKs [here](https://github.com/paypal/sdk-packages/tree/gh-pages/merchant-sdk/java)
			if (request.getRequestURI().contains("DoExpressCheckout")) {
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
				doExpressCheckoutPaymentReq
						.setDoExpressCheckoutPaymentRequest(doCheckoutPaymentRequestType);

				DoExpressCheckoutPaymentResponseType doCheckoutPaymentResponseType = service
						.doExpressCheckoutPayment(doExpressCheckoutPaymentReq);
				response.setContentType("text/html");
				if (doCheckoutPaymentResponseType != null) {
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
						//The ID of the billing agreement associated with the Express Checkout transaction.
						map.put("Billing Agreement ID",
								doCheckoutPaymentResponseType
										.getDoExpressCheckoutPaymentResponseDetails()
										.getBillingAgreementID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {

						session.setAttribute("Error",
								doCheckoutPaymentResponseType.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}

			} else if (request.getRequestURI().contains("DoUATPExpressCheckout")) {
				DoUATPExpressCheckoutPaymentReq req = new DoUATPExpressCheckoutPaymentReq();
				DoUATPExpressCheckoutPaymentRequestType reqType = new DoUATPExpressCheckoutPaymentRequestType();

				DoExpressCheckoutPaymentRequestDetailsType checkoutDetails = new DoExpressCheckoutPaymentRequestDetailsType();
				/*
				 * Unique PayPal Customer Account identification number.
				   Character length and limitations: 13 single-byte alphanumeric characters
				 */
				checkoutDetails.setPayerID(request.getParameter("payerID"));
				/*
				 * A timestamped token by which you identify to PayPal that you are processing
				 * this payment with Express Checkout. The token expires after three hours. 
				 * If you set the token in the SetExpressCheckout request, the value of the token
				 * in the response is identical to the value in the request.
				   Character length and limitations: 20 single-byte characters
				 */
				checkoutDetails.setToken(request.getParameter("token"));
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
				checkoutDetails.setPaymentAction(PaymentActionCodeType.fromValue(request.getParameter("paymentAction")));
				BasicAmountType amount = new BasicAmountType(
						CurrencyCodeType.fromValue(request
								.getParameter("currencyID")),
						request.getParameter("amt"));
				PaymentDetailsType detailsType = new PaymentDetailsType();
				detailsType.setOrderTotal(amount);
				List<PaymentDetailsType> payList = new ArrayList<PaymentDetailsType>();
				payList.add(detailsType);
				checkoutDetails.setPaymentDetails(payList);
				reqType.setDoExpressCheckoutPaymentRequestDetails(checkoutDetails);
				req.setDoUATPExpressCheckoutPaymentRequest(reqType);
				DoUATPExpressCheckoutPaymentResponseType resp = service
						.doUATPExpressCheckoutPayment(req);

				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						map.put("UATP Number", resp.getUATPDetails()
								.getUATPNumber());
						map.put("Expiry Month", resp.getUATPDetails()
								.getExpMonth());
						map.put("Expiry Year", resp.getUATPDetails()
								.getExpYear());
						Iterator<PaymentInfoType> iterator = resp
								.getDoExpressCheckoutPaymentResponseDetails()
								.getPaymentInfo().iterator();
						int index = 1;
						while (iterator.hasNext()) {
							PaymentInfoType result = (PaymentInfoType) iterator
									.next();
							map.put("Transaction ID" + index,
									result.getTransactionID());
							index++;
						}
						map.put("Billing Agreement ID", resp
								.getDoExpressCheckoutPaymentResponseDetails()
								.getBillingAgreementID());
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");

					} else {

						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("ExternalRememberMeOptOut")) {
				ExternalRememberMeOptOutReq req = new ExternalRememberMeOptOutReq();
				ExternalRememberMeOptOutRequestType reqType = new ExternalRememberMeOptOutRequestType(
						request.getParameter("externalRememberMeID"));
				ExternalRememberMeOwnerDetailsType externalRememberMeOwner = new ExternalRememberMeOwnerDetailsType();
				externalRememberMeOwner
						.setExternalRememberMeOwnerIDType(request.getParameter("ownerIDType"));
				externalRememberMeOwner.setExternalRememberMeOwnerID(request.getParameter("ownerID"));
				reqType.setExternalRememberMeOwnerDetails(externalRememberMeOwner);
				req.setExternalRememberMeOptOutRequest(reqType);
				ExternalRememberMeOptOutResponseType resp = service
						.externalRememberMeOptOut(req);
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
			} else if (request.getRequestURI().contains("ExecuteCheckoutOperations")) {
				ExecuteCheckoutOperationsReq req = new ExecuteCheckoutOperationsReq();
				SetDataRequestType setDataRequest = new SetDataRequestType();
				List<BillingApprovalDetailsType> billingApprovalList = new ArrayList<BillingApprovalDetailsType>();
				BillingApprovalDetailsType billingApproval = new BillingApprovalDetailsType(
						ApprovalTypeType.fromValue(request.getParameter("billingApprovalType")));
				billingApproval.setApprovalSubType(ApprovalSubTypeType
						.fromValue(request
								.getParameter("billingApprovalSubType")));
				OrderDetailsType orderDetails = new OrderDetailsType();
				orderDetails.setMaxAmount(new BasicAmountType(CurrencyCodeType
						.fromValue(request.getParameter("currencyID")), request
						.getParameter("amt")));
				billingApproval.setOrderDetails(orderDetails);
				PaymentDirectivesType paymentDirectives = new PaymentDirectivesType();
				paymentDirectives.setPaymentType(MerchantPullPaymentCodeType
						.fromValue(request.getParameter("paymentType")));
				billingApproval.setPaymentDirectives(paymentDirectives);
				billingApprovalList.add(billingApproval);
				setDataRequest.setBillingApprovalDetails(billingApprovalList);

				BuyerDetailType buyerDetail = new BuyerDetailType();
				IdentificationInfoType identificationInfo = new IdentificationInfoType();
				if (request.getParameter("externalRememberMeID") != "") {
					RememberMeIDInfoType rememberMeIDInfo = new RememberMeIDInfoType();
					rememberMeIDInfo.setExternalRememberMeID(request
							.getParameter("externalRememberMeID"));
					identificationInfo.setRememberMeIDInfo(rememberMeIDInfo);
				}
				if (request.getParameter("sessionToken") != "") {
					MobileIDInfoType mobileIDInfo = new MobileIDInfoType();
					mobileIDInfo.setSessionToken(request
							.getParameter("sessionToken"));
					identificationInfo.setMobileIDInfo(mobileIDInfo);
				}
				buyerDetail.setIdentificationInfo(identificationInfo);
				setDataRequest.setBuyerDetail(buyerDetail);

				InfoSharingDirectivesType infoSharingDirectives = new InfoSharingDirectivesType();
				infoSharingDirectives.setReqBillingAddress(request
						.getParameter("reqBillingAddress"));
				setDataRequest.setInfoSharingDirectives(infoSharingDirectives);

				ExecuteCheckoutOperationsRequestDetailsType reqDetails = new ExecuteCheckoutOperationsRequestDetailsType(
						setDataRequest);

				AuthorizationRequestType authRequest = new AuthorizationRequestType(
						Boolean.parseBoolean(request
								.getParameter("isRequested")));
				reqDetails.setAuthorizationRequest(authRequest);

				ExecuteCheckoutOperationsRequestType reqType = new ExecuteCheckoutOperationsRequestType(
						reqDetails);
				req.setExecuteCheckoutOperationsRequest(reqType);
				ExecuteCheckoutOperationsResponseType resp = service
						.executeCheckoutOperations(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						map.put("Token", resp
								.getExecuteCheckoutOperationsResponseDetails()
								.getSetDataResponse().getToken());
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
