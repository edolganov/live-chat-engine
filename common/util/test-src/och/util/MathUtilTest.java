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


import och.junit.AssertExt;

import org.junit.Test;

import static och.util.MathUtil.*;

public class MathUtilTest extends AssertExt {
	
	@Test
	public void test_round(){
		
		assertEquals(0, roundUp(0, 15));
		assertEquals(15, roundUp(1, 15));
		assertEquals(15, roundUp(14, 15));
		assertEquals(15, roundUp(15, 15));
		assertEquals(30, roundUp(16, 15));
		assertEquals(-30, roundUp(-16, 15));
		
		
		assertEquals(0, roundDown(0, 15));
		assertEquals(0, roundDown(1, 15));
		assertEquals(0, roundDown(14, 15));
		assertEquals(15, roundDown(15, 15));
		assertEquals(15, roundDown(16, 15));
		assertEquals(-15, roundDown(-16, 15));
	}
	


}
