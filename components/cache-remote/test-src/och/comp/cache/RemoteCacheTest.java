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
package och.comp.cache;

import static och.api.model.PropKey.*;
import och.comp.cache.client.CacheClient;
import och.comp.cache.server.CacheSever;
import och.junit.AssertExt;
import och.service.props.impl.MapProps;
import och.util.socket.json.exception.InvalidRespException;

import org.junit.Test;

public class RemoteCacheTest extends AssertExt {
	
	String host = "localhost";
	int port = 11001;
	int maxThreads = 10;
	String key1 = "key1";
	int waitTime = 50;
	
	@Test
	public void test_all() throws Exception {
		
		String secureKey = "1234567890123456";
		
		MapProps props = new MapProps();
		props.putVal(cache_plugins, "");
		
		CacheSever server = new CacheSever(port, maxThreads, waitTime, secureKey, props);
		server.runAsync();
		
		try {
			
			CacheClient client = new CacheClient(host, port, 1, 1, secureKey);
			
			assertNull(client.tryGetVal(key1, null));
			
			String val1 = "val1 абв \n\t\0\r\" ";
			String val2 = "val2";
			
			client.tryPutCache(key1, val1);
			assertEquals(val1, client.tryGetVal(key1, null));
			
			client.tryPutCache(key1, val2);
			assertEquals(val2, client.tryGetVal(key1, null));
			
			assertEquals(val2, client.tryRemoveCache(key1));
			assertNull(client.tryGetVal(key1, null));
			
			//with live time
			client.tryPutCache(key1, val1, waitTime / 2);
			Thread.sleep(waitTime + 50);
			assertNull(client.tryGetVal(key1, null));
			
			
			//change secure key
			client.setSecureKey("some-key");
			try {
				client.putCache("some", "some");
				fail_exception_expected();
			}catch(InvalidRespException e){
				//ok
			}
			
			client.setSecureKey(secureKey);
			client.putCache("some", "some");
			assertEquals("some", server.getVal("some"));
			
		}finally {
			server.shutdownWait();
		}
		
	}

}
