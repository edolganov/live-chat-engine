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
import static och.util.Util.*;

import java.math.BigDecimal;
import java.util.List;

import och.api.exception.paypal.PaypalSoapException;
import och.api.model.billing.PayData;
import och.api.model.billing.PaymentBase;
import och.junit.AssertExt;
import och.service.props.impl.MapProps;

import org.junit.Ignore;
import org.junit.Test;

public class PaypalSoapClientTest extends AssertExt{
	
	
	
	static MapProps conProps() {
		MapProps out = new MapProps();
		out.putVal("http.ConnectionTimeOut", "5000");
		out.putVal("http.Retry", "1");
		out.putVal("http.ReadTimeOut", "5000");
		out.putVal("http.MaxConnection", "100");
		out.putVal("service.EndPoint", "https://api.sandbox.paypal.com");
		out.putVal("service.ExpressUrl", "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=");
		out.putVal("acct1.UserName", "");
		out.putVal("acct1.Password", "");
		out.putVal("acct1.Signature", "");
		out.putVal("mode", "sandbox");
		return out;
	}
	
	
	static MapProps sysProps(){
		MapProps out = new MapProps();
		out.putVal(httpsServerUrl, "http://ya.ru");
		return out;
	}
	
	
	@Ignore
	@Test
	public void test_getPayments() throws Exception{
		
		
		PaypalSoapClient client = new PaypalSoapClient(sysProps(), conProps());
		List<PaymentBase> list = client.getPaymentHistory(10);
		assertTrue( ! isEmpty(list));
		for (PaymentBase item : list) {
			System.out.println(item);
		}
		
		//by id
		PaymentBase existData = list.get(0);
		PaymentBase item = client.getPayment(existData.created, existData.externalId);
		assertNotNull(item);
		System.out.println(item);
		
		//invalid id
		try {
			client.getPayment(existData.created, existData.externalId+"-123");
			fail_exception_expected();
		}catch(PaypalSoapException e){
			//ok
		}
		
	}
	
	@Ignore
	@Test
	public void test_payWithAccReq() throws Exception{
		
		PaypalSoapClient client = new PaypalSoapClient(sysProps(), conProps());
		PayData resp = client.payWithAccReq(new BigDecimal(12), 1);
		assertNotNull(resp);
		
		System.out.println(resp.redirectUrl);
		
	}
	
	public static void main(String[] args) throws Exception {
		//for confitm test_payWithAccReq
		String token = "EC-7YA19209X4509412B";
		String payerId = "WC3NUAE4H457J";
		PaypalSoapClient client = new PaypalSoapClient(sysProps(), conProps());
		client.finishPayment(token, payerId, new BigDecimal(12), 1);
	}
	
	
	@Ignore
	@Test(expected=PaypalSoapException.class)
	public void test_wrong_login() throws Exception{
		
		MapProps conProps = conProps();
		conProps.putVal("acct1.UserName", "123");
		PaypalSoapClient client = new PaypalSoapClient(sysProps(), conProps);
		client.payWithAccReq(new BigDecimal(12), 1);
		
	}
	


}
