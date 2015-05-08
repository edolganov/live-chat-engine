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
package och.util.socket.json.client;


import static och.util.concurrent.AsyncListener.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import och.util.concurrent.AsyncListener;
import och.util.concurrent.ExecutorsUtil;
import och.util.model.SecureKeyHolder;
import och.util.model.SecureProviderImpl;
import och.util.socket.pool.SocketsPool;



public class JsonSocketClient implements SecureKeyHolder {
	
	private SocketsPool socketsPool;
	private ExecutorService writeThread = ExecutorsUtil.newSingleThreadExecutor("JsonSocketClient");
	private ArrayList<AsyncListener> listeners = new ArrayList<>();
	private final SecureProviderImpl secureProvider; 

	public JsonSocketClient(String host, int port, int maxConnections, int idleConnections) {
		this(host, port, maxConnections, idleConnections, 10000);
	}
	
	public JsonSocketClient(String host, int port, int maxConnections, int idleConnections, Integer socketSoTimeout) {
		socketsPool = new SocketsPool(host, port);
		socketsPool.setPoolMaximumActiveConnections(maxConnections);
		socketsPool.setPoolMaximumIdleConnections(idleConnections);
		socketsPool.setSocketSoTimeoutForNewConn(socketSoTimeout);
		
		secureProvider = new SecureProviderImpl("JsonSocketClient("+host+":"+port+")"); 
	}
	
	public String getHost() {
		return socketsPool.getHost();
	}

	public int getPort() {
		return socketsPool.getPort();
	}
	
	public String getRemoteAddress(){
		return socketsPool.getRemoteAddress();
	}
	
	public void addListener(AsyncListener l){
		listeners.add(l);
	}
	
	@Override
	public void setSecureKey(String key){
		secureProvider.setSecureKey(key);
	}
	
	@Override
	public boolean isSecuredByKey(){
		return secureProvider.isSecuredByKey();
	}
	
	public Future<Object> invokeAsync(Object data){
		return invokeAsync(data.getClass(), data);
	}
	
	public Future<Object> invokeAsync(final Class<?> type, final Object data){
		Future<Object> future = writeThread.submit(() -> invoke(type, data));
		fireAsyncEvent(listeners, future);
		return future;
	}
	
	public Object invoke(Object data) throws IOException {
		return invoke(data.getClass(), data);
	}
	
	public Object invoke(final Class<?> type, final Object data) throws IOException {
		return socketsPool.invoke(new JsonProtocolConnHandler(type, data, secureProvider));
	}


}
