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
package och.api.model.web;

import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ReqInfo {
	
	 
	private static final ThreadLocal<ReqInfo> THREAD_LOCAL = new ThreadLocal<ReqInfo>();
	
	public static void putInfoToThreadLocal(HttpServletRequest req){
		String ip = getClientIp(req);
		String userAgent = getUserAgent(req);
		String ref = getReferer(req);
		String origRef = req.getParameter("origRef");
		
		HttpSession session = req.getSession(false);
		String sessionId = session == null? null : session.getId();
		
		putInfoToThreadLocal(new ReqInfo(ip, userAgent, ref, origRef, sessionId));
	}
	
	public static void putInfoToThreadLocal(ReqInfo reqInfo){
		THREAD_LOCAL.set(reqInfo);
	}
	
	public static void removeInfoFromThreadLocal(){
		THREAD_LOCAL.remove();
	}
	
	public static ReqInfo getReqInfo(){
		return THREAD_LOCAL.get();
	}
	
	public static String getReqInfoStr(){
		return String.valueOf(getReqInfo());
	}
	

	public final String ip;
	public final String userAgent;
	public final String ref;
	public final String origRef;
	public final String sessionId;
	
	public ReqInfo(String ip, String userAgent, String ref, String origRef, String sessionId) {
		this.ip = ip;
		this.userAgent = userAgent;
		this.ref = ref;
		this.origRef = origRef;
		this.sessionId = sessionId;
	}
	
	public String getFinalRef(){
		return hasText(origRef)? origRef : ref;
	}

	@Override
	public String toString() {
		return "[ip=" + ip 
				+ ", agent=" + userAgent
				+ ", ref=" + ref 
				+ (origRef != null? ", origRef="+origRef : "")
				+ ", session=" + sessionId 
				+ "]";
	}
	
	
	
	
}
