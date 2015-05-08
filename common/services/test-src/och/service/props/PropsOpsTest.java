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

import och.api.model.PropKey;
import och.junit.AssertExt;
import och.service.props.impl.MapProps;
import och.util.model.SecureProviderImpl;

import org.junit.Test;

public class PropsOpsTest extends AssertExt {
	
	@Test
	public void test_addUpdateSecureKeyListener(){
		
		PropKey secureKey = PropKey.cache_encyptedKey;
		SecureProviderImpl keyHolder = new SecureProviderImpl("test");
		
		MapProps props = new MapProps();
		props.putVal(secureKey, "1");
		
		PropsOps.addUpdateSecureKeyListener(props, keyHolder, secureKey);
		
		//init
		assertFalse(keyHolder.isSecuredByKey());
		
		//set
		props.putVal(secureKey, "2");
		assertTrue(keyHolder.isSecuredByKey());
		
		//reset
		props.putVal(secureKey, (String)null);
		assertFalse(keyHolder.isSecuredByKey());
		
		
		
	}

}
