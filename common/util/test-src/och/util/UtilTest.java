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


import static och.util.Util.*;

import java.util.List;

import och.junit.AssertExt;
import och.util.Util.DuplicateProvider;

import org.junit.Test;


public class UtilTest extends AssertExt {
	
	@Test
	public void test_randomUUID(){
		
		String str = String.valueOf(Long.MAX_VALUE);
		assertEquals(19, str.length());
		
		assertEquals(36, Util.randomUUID().length());
	}
	
	
	
	@Test
	public void test_filterByBestFromDuplicates(){
		
		{
			List<String> res = filterByBestFromDuplicates(list("a", "a", "b", "A", "c", "B"), new DuplicateProvider<String>() {
	
				@Override
				public boolean isDuplicates(String a, String b) {
					return a.equalsIgnoreCase(b);
				}
	
				@Override
				public int findBestFrom(List<String> duplicates) {
					for (int i = 0; i < duplicates.size(); i++) {
						String s = duplicates.get(i);
						if(Character.isUpperCase(s.charAt(0))){
							return i;
						}
					}
					return 0;
				}
			});
			assertEquals(list("A", "B", "c"), res);
		}
		
		
		{
			List<String> res = filterByBestFromDuplicates(list("a", "a", "b", "A", "c", "B"), new DuplicateProvider<String>() {
	
				@Override
				public boolean isDuplicates(String a, String b) {
					return a.equalsIgnoreCase(b);
				}
	
				@Override
				public int findBestFrom(List<String> duplicates) {
					return -1;
				}
			});
			assertEquals(list("c"), res);
		}
		
		{
			List<String> res = filterByBestFromDuplicates(list("a", "b", "c"), new DuplicateProvider<String>() {
	
				@Override
				public boolean isDuplicates(String a, String b) {
					return a.equalsIgnoreCase(b);
				}
	
				@Override
				public int findBestFrom(List<String> duplicates) {
					return -1;
				}
			});
			assertEquals(list("a", "b", "c"), res);
		}
		
	}

}
