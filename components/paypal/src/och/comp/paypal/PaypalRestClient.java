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
package och.comp.paypal;

import static java.lang.System.*;
import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.util.DateUtil.*;
import static och.util.Util.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import och.api.model.user.User;
import och.service.props.Props;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentHistory;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;
import com.paypal.core.rest.PayPalResource;


public class PaypalRestClient {
	
	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String ERROR_CODE_401 = "Error code : 401";
	
	
	private Props sysProps;
	private Props conProps;
	
	private volatile String accessToken;
	private volatile long accessTokenLastUse;
	
	public PaypalRestClient(Props systemProps, Props connectProps) throws Exception {
		
		this.sysProps = systemProps;
		this.conProps = connectProps;
		
		Properties config = new Properties();
		config.putAll(connectProps.toMap());
		PayPalResource.initConfig(config);
		
		updateAccessToken();
	}
	
	public String getAccessToken(){
		return accessToken;
	}
	
	
	public String payWithPaypalAcc(User user, long val) throws Exception {
		
		validateState(val > 0, "val");
		
		return invoke(()->{

			Amount amount = new Amount();
			amount.setCurrency("USD");
			amount.setTotal(String.valueOf(val));

			Transaction transaction = new Transaction();
			transaction.setAmount(amount);
			transaction.setDescription("Online Chat Payment for User Account: "+user.login);
			List<Transaction> transactions = list(transaction);
			

			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");
			
			Payment payment = new Payment();
			payment.setIntent("sale");
			payment.setPayer(payer);
			payment.setTransactions(transactions);

			
			RedirectUrls redirectUrls = new RedirectUrls();
			redirectUrls.setReturnUrl(sysProps.findVal(httpsServerUrl) + sysProps.getStrVal(paypal_successUri));
			redirectUrls.setCancelUrl(sysProps.findVal(httpsServerUrl) + sysProps.getStrVal(paypal_failUri));
			payment.setRedirectUrls(redirectUrls);


			Payment createdPayment = payment.create(new APIContext(accessToken));
			Iterator<Links> links = createdPayment.getLinks().iterator();
			while (links.hasNext()) {
				Links link = links.next();
				if (link.getRel().equalsIgnoreCase("approval_url")) {
					return link.getHref();
				}
			}

			throw new IllegalStateException("No approval_url in payment resp: "+createdPayment.toJSON());
		});
	}
	
	
	
	public PaymentHistory getLastPaymentHistory() throws Exception{
		return invoke(()->{
			
			DateFormat dateFormat = new SimpleDateFormat(PaypalRestClient.TIME_FORMAT);
			String dayBefore = dateFormat.format(addDays(new Date(), -1));
			
			PaymentHistory hist = Payment.list(accessToken, map(
					"start_time", dayBefore, 
					"count", "10"));
			
			return hist;
		});
	}
	
	
	
	
	
	
	
	private <T> T invoke(Callable<T> body) throws Exception{
		try {
			
			updateAccessTokenIfNeed();
			
			return body.call();
			
		}catch(PayPalRESTException e){
			String msg = e.getMessage();
			if(hasText(msg) && msg.contains(ERROR_CODE_401)){
				updateAccessToken();
				return body.call();
			}
			throw e;
		}
	}
	
	private void updateAccessTokenIfNeed() throws Exception{
		
		long curLastTimeToUse = accessTokenLastUse;
		if(currentTimeMillis() < curLastTimeToUse) return;
		
		updateAccessToken(curLastTimeToUse);
	}
	
	private void updateAccessToken() throws Exception {
		updateAccessToken(accessTokenLastUse);
	}
	
	private synchronized void updateAccessToken(long oldLastTime) throws Exception {
		
		if(accessTokenLastUse != oldLastTime) return;
		
		accessToken = new OAuthTokenCredential(
				conProps.findVal("clientID"), 
				conProps.findVal("clientSecret"), 
				conProps.toMap()).getAccessToken();
		
		accessTokenLastUse = currentTimeMillis() + sysProps.getLongVal(paypal_accessTokenLiveTime);
	}
	

}
