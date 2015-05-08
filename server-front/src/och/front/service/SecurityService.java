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
package och.front.service;

import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.remtoken.ClientRemToken.*;
import static och.api.model.user.SecurityContext.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Future;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import och.api.exception.ExpectedException;
import och.api.exception.user.BannedUserException;
import och.api.exception.user.InvalidLoginDataException;
import och.api.exception.user.NotActivatedUserException;
import och.api.exception.user.UserSessionAlreadyExistsException;
import och.api.model.remtoken.ClientRemToken;
import och.api.model.remtoken.RemToken;
import och.api.model.user.LoginUserReq;
import och.api.model.user.UpdateUserReq;
import och.api.model.user.User;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.base.universal.UpdateStub;
import och.comp.db.main.table.remtoken.CreateRemToken;
import och.comp.db.main.table.remtoken.DeleteRemToken;
import och.comp.db.main.table.remtoken.SelectRemToken;
import och.comp.web.BaseServlet.WebSecurityProvider;
import och.front.service.event.user.UserBannedEvent;
import och.front.service.event.user.UserSessionDesroyedEvent;
import och.front.service.event.user.UserUnbannedEvent;
import och.front.service.model.UserSession;
import och.util.servlet.WebUtil;

public class SecurityService extends BaseFrontService implements HttpSessionListener, WebSecurityProvider {
	
	public static final String REM_TOKEN = "rem-t";
	public static final String SESSION_OBJ_KEY = "security.user";
	public static final String BANNED_CACHE_PREFIX = "sc.user.banned.";
	public static final int INVALID_LOGINS_CACHE_LIVETIME_SEC = 60*30; //30 min
	public static final int INVALID_LOGINS_CACHE_LIVETIME_MS = 1000*INVALID_LOGINS_CACHE_LIVETIME_SEC;
	
	private UniversalQueries universal;
	

	public SecurityService(FrontAppContext c) {
		super(c);
	}
	
	@Override
	public void init() throws Exception {
		
		universal = c.db.universal;
		
		c.events.addListener(UserBannedEvent.class, (event)-> onUserBanned(event.userId));
		c.events.addListener(UserUnbannedEvent.class, (event)-> onUserUnbanned(event.userId));
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
		removeUserSession(session);
	}
	
	

	public int maxRemCount(){
		return c.props.getIntVal(remToken_maxCount);
	}
	
	public int deleteRemCount(){
		return c.props.getIntVal(remToken_deleteCount);
	}
	
	public boolean isUserInSession(HttpServletRequest req){
		return getUserSession(req) != null;
	}
	
	@Override
	public User getUserFromSession(HttpServletRequest req){
		UserSession session = getUserSession(req);
		return session == null? null : session.user;
	}
	
	public int getInvalidLoginsCount(HttpServletRequest req, LoginUserReq data, int onErrorVal){
		String key = getInvalidLoginsKey(req, data);
		try {
			
			String val = c.cache.getVal(key);
			return isEmpty(val)? 0 : Integer.parseInt(val);
			
		}catch (Throwable t) {
			ExpectedException.logError(log, t, "can't get val");
			return onErrorVal;
		}
	}
	
	public Future<?> setInvalidLoginsCountAsync(HttpServletRequest req, LoginUserReq data, int count){
		String key = getInvalidLoginsKey(req, data);
		if(count < 1) return c.cache.removeCacheAsync(key);
		return c.cache.putCacheAsync(key, String.valueOf(count), INVALID_LOGINS_CACHE_LIVETIME_MS);
	}

	public User createUserSession(HttpServletRequest req, HttpServletResponse resp, LoginUserReq data) 
			throws NotActivatedUserException, BannedUserException, Exception {
				
		validateState(data);
		if(isUserInSession(req)) throw new UserSessionAlreadyExistsException();
		
		User user = c.root.users.checkEmailOrLoginAndPsw(data.login, data.psw);
		if(user == null) {
			removeRemCookie(resp);
			throw new InvalidLoginDataException();
		}
		
		HttpSession session = setUserToSession(req, user, null);
		
		if(data.rememberMe){
			createAndReplaceRemToken(resp, user.id, findRemCookie(req));
		} else {
			removeRemToken(req, resp, user.id);
		}
		
		log.info("session created: userId="+user.id
				+", login="+user.login
				+", ip="+getClientIp(req)
				+", userAgent="+getUserAgent(req)
				+", sessionId="+session.getId());
		
		return user;
	}
	
	public User restoreUserSession(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		
		
		if(isUserInSession(req)) return null;
		
		ClientRemToken curToken = findRemCookie(req);
		if(curToken == null) return null;
		
		RemToken stored = universal.selectOne(new SelectRemToken(curToken.uid));
		if(stored == null) return null;
		if( ! Arrays.equals(stored.tokenHash, curToken.getHash(stored.tokenSalt))){
			//wrong cookies random
			return null;
		}
		
		User user = c.root.users.checkClientUser(stored.userId, false);
		if(user == null) {
			removeRemCookie(resp);
			return null;
		}
		
		HttpSession session = setUserToSession(req, user, null);
		
		//remove cur token and create new for prevent stealing of cookie
		createAndReplaceRemToken(resp, stored.userId, curToken);
		
		log.info("session restored: userId="+user.id
				+", login="+user.login
				+", ip="+getClientIp(req)
				+", userAgent="+getUserAgent(req)
				+", sessionId="+session.getId());
		
		return user;
		
	}
	
	public User updateUserSession(HttpServletRequest req, String curPsw, UpdateUserReq data) throws Exception{
		
		UserSession oldSession = getUserSession(req);
		if( oldSession == null) return null;
		
		User user = oldSession.user;
		User updated = null;
		
		//update data
		pushToSecurityContext_SYSTEM_USER();
		try {
			updated = c.root.users.updateUser(user.id, curPsw, data);
		}finally {
			popUserFromSecurityContext();
		}
		
		//update session
		HttpSession session = setUserToSession(req, updated, oldSession.attrs);
		
		log.info("session updated: userId="+updated.id
				+", login="+updated.login
				+", ip="+getClientIp(req)
				+", userAgent="+getUserAgent(req)
				+", sessionId="+session.getId());
		
		return updated;
	}
	
	
	
	public void logout(HttpServletRequest req, HttpServletResponse resp){
		
		HttpSession session = req.getSession(false);
		if(session == null) return;
		
		UserSession userSession = removeUserSession(session);
		if(userSession == null) return;

		User user = userSession.user;
		removeRemToken(req, resp, user.id);
		
		log.info("logout: userId="+user.id
				+", login="+user.login
				+", ip="+getClientIp(req)
				+", userAgent="+getUserAgent(req)
				+", sessionId="+session.getId());
	}
	
	private UserSession removeUserSession(HttpSession session){
		
		UserSession userSession = (UserSession)session.getAttribute(SESSION_OBJ_KEY);
		if(userSession == null) return null;
		
		session.removeAttribute(SESSION_OBJ_KEY);
		
		//fire event
		c.events.tryFireEvent(new UserSessionDesroyedEvent(userSession));
		
		return userSession;
	}
	
	
	public void setUserSessionAttr(HttpServletRequest req, String key, Object val){
		setUserSessionAttr(req, key, val, true);
	}
	
	public Object setUserSessionAttr(HttpServletRequest req, String key, Object val, boolean replaceIfExists){
		UserSession session = getUserSession(req);
		if(session == null) return null;
		
		synchronized (session.attrs) {
			Object out = val;
			if(replaceIfExists) session.attrs.put(key, val);
			else {
				if(session.attrs.containsKey(key)){
					out = session.attrs.get(key);
				} else {
					session.attrs.put(key, val);
				}
			}
			return out;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getUserSessionAttr(HttpServletRequest req, String key){
		UserSession session = getUserSession(req);
		if(session == null) return null;
		
		synchronized (session.attrs) {
			return (T)session.attrs.get(key);			
		}
	}
	
	public boolean hasUserSessionAttr(HttpServletRequest req, String key){
		UserSession session = getUserSession(req);
		if(session == null) return false;
		
		synchronized (session.attrs) {
			return session.attrs.containsKey(key);
		}
	}
	
	public void removeUserSessionAttr(HttpServletRequest req, String key){
		UserSession session = getUserSession(req);
		if(session == null) return;
		
		synchronized (session.attrs) {
			session.attrs.remove(key);
		}
	}
	
	
	
	
	
	
	
	
	
	
	private UserSession getUserSession(HttpServletRequest req){
		HttpSession session = req.getSession(false);
		if(session == null) return null;
		
		UserSession userSession = (UserSession)session.getAttribute(SESSION_OBJ_KEY);
		if(userSession == null) return null;
		
		//user banned runtime
		String bannedFlag = c.cache.tryGetVal(getBannedCacheKey(userSession.user.id), null);
		if(bannedFlag != null) return null;
		
		return userSession;
	}
	
	/**
	 * (Based by 
	 * 	http://jaspan.com/improved_persistent_login_cookie_best_practice
	 * 	http://docs.spring.io/spring-security/site/docs/3.0.x/reference/remember-me.html )
	 */
	private void createAndReplaceRemToken(HttpServletResponse resp, final long userId, final ClientRemToken oldToken){
		
		ClientRemToken newToken = new ClientRemToken();
		
		//add cookie
		int maxAge = 365 * 24 * 60 * 60; // one year
		resp.addCookie(cookie(REM_TOKEN, newToken.encodeToCookie(), true, maxAge));
		
		//replace in db
		c.async.invoke(()-> {
			String salt = randomSimpleId();
			byte[] hash = newToken.getHash(salt);
			
			universal.update(
				oldToken != null? new DeleteRemToken(oldToken.uid) : new UpdateStub(),
				new CreateRemToken(newToken.uid, hash, salt, userId));
			
			//delete if maxCount - protection from New Tokens Rush Attack
			c.db.remTokens.deleteOldIfMaxCount(userId, maxRemCount(), deleteRemCount());
			
			return null;
		});
	}
	
	
	
	private void removeRemToken(HttpServletRequest req, HttpServletResponse resp, final long userId){
		
		//remove resp cookie
		removeRemCookie(resp);
		
		//clean db
		ClientRemToken reqToken = findRemCookie(req);
		if(reqToken == null) return;
		c.async.invoke(()-> 
			universal.update(new DeleteRemToken(reqToken.uid))
		);
	}
	
	
	private static HttpSession setUserToSession(HttpServletRequest req, User user, Map<String, Object> initAttrs) {
		HttpSession session = req.getSession(true);
		String ip = getClientIp(req);
		String userAgent = getUserAgent(req);
		session.setAttribute(SESSION_OBJ_KEY, new UserSession(ip, userAgent, user, initAttrs));
		return session;
	}
	
	public static ClientRemToken findRemCookie(HttpServletRequest req){
		
		Cookie remCookie = null;
		Cookie[] cookies = req.getCookies();
		if(isEmpty(cookies)) return null;
		for (Cookie cookie : cookies) {
			if(REM_TOKEN.equals(cookie.getName())
					&& hasText(cookie.getValue())){
				remCookie = cookie;
				break;
			}
		}
		
		if(remCookie == null) return null;
		return decodeFromCookieVal(remCookie.getValue());
	}

	public static void removeRemCookie(HttpServletResponse resp) {
		resp.addCookie(deletedCookie(REM_TOKEN));
	}
	
	
	
	private void onUserBanned(long userId) {
		int banUserFlagLiveTime = c.props.getIntVal(users_banUserFlagLiveTime);
		c.cache.tryPutCache(getBannedCacheKey(userId), "1", banUserFlagLiveTime);
	}
	
	private void onUserUnbanned(long userId) {
		c.cache.tryRemoveCache(getBannedCacheKey(userId));
	}

	private static String getBannedCacheKey(long userId) {
		return BANNED_CACHE_PREFIX + userId;
	}
	
	public static String getInvalidLoginsKey(HttpServletRequest req, LoginUserReq data) {
		String clientIp = WebUtil.getClientIp(req);
		return "invalidLogins."+data.login+"."+clientIp;
	}


	@Override
	public boolean hasClientSession(HttpServletRequest req) {
		return false;
	}

}
