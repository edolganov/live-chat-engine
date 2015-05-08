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
package heap;

import java.net.MalformedURLException;
import java.net.URL;

import och.junit.AssertExt;

import org.junit.Test;

public class URLTest extends AssertExt {
	
	@Test
	public void test_host() throws Exception{
		
		try {
			new URL("ya.ru");
			fail_exception_expected();
		}catch(MalformedURLException e){
			//ok
		}
		
		assertEquals("ya.ru", new URL("http://ya.ru").getHost());
		assertEquals("1.ru", new URL("http://1.ru").getHost());
		assertEquals("1.ru", new URL("https://1.ru").getHost());
		
	}

}
