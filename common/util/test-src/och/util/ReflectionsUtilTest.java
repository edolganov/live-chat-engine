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

import static och.util.ReflectionsUtil.*;

import och.junit.AssertExt;

import org.junit.Test;


public class ReflectionsUtilTest extends AssertExt {
	
	@Test
	public void test_getFirstActualArgType(){
		
		assertEquals(String.class, getFirstActualArgType(RealB.class));
		assertEquals(String.class, getFirstActualArgType(RealD.class));
	}
	
	public static class BaseA<I> {}
	
	//one level
	public static class RealB extends BaseA<String> {}
	
	public static class BaseC<I> extends BaseA<I> {}
	
	//two levels
	public static class RealD extends BaseC<String> {}

}
