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
package och.util.socket.json;


import java.io.IOException;
import java.net.SocketAddress;

import och.junit.AssertExt;
import och.util.socket.json.client.JsonSocketClient;
import och.util.socket.json.exception.InvalidRespException;
import och.util.socket.json.exception.ValidationException;
import och.util.socket.json.server.JsonSocketServer;
import och.util.socket.json.server.ReqController;

import org.junit.Test;


public class JsonSocketTest extends AssertExt {
	
	String host = "localhost";
	int port = 11001;
	int maxThreads = 10;
	
	
	@Test
	public void test_change_secure_key() throws IOException{
		
		JsonSocketServer server = new JsonSocketServer(port, maxThreads);
		JsonSocketClient client = new JsonSocketClient(host, port, 1, 1);
		
		String key1 = "djdjda";
		String key2 = "asdf333";
		server.setSecureKey(key1);
		client.setSecureKey(key1);
		
		server.runAsync();

		try {
			
			server.putController(String.class, new ReqController<String, String>() {
				@Override
				public String processReq(String data, SocketAddress remoteAddress) throws Exception {
					return "echo:"+data;
				}
			});
			
			assertEquals("echo:hello", client.invoke("hello"));
			
			
			server.setSecureKey(key2);
			try {
				client.invoke("hello");
				fail_exception_expected();
			}catch(InvalidRespException e){
				//ok
			}
			
			
			client.setSecureKey(key2);
			assertEquals("echo:hello", client.invoke("hello"));
			
		} finally {
			server.shutdownWait();
		}
	}
	
	
	@Test
	public void test_secured() throws Exception {
		
		JsonSocketServer server = new JsonSocketServer(port, maxThreads);
		JsonSocketClient client = new JsonSocketClient(host, port, 1, 1);
		
		String key = "djdjda";
		
		server.setSecureKey(key);
		client.setSecureKey(key);
		
		baseClientServerTest(server, client);
	}
	
	
	@Test
	public void test_unsecure() throws Exception{
		
		JsonSocketServer server = new JsonSocketServer(port, maxThreads);
		JsonSocketClient client = new JsonSocketClient(host, port, 1, 1);
		
		baseClientServerTest(server, client);
	}
	
	private void baseClientServerTest(JsonSocketServer server, JsonSocketClient client) throws Exception{
		
		server.runAsync();
		
		try {
			
			//no controller
			{
				try {
					 client.invoke("hello");
					 fail_exception_expected();
				}catch (ValidationException e) {
					//ok
				}
			}
			
			
			//put controller
			{
				server.putController(String.class, new ReqController<String, String>() {
					@Override
					public String processReq(String data, SocketAddress remoteAddress) throws Exception {
						return "echo:"+data;
					}
				});
				assertEquals("echo:hello", client.invoke("hello"));
			}
			
			
			//remove controller
			{
				server.removeController(String.class);
				try {
					 client.invoke("hello");
					 fail_exception_expected();
				}catch (ValidationException e) {
					//ok
				}
			}
			
			
			//null req
			{
				server.putController(String.class, new ReqController<String, String>() {
					@Override
					public String processReq(String data, SocketAddress remoteAddress) throws Exception {
						return "echo:"+data;
					}
				});
				assertEquals("echo:null", client.invoke(String.class, null));
			}
			
			
			//null resp
			{
				server.putController(String.class, new ReqController<String, String>() {
					@Override
					public String processReq(String data, SocketAddress remoteAddress) throws Exception {
						return null;
					}
				});
				assertNull(client.invoke("hello"));
			}
			
			
			//async
			{
				server.putController(String.class, new ReqController<String, String>() {
					@Override
					public String processReq(String data, SocketAddress remoteAddress) throws Exception {
						return "echo:"+data;
					}
				});
				assertEquals("echo:hello", client.invokeAsync("hello").get());
			}
			
			
			//void
			{
				server.putController(String.class, new ReqController<String, Void>() {
					@Override
					public Void processReq(String data, SocketAddress remoteAddress) throws Exception {
						return null;
					}
				});
				assertNull(client.invoke("hello"));
			}
			
			
		} finally {
			server.shutdownWait();
		}
		
		
		
	}

}
