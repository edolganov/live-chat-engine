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
package och.chat.service;

import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.user.SecurityContext.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import och.api.annotation.Secured;
import och.api.exception.DebugException;
import och.api.exception.InvalidInputException;
import och.api.exception.client.NoClientSessionException;
import och.api.exception.user.AccessDeniedException;
import och.api.exception.web.MaxSessionsCountByIpException;
import och.api.model.chat.account.PrivilegeType;
import och.api.model.client.ClientInfo;
import och.api.model.client.ClientSession;
import och.api.model.user.User;
import och.api.remote.chats.InitUserTokenReq;
import och.api.remote.chats.RemoveUserSessionReq;
import och.api.remote.chats.UpdateUserSessionsReq;
import och.chat.service.event.client.ClientSessionDesroyedEvent;
import och.chat.service.event.user.UserSessionCreatedEvent;
import och.chat.service.event.user.UserSessionDestroyedEvent;
import och.chat.service.event.user.UserSessionUpdatedEvent;
import och.chat.service.model.UserSession;
import och.comp.cache.impl.CacheImpl;
import och.comp.web.BaseServlet.WebSecurityProvider;


public class SecurityService extends BaseChatService implements HttpSessionListener, WebSecurityProvider {
	
	public static final String PRIVILEGES_PARAM = "privilegesByAccount";
	
	public static final String CLIENT_INFO = "clientInfo";
	public static final String USER_INFO = "userInfo";
	
	CacheImpl cache;
	
	
	public SecurityService(ChatsAppContext c) {
		super(c);
		cache = c.cache;
	}
	
	public void init(ServletContext servletContext){
		servletContext.addListener(this);
	}
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}


	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		
		HttpSession session = se.getSession();
		
		ClientSession clientSession = (ClientSession) session.getAttribute(CLIENT_INFO);
		if(clientSession != null){
			
			
			List<String> accIds = clientSession.getAccIds();
			
			c.events.tryFireEvent(new ClientSessionDesroyedEvent(clientSession));
			
			for(String accId : accIds){
				log.info("["+accId+"] "+"CLIENT session closed: ip="+clientSession.info.ip
						+", userAgent="+clientSession.info.userAgent
						+", sessionId="+session.getId());				
			}
		}
		
		UserSession userSession = (UserSession) session.getAttribute(USER_INFO);
		if(userSession != null){
			
			Map<String, Set<PrivilegeType>> privsByAcc = userSession.privsByAcc;
			
			c.events.tryFireEvent(new UserSessionDestroyedEvent(userSession));
			
			for (Entry<String, Set<PrivilegeType>> entry : privsByAcc.entrySet()) {
				String accId = entry.getKey();
				if( ! c.root.chats.isAccExists(accId)) continue;
				log.info("["+accId+"] "+"OPERATOR session closed: privs="+entry.getValue()
						+", ip="+userSession.info.ip
						+", userAgent="+userSession.info.userAgent
						+", sessionId="+session.getId());
			}
			
		}
	}
	

	public boolean initClientSessionForAcc(HttpServletRequest req, String accId) {
		
		String clientIp = getClientIp(req);
		String userAgent = getUserAgent(req);
		boolean isNewSession = false;
		
		HttpSession session = req.getSession(false);
		if(session == null){
			int maxSessionsCount = getMaxSessionsCountForChatClient(clientIp);
			int curSessionsByIp = c.sessionsCounter.getSessionsCount(clientIp);
			if(curSessionsByIp >= maxSessionsCount)
				throw new MaxSessionsCountByIpException(clientIp);
			
			session = req.getSession(true);
			if(session.isNew()){
				session.setMaxInactiveInterval(props.getIntVal(chats_sessionLivetime));	
				isNewSession = true;
			}
		}
		
		ClientSession clientSession = (ClientSession) session.getAttribute(CLIENT_INFO);
		if(clientSession == null){
			
			clientSession = new ClientSession(session.getId(), clientIp, userAgent);
			session.setAttribute(CLIENT_INFO, clientSession);
			
			clientSession.addAccId(accId);
			log.info("["+accId+"] "+"CLIENT session created: "
					+"ip="+clientIp
					+", userAgent="+userAgent
					+", sessionId="+session.getId());
			
		} else {
			
			boolean added = clientSession.addAccId(accId);
			if(added) {
				log.info("["+accId+"] "+"CLIENT session taken: "
						+"ip="+clientIp
						+", userAgent="+userAgent
						+", sessionId="+session.getId());
			}
		}
		
		return isNewSession;
		
	}
	
	public ClientInfo getClientInfo(HttpServletRequest req){
		String ip = getClientIp(req);
		String userAgent = getUserAgent(req);
		return new ClientInfo(ip, userAgent);
	}
	
	
	/**
	 * @return session or null
	 */
	public ClientSession getClientSession(HttpServletRequest req){
		HttpSession session = req.getSession(false);
		if(session == null) return null;
		return (ClientSession) session.getAttribute(CLIENT_INFO);
	}
	
	
	public ClientSession findClientSession(HttpServletRequest req) throws NoClientSessionException {
		ClientSession clientSession = getClientSession(req);
		if(clientSession == null) throw new NoClientSessionException();
		return clientSession;
	}


	public int getMaxSessionsCountForChatClient(String clientIp) {
		int out = props.getVal(chats_maxSessionsByIP+"_"+clientIp, -1);
		if(out == -1){
			out = props.getIntVal(chats_maxSessionsByIP);
		}
		return out;
	}



	
	@Override
	public User getUserFromSession(HttpServletRequest req) {
		UserSession userSession = getUserSession(req);
		if(userSession == null) return null;
		
		User user = new User(userSession.userId);
		user.putParam(PRIVILEGES_PARAM, userSession.clonePrivsByAcc());
		return user;
	}

	@Override
	public boolean hasClientSession(HttpServletRequest req) {
		return getClientSession(req) != null;
	}

	/** 
	 * Сохранение инфы о юзере, на основе которой будет создана сессия юзера 
	 */
	public void initUserToken(InitUserTokenReq data) {
		
		if(props.getBoolVal(chatApp_debug_failInitToken)){
			throw new DebugException();
		}
		
		validateState(data);

		cache.putObjVal(data.token, data, props.getIntVal(users_waitChatSessionTokenLivetime));
	}

	public Integer getUserTokenLivetime(String token) {
		return cache.getItemLivetime(token);
	}
	
	/**
	 * Создание сессии на основе токена, сохраненного ранее.
	 * Если сессия уже есть, то пропуск
	 */
	public long initUserSession(HttpServletRequest req, String reqToken) throws InvalidInputException, AccessDeniedException {
		
		if( ! hasText(reqToken)) throw new InvalidInputException("reqToken is null");
		
		InitUserTokenReq tokenData = (InitUserTokenReq) cache.getObjVal(reqToken);
		if(isEmpty(tokenData)) throw new InvalidInputException("no data by token: "+reqToken);
		
		//check client
		String clientIp = getClientIp(req);
		if( ! tokenData.clientIp.equals(clientIp)){
			throw new AccessDeniedException("invalid client ip: "+clientIp);
		}
		String userAgent = getUserAgent(req);
		if( ! tokenData.clientUserAgent.equals(userAgent)){
			throw new AccessDeniedException("invalid userAgent: "+userAgent);
		}
		
		
		UserSession userSession = null;
		
		HttpSession session = req.getSession(false);
		if(session != null){
			userSession = (UserSession)session.getAttribute(USER_INFO);
		}
		//session already exists
		if(userSession != null){
			if(userSession.userId == tokenData.userId){
				return userSession.userId;
			} else {
				throw new InvalidInputException("already has a session for other user. req token: "+tokenData);
			}
		}
		
		//create new operator's session
		if(session == null) session = req.getSession(true);
		userSession = new UserSession(tokenData);
		session.setAttribute(USER_INFO, userSession);
		
		c.events.tryFireEvent(new UserSessionCreatedEvent(userSession));
		
		//log by single acc
		for(Entry<String, Set<PrivilegeType>> entry : userSession.privsByAcc.entrySet()){
			String accId = entry.getKey();
			if( ! c.root.chats.isAccExists(accId)) continue;
			log.info("["+accId+"] "+"OPERATOR session created: privs="+entry.getValue()	
					+", ip="+clientIp
					+", userAgent="+userAgent
					+", sessionId="+session.getId());
		}
		
		return userSession.userId;
	}
	
	
	public void removeUserSession(RemoveUserSessionReq req){
		
		validateState(req);
		
		String token = req.token;
		
		//clear cache
		cache.removeObjVal(token);
		
		//clear sessions
		Collection<HttpSession> sessions = c.sessionsHolder.getState();
		for (HttpSession session : sessions) {
			
			UserSession userSession = (UserSession) session.getAttribute(USER_INFO);
			
			if(userSession == null) continue;
			if( ! userSession.token.equals(token)) continue;
			
			session.removeAttribute(USER_INFO);
			c.events.tryFireEvent(new UserSessionDestroyedEvent(userSession));
			
			
			//log by single acc
			for(Entry<String, Set<PrivilegeType>> entry : userSession.privsByAcc.entrySet()){
				String accId = entry.getKey();
				if( ! c.root.chats.isAccExists(accId)) continue;
				log.info("["+accId+"] "+"OPERATOR session removed: privs="+entry.getValue()	
						+", ip="+userSession.info.ip
						+", userAgent="+userSession.info.userAgent
						+", sessionId="+session.getId());
			}
			
		}
		
	}
	
	
	public void updateUserSessions(UpdateUserSessionsReq data) {
		
		Collection<HttpSession> sessions = c.sessionsHolder.getState();
		for (HttpSession session : sessions) {
			UserSession userSession = (UserSession) session.getAttribute(USER_INFO);
			if(userSession == null) continue;
			if(userSession.userId != data.userId) continue;
			
			Map<String, Set<PrivilegeType>> oldPrivs = userSession.privsByAcc;

			UserSession clone = userSession.clone();
			clone.privsByAcc = data.privilegesByAccount();
			session.setAttribute(USER_INFO, clone);
			c.events.tryFireEvent(new UserSessionUpdatedEvent(clone));
			
			
			//log by single acc
			for(Entry<String, Set<PrivilegeType>> entry : userSession.privsByAcc.entrySet()){
				String accId = entry.getKey();
				if( ! c.root.chats.isAccExists(accId)) continue;
				Set<PrivilegeType> newValues = entry.getValue();
				Set<PrivilegeType> oldValues = oldPrivs.get(accId);
				if(oldValues == null ||  ! oldValues.equals(newValues)){
					log.info("["+accId+"] "+"OPERATOR session updated: privs="+newValues	
							+", ip="+userSession.info.ip
							+", userAgent="+userSession.info.userAgent
							+", sessionId="+session.getId());					
				}
			}
			
		}
	}

	
	
	
	
	
	@Secured
	public InitUserTokenReq getUserTokenDataFromCache(String token){
		checkAccessFor_ADMIN();
		return (InitUserTokenReq) cache.getObjVal(token);
	}
	


	private UserSession getUserSession(HttpServletRequest req){
		HttpSession session = req.getSession(false);
		if(session == null) return null;
		return (UserSession) session.getAttribute(USER_INFO);
	}



	
	

}
