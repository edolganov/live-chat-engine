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

import java.util.Properties;

public interface ReCaptcha {

	/**
	 * Creates HTML output with embedded recaptcha. The string response should be output on a HTML page (eg. inside a JSP).
	 * 
	 * @param errorMessage An errormessage to display in the captcha, null if none.
	 * @param options Options for rendering, <code>tabindex</code> and <code>theme</code> are currently supported by recaptcha. You can
	 *   put any options here though, and they will be added to the RecaptchaOptions javascript array.
	 * @return
	 */
	public String createRecaptchaHtml(String errorMessage, Properties options);

	/**
	 * Creates HTML output with embedded recaptcha. The string response should be output on a HTML page (eg. inside a JSP).
	 * 
	 * This is just a wrapper that accepts the properties known to recaptcha.net
	 * 
	 * @param errorMessage The error message to show in the recaptcha ouput
	 * @param theme The theme to use for the recaptcha output (null if default)
	 * @param tabindex The tabindex to use for the recaptcha element (null if default)
	 * @return
	 */
	public String createRecaptchaHtml(String errorMessage, String theme, Integer tabindex);
	
	/**
	 * Validates a reCaptcha challenge and response.
	 * 
	 * @param remoteAddr The address of the user, eg. request.getRemoteAddr()
	 * @param challenge The challenge from the reCaptcha form, this is usually request.getParameter("recaptcha_challenge_field") in your code.
	 * @param response The response from the reCaptcha form, this is usually request.getParameter("recaptcha_response_field") in your code.
	 * @return
	 */
	public ReCaptchaResponse checkAnswer(String remoteAddr, String challenge, String response) throws Exception; 
}
