/*
 * Copyright 2007 Soren Davidsen, Tanesha Networks
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package och.comp.captcha.impl.recaptcha;

public class ReCaptchaResponse {

	private boolean valid;
	private String errorMessage;

	protected ReCaptchaResponse(boolean valid, String errorMessage) {
		this.valid = valid;
		this.errorMessage = errorMessage;
	}

	/**
	 * The reCaptcha error message. invalid-site-public-key invalid-site-private-key invalid-request-cookie 
	 * incorrect-captcha-sol verify-params-incorrect verify-params-incorrect recaptcha-not-reachable
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * True if captcha is "passed".
	 * @return
	 */
	public boolean isValid() {
		return valid;
	}
}
