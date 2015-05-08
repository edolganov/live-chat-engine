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
package och.comp.web;

import static java.util.Collections.*;
import static och.api.model.PropKey.*;
import static och.comp.web.ErrorCode.*;
import static och.util.NetUtil.*;
import static och.util.Util.*;
import static och.util.json.GsonUtil.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletResponse;

import och.api.exception.web.RemoteServerException;
import och.service.props.Props;

import org.apache.commons.logging.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonOps {
	
	private static final Log log = getLog(JsonOps.class);
	
	private static CopyOnWriteArrayList<PostEncryptedJsonListener> postListeners = new CopyOnWriteArrayList<>();
	
	public static void addPostEncryptedJsonListener(PostEncryptedJsonListener l){
		postListeners.add(l);
	}
	
	
	
	
	
	public static void setJsonResponse(HttpServletResponse resp){
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json; charset=UTF-8");
	}
	
	public static String jsonOk(){
		return "{\"status\":\"ok\"}";
	}
	
	public static String jsonOk(Object ob){
		String data = defaultGson.toJson(ob);
		return "{\"status\":\"ok\", \"data\":"+data+"}";
	}
	
	public static String jsonUnexpectedError(String msg){
		return jsonError(UNEXPECTED, msg, null);
	}
	
	public static String jsonValidationError(Exception e){
		return jsonError(VALIDATION, e.getMessage(), e.getClass());
	}
	
	public static String jsonValidationError(String msg){
		return jsonError(VALIDATION, msg, null);
	}
	
	public static String jsonValidationError(String msg, Class<?> errorType){
		return jsonError(VALIDATION, msg, errorType);
	}
	
	public static String jsonAccessDeniedError(){
		return jsonError(ACCESS_DENIED, "need sign in", null);
	}
	
	public static String jsonError(ErrorCode codeType, String msg, Class<?> type){
		String typeBlock = type == null? "" : ", \"type\":\""+type.getName()+"\"";
		if(msg == null) msg = "";
		return "{\"status\":\"error\", \"code\":"+codeType.code+", \"msg\":\""+msg+"\""+typeBlock+"}";
	}
	
	
	
	
	public static Map<String, Void> postEncryptedJsonToAny(Props props, Collection<String> urls, Object req) {
		return postEncryptedJsonToAny(props, urls, true, req, null);
	}
	
	
	public static <T> Map<String, T> postEncryptedJsonToAny(Props props, Collection<String> urls, Object req, Class<T> respType) {
		return postEncryptedJsonToAny(props, urls, true, req, respType);
	}
	
	
	public static <T> Map<String, T> postEncryptedJsonToAny(Props props, Collection<String> urlsCollection, 
			boolean removeCurServerUrl, Object req, Class<T> respType) {
		
		if(isEmpty(urlsCollection)) return emptyMap();
		
		ArrayList<String> urls = toList(urlsCollection);
		
		if(removeCurServerUrl){
			String httpUrl = props.getVal(httpServerUrl);
			String httpsUrl = props.getVal(httpsServerUrl);
			for (int i = urls.size()-1; i > -1; i--) {
				String url = urls.get(i);
				if( (hasText(httpUrl) && url.startsWith(httpUrl)) 
					|| ( hasText(httpsUrl) && url.startsWith(httpsUrl))){
					urls.remove(i);
				}
			}
		}
		
		
		HashMap<String, T> out = new HashMap<>();
		for (String url : urls) {
			try {
				T result = (T) postEncryptedJson(props, url, req, respType);
				out.put(url, result);
			}catch (Throwable t) {
				log.error("can't postEncryptedJson: url="+url+", ex="+t);
			}
		}
		return out;
	}
	
	
	public static void postEncryptedJson(Props props, String url, Object req) throws Exception{
		postEncryptedJson(props, url, req, Void.class);
	}
	
	public static <T> T postEncryptedJson(Props props, String url, Object req, Class<T> respType) throws Exception{
		String key = props.getVal(remote_encyptedKey);
		int connTimeout = props.getIntVal(remote_connTimeout);
		int readTimeout = props.getIntVal(remote_readTimeout);
		return postEncryptedJson(url, key, req, respType, connTimeout, readTimeout);
	}
	
	
	public static <T> T postEncryptedJson(String url, String key, Object req, Class<T> respType, int connTimeout, int readTimeout) throws Exception{
		
		String data = defaultGson.toJson(req);
		String encoded = isEmpty(key) ? data : encodeToken(data, key);
		String postBody = createPostBody(map("data", encoded));
		
		HttpURLConnection conn = openPostConn(url, connTimeout, readTimeout);
		setTrustAnyHttps(conn);
		String encodedResp = invokePost(conn, postBody);
		
		boolean hasError = false;
		try {

			if(isEmpty(encodedResp)) return null;
			
			String resp = isEmpty(key) ? encodedResp : decodeToken(encodedResp, key);
			
			JsonObject parsed = parseJsonResp(resp);
			if(isEmpty(parsed)) return null;
			
			if( ! isOkStatus(parsed)){
				throw new RemoteServerException(getErrorMsg(parsed));
			}
			
			T out = respType == null? null : getData(parsed, respType);
			return out;
			
		}catch (Exception e) {
			hasError = true;
			throw e;
		}finally {
			//listeners
			if(!hasError){
				try {
					for(PostEncryptedJsonListener l : postListeners) l.onPosted(url, req);
				}catch (Exception e) {
					log.error("can't call listeners", e);
				}
			}
		}
	}
	
	
	
	
	
	
	
	public static JsonObject parseJsonResp(String resp) throws IOException{
		if(isEmpty(resp)) return null;
		
		try {
			return new JsonParser().parse(resp).getAsJsonObject();
		}catch (JsonSyntaxException e) {
			throw new RemoteServerException("can't parse server's resp: "+resp);
		}
	}
	
	
	public static boolean isOkStatus(JsonObject json){
		JsonElement statusElem = json.get("status");
		return statusElem != null && statusElem.getAsString().equals("ok");
	}
	
	
	
	public static String getErrorMsg(JsonObject json){
		ErrorResp errorResp = null;
		try {
			errorResp = defaultGson.fromJson(json, ErrorResp.class);
		}catch (Exception e) {
			//unknown resp answer
		}
		return errorResp == null? json.toString() : errorResp.getExceptionMsg();
	}
	
	public static <T> T getData(JsonObject json, Class<T> respType){
		JsonElement dataElem = json.get("data");
		if(dataElem == null) return null;
		
		T out = defaultGson.fromJson(dataElem, respType);
		return out;
	}

}
