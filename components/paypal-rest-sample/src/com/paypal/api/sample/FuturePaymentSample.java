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
package com.paypal.api.sample;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.FuturePayment;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.core.ClientCredentials;
import com.paypal.core.LoggingManager;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.PayPalRESTException;
import com.paypal.sdk.openidconnect.CreateFromAuthorizationCodeParameters;
import com.paypal.sdk.openidconnect.Tokeninfo;

public class FuturePaymentSample {
	public Payment create(String correlationId, String authorizationCode) throws PayPalRESTException, FileNotFoundException, IOException {
		
		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");
		Amount amount = new Amount();
		amount.setTotal("0.17");
		amount.setCurrency("USD");
		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription("This is the payment tranasction description.");
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);
		
		FuturePayment futurePayment = new FuturePayment();
		futurePayment.setIntent("authorize");
		futurePayment.setPayer(payer);
		futurePayment.setTransactions(transactions);
		
		Tokeninfo tokeninfo = null;
		if (authorizationCode != null && authorizationCode.trim().length() > 0) {
			LoggingManager.info(getClass(), "creating future payment with auth code: " + authorizationCode);
			
			ClientCredentials credentials = futurePayment.getClientCredential();
			CreateFromAuthorizationCodeParameters params = new CreateFromAuthorizationCodeParameters();
			params.setClientID(credentials.getClientID());
			params.setClientSecret(credentials.getClientSecret());
			params.setCode(authorizationCode);

			Map<String, String> configurationMap = new HashMap<String, String>();
			configurationMap.put("mode", "sandbox");
			APIContext apiContext = new APIContext();
			apiContext.setConfigurationMap(configurationMap);
			tokeninfo = Tokeninfo.createFromAuthorizationCode(apiContext, params);
			tokeninfo.setAccessToken(tokeninfo.getTokenType() + " " + tokeninfo.getAccessToken());

			System.out.println("Generated access token from auth code: " + tokeninfo.getAccessToken());
		}

		Payment createdPayment = futurePayment.create(tokeninfo.getAccessToken(), correlationId);
		if (createdPayment.getIntent().equals("authorize")) {
			System.out.println("payment authorized");
			System.out.println("Payment ID=" + createdPayment.getId());
			System.out.println("Authorization ID=" + createdPayment.getTransactions().get(0).getRelatedResources().get(0).getAuthorization().getId());
		}
		
		return createdPayment;
	}

	public static void main(String[] args) {
		try {
			
			String authorizationCode = "";
			String correlationId = "";
			for (int i = 0; i < args.length; ++i) {
				if (args[i].startsWith("authorization_code")) {
					authorizationCode = args[i].split("=")[1];
				}
				else if (args[i].startsWith("correlationId")) {
					correlationId = args[i].split("=")[1];
				}
			}

			FuturePaymentSample fps = new FuturePaymentSample();
			Payment payment = fps.create(correlationId, authorizationCode);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(Payment.getLastRequest());
			System.out.println(Payment.getLastResponse());
		}
	}
}
