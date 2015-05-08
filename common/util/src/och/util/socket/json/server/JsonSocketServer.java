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
package och.util.socket.json.server;


import java.io.IOException;

import och.util.model.SecureKeyHolder;
import och.util.model.SecureProviderImpl;
import och.util.socket.server.SocketServer;



public class JsonSocketServer implements SecureKeyHolder {
	
	private SocketServer serverImpl;
	private JsonProtocolSocketHandler socketHandler;
	private final SecureProviderImpl secureProvider; 

	public JsonSocketServer(int port, int maxThreads) {
		this("JsonSocketServer", port, maxThreads);
	}

	public JsonSocketServer(String name, int port, int maxThreads) {
		this.secureProvider = new SecureProviderImpl("JsonSocketServer("+port+")-"+name); 
		this.socketHandler = new JsonProtocolSocketHandler(secureProvider);
		this.serverImpl = new SocketServer(name, port, maxThreads, socketHandler);
	}
	
	@Override
	public void setSecureKey(String key){
		secureProvider.setSecureKey(key);
	}
	
	@Override
	public boolean isSecuredByKey(){
		return secureProvider.isSecuredByKey();
	}
	
	
	public void runAsync() throws IOException {
		serverImpl.runAsync();
	}
	
	public void runWait() throws IOException {
		serverImpl.runWait();
	}
	
	public void shutdownAsync(){
		serverImpl.shutdownAsync();
	}
	
	public void shutdownWait(){
		serverImpl.shutdownWait();
	}
	
	public <T> void putController(Class<T> type, ReqController<T, ?> controller){
		socketHandler.put(type, controller);
	}
	
	public void removeController(Class<?> type){
		socketHandler.remove(type);
	}
	
	

}
