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
package och.util.stopwatch;

import static och.util.stopwatch.ThreadStopwatch.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import och.junit.AssertExt;
import och.util.concurrent.ExecutorsUtil;

import org.junit.Test;

public class ThreadStopwatchTest extends AssertExt {
	
	@Test
	public void test_sw() throws Exception {
		
		assertNull(getRootThreadStopwatch());
		
		//create, remove
		{
			ThreadStopwatch sw = startThreadStopwatch("1");
			assertNotNull(getRootThreadStopwatch());
			assertEquals(sw, getRootThreadStopwatch());
			
			sw.remove();
			assertNull(getRootThreadStopwatch());
			
			//test
			sw.remove();
			sw.remove();
		}
		
		//children
		{
			ThreadStopwatch sw = startThreadStopwatch("root");
			assertNotNull(getRootThreadStopwatch());
			
			ThreadStopwatch sw1 = startThreadStopwatch("1");
			
			ThreadStopwatch sw11 = startThreadStopwatch("11");
			sw11.remove();
			sw11.remove();
			
			ThreadStopwatch sw12 = startThreadStopwatch("12");
			sw12.remove();
			sw12.remove();
			
			sw1.remove();
			sw1.remove();
			
			ThreadStopwatch sw2 = startThreadStopwatch("2");
			ThreadStopwatch sw21 = startThreadStopwatch("21");
			sw21.remove();
			sw2.remove();
			
			sw.remove();
			
			assertNull(getRootThreadStopwatch());
			
			List<ThreadStopwatch> ch = sw.getChildren();
			assertEquals(2, ch.size());
			assertEquals(sw1, ch.get(0));
			assertEquals(sw2, ch.get(1));
			
			List<ThreadStopwatch> ch1 = ch.get(0).getChildren();
			assertEquals(2, ch1.size());
			assertEquals(sw11, ch1.get(0));
			assertEquals(sw12, ch1.get(1));
			
			List<ThreadStopwatch> ch2 = ch.get(1).getChildren();
			assertEquals(1, ch2.size());
			assertEquals(sw21, ch2.get(0));
			assertEquals(0, ch2.get(0).getChildren().size());
		}
		
		//from other thread
		{
			ThreadStopwatch sw = startThreadStopwatch("root");
			ThreadStopwatch[] sw1 = new ThreadStopwatch[1];
			
			ExecutorService executor = ExecutorsUtil.newSingleThreadExecutor("single");
			Future<?> future = executor.submit(()->{
				
				assertNull(getRootThreadStopwatch());
				
				sw1[0] = startThreadStopwatch("1");
				sw.addExternalChild(sw1[0]);
				
				ThreadStopwatch sw11 = startThreadStopwatch("11");
				sw11.remove();
				
				sw1[0].remove();
			});
			future.get();
			executor.shutdownNow();
			
			sw.remove();
			assertNull(getRootThreadStopwatch());
			
			
			List<ThreadStopwatch> ch = sw.getChildren();
			assertEquals(1, ch.size());
			assertEquals(sw1[0], ch.get(0));
			
			List<ThreadStopwatch> ch1 = ch.get(0).getChildren();
			assertEquals(1, ch1.size());
			
		}
		
	}

}
