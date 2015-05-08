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
package och.util.crypto;

import java.security.SecureRandom;


import och.junit.AssertExt;

import org.junit.Test;

import static och.util.StringUtil.*;
import static och.util.crypto.AES128.*;

public class AES128Test extends AssertExt {
	
	@Test
	public void test_code_encode(){
		
		String input = "my str";
		byte[] inputBytes = getBytesUTF8(input);
		
		//<128bit key generation
		{
			byte[] key = new byte[5];
			new SecureRandom().nextBytes(key);
			byte[] encoded = encode(inputBytes, key);
			String decoded = getStrUTF8(decode(encoded, key));
			assertEquals(input, decoded);
			assertEquals(16, encoded.length);
		}
		
		//128bit key generation
		{
			byte[] key = new byte[16];
			new SecureRandom().nextBytes(key);
			byte[] encoded = encode(inputBytes, key);
			String decoded = getStrUTF8(decode(encoded, key));
			assertEquals(input, decoded);
			assertEquals(16, encoded.length);
		}
		
		//>128bit key generation
		{
			byte[] key = new byte[17];
			new SecureRandom().nextBytes(key);
			try {
				encode(inputBytes, key);
				fail_exception_expected();
			}catch (IllegalArgumentException e) {
				//ok
			}
		}
	}

}
