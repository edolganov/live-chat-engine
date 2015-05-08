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

import static och.util.Util.*;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentHistory;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;
import com.paypal.core.rest.PayPalResource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import och.junit.AssertExt;
import och.util.DateUtil;

import org.junit.BeforeClass;
import org.junit.Test;

@org.junit.Ignore
public class Paypal_RestAPI_Test extends AssertExt {
	
	static String accessToken;
	
	DateFormat dateFormat = new SimpleDateFormat(PaypalRestClient.TIME_FORMAT);
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BeforeClass
	public static void setupPaypal() throws Exception{
		
		
		Properties props = new Properties();
		props.put("http.ConnectionTimeOut", "5000");
		props.put("http.Retry", "1");
		props.put("http.ReadTimeOut", "30000");
		props.put("http.MaxConnection", "100");
		props.put("service.EndPoint", "https://api.sandbox.paypal.com");
		props.put("clientID", "");
		props.put("clientSecret", "");
		PayPalResource.initConfig(props);
		
		accessToken = new OAuthTokenCredential(
				props.getProperty("clientID"), 
				props.getProperty("clientSecret"), 
				(Map)props).getAccessToken();
	}
	
	
	@Test
	public void test_wrongToken(){
		try {
			
			Payment.list(accessToken+"123", map("count", "10"));
			fail_exception_expected();
		}catch(PayPalRESTException e){
			String msg = e.getMessage();
			assertTrue(msg.contains(PaypalRestClient.ERROR_CODE_401));
		}
	}
	
	
	@Test
	public void test_getPayments() throws Exception{
		
	    String dayBefore = dateFormat.format(DateUtil.addDays(new Date(), -1));
	    System.out.println(dayBefore);
		
		PaymentHistory hist = Payment.list(accessToken, map(
				"start_time_", dayBefore, 
				"count", "10"));
		assertNotNull(hist);
		
		System.out.println(hist.toJSON());
		
	}

}
