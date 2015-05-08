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
package och.util.model;


import static och.util.Util.*;

import java.util.Iterator;

import och.junit.AssertExt;
import och.util.Util;

import org.junit.Test;

public class CircularFifoBufferTest extends AssertExt {
	
	
	@Test
	public void test_buffer(){
		
		CircularFifoBuffer<Integer> buffer = new CircularFifoBuffer<>(3);
		
		buffer.add(1);
		assertEquals(Util.list(1), toList(buffer));
		
		buffer.add(2);
		assertEquals(Util.list(1, 2), toList(buffer));
		
		buffer.add(3);
		assertEquals(Util.list(1, 2, 3), toList(buffer));
		
		buffer.add(4);
		assertEquals(Util.list(2, 3, 4), toList(buffer));
		
		Iterator<Integer> it = buffer.iterator();
		assertEquals(new Integer(2), it.next());
		assertEquals(new Integer(3), it.next());
		assertEquals(new Integer(4), it.next());
		assertEquals(false, it.hasNext());
		
		
		
	}

}
