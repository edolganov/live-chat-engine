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
package json;

import com.google.gson.Gson;


import och.junit.AssertExt;
import och.util.json.JsonUtil;

import org.junit.Test;


public class GsonAndJsonTest extends AssertExt {
	
	Gson gson = new Gson();
	
	@Test
	public void test_string_escape(){
		
		assertEqualsEscape("\\n\\n\\n", "\n\n\n");
		assertEqualsEscape("\\r\\r\\r", "\r\r\r");
		assertEqualsEscape("\\\"\\\"\\\"", "\"\"\"");
		assertEqualsEscape("\\u0000", "\0");
		assertEqualsEscape("\\u003cb/\\u003e", "<b/>");
		assertEqualsEscape("абв", "абв");
		assertEqualsEscape("عدد", "عدد");
		assertEqualsEscape("///", "///");
	}
	
	private void assertEqualsEscape(String result, String val){
		assertEquals("\""+result+"\"", gson.toJson(val));
		assertEquals(result, JsonUtil.escapeStr(val, true));
	}

}
