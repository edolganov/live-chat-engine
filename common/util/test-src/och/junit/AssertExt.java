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
package och.junit;


import static och.util.ExceptionUtil.*;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import och.util.ExceptionUtil;
import och.util.StreamUtil;

import org.junit.Assert;

public class AssertExt extends Assert {
	
	public static void fail_TODO(){
		fail("TODO");
	}
	
	public static void fail_exception_expected(){
		fail("Exception expected");
	}
	
	public static void assertExceptionWithText(Throwable t, String... texts){
		assertTrue(t.toString(), containsAnyTextInMessage(t, texts));
	}
	
	public static void assertFileExists(String path){
		assertTrue("assertFileExists:"+path, new File(path).exists());
	}
	
	public static void assertFileNotExists(String path){
		assertTrue("assertFileNotExists:"+path, ! new File(path).exists());
	}
	
	public static void assertEquals(String expected, String actual){
		assertEquals((Object)expected, (Object)actual);
	}
	
	public static void assertEquals(String expected, InputStream in){
		try {
			String actual = in == null? null : StreamUtil.streamToStr(in);
			assertEquals(expected, actual);
		}catch (Exception e) {
			throw ExceptionUtil.getRuntimeExceptionOrThrowError(e);
		}
	}
	
	public static void assertEquals(byte[] expected, byte[] actual){
		assertTrue("arrays are not equals", Arrays.equals(expected, actual));
	}
	

}
