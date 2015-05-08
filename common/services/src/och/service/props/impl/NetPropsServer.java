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

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import och.service.props.Props;
import och.service.props.net.GetUpdateReq;
import och.service.props.net.GetUpdateResp;
import och.util.model.CircularFifoBuffer;
import och.util.model.Pair;
import och.util.model.SecureKeyHolder;
import och.util.socket.json.server.JsonSocketServer;
import och.util.socket.json.server.ReqController;

import org.apache.commons.logging.Log;

public class NetPropsServer implements SecureKeyHolder {
	
	private static final int historySize = 20;
	private Log log = getLog(getClass());
	
	private JsonSocketServer serverImpl;
	private final Props props;
	
	//model
	private ReadWriteLock rw = new ReentrantReadWriteLock();
	private Lock read = rw.readLock();
	private Lock write = rw.writeLock();
	private CircularFifoBuffer<Pair<Long, Set<String>>> updateHistory;
	private long delta = currentTimeMillis();
	
	public NetPropsServer(int port, int maxThreads, String secureKey, Props props) {
		
		this.props = props;
		
		serverImpl = new JsonSocketServer("NetPropsServer("+props+")", port, maxThreads);
		serverImpl.setSecureKey(secureKey);
		
		updateHistory = new CircularFifoBuffer<>(historySize);
		
		props.addChangedListener((keys)->{
			addToHist(keys);
		});
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

		serverImpl.putController(GetUpdateReq.class, new ReqController<GetUpdateReq, GetUpdateResp>() {
			@Override
			public GetUpdateResp processReq(GetUpdateReq req, SocketAddress remoteAddress) throws Exception {
				
				GetUpdateResp out = null;
				if(req.delta < 1){
					out = getFullPropsUpdateResp(getDelta());
					log.info("send props to "+remoteAddress+": "+out.getAllKeys());
				} else {
					out = getUpdateResp(req.delta);					
					if(out != null) log.info("send updates to "+remoteAddress+": "+out.getAllKeys());
				}
				
				return out;
			}
		});
		
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
	}
	
	public void shutdownWait(){
		serverImpl.shutdownWait();
	}

	private void addToHist(Set<String> keys) {
		write.lock();
		try {
			
			long oldDelta = delta;
			delta++;
			updateHistory.add(new Pair<>(oldDelta, keys));
			
		}finally {
			write.unlock();
		}
	}
	
	public long getDelta(){
		read.lock();
		try {
			return delta;
		}finally {
			read.unlock();
		}
	}
	
	public GetUpdateResp getUpdateResp(long clientDelta) {
		
		long curDelta = 0;
		boolean clientDeltaInHist = false;
		Set<String> updatedKeys = null;
		
		read.lock();
		try {
			
			curDelta = delta;
			if(clientDelta == curDelta) return null;
			
			for(Pair<Long, Set<String>> item : updateHistory){
				if(clientDelta > item.first) continue;
				if(clientDelta == item.first){
					clientDeltaInHist = true;
					updatedKeys = new HashSet<>();
					updatedKeys.addAll(item.second);
					continue;
				}
				//clientDelta < item.first
				if( ! clientDeltaInHist) break;
				updatedKeys.addAll(item.second);
			}
			
			
		}finally {
			read.unlock();
		}
		
		if( ! clientDeltaInHist){
			return getFullPropsUpdateResp(curDelta);
		} 
		return getUpdateResp(curDelta, updatedKeys);
		
	}
	
	private GetUpdateResp getUpdateResp(long curDelta, Set<String> keys) {
		Map<String, String> updated = new HashMap<>();
		for (String key : keys) {
			updated.put(key, props.getVal(key));
		}
		return new GetUpdateResp(false, updated, curDelta);
	}

	private GetUpdateResp getFullPropsUpdateResp(long curDelta) {
		Map<String, String> updated = props.toMap();
		return new GetUpdateResp(true, updated, curDelta);
	}

}
