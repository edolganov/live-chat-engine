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

import java.io.File;

import org.junit.Test;

import test.BaseTest;

public class ZipUtilTest extends BaseTest {
	
	@Test
	public void test_zipFiles() throws Exception{
		
		File dirA = new File(TEST_DIR, "dirA");
		dirA.mkdir();
		
		File dirAA = new File(dirA, "dirAA");
		dirAA.mkdir();
		
		File fileAAA = new File(dirAA, "fileAAA.txt");
		FileUtil.writeFileUTF8(fileAAA, "fileAAA");
		
		File fileAAB = new File(dirAA, "fileAAB.txt");
		FileUtil.writeFileUTF8(fileAAB, "fileAAB");
		
		File dirAB = new File(dirA, "dirAB-empty");
		dirAB.mkdir();
		
		File fileAC = new File(dirA, "fileAC.json");
		FileUtil.writeFileUTF8(fileAC, "fileAC");
		
		File fileB = new File(TEST_DIR, "fileB.txt");
		FileUtil.writeFileUTF8(fileB, "fileB");
		
		//zip
		File zipFile = new File(TEST_DIR, "test.zip");
		ZipUtil.zipFiles(list(dirA, fileB), zipFile);
		
		//unzip
		File unzipDir = new File(TEST_DIR, "unzipped");
		ZipUtil.unzip(zipFile, unzipDir);
		
		assertTrue(new File(path(unzipDir, dirA, dirAA, fileAAA)).exists());
		assertTrue(new File(path(unzipDir, dirA, dirAA, fileAAB)).exists());
		assertTrue(new File(path(unzipDir, dirA, dirAB)).exists());
		assertTrue(new File(path(unzipDir, dirA, fileAC)).exists());
		assertTrue(new File(path(unzipDir, fileB)).exists());
		
	}


}
