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
package och.comp.mail;

import static och.api.model.PropKey.*;

import java.util.ArrayList;

import och.comp.mail.common.SendTask;
import och.service.props.Props;
import och.service.props.WriteProps;
import och.service.props.impl.MapProps;

import org.junit.Before;
import org.junit.Test;

import test.BaseTest;
import test.TestException;

public class MailServiceTest extends BaseTest {
	
	MapProps props = new MapProps();
	
	@Before
	public void init(){
		props.putVal(mail_storeToDisc, false);
	}
	
	
	
	@Test
	public void test_send_once() throws Exception {
		
		boolean[] succeed = {true};
		
		MailService mailService = new MailService(new Sender() {
			@Override
			public void send(SendTask task, Props config) throws Exception {
				if( ! succeed[0]) throw new TestException();
			}
		}, props);
		
		//ok
		mailService.sendOnce(new SendReq().to("ddd@dd.dd").subject("test").text("test"));
		
		//fail
		try {
			succeed[0] = false;
			mailService.sendOnce(new SendReq().to("ddd@dd.dd").subject("test").text("test"));
			fail_exception_expected();
		}catch(TestException e){
			//ok
		}
		
	}
	
	
	@Test
	public void test_send_fail() throws Exception{
		
		class Result {
			boolean done;
			String text;
			Throwable t;
			int tryCount;
			ArrayList<Long> times = new ArrayList<>();
			
			void addTime(){
				times.add(System.currentTimeMillis());
			}
		}
		
		final Result result = new Result();
		result.addTime();
		final Object monitor = new Object();
		
		WriteProps props = new MapProps();
		int errorWaitDelta = 100;
		props.putVal(mail_errorWaitDelta, ""+errorWaitDelta);
		props.putVal(mail_storeToDisc, false);
		
		MailService mailService = new MailService(new Sender() {
			
			@Override
			public void send(SendTask task, Props props) throws Exception {
				result.tryCount++;
				result.addTime();
				throw new TestException();
			}
		}, props);
		
		
		
		String msg = "test";
		mailService.sendAsync(new SendReq("test@test.ru", "subject", msg), new SendCallback() {
			
			@Override
			public void onSuccess() {
				synchronized (monitor) {
					result.done = true;
					monitor.notifyAll();
				}
			}
			
			@Override
			public void onFailed(Throwable t) {
				synchronized (monitor) {
					result.done = true;
					result.t = t;
					monitor.notifyAll();
				}
			}
		});
		
		synchronized (monitor) {
			if( ! result.done) monitor.wait();
			mailService.shutdown();
			
			assertTrue(result.t instanceof TestException);
			assertEquals(3, result.tryCount);
			assertNull(result.text);
			
			//check wait time
			ArrayList<Long> times = result.times;
			assertEquals(4, times.size());
			
			for (int i = 1; i < times.size(); i++) {
				long timeDelta = times.get(i) - times.get(i-1);
				System.out.println(timeDelta);
				
				long expectedWait = (i-1)*errorWaitDelta;
				long marginOfError = 100;
				assertTrue("timeDelta="+timeDelta+", expectedWait="+expectedWait,
						timeDelta >= expectedWait-marginOfError 
						&& timeDelta <= expectedWait+marginOfError);
			}
		}
		
	}
	
	
	
	@Test
	public void test_send_success() throws Exception{
		
		
		class Result {
			boolean done;
			String text;
			Throwable t;
		}
		
		final Result result = new Result();
		final Object monitor = new Object();
		

		MailService mailService = new MailService(new Sender() {
			
			@Override
			public void send(SendTask task, Props config) throws Exception {
				result.text = task.msg.text;
			}
		}, props);
		
		
		
		String msg = "test";
		mailService.sendAsync(new SendReq("test@test.ru", "subject", msg), new SendCallback() {
			
			@Override
			public void onSuccess() {
				synchronized (monitor) {
					result.done = true;
					monitor.notifyAll();
				}
			}
			
			@Override
			public void onFailed(Throwable t) {
				synchronized (monitor) {
					result.done = true;
					result.t = t;
					monitor.notifyAll();
				}
			}
		});
		
		synchronized (monitor) {
			if( ! result.done) monitor.wait();
			mailService.shutdown();
			
			assertEquals(msg, result.text);
			assertNull(result.t);
		}
		
	}

}
