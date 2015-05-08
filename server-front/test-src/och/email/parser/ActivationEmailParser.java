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
package och.email.parser;

import java.net.URLDecoder;

public class ActivationEmailParser {
	
	public String email;
	public String code;
	
	public ActivationEmailParser(String text) throws Exception {
		
		String mailPattern = "?email=";
		String codePattern = "&code=";
		
		int emailIndex = text.indexOf(mailPattern);
		int codeIndex = text.indexOf(codePattern);
		int codeEndIndex = text.indexOf("'", codeIndex);
		email = URLDecoder.decode(
				text.substring(emailIndex+mailPattern.length(), codeIndex), "UTF-8");
		code = URLDecoder.decode(
				text.substring(codeIndex+codePattern.length(), codeEndIndex), "UTF-8");
	}
	
}
