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
package och.util;

import static och.util.MapUtil.*;
import static och.util.Util.*;
import och.junit.AssertExt;

import org.junit.Test;

public class MapUtilTest extends AssertExt {
	
	@Test
	public void test_updated_keys(){
		
		// . . 
		assertEquals(set(), getUpdatedKeys(map(1, 1, 2, 2), map(1, 1, 2, 2)));
		// . .+
		assertEquals(set(2), getUpdatedKeys(map(1, 1), map(1, 1, 2, 2)));
		// .-. 
		assertEquals(set(2), getUpdatedKeys(map(1, 1, 2, 2), map(1, 1)));
		// .-.+
		assertEquals(set(2, 3), getUpdatedKeys(map(1, 1, 2, 2), map(1, 1, 3, 3)));
		//~. .
		assertEquals(set(2), getUpdatedKeys(map(1, 1, 2, 2), map(1, 1, 2, 0)));
		//~. .+
		assertEquals(set(2, 3), getUpdatedKeys(map(1, 1, 2, 2), map(1, 1, 2, 0, 3, 3)));
		//~.-.
		assertEquals(set(1, 2), getUpdatedKeys(map(1, 1, 2, 2), map(2, 0)));
		//~.-.+
		assertEquals(set(1, 2, 3), getUpdatedKeys(map(1, 1, 2, 2), map(1, 0, 3, 3)));


	}

}
