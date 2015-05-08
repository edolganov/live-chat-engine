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
package och.comp.ops;

import static och.api.model.PropKey.*;
import static och.comp.ops.ChatOps.*;
import och.junit.AssertExt;
import och.service.props.impl.MapProps;

import org.junit.Test;

public class ChatOpsTest extends AssertExt {
	
	@Test
	public void test_getHostImportantFlag(){
		
		MapProps props = new MapProps();
		
		props.putVal(chats_hosts_unimportant+"_127.0.0", "1");
		{
			assertFalse(getHostImportantFlag(props, "127.0.0.1"));
			assertFalse(getHostImportantFlag(props, "127.0.0.2"));
			assertTrue(getHostImportantFlag(props, "127.0.1.1"));
			assertTrue(getHostImportantFlag(props, "ya.ru"));
			assertTrue(getHostImportantFlag(props, "ffff"));
		}
		
		props.putVal(chats_hosts_unimportant+"_192.168", "1");
		{
			assertFalse(getHostImportantFlag(props, "192.168.1.1"));
			assertFalse(getHostImportantFlag(props, "192.168.255.1"));
			assertTrue(getHostImportantFlag(props, "192.169.1.1"));
			assertTrue(getHostImportantFlag(props, "ya.ru"));
			assertTrue(getHostImportantFlag(props, "ffff"));
		}
		
		props.putVal(chats_hosts_unimportant+"_localhost", "1");
		{
			assertFalse(getHostImportantFlag(props, "localhost"));
			assertFalse(getHostImportantFlag(props, "localhost.com"));
			assertTrue(getHostImportantFlag(props, "ya.ru"));
			assertTrue(getHostImportantFlag(props, "ffff"));
		}
		
		props.putVal(chats_hosts_unimportant+"_1.1.1.1", "1");
		{
			assertFalse(getHostImportantFlag(props, "1.1.1.1"));
			assertTrue(getHostImportantFlag(props, "1.1.1.2"));
		}
		
	}

}
