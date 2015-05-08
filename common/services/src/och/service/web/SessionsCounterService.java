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

import static och.util.servlet.WebUtil.*;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionsCounterService implements HttpSessionListener, ServletRequestListener {
	
	private static ThreadLocal<String> reqIpThreadLocal = new ThreadLocal<>();
	
	private boolean inited = false;
	private ConcurrentHashMap<String, Integer> sessionsCountByIp = new ConcurrentHashMap<>();
	
	public void init(ServletContext servletContext){
		servletContext.addListener(this);
		inited = true;
	}
	
	public boolean isInited() {
		return inited;
	}
	
	
	public int getSessionsCount(HttpServletRequest req){
		String clientIp = getClientIp(req);
		return getSessionsCount(clientIp);
	}
	
	public int getSessionsCount(String clientIp){
		if(clientIp == null) return 0;
		Integer out = sessionsCountByIp.get(clientIp);
		return out == null? 0 : out;
	}
	
	
	@Override
	public void requestInitialized(ServletRequestEvent event) {
		HttpServletRequest req = (HttpServletRequest) event.getServletRequest();
		String clientIp = getClientIp(req);
		reqIpThreadLocal.set(clientIp);
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		reqIpThreadLocal.remove();
	}


	
	@Override
	public void sessionCreated(HttpSessionEvent event) {
		String clientIp = reqIpThreadLocal.get();
		if(clientIp == null) return;
		
		HttpSession session = event.getSession();
		session.setAttribute("counter.clientIp", clientIp);

		Integer oldVal = sessionsCountByIp.get(clientIp);
		if(oldVal == null) oldVal = 0;
		sessionsCountByIp.put(clientIp, oldVal+1);
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		
		HttpSession session = event.getSession();
		String clientIp = (String)session.getAttribute("counter.clientIp");
		if(clientIp == null) return;
		
		Integer oldVal = sessionsCountByIp.get(clientIp);
		if(oldVal == null) return;
		
		int newVal = oldVal - 1;
		if(newVal < 1){
			sessionsCountByIp.remove(clientIp);
		} else {
			sessionsCountByIp.put(clientIp, newVal);
		}
	}

	public void clearSessionsCount(String clientIp) {
		sessionsCountByIp.remove(clientIp);
	}



}
