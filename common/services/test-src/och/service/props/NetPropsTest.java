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
package och.service.props;

import static och.util.Util.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import och.junit.AssertExt;
import och.service.props.impl.MapProps;
import och.service.props.impl.NetPropsClient;
import och.service.props.impl.NetPropsServer;
import och.service.props.net.GetUpdateResp;
import och.util.concurrent.ExecutorsUtil;

import org.junit.Test;

@SuppressWarnings("rawtypes")
public class NetPropsTest extends AssertExt {
	
	String host = "localhost";
	int port = 11011;
	int maxThreads = 10;
	
	
	@Test
	public void test_wait_conn_to_init() throws Exception{
		
		String secureKey = "DSF@s";
		
		MapProps serverProps = new MapProps(map("1", "1", "2", "2"));
		NetPropsServer server = new NetPropsServer(port, maxThreads, secureKey, serverProps);
		try {
			
			System.setProperty(NetPropsClient.CONFIG_SLEEP_TO_INIT_RETRY, "10");
			System.setProperty(NetPropsClient.CONFIG_RETRY_COUNT, "30");
			
			Future<Map<String, String>> f = ExecutorsUtil.newSingleThreadExecutor("test").submit(()->{
				NetPropsClient client = new NetPropsClient(host, port, secureKey, true);
				return client.getProps().toMap();
			});
			
			Thread.sleep(100);
			
			server.runAsync();
			
			//проверка, что в итоге данные получили
			assertEquals(map("1", "1", "2", "2"), f.get());
			
			//проверка новых пропертей после старта сервера
			NetPropsClient client1 = new NetPropsClient(host, port, secureKey, true);
			assertEquals(map("1", "1", "2", "2"), client1.getProps().toMap());
			
			
		}finally {
			System.setProperty(NetPropsClient.CONFIG_SLEEP_TO_INIT_RETRY, "");
			System.setProperty(NetPropsClient.CONFIG_RETRY_COUNT, "");
			server.shutdownWait();
		}
		
	}
	
	@Test
	public void test_net() throws Exception{
		
		String secureKey = "fjfj!!edf";
		
		MapProps serverProps = new MapProps(map("1", "1", "2", "2"));
		NetPropsServer server = new NetPropsServer(port, maxThreads, secureKey, serverProps);
		server.runAsync();
		
		try {
			
			
			NetPropsClient client1 = new NetPropsClient(host, port, secureKey, false);
			NetPropsClient client2 = new NetPropsClient(host, port, secureKey, false, 50);
			
			Set[] eventsKey1 = {null};
			Set[] eventsKey2 = {null};
			client1.getProps().addChangedListener((keys) -> eventsKey1[0] = keys);
			client2.getProps().addChangedListener((keys) -> eventsKey2[0] = keys);
			
			
			//load
			{
				assertEquals(map("1", "1", "2", "2"), client1.getProps().toMap());
				assertEquals(map("1", "1", "2", "2"), client2.getProps().toMap());
			}
			
			//updates
			{
				serverProps.putVal("3", "3");
				client1.updateFromNetIfNeed();
				assertEquals(map("1", "1", "2", "2", "3", "3"), client1.getProps().toMap());
				
				Thread.sleep(100);
				assertEquals(map("1", "1", "2", "2", "3", "3"), client2.getProps().toMap());
				
				assertEquals(set("3"), eventsKey1[0]);
				assertEquals(set("3"), eventsKey2[0]);
			}
			
			//removes
			{
				serverProps.putVal("3", (String)null);
				client1.updateFromNetIfNeed();
				assertEquals(map("1", "1", "2", "2"), client1.getProps().toMap());
				
				serverProps.putVal("3", "3");
				client1.updateFromNetIfNeed();
				assertEquals(map("1", "1", "2", "2", "3", "3"), client1.getProps().toMap());
			}
			
			
			//full updates after reload
			{
				
				//reload server
				server.shutdownWait();
			
				serverProps.removeVal("1");
				
				server = new NetPropsServer(port, maxThreads, secureKey, serverProps);
				server.runAsync();
				
				//неудавшийся релоад с потерей соединения
				assertFalse(client1.updateFromNetIfNeed());
				assertEquals(map("1", "1", "2", "2", "3", "3"), client1.getProps().toMap());
				
				//удачный релоад
				assertTrue(client1.updateFromNetIfNeed());
				assertEquals(map("2", "2", "3", "3"), client1.getProps().toMap());
				
				Thread.sleep(100);
				assertEquals(map("2", "2", "3", "3"), client2.getProps().toMap());
				
				assertEquals(set("1", "2", "3"), eventsKey1[0]);
				assertEquals(set("1", "2", "3"), eventsKey2[0]);
				
				
				//неверный ключ убирает обновления
				{
					client1.setSecureKey("some-wrong");
					serverProps.putVal("4", "4");
					
					assertFalse(client1.updateFromNetIfNeed());
					assertEquals(map("2", "2", "3", "3"), client1.getProps().toMap());
					
					
					client1.setSecureKey(secureKey);
					assertTrue(client1.updateFromNetIfNeed());
					assertEquals(map("2", "2", "3", "3", "4", "4"), client1.getProps().toMap());
				}
				
				
				//отсуствие обновлений
				eventsKey1[0] = null;
				client1.updateFromNetIfNeed();
				assertEquals(null, eventsKey1[0]);
			}
			
			
			client1.shutdown();
			client2.shutdown();
			
		}finally {
			server.shutdownWait();
		}
		
		
	}
	
	
	
	@Test
	public void test_getUpdates(){
		
		MapProps props = new MapProps(map("1", "1", "2", "2"));
		NetPropsServer server = new NetPropsServer(0, 1, "test", props);
		
		long delta1 = server.getDelta();
		
		//old client
		{
			GetUpdateResp resp = server.getUpdateResp(delta1 - 1);
			assertEquals(true, resp.full);
			assertEquals(delta1, resp.delta);
			assertEquals(map("1", "1", "2", "2"), resp.updated);
		}
		
		props.putVal("1", "0");
		long delta2 = server.getDelta();
		assertFalse(delta1 == delta2);
		{
			GetUpdateResp resp = server.getUpdateResp(delta1 - 1);
			assertEquals(true, resp.full);
			assertEquals(delta2, resp.delta);
			assertEquals(map("1", "0", "2", "2"), resp.updated);
		}
		{
			GetUpdateResp resp = server.getUpdateResp(delta1);
			assertEquals(false, resp.full);
			assertEquals(delta2, resp.delta);
			assertEquals(map("1", "0"), resp.updated);
		}
		{
			GetUpdateResp resp = server.getUpdateResp(delta2);
			assertEquals(null, resp);
		}
		
		props.putVal("3", "3");
		long delta3 = server.getDelta();
		assertFalse(delta2 == delta3);
		{
			GetUpdateResp resp = server.getUpdateResp(delta1 - 1);
			assertEquals(true, resp.full);
			assertEquals(delta3, resp.delta);
			assertEquals(map("1", "0", "2", "2", "3", "3"), resp.updated);
		}
		{
			GetUpdateResp resp = server.getUpdateResp(delta1);
			assertEquals(false, resp.full);
			assertEquals(delta3, resp.delta);
			assertEquals(map("1", "0", "3", "3"), resp.updated);
		}
		{
			GetUpdateResp resp = server.getUpdateResp(delta2);
			assertEquals(false, resp.full);
			assertEquals(delta3, resp.delta);
			assertEquals(map("3", "3"), resp.updated);
		}
		{
			GetUpdateResp resp = server.getUpdateResp(delta3);
			assertEquals(null, resp);
		}
		
		
	}

}
