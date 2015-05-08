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


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import och.util.codec.Hex;


import static och.util.ArrayUtil.*;
import static och.util.StringUtil.*;

public class AES128 {
	
	public static final int AES128_KEY_LENGTH = 16;
	//help from: http://habrahabr.ru/post/113012/
    private static final String CIPHER = "AES/ECB/PKCS5Padding";
    private static final String KEY_ALG = "AES";
    
    
    public static byte[] encode(String input, String key) {
    	return encode(getBytesUTF8(input), getBytesUTF8(key));
    }
    
    public static String decode(byte[] encoded, String key) {
    	return getStrUTF8(decode(encoded, getBytesUTF8(key)));
    }
    
    public static byte[] encode(byte[] input, byte[] key) {
    	key = getNormalKey(key);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, KEY_ALG));
            byte[] hexed = new Hex().encode(input);
			byte[] encoded = cipher.doFinal(hexed);
			return encoded;
        } catch (Exception e) {
            throw new IllegalStateException("can't encode", e);
        }
    }
    
    public static byte[] decode(byte[] encoded, byte[] key) {
    	key = getNormalKey(key);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALG));
            byte[] decoded = cipher.doFinal(encoded);
            byte[] unhexed = new Hex().decode(decoded);
			return unhexed;
        } catch (Exception e) {
            throw new IllegalStateException("can't decode", e);
        }
    }
    
    
    
    
    public static byte[] getNormalKey(byte[] key) {
    	int length = key.length;
		if(length < AES128_KEY_LENGTH) return copyFromSmallToBig(key, new byte[AES128_KEY_LENGTH], 0);
		if(length == AES128_KEY_LENGTH) return key;
		throw new IllegalArgumentException("key length must be "+AES128_KEY_LENGTH+" bytes");
	}

}
