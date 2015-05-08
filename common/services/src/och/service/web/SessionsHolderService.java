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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionsHolderService implements HttpSessionListener {
	
	private HashMap<String, HttpSession> byId = new HashMap<>();
	
	public void init(ServletContext servletContext){
		servletContext.addListener(this);
	}

	@Override
	public synchronized void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		byId.put(session.getId(), session);
	}

	@Override
	public synchronized void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		byId.remove(session.getId());
	}
	
	public synchronized Collection<HttpSession> getState(){
		return new ArrayList<>(byId.values());
	}

}
