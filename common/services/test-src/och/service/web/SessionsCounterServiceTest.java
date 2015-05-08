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
package och.service.web;


import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;


import och.junit.AssertExt;

import org.junit.Test;

import web.MockHttpServletRequest;
import web.MockServletContext;

public class SessionsCounterServiceTest extends AssertExt {
	
	
	@Test
	public void test_work(){
		
		MockServletContext context = new MockServletContext();
		MockHttpServletRequest req1 = new MockHttpServletRequest();
		MockHttpServletRequest req2 = new MockHttpServletRequest();
		
		SessionsCounterService service = new SessionsCounterService();
		
		assertFalse(service.isInited());
		service.init(context);
		assertTrue(service.isInited());
		assertEquals(0, service.getSessionsCount(req1));
		assertEquals(0, service.getSessionsCount(req2));
		
		service.requestInitialized(new ServletRequestEvent(context, req1));
		service.sessionCreated(new HttpSessionEvent(req1.getSession()));
		service.requestDestroyed(new ServletRequestEvent(context, req1));
		assertEquals(1, service.getSessionsCount(req1));
		assertEquals(1, service.getSessionsCount(req2));
		
		
		service.requestInitialized(new ServletRequestEvent(context, req2));
		service.sessionCreated(new HttpSessionEvent(req2.getSession()));
		assertEquals(2, service.getSessionsCount(req1));
		assertEquals(2, service.getSessionsCount(req2));
		
		service.sessionDestroyed(new HttpSessionEvent(req1.getSession()));
		assertEquals(1, service.getSessionsCount(req1));
		assertEquals(1, service.getSessionsCount(req2));
		
		service.sessionDestroyed(new HttpSessionEvent(req2.getSession()));
		assertEquals(0, service.getSessionsCount(req1));
		assertEquals(0, service.getSessionsCount(req2));
		
	}

}
