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

import java.io.File;

import org.junit.Test;

import test.BaseTest;
import static och.util.FileUtil.*;

public class FileUtilTest extends BaseTest{
	
	@Test
	public void test_replace() throws Exception{
		
		String content1 = "content1";
		String content2 = "content2";
		
		File file1 = new File(TEST_DIR, "test1.txt");
		File file2 = new File(TEST_DIR, "test2.txt");
		
		replaceFileUTF8(file1, content1);
		assertEquals(content1, readFileUTF8(file1));
		
		writeFileUTF8(file2, content1);
		assertEquals(content1, readFileUTF8(file2));
		replaceFileUTF8(file2, content2);
		assertEquals(content2, readFileUTF8(file2));
		
	}

}
