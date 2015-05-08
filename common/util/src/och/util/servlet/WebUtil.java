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
package och.util.servlet;


import static och.util.ExceptionUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.codec.Base64.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import och.util.crypto.AES128;



public class WebUtil {
	
	public static final String CSRF_PROTECT_TOKEN = "CSRF_ProtectToken";
	public static final String XML_HTTP_REQUEST_VAL = "XMLHttpRequest";
	public static final String HEADER_X_REQUESTED_WITH = "X-Requested-With";
	
	public static String getClientIp(HttpServletRequest req){
		
		String ipAddress = req.getHeader("x-forwarded-for");
        if (ipAddress == null) {
        	ipAddress = req.getRemoteAddr();
        }
        return ipAddress;
	}
	
	public static String getUserAgent(HttpServletRequest req){
		return req.getHeader("User-Agent");
	}
	
	public static String getReferer(HttpServletRequest req){
		return req.getHeader("Referer");
	}
	
	public static String encodeToken(String token, String key) {
		byte[] encoded = AES128.encode(token, key);
		String result = encodeBase64String(encoded);
		return result;
	}

	
	public static String decodeToken(String encoded, String key) {
		byte[] bytes = decodeBase64(encoded);
		return AES128.decode(bytes, key);
	}
	
	public static String createActivationCode(){
		return randomSimpleId();
	}
	
	public static String createSalt(){
		return randomSimpleId();
	}
	
	
	public static byte[] getHash(String input, String salt) {
		try {
			
			String salted = input + salt;
			MessageDigest md = MessageDigest.getInstance("SHA-512"); //512 - 64bytes
			md.update(getBytesUTF8(salted));
			return md.digest();
			
		}catch (Exception e) {
			throw getRuntimeExceptionOrThrowError(e);
		}
	}
	
	public static String generateRandomPsw(int length) {
		String out = encodeBase64String(randomUUID()).toLowerCase();
		return out.length() > length? out.substring(0, length) : out;
	}
	
	public static boolean hasXReqHeader(HttpServletRequest req){
		return XML_HTTP_REQUEST_VAL.equals(req.getHeader(HEADER_X_REQUESTED_WITH));
	}
	
	public static void createAndSet_CSRF_ProtectToken(HttpSession session){
		session.setAttribute(CSRF_PROTECT_TOKEN, create_CSRF_ProtectToken());
	}
	
	public static String create_CSRF_ProtectToken(){
		return encodeBase64String(randomUUID());
	}
	
	public static String get_CSRF_ProtectTokenFromSession(HttpServletRequest req){
		HttpSession session = req.getSession(false);
		return session == null? null : (String) session.getAttribute(CSRF_PROTECT_TOKEN);
	}
	
	public static void set_CSRF_ProtectTokenCookieFromSession(HttpServletRequest req, HttpServletResponse resp){
		
		String token = get_CSRF_ProtectTokenFromSession(req);
		if(token == null){
			return;
		}
		
		resp.addCookie(cookie(CSRF_PROTECT_TOKEN, token, true, -1));
	}
	
	
	public static boolean isValid_CSRF_ProtectTokenInReq(HttpServletRequest req){
		
		String token = get_CSRF_ProtectTokenFromSession(req);
		if(token == null){
			return true;
		}
		
		String cookieToken = null;
		Cookie[] cookies = req.getCookies();
		if(isEmpty(cookies)) return false;
		for (Cookie cookie : cookies) {
			if(CSRF_PROTECT_TOKEN.equals(cookie.getName())){
				cookieToken = cookie.getValue();
				break;
			}
		}
		
		String reqToken = req.getParameter("token");
		
		return token.equals(cookieToken) && token.equals(reqToken);
	}
	
	public static Cookie getCookie(HttpServletRequest req, String name){
		Cookie[] cookies = req.getCookies();
		if(isEmpty(cookies)) return null;
		for (Cookie cookie : cookies) {
			if(name.equals(cookie.getName())){
				return cookie;
			}
		}
		return null;
	}
	
	public static String getCookieVal(HttpServletRequest req, String name){
		Cookie c = getCookie(req, name);
		return c != null? c.getValue() : null;
	}
	
	
	
	
	
	public static String filterQuery(String query, Set<String> onlyParams, Set<String> removeParams){
		if( isEmpty(query) || (isEmpty(onlyParams) && isEmpty(removeParams))) return query;
		
		boolean isOnlyMode = ! isEmpty(onlyParams);
		List<String> validPairs = new LinkedList<>();
		StringTokenizer st = new StringTokenizer(query, "&");
		String pair;
		int eqIndex;
		String key;
		while(st.hasMoreTokens()){
			pair = st.nextToken();
			if(isEmpty(pair)) continue;
			eqIndex = pair.indexOf('=');
			if(eqIndex < 1) continue;
			key = pair.substring(0, eqIndex);
			if( isOnlyMode && ! onlyParams.contains(key)) continue;
			if( ! isOnlyMode && removeParams.contains(key)) continue;
			validPairs.add(pair);
		}
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String pairVal : validPairs){
			if(!first) sb.append('&');
			else first = false;
			sb.append(pairVal);
		}
		return sb.toString();
	}
	
	public static Cookie cookieForYear(String name, String val, boolean isHttpOnly){
		return cookie(name, val, isHttpOnly, 60*60*24*365);
	}
	
	public static Cookie cookieForMonth(String name, String val, boolean isHttpOnly){
		return cookie(name, val, isHttpOnly, 60*60*24*30);
	}
	
	public static Cookie cookie(String name, String val, boolean isHttpOnly, int maxAgeSec){
		return cookie(name, val, isHttpOnly, "/", maxAgeSec);
	}
	
	public static Cookie cookie(String name, String val, boolean isHttpOnly, String path, int maxAgeSec){
		Cookie cookie = new Cookie(name, val);
		cookie.setHttpOnly(isHttpOnly);
		cookie.setPath(path);
		cookie.setMaxAge(maxAgeSec);
		return cookie;
	}
	
	public static Cookie cookie(String name, String val, boolean isHttpOnly, String path, int maxAgeSec, String domain){
		Cookie cookie = new Cookie(name, val);
		cookie.setHttpOnly(isHttpOnly);
		cookie.setPath(path);
		cookie.setMaxAge(maxAgeSec);
		cookie.setDomain(domain);
		return cookie;
	}
	
	public static Cookie deletedCookie(String name){
		return deletedCookie(name, "/");
	}
	
	public static Cookie deletedCookie(String name, String path){
		Cookie cookie = new Cookie(name, "");
		cookie.setPath(path);
		cookie.setMaxAge(0);
		return cookie;
	}
	
	
	public static String toHttps(String httpUrl){
		if(httpUrl.startsWith("http://")){
			return "https://"+httpUrl.substring("http://".length());
		}
		return httpUrl;
	}
	
	public static void forward(HttpServletRequest req, HttpServletResponse resp, String path) throws ServletException, IOException{
		req.getRequestDispatcher(path).forward(req, resp);
	}
	
	public static boolean isIp6_SimpleCheck(String ip){
		return ip.contains(":");
	}
	
	public static byte[] md5Hash(String s) {
		if(s == null) return null;
		try {
			byte[] bytes = s.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytes);
			return thedigest;
		}catch(Exception e){
			throw new IllegalStateException("can't md5Encode: "+s, e);
		}
	}
	
	public static String md5HashStr(String s){
		return md5HashToStr(md5Hash(s));
	}
	
	public static String md5HashToStr(byte[] messageDigest){
		// Convert to hex string
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < messageDigest.length; i++) {
		    if ((0xff & messageDigest[i]) < 0x10) {
		        sb.append('0');
		    }
		    sb.append(Integer.toHexString(0xff & messageDigest[i]));
		}
		return sb.toString();
	}
	
	
	public static void writeResp(HttpServletResponse resp, String text) throws IOException{
		PrintWriter writer = resp.getWriter();
		writer.println(text);
		writer.flush();
	}
	
	public static String urlEncode(String param){
		try {
			return URLEncoder.encode(param, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static String urlDecode(String param){
		try {
			return URLDecoder.decode(param, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	
	
	
	
	
}
