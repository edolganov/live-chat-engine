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
package och.util.servlet;

import static och.util.servlet.WebUtil.*;
import och.junit.AssertExt;

import org.junit.Test;

public class WebUtilTest extends AssertExt {
	
	
	@Test
	public void test_md5Hash(){
		//http://www.miraclesalad.com/webtools/md5.php
		assertEquals("07e1c0d9533b5168e18a99f4540448af", md5HashStr("wwq123456"));
		
	}
	
	@Test
	public void test_isIp6SimpleCheck(){
		
		//ip4
		assertFalse(isIp6_SimpleCheck("127.0.0.1"));
		assertFalse(isIp6_SimpleCheck("151.38.39.114"));
		
		//ip6
		assertTrue(isIp6_SimpleCheck("::127.0.0.1"));
		assertTrue(isIp6_SimpleCheck("::151.38.39.114"));
		assertTrue(isIp6_SimpleCheck("::ffff:151.38.39.114"));
		assertTrue(isIp6_SimpleCheck("2001:4860:0:1001::68"));

		
	}

}
