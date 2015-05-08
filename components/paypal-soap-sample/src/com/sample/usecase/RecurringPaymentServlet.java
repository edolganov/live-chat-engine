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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import urn.ebay.api.PayPalAPI.CreateRecurringPaymentsProfileReq;
import urn.ebay.api.PayPalAPI.CreateRecurringPaymentsProfileRequestType;
import urn.ebay.api.PayPalAPI.CreateRecurringPaymentsProfileResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.BillingAgreementDetailsType;
import urn.ebay.apis.eBLBaseComponents.BillingCodeType;
import urn.ebay.apis.eBLBaseComponents.BillingPeriodDetailsType;
import urn.ebay.apis.eBLBaseComponents.BillingPeriodType;
import urn.ebay.apis.eBLBaseComponents.CreateRecurringPaymentsProfileRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.CreditCardDetailsType;
import urn.ebay.apis.eBLBaseComponents.CreditCardTypeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.ItemCategoryType;
import urn.ebay.apis.eBLBaseComponents.PayerInfoType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsItemType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.RecurringPaymentsProfileDetailsType;
import urn.ebay.apis.eBLBaseComponents.ScheduleDetailsType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;

public class RecurringPaymentServlet extends HttpServlet {
	private static final long serialVersionUID = 3409273409234L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (request.getRequestURI().contains("SetExpressCheckoutForRecurringPayments")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher("/usecase_jsp/SetExpressCheckoutForRecurringPayments.jsp")
					.forward(request, response);
		} else if (request.getRequestURI().contains("RecurringPaymentsUsingPayPal")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/usecase_jsp/RecurringPaymentsUsingPayPal.jsp")
					.forward(request, response);
		} else if (request.getRequestURI().contains("RecurringPaymentsUsingCreditCard")) {
			getServletConfig()
					.getServletContext()
					.getRequestDispatcher(
							"/usecase_jsp/RecurringPaymentsUsingCreditCard.jsp")
					.forward(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		CurrencyCodeType currency = CurrencyCodeType.fromValue("USD");
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
		
		if (request.getRequestURI().contains("SetExpressCheckoutForRecurringPayments")) {

			SetExpressCheckoutRequestType setExpressCheckoutReq = new SetExpressCheckoutRequestType();
			SetExpressCheckoutRequestDetailsType details = new SetExpressCheckoutRequestDetailsType();

			StringBuffer url = new StringBuffer();
			url.append("http://");
			url.append(request.getServerName());
			url.append(":");
			url.append(request.getServerPort());
			url.append(request.getContextPath());

			String returnURL = url.toString() + "/RecurringPaymentsUsingPayPal";
			String cancelURL = url.toString() + "/SetExpressCheckoutForRecurringPayments";
			
			/*
			 *  (Required) URL to which the buyer's browser is returned after choosing 
			 *  to pay with PayPal. For digital goods, you must add JavaScript to this 
			 *  page to close the in-context experience.
			  Note:
				PayPal recommends that the value be the final review page on which the buyer 
				confirms the order and payment or billing agreement.
				Character length and limitations: 2048 single-byte characters
			 */
			details.setReturnURL(returnURL + "?currencyCodeType="+ request.getParameter("currencyCode"));

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
					session.setAttribute("ecToken",setExpressCheckoutResponse.getToken());
					response.sendRedirect("https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+setExpressCheckoutResponse.getToken());
				} else {
					session.setAttribute("Error",setExpressCheckoutResponse.getErrors());
					response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
				}
			}
		
		} 
		 // ********** Creating Recurring Profile using PayPal *********
		else if (request.getRequestURI().contains("RecurringPaymentsUsingPayPal")) {

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
			 * SetCustomerBillingAgreement response.Each CreateRecurringPaymentsProfile request creates 
			 * a single recurring payments profile.
				Note:
				Tokens expire after approximately 3 hours.
			 */
			reqDetails.setToken(request.getParameter("token"));

			reqType.setCreateRecurringPaymentsProfileRequestDetails(reqDetails);
			req.setCreateRecurringPaymentsProfileRequest(reqType);
			CreateRecurringPaymentsProfileResponseType resp = null;
			try{
				resp = service.createRecurringPaymentsProfile(req);
			}catch(Exception e){
				e.printStackTrace();
			}
			
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

		
		}
		 // ************  Creating recurring profile using credit card ****************
		else if(request.getRequestURI().contains("RecurringPaymentsUsingCreditCard")){


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
			 * credit card number is required for CreateRecurringPaymentsProfile.  
			 * Each CreateRecurringPaymentsProfile request creates 
			 * a single recurring payments profile.
				Note:
			 */
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

			reqType.setCreateRecurringPaymentsProfileRequestDetails(reqDetails);
			req.setCreateRecurringPaymentsProfileRequest(reqType);
			CreateRecurringPaymentsProfileResponseType resp = null;
			try{
				resp = service.createRecurringPaymentsProfile(req);
			}catch(Exception e){
				e.printStackTrace();
			}
			
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

		
		}
	}
}
