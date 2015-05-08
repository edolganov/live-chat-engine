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

import static och.api.model.PropKey.*;
import com.paypal.api.payments.PaymentHistory;
import och.api.model.user.User;
import och.junit.AssertExt;
import och.service.props.impl.MapProps;

import org.junit.Ignore;
import org.junit.Test;


public class PaypalRestClientTest extends AssertExt {
	
	static MapProps connectProps = new MapProps();
	static {
		connectProps.putVal("http.ConnectionTimeOut", "5000");
		connectProps.putVal("http.Retry", "1");
		connectProps.putVal("http.ReadTimeOut", "5000");
		connectProps.putVal("http.MaxConnection", "100");
		connectProps.putVal("service.EndPoint", "https://api.sandbox.paypal.com");
		connectProps.putVal("clientID", "");
		connectProps.putVal("clientSecret", "");
	}
	
	private static PaypalRestClient inst;
	
	static synchronized PaypalRestClient getInstance() throws Exception{
		if(inst == null){
			
			MapProps systemProps = new MapProps();
			systemProps.putVal(httpsServerUrl, "http://ya.ru");
			systemProps.putVal(paypal_accessTokenLiveTime, paypal_accessTokenLiveTime.strDefVal());
			
			inst = new PaypalRestClient(systemProps, connectProps);
		}
		return inst;
	}
	
	
	@Ignore
	@Test
	public void test_paymentWithPaypalAcc() throws Exception{
		
		PaypalRestClient s = getInstance();
		String redirectUrl = s.payWithPaypalAcc(new User(-1, "testLogin"), 5);
		assertNotNull(redirectUrl);
		
		System.out.println(redirectUrl);
		
	}
	
	
	
	
	@Ignore
	@Test
	public void test_useOldToken() throws Exception{
		
		PaypalRestClient s = getInstance();
		String token1 = s.getAccessToken();
		
		PaymentHistory hist = s.getLastPaymentHistory();
		String token2 = s.getAccessToken();
		
		assertNotNull(hist);
		assertEquals(token1, token2);
	}
	
	
	
	
	@Ignore
	@Test
	public void test_recreateAccessToken() throws Exception{
		
		MapProps systemProps = new MapProps();
		systemProps.putVal(paypal_accessTokenLiveTime, 1);
		
		
		PaypalRestClient s = new PaypalRestClient(systemProps, connectProps);
		String token1 = s.getAccessToken();
		
		Thread.sleep(10);
		
		s.getLastPaymentHistory();
		String token2 = s.getAccessToken();
		
		assertFalse(token1.equals(token2));
		
	}
	

	
	

	
	
	

}
