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
package test;



import java.io.File;

import och.junit.AssertExt;
import och.util.FileUtil;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestName;

import web.MockHttpServletRequest;
import web.MockHttpServletResponse;


@Ignore
public abstract class BaseTest extends AssertExt {
	
	public static final String ROOT_PATH = "./test-out";
	public static final File ROOT_DIR = new File(ROOT_PATH);
	
	static {
		FileUtil.deleteDirRecursive(ROOT_DIR);
		ROOT_DIR.mkdirs();
	}
	
	public String TEST_PATH;
	public File TEST_DIR;
	protected boolean createDir = true;
	
	@Rule
	public TestName name = new TestName();
	
	@Before
	public void createDir(){
		TEST_PATH = ROOT_PATH + "/"+getClass().getSimpleName()+"@"+name.getMethodName()+"__"+System.currentTimeMillis();
		if(createDir){
			TEST_DIR = new File(TEST_PATH);
			TEST_DIR.mkdir();
		}
	}
	
	public String testPath(String path){
		return TEST_PATH+path;
	}
	
	public void assertTestFileExists(String path){
		if( ! path.startsWith("/")) path = "/" + path;
		assertFileExists(testPath(path));
	}
	
	public void assertTestFileNotExists(String path){
		assertFileNotExists(testPath(path));
	}
	
	
	public static MockHttpServletRequest mockReq(){
		return new MockHttpServletRequest();
	}
	
	public static MockHttpServletResponse mockResp(){
		return new MockHttpServletResponse();
	}
	
	
	public static String path(File parent, File... pathElems){
		StringBuilder sb = new StringBuilder();
		sb.append(parent.getPath());
		for(File child : pathElems) {
			sb.append("/").append(child.getName());
		}
		return sb.toString();
	}

}
