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
package och.comp.cache.server;

import static och.api.model.PropKey.*;
import static och.comp.cache.server.CacheSeverOps.*;
import static och.service.props.PropsOps.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.concurrent.DoneFuture.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import och.comp.cache.Cache;
import och.comp.cache.common.GetValReq;
import och.comp.cache.common.PutValReq;
import och.comp.cache.common.RemoveValReq;
import och.comp.cache.impl.CacheImpl;
import och.comp.cache.server.CacheSeverOps.ServersPropsResp;
import och.comp.db.base.BaseDb;
import och.comp.db.main.MainDb;
import och.comp.mail.MailService;
import och.service.props.Props;
import och.service.props.impl.FileProps;
import och.service.props.impl.MultiProps;
import och.service.props.impl.NetPropsServer;
import och.util.model.HasInitState;
import och.util.model.SecureKeyHolder;
import och.util.socket.json.server.JsonSocketServer;
import och.util.socket.json.server.ReqController;

import org.apache.commons.logging.Log;

public class CacheSever implements Cache, SecureKeyHolder {
	
	
	
	public static void main(String[] args) throws Exception {
		
		FileProps startProps = new FileProps("./config.properties");
		
		ServersPropsResp serversProps = getServersProps(startProps);
		
		MultiProps cacheProps = serversProps.cacheProps;
		MultiProps frontProps = serversProps.frontProps;
		MultiProps chatsProps = serversProps.chatsProps;
		
		
		//run front props
		NetPropsServer frontPropsServer = new NetPropsServer(
				startProps.getIntVal(netProps_front_port), 
				startProps.getIntVal(netProps_front_maxConns), 
				startProps.getStrVal(netProps_front_secureKey), 
				frontProps);
		addUpdateSecureKeyListener(startProps, frontPropsServer, netProps_front_secureKey);
		frontPropsServer.runAsync();
		
		
		
		//run chats props
		NetPropsServer chatsPropsServer = new NetPropsServer(
				startProps.getIntVal(netProps_chats_port), 
				startProps.getIntVal(netProps_chats_maxConns), 
				startProps.getStrVal(netProps_chats_secureKey), 
				chatsProps);
		addUpdateSecureKeyListener(startProps, chatsPropsServer, netProps_chats_secureKey);
		chatsPropsServer.runAsync();
			
		
		
		
		//run cache server
		CacheSever server = new CacheSever(cacheProps);
		addUpdateSecureKeyListener(cacheProps, server, cache_encyptedKey);
		server.runWait();
		

	}
	
	
	private Log log = getLog(getClass());
	
	private JsonSocketServer serverImpl;
	private CacheImpl cache;
	private Props props;
	
	
	public CacheSever(Props props) {
		this(
			props.getIntVal(cache_remote_port), 
			props.getIntVal(cache_remote_maxConns), 
			props.getLongVal(cache_cleanTime),
			props.getStrVal(cache_encyptedKey),
			props);
	}
	
	public CacheSever(int port, int maxThreads, long removeOldDeltaTime, String secureKey, Props props) {
		this.cache = new CacheImpl(removeOldDeltaTime);
		this.props = props;
		
		serverImpl = new JsonSocketServer("CacheSever", port, maxThreads);
		serverImpl.setSecureKey(secureKey);
	}
	
	public void runAsync() throws IOException {
		init();
		serverImpl.runAsync();
	}
	
	public void runWait() throws IOException {
		init();
		serverImpl.runWait();
	}
	
	private void init() throws IOException{

		serverImpl.putController(PutValReq.class, new ReqController<PutValReq, Void>() {
			@Override
			public Void processReq(PutValReq req, SocketAddress remoteAddress) throws Exception {
				putVal(req.key, req.val, req.liveTime);
				return null;
			}
		});
		serverImpl.putController(GetValReq.class, new ReqController<GetValReq, String>() {
			@Override
			public String processReq(GetValReq req, SocketAddress remoteAddress) throws Exception {
				return getVal(req.key);
			}
		});
		serverImpl.putController(RemoveValReq.class, new ReqController<RemoveValReq, String>() {
			@Override
			public String processReq(RemoveValReq req, SocketAddress remoteAddress) throws Exception {
				return removeVal(req.key);
			}
		});
		
		cache.run();
		
		initPlugins();
	}

	private void initPlugins() {
		
		List<String> classes = strToList(props.getStrVal(cache_plugins));
		if(isEmpty(classes)) return;
		
		try {
			
			MailService mailService = new MailService(props);
			DataSource ds = BaseDb.createDataSource(props);
			MainDb db = new MainDb(ds, props);
			
			CacheServerContext c = new CacheServerContext(props, this, db, mailService);

			for (String clazz : classes) {
				try {
					
					Object inst = Class.forName(clazz).newInstance();
					if(inst instanceof CacheServerContextHolder) ((CacheServerContextHolder)inst).setCacheServerContext(c);
					if(inst instanceof HasInitState) ((HasInitState)inst).init();
					
					log.info("inited plugin: "+inst);
					
				}catch(Exception e){
					log.error("can't init plugin: "+clazz+": "+e);
				}
			}
			
			
		}catch(Exception e) {
			log.error("can't init plugins", e);
		}
		
	}
	
	@Override
	public void setSecureKey(String key){
		serverImpl.setSecureKey(key);
	}
	
	@Override
	public boolean isSecuredByKey(){
		return serverImpl.isSecuredByKey();
	}

	public void shutdownAsync(){
		serverImpl.shutdownAsync();
		cache.shutdown();
	}
	
	public void shutdownWait(){
		serverImpl.shutdownWait();
		cache.shutdown();
	}
	
	public void putVal(String key, String val) {
		putVal(key, val, 0);
	}
	
	public void putVal(String key, String val, int liveTime) {
		cache.putObjVal(key, val, liveTime);
	}
	
	@Override
	public String getVal(String key) {
		return cache.tryGetVal(key);
	}
	
	public Integer getItemLivetime(String key){
		return cache.getItemLivetime(key);
	}
	
	public String removeVal(String key){
		return (String) cache.removeObjVal(key);
	}

	@Override
	public Future<?> putCacheAsync(String key, String val) {
		putVal(key, val);
		return EMPTY_DONE_FUTURE;
	}

	@Override
	public Future<?> putCacheAsync(String key, String val, int liveTime) {
		putVal(key, val, liveTime);
		return EMPTY_DONE_FUTURE;
	}

	@Override
	public void tryPutCache(String key, String val) {
		putVal(key, val);
	}

	@Override
	public void tryPutCache(String key, String val, int liveTime) {
		putVal(key, val, liveTime);
	}

	@Override
	public void putCache(String key, String val) throws IOException {
		putVal(key, val);
	}

	@Override
	public void putCache(String key, String val, int liveTime) throws IOException {
		putVal(key, val, liveTime);
	}

	@Override
	public String tryGetVal(String key) {
		return getVal(key);
	}

	@Override
	public String tryGetVal(String key, String defaultVal) {
		String out = getVal(key);
		if(out == null) return defaultVal;
		return out;
	}

	@Override
	public String tryRemoveCache(String key) {
		return removeVal(key);
	}

	@Override
	public String removeCache(String key) throws IOException {
		return removeVal(key);
	}

	@Override
	public Future<?> removeCacheAsync(String key) {
		removeVal(key);
		return EMPTY_DONE_FUTURE;
	}
	

}
