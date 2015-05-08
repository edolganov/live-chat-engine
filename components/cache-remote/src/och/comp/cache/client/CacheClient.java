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
package och.comp.cache.client;

import static och.api.model.PropKey.*;
import static och.util.Util.*;

import java.io.IOException;
import java.util.concurrent.Future;

import och.api.exception.ExpectedException;
import och.comp.cache.Cache;
import och.comp.cache.common.GetValReq;
import och.comp.cache.common.PutValReq;
import och.comp.cache.common.RemoveValReq;
import och.service.props.Props;
import och.util.concurrent.AsyncListener;
import och.util.model.SecureKeyHolder;
import och.util.socket.json.client.JsonSocketClient;

import org.apache.commons.logging.Log;

public class CacheClient implements Cache, SecureKeyHolder{
	
	private Log log = getLog(getClass());
	private JsonSocketClient jsonSocketClient;
	
	public CacheClient(Props props) {
		this(
			props.getStrVal(cache_remote_host), 
			props.getIntVal(cache_remote_port),
			props.getIntVal(cache_remote_maxConns), 
			props.getIntVal(cache_remote_idleConns),
			props.getStrVal(cache_encyptedKey));
	}

	public CacheClient(String host, int port, int maxConnections, int idleConnections, String secureKey) {
		this(host, port, maxConnections, idleConnections, secureKey, 10000);
	}
	
	public CacheClient(String host, int port, int maxConnections, int idleConnections, String secureKey, Integer socketSoTimeout) {
		jsonSocketClient = new JsonSocketClient(host, port, maxConnections, idleConnections, socketSoTimeout);
		jsonSocketClient.setSecureKey(secureKey);
	}
	
	@Override
	public void setSecureKey(String key){
		jsonSocketClient.setSecureKey(key);
	}
	
	@Override
	public boolean isSecuredByKey(){
		return jsonSocketClient.isSecuredByKey();
	}
	
	
	
	public void addListener(AsyncListener l){
		jsonSocketClient.addListener(l);
	}
	
	
	@Override
	public Future<?> putCacheAsync(String key, String val){
		return putCacheAsync(key, val, 0);
	}
	
	@Override
	public Future<?> putCacheAsync(String key, String val, int liveTime){
		return jsonSocketClient.invokeAsync(new PutValReq(key, val, liveTime));
	}
	
	@Override
	public void tryPutCache(String key, String val){
		tryPutCache(key, val, 0);
	}
	
	@Override
	public void tryPutCache(String key, String val, int liveTime){
		try {
			putCache(key, val, liveTime);
		}catch (Exception e) {
			ExpectedException.logError(log, e, "can't putCache");
		}
	}
	
	@Override
	public void putCache(String key, String val) throws IOException{
		putCache(key, val, 0);
	}
	
	@Override
	public void putCache(String key, String val, int liveTime) throws IOException{
		jsonSocketClient.invoke(new PutValReq(key, val, liveTime));
	}
	
	@Override
	public String tryGetVal(String key){
		return tryGetVal(key, null);
	}
	
	@Override
	public String tryGetVal(String key, String defaultVal){
		try {
			return getVal(key);
		}catch (Exception e) {
			ExpectedException.logError(log, e, "can't getVal");
			return defaultVal;
		}
	}
	
	@Override
	public String getVal(String key) throws IOException{
		return (String)jsonSocketClient.invoke(new GetValReq(key));
	}
	
	@Override
	public String tryRemoveCache(String key){
		try {
			return removeCache(key);
		}catch (Exception e) {
			ExpectedException.logError(log, e, "can't removeVal");
			return null;
		}
	}
	
	@Override
	public String removeCache(String key) throws IOException{
		return (String) jsonSocketClient.invoke(new RemoveValReq(key));
	}
	
	@Override
	public Future<?> removeCacheAsync(String key){
		return jsonSocketClient.invokeAsync(new RemoveValReq(key));
	}

}
