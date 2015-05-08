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
package och.api.model.remtoken;

import static och.util.Util.*;
import static och.util.codec.Base64.*;
import och.util.servlet.WebUtil;

public class ClientRemToken {
	

	public final String uid;
	public final String random;

	/**
	 * New token
	 */
	public ClientRemToken() {
		this.uid = randomUUID();
		this.random = randomUUID();
	}
	
	/**
	 * Restored token
	 */
	private ClientRemToken(String uid, String random) {
		this.uid = uid;
		this.random = random;
	}
	
	public String encodeToCookie(){
		return encodeBase64String(toCookieString());
	}
	
	public byte[] getHash(String salt){
		return WebUtil.getHash(random, salt);
	}
	
	public static ClientRemToken decodeFromCookieVal(String val){
		if( ! hasText(val)) return null;
		val = decodeBase64String(val);
		int sepIndex = val.indexOf('$');
		if(sepIndex < 0 || sepIndex > val.length()-2) return null;
		
		String uid = val.substring(0, sepIndex);
		String random = val.substring(sepIndex+1);
		return new ClientRemToken(uid, random);
	}
	
	public String toCookieString() {
		return uid + "$" +random;
	}
	
	@Override
	public String toString() {
		return toCookieString();
	}



}
