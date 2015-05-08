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

import urn.ebay.api.PayPalAPI.EnterBoardingReq;
import urn.ebay.api.PayPalAPI.EnterBoardingRequestType;
import urn.ebay.api.PayPalAPI.EnterBoardingResponseType;
import urn.ebay.api.PayPalAPI.GetBoardingDetailsReq;
import urn.ebay.api.PayPalAPI.GetBoardingDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetBoardingDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.apis.eBLBaseComponents.AddressType;
import urn.ebay.apis.eBLBaseComponents.AverageMonthlyVolumeType;
import urn.ebay.apis.eBLBaseComponents.AverageTransactionPriceType;
import urn.ebay.apis.eBLBaseComponents.BankAccountDetailsType;
import urn.ebay.apis.eBLBaseComponents.BankAccountTypeType;
import urn.ebay.apis.eBLBaseComponents.BusinessCategoryType;
import urn.ebay.apis.eBLBaseComponents.BusinessInfoType;
import urn.ebay.apis.eBLBaseComponents.BusinessOwnerInfoType;
import urn.ebay.apis.eBLBaseComponents.BusinessTypeType;
import urn.ebay.apis.eBLBaseComponents.CountryCodeType;
import urn.ebay.apis.eBLBaseComponents.EnterBoardingRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.MarketingCategoryType;
import urn.ebay.apis.eBLBaseComponents.PayerInfoType;
import urn.ebay.apis.eBLBaseComponents.PercentageRevenueFromOnlineSalesType;
import urn.ebay.apis.eBLBaseComponents.PersonNameType;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.sample.util.Configuration;

/**
 * Servlet implementation class OnboardingServlet
 */
public class OnboardingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public OnboardingServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getRequestURI().contains("EnterBoarding"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Onboard/EnterBoarding.jsp")
					.forward(request, response);
		else if (request.getRequestURI().contains("GetBoardingDetails"))
			getServletConfig().getServletContext()
					.getRequestDispatcher("/Onboard/GetBoardingDetails.jsp")
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
				"<ul><li><a href='Onboard/EnterBoarding'>EnterBoarding</a></li><li><a href='Onboard/GetBoardingDetails'>GetBoardingDetails</a></li></ul>");
		response.setContentType("text/html");
		try {
			// Configuration map containing signature credentials and other required configuration.
			// For a full list of configuration parameters refer in wiki page.
			// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
			Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
			
			// Creating service wrapper object to make an API call by loading configuration map.
			PayPalAPIInterfaceServiceService service = new PayPalAPIInterfaceServiceService(configurationMap);
			
			if (request.getRequestURI().contains("EnterBoarding")) {
				EnterBoardingReq req = new EnterBoardingReq();
				EnterBoardingRequestDetailsType reqDetails = new EnterBoardingRequestDetailsType();
				reqDetails.setProgramCode(request.getParameter("programCode"));
				reqDetails.setProductList(request.getParameter("prodList"));
				if (request.getParameter("accNum") != null) {
					BankAccountDetailsType bankAccountDetails = new BankAccountDetailsType();
					bankAccountDetails.setAccountNumber(request
							.getParameter("accNum"));
					bankAccountDetails.setName(request.getParameter("accName"));
					bankAccountDetails.setType(BankAccountTypeType
							.fromValue(request.getParameter("accType")));
					reqDetails.setBankAccount(bankAccountDetails);
				}
				BusinessInfoType businessInfo = new BusinessInfoType();
				AddressType address = new AddressType();
				address.setName(request.getParameter("name"));
				address.setStreet1(request.getParameter("street"));
				address.setCityName(request.getParameter("city"));
				address.setStateOrProvince(request.getParameter("state"));
				address.setCountryName(request.getParameter("countryCode"));
				address.setCountry(CountryCodeType.fromValue(request
						.getParameter("countryCode")));
				address.setPostalCode(request.getParameter("postalCode"));
				businessInfo.setAddress(address);
				businessInfo.setCategory(BusinessCategoryType.fromValue(request
						.getParameter("businessCategory")));
				businessInfo.setName(request.getParameter("businessName"));
				businessInfo.setType(BusinessTypeType.fromValue(request
						.getParameter("businessType")));
				businessInfo
						.setAverageMonthlyVolume(AverageMonthlyVolumeType
								.fromValue(request
										.getParameter("averageMonthlyVolume")));
				businessInfo.setAveragePrice(AverageTransactionPriceType
						.fromValue(request.getParameter("averageTransPrice")));
				businessInfo
						.setRevenueFromOnlineSales(PercentageRevenueFromOnlineSalesType
								.fromValue(request
										.getParameter("revenuePercentage")));
				reqDetails.setBusinessInfo(businessInfo);
				reqDetails.setMarketingCategory(MarketingCategoryType
						.fromValue(request.getParameter("marketingCategory")));
				BusinessOwnerInfoType businessOwnerInfo = new BusinessOwnerInfoType();
				PayerInfoType payerInfo = new PayerInfoType();
				payerInfo.setPayer(request.getParameter("ownerMail"));
				payerInfo.setAddress(address);
				PersonNameType personName = new PersonNameType();
				personName.setFirstName(request.getParameter("firstName"));
				personName.setLastName(request.getParameter("lastName"));
				payerInfo.setPayerName(personName);
				businessOwnerInfo.setOwner(payerInfo);
				businessOwnerInfo.setHomePhone(request
						.getParameter("ownerPhone"));
				businessOwnerInfo.setSSN(request.getParameter("SSN"));
				reqDetails.setOwnerInfo(businessOwnerInfo);
				EnterBoardingRequestType reqType = new EnterBoardingRequestType(
						reqDetails);
				req.setEnterBoardingRequest(reqType);
				EnterBoardingResponseType resp = service.enterBoarding(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						map.put("OnBoarding Token", resp.getToken());

						map.put("RedirectUrl",
								"<a href=https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_partner-onboard-flow&onboarding_token="
										+ resp.getToken()
										+ ">Redirect to https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_partner-onboard-flow&onboarding_token="
										+ resp.getToken() + "</a>");
						session.setAttribute("map", map);
						response.sendRedirect(this.getServletContext().getContextPath()+"/Response.jsp");
					} else {
						session.setAttribute("Error", resp.getErrors());
						response.sendRedirect(this.getServletContext().getContextPath()+"/Error.jsp");
					}
				}
			} else if (request.getRequestURI().contains("GetBoardingDetails")) {
				GetBoardingDetailsReq req = new GetBoardingDetailsReq();
				GetBoardingDetailsRequestType reqType = new GetBoardingDetailsRequestType(
						request.getParameter("onboardingToken"));
				req.setGetBoardingDetailsRequest(reqType);
				GetBoardingDetailsResponseType resp = service
						.getBoardingDetails(req);
				if (resp != null) {
					session.setAttribute("lastReq", service.getLastRequest());
					session.setAttribute("lastResp", service.getLastResponse());
					if (resp.getAck().toString().equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getAck());
						map.put("Account", resp
								.getGetBoardingDetailsResponseDetails()
								.getAccountOwner().getPayer());
						map.put("Bank Account Verification Status", resp
								.getGetBoardingDetailsResponseDetails()
								.getBankAccountVerificationStatus());
						map.put("Email Verification Status", resp
								.getGetBoardingDetailsResponseDetails()
								.getEmailVerificationStatus());
						map.put("Boarding Status", resp
								.getGetBoardingDetailsResponseDetails()
								.getStatus());
						map.put("Program Code", resp
								.getGetBoardingDetailsResponseDetails()
								.getProgramCode());
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
