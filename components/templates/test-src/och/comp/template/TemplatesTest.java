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
package och.comp.template;

import static och.service.i18n.I18n.*;
import static och.util.Util.*;

import org.junit.Test;

import test.BaseTest;

public class TemplatesTest extends BaseTest {
	
	
	@Test
	public void test_get_i18n() throws Exception{
		
		String baseTmpl = "Привет, мир!";
		String ruTmpl = "RUS Привет, мир!";
		
		Templates temp = new Templates("./server-front/web/WEB-INF/templates");
		try {
			setThreadLang(null);
			assertEquals(baseTmpl, temp.fromTemplate("test.ftl", map("name", "мир")));
			
			setThreadLang("ru");
			assertEquals(ruTmpl, temp.fromTemplate("test.ftl", map("name", "мир")));
			
			setThreadLang("ru123");
			assertEquals(baseTmpl, temp.fromTemplate("test.ftl", map("name", "мир")));
			
		} finally {
			setThreadLang(null);
		}
	}

	@Test
	public void test_get() throws Exception{
		
		Templates temp = new Templates("./server-front/web/WEB-INF/templates");
		assertEquals("Привет, мир!", temp.fromTemplate("test.ftl", map("name", "мир")));
		
	}

}
