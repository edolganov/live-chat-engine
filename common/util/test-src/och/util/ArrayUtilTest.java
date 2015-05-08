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

import static och.util.ArrayUtil.*;


import java.nio.ByteBuffer;


import och.junit.AssertExt;

import org.junit.Test;


public class ArrayUtilTest extends AssertExt {
	
	@Test
	public void test_convert(){
		
		assertEquals(0, getLong(getLongBytes(0)));
		assertEquals(256, getLong(getLongBytes(256)));
		assertEquals(-256, getLong(getLongBytes(-256)));
		assertEquals(Long.MAX_VALUE, getLong(getLongBytes(Long.MAX_VALUE)));
		assertEquals(Long.MIN_VALUE, getLong(getLongBytes(Long.MIN_VALUE)));
		
	}
	
	@Test
	public void test_check_with_ByteBuffer(){
		
		byte[] bytes = new byte[]{0,0,0,0,-128,0,59,48};
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    long val = buffer.getLong();
	    long val2 = getLong(bytes, 0);
	    assertEquals(val, val2);
		
	}

}
