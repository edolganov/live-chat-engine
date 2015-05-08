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
package och.service.props;

import static och.util.FileUtil.*;
import static och.util.Util.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

import och.service.props.impl.FileProps;

import org.junit.Test;

import test.BaseTest;

@SuppressWarnings("rawtypes")
public class FilePropsTest extends BaseTest {
	
	
	@Test
	public void test_utf8() throws Exception {
		File propsFile = new File(TEST_DIR, "some.txt");
		writeFileUTF8(propsFile, "\nsome=привет");
		
		FileProps props = new FileProps(propsFile);
		assertEquals("привет", props.getVal("some"));
	}

	@Test
	public void test_noPropsFile() throws Exception {
		
		File propsFile = new File(TEST_DIR, "some.txt");
		assertFalse(propsFile.exists());
		
		FileProps props = new FileProps(propsFile);
		
		//no file
		assertEquals(null, props.getVal("some"));
		
		//create file
		writeFileUTF8(propsFile, "\nsome=123");
		props.updateFromFileIfNeed();
		assertEquals("123", props.getVal("some"));
		
		//remove file
		propsFile.delete();
		props.updateFromFileIfNeed();
		assertEquals(null, props.getVal("some"));
		
	}
	

	@Test
	public void test_changedEvent() throws Exception{
		
		String key1 = "key1";
		String val1 = "val1";
		String val2 = "val2";
		String val3 = "val3";
		
		File file = new File(TEST_DIR, "test.props");
		writeFileUTF8(file, "#коммент\n\n"+key1+"="+val1+"\n");
		
		int updateTime = 50;
		FileProps props = new FileProps(file, updateTime, null);
		assertEquals(val1, props.getVal(key1));
		
		Set[] keys = {null};
		String[] curVal = {null};
		int[] countCall = {0};
		props.addChangedListener((k)->{
			keys[0] = k;
			curVal[0] = props.getVal(key1);
			countCall[0]++;
		});
		
		//update by put
		{
			props.putVal(key1, val2);
			
			assertEquals(set("key1"), keys[0]);
			assertEquals(val2, curVal[0]);
		}
		
		//remove
		{
			props.removeVal("key1");
			
			assertEquals(set("key1"), keys[0]);
			assertEquals(null, curVal[0]);
		}
		
		//update by file
		{
			//some times lastModified2 == lastModified1 because write is too fast =)
			//so wait some time to prevent it
			Thread.sleep(100);
			writeFileUTF8(file, key1+"="+val3+"\nkey2=val2");

			Thread.sleep(100);
			
			assertEquals(set("key1", "key2"), keys[0]);
			assertEquals(val3, curVal[0]);
		}
		
	}
	
	
	@Test
	public void test_read_update_save() throws Exception{
		
		
		String key1 = "key1";
		String key2 = "key2";
		String key3 = "key3";
		
		String val1 = "val1";
		String val2 = "val2";
		String val3 = "val3";
		
		File file = new File(TEST_DIR, "test.props");
		writeFileUTF8(file, "#коммент\n\n"+key1+"="+val1+"\n"+key2+"="+val2);
		
		//read
		int updateTime = 50;
		FileProps props = new FileProps(file, updateTime, null);
		assertEquals(val1, props.getVal(key1, ""));
		assertEquals(val2, props.getVal(key2, ""));
		
		//update
		long lastModified1 = file.lastModified();
		//some times lastModified2 == lastModified1 because write is too fast =)
		//so wait some time to prevent it
		Thread.sleep(100);
		writeFileUTF8(file, key1+"="+val2+"\n"+key3+"="+val3);
		long lastModified2 = file.lastModified();
		assertTrue(lastModified1 != lastModified2);
		
		Thread.sleep(updateTime * 2);
		assertEquals(val2, props.getVal(key1, ""));
		assertEquals("", props.getVal(key2, ""));
		assertEquals(val3, props.getVal(key3, ""));
		
		//save
		props.putVal(key2, val1);
		assertEquals(val2, props.getVal(key1, ""));
		assertEquals(val1, props.getVal(key2, ""));
		assertEquals(val3, props.getVal(key3, ""));
		{
			Properties fromFile = new Properties();
			FileInputStream is = new FileInputStream(file);
			fromFile.load(is);
			is.close();
			assertEquals(val2, fromFile.getProperty(key1));
			assertEquals(val1, fromFile.getProperty(key2));
			assertEquals(val3, fromFile.getProperty(key3));
		}

		
	}

}
