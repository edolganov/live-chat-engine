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
package och.util.string;



import java.io.IOException;


import och.util.string.WordsCounter.IndexesStorage;
import och.util.string.WordsCounter.WordStat;

import org.junit.Test;

import test.BaseTest;

@org.junit.Ignore
public class IndexesStorageTest extends BaseTest {
	
	
	@Test
	public void test_put_get() throws IOException{
		
		String word1 = "один";
		String word2 = "second";
		String word3 = "word3";
		
		IndexesStorage storage = new IndexesStorage(TEST_DIR, 100);
			
		assertNull(storage.getFromStorage(word1));
		storage.putToStorage(new WordStat(word1, 1));
		{
			WordStat stat = storage.getFromStorage(word1);
			assertNotNull(stat);
			assertEquals(word1, stat.word);
			assertEquals(1, stat.count());
		}
		
		
		storage.putToStorage(new WordStat(word1, 2));
		storage.putToStorage(new WordStat(word2, 1));
		{
			WordStat stat = storage.getFromStorage(word1);
			assertNotNull(stat);
			assertEquals(word1, stat.word);
			assertEquals(2, stat.count());
		}
		{
			WordStat stat = storage.getFromStorage(word2);
			assertNotNull(stat);
			assertEquals(word2, stat.word);
			assertEquals(1, stat.count());
		}
		assertNull(storage.getFromStorage(word3));
		
	}
	

}
