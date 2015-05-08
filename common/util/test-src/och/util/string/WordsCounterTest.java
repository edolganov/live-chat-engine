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


import static och.util.Util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import och.util.string.WordsCounter.WordStat;

import org.junit.Test;

import test.BaseTest;

@org.junit.Ignore
public class WordsCounterTest extends BaseTest {
	
	
	
	@org.junit.Ignore
	@Test
	public void test_big_counts_in_hdd() throws IOException{
		
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		counter.setRemoveIndexesFiles(false);
		counter.setRamTopSize(5);
		
		String str = toWordsStr(list(
				new WordStat("a", 1000),
				new WordStat("b", 999),
				new WordStat("c", 999),
				new WordStat("d", 888),
				new WordStat("e", 777),
				new WordStat("f", 666)
			));
		assertEquals("a-1000 b-999 c-999 d-888 e-777", toStr(counter.getTop(str, 5)));
	}
	
	
	
	
	
	@Test
	public void test_big_counts_in_ram() throws IOException{
		
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		counter.setRemoveIndexesFiles(false);
		
		String str = toWordsStr(list(
				new WordStat("a", 1000),
				new WordStat("b", 999),
				new WordStat("c", 999),
				new WordStat("d", 888),
				new WordStat("e", 777),
				new WordStat("f", 666)
			));
		assertEquals("a-1000", toStr(counter.getTop(str, 1)));
		assertEquals("a-1000 b-999", toStr(counter.getTop(str, 2)));
		assertEquals("a-1000 b-999 c-999", toStr(counter.getTop(str, 3)));
		assertEquals("a-1000 b-999 c-999 d-888", toStr(counter.getTop(str, 4)));
		assertEquals("a-1000 b-999 c-999 d-888 e-777", toStr(counter.getTop(str, 5)));
		assertEquals("a-1000 b-999 c-999 d-888 e-777 f-666", toStr(counter.getTop(str, 10)));
	}
	
	
	
	@Test
	public void test_fromExample() throws IOException{
		
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		
		String words = "sergey_martynov " +
			"rustem_bedretdinov " +
			"sergey_martynov " +
			"guest123 " +
			"sergey_martynov " +
			"rustem_bedretdinov " +
			"BATMAN " +
			"batman ";
		
		List<WordStat> top = counter.getTop(words, 4);
		assertEquals("sergey_martynov-3 batman-2 rustem_bedretdinov-2 guest123-1", toStr(top));
	}
	
	
	@Test
	public void test_uppercase() throws IOException{
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		assertEquals("aa-3 b-2 c-1", toStr(counter.getTop("Aa b c AA aA B D", 3)));
	}
	
	
	@Test
	public void test_count() throws IOException{
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		assertEquals("aa-3 b-2 c-1", toStr(counter.getTop("aa b c aa aa b d", 3)));
	}
	
	
	@Test
	public void test_top_word_max_size() throws IOException{
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		assertEquals("a-1 aa-1 b-1", toStr(counter.getTop("a b c aa", 3, 2)));
		assertEquals("a-1 b-1 c-1", toStr(counter.getTop("a b c aa", 3, 1)));
	}
	
	
	@Test
	public void test_top_size() throws IOException{
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		assertEquals("a-1 b-1 c-1 d-1", toStr(counter.getTop("a b c d", 5)));
		assertEquals("a-1 b-1 c-1 d-1", toStr(counter.getTop("a b c d", 4)));
		assertEquals("a-1 b-1 c-1", toStr(counter.getTop("a b c d", 3)));
		assertEquals("a-1 aa-1 b-1", toStr(counter.getTop("a b c aa", 3)));
		assertEquals("d-3 a-1 b-1", toStr(counter.getTop("a b c d d d", 3)));
	}
	
	@Test
	public void test_empty() throws IOException{
		WordsCounter counter = new WordsCounter();
		counter.setTmpIndexDirParent(TEST_DIR);
		assertEquals(0, counter.getTop((InputStream)null, 2).size());
		assertEquals(0, counter.getTop("   \n \r\0   ", 2).size());
	}
	
	
	private static String toWordsStr(List<WordStat> list){
		
		Random r = new Random();
		
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		while(list.size() > 0){
			
			if( ! isFirst) sb.append(' ');
			isFirst = false;
			
			int index = r.nextInt(list.size());
			WordStat stat = list.get(index);
			sb.append(stat.word);
			
			stat.decCount();
			if(stat.count() == 0){
				list.remove(index);
			}
		}
		
		return sb.toString();
	}
	
	
	
	private static String toStr(List<WordStat> top) {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (WordStat stat : top) {
			if( ! isFirst) sb.append(' ');
			isFirst = false;
			sb.append(stat.word).append('-').append(stat.count());
		}
		return sb.toString();
	}

	
	
	
	
	
	
	
	
	
	

	public static void main(String[] args) throws IOException {
		
		long start = System.currentTimeMillis();
		
		//File bigFile = new File("../tmp/tolst-vojna-mir.txt");
		//File bigFile = new File("../tmp/tolk-slovar.txt");
		File bigFile = new File("../tmp/access_log.txt");
		
		
		
		WordsCounter counter = new WordsCounter();
		counter.setDebug(false);
		counter.setTmpIndexDirParent(new File("../tmp"));
		counter.setRemoveIndexesFiles(false);
		//counter.setRamTopSize(10_000);
		
		List<WordStat> result = counter.getTop(new FileInputStream(bigFile), 100, 50);
		
		//print results
		for (WordStat stat : result) {
			System.out.println(stat.word + " " + stat.count());
		}
		
		System.out.println((System.currentTimeMillis() - start)/1000+" sec");
	}

}
