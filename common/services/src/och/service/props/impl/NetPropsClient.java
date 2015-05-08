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
package och.service.props.impl;

import static java.lang.System.*;
import static och.util.Util.*;

import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import och.service.props.Props;
import och.service.props.net.GetUpdateReq;
import och.service.props.net.GetUpdateResp;
import och.util.concurrent.AsyncListener;
import och.util.concurrent.ExecutorsUtil;
import och.util.model.SecureKeyHolder;
import och.util.socket.json.client.JsonSocketClient;

import org.apache.commons.logging.Log;

public class NetPropsClient implements SecureKeyHolder {
	
	public static final String CONFIG_SLEEP_TO_INIT_RETRY = "NetPropsClient.sleepToInitRetry";
	public static final String CONFIG_RETRY_COUNT = "NetPropsClient.retryCount";
	
	private Log log = getLog(getClass());
	private JsonSocketClient jsonSocketClient;
	private ScheduledExecutorService scheduledService;
	private long delta;
	
	//wait config
	private long sleepToInitRetry = tryParseInt(getProperty(CONFIG_SLEEP_TO_INIT_RETRY), 2000);
	private int initRetryCount = tryParseInt(getProperty(CONFIG_RETRY_COUNT), -1);
	
	//model
	private final MultiProps externalProps;
	private MapProps intProps;
	
	public NetPropsClient(String host, int port, String secureKey, boolean waitConnect) {
		this(host, port, secureKey, waitConnect, 1000*60*2, 10000);
	}
	

	public NetPropsClient(String host, int port, String secureKey, boolean waitConnect, long updateTimeMs) {
		this(host, port, secureKey, waitConnect, updateTimeMs, 10000);
	}
	
	public NetPropsClient(String host, int port, String secureKey, boolean waitConnect, long updateTimeMs, Integer socketSoTimeout) {
		
		jsonSocketClient = new JsonSocketClient(host, port, 1, 1, socketSoTimeout);
		jsonSocketClient.setSecureKey(secureKey);
		
		intProps = new MapProps();
		externalProps = new MultiProps(intProps);
		
		
		//first load
		boolean loaded = updateFromNetIfNeed();
		while(waitConnect && !loaded){
			
			if(initRetryCount > -1){
				if(initRetryCount == 0) throw new IllegalStateException("can't init props - no connection to server");
				initRetryCount--;
			}
			
			log.info("wait connection to server ("+sleepToInitRetry/1000.+"s)...");
			try {
				Thread.sleep(sleepToInitRetry);
			}catch(Exception e){
				throw new IllegalStateException("can't sleep to wait", e);
			}
			loaded = updateFromNetIfNeed();
		}
		
		//server ping
		updateTimeMs = updateTimeMs > 0 && updateTimeMs < 50? 50 : updateTimeMs;
		if(updateTimeMs > 0){
			scheduledService = ExecutorsUtil.newScheduledThreadPool("NetPropsClient", 1);
			scheduledService.scheduleWithFixedDelay(new Runnable() {
				
				@Override
				public void run() {
					updateFromNetIfNeed();
				}
			}, updateTimeMs, updateTimeMs, TimeUnit.MILLISECONDS);
		}
	}

	public void addListener(AsyncListener l){
		jsonSocketClient.addListener(l);
	}

	public Props getProps() {
		return externalProps;
	}
	
	@Override
	public void setSecureKey(String key){
		jsonSocketClient.setSecureKey(key);
	}
	
	@Override
	public boolean isSecuredByKey(){
		return jsonSocketClient.isSecuredByKey();
	}
	
	public void shutdown(){
		scheduledService.shutdown();
	}
	
	
	public synchronized boolean updateFromNetIfNeed() {
		try {
			
			GetUpdateResp res = (GetUpdateResp)jsonSocketClient.invoke(new GetUpdateReq(delta));
			if(isEmpty(res)) return true;
			
			delta = res.delta;
			
			res = res.putDeletedKeysToUpdates();
			if(isEmpty(res.updated)) return true;
			
			if(res.full) {
				log.info("load props from "+jsonSocketClient.getRemoteAddress()+": "+res.updated.keySet());
				intProps = new MapProps(res.updated);
				externalProps.resetSources(intProps);
			} else {
				log.info("load updates from "+jsonSocketClient.getRemoteAddress()+": "+res.updated.keySet());
				for (Entry<String, String> entry : res.updated.entrySet()) {
					intProps.putVal(entry.getKey(), entry.getValue());
				}
			}

			return true;
			
		}catch(Throwable t){
			log.error("can't load props from "+jsonSocketClient.getRemoteAddress()+": "+t);
			return false;
		}
	}
	
	
	
	

}
