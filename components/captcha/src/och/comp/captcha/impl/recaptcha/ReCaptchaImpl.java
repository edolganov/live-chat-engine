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

import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

import och.comp.captcha.impl.recaptcha.http.HttpLoader;
import och.comp.captcha.impl.recaptcha.http.SimpleHttpLoader;


public class ReCaptchaImpl implements ReCaptcha {

	public static final String PROPERTY_THEME = "theme";
	public static final String PROPERTY_TABINDEX = "tabindex";
	
	public static final String HTTP_SERVER = "http://api.recaptcha.net";
	public static final String HTTPS_SERVER = "https://api-secure.recaptcha.net";
	public static final String VERIFY_URL = "http://api-verify.recaptcha.net/verify";
	
	private String privateKey;
	private String publicKey;
	private String recaptchaServer = HTTP_SERVER;
	private boolean includeNoscript = false;
	private HttpLoader httpLoader = new SimpleHttpLoader();
	
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public void setRecaptchaServer(String recaptchaServer) {
		this.recaptchaServer = recaptchaServer;
	}
	public void setIncludeNoscript(boolean includeNoscript) {
		this.includeNoscript = includeNoscript;
	}
	public void setHttpLoader(HttpLoader httpLoader) {
		this.httpLoader  = httpLoader;
	}

	@Override
	@SuppressWarnings("deprecation")
	public ReCaptchaResponse checkAnswer(String remoteAddr, String challenge, String response) throws Exception {

		String postParameters = "privatekey=" + URLEncoder.encode(privateKey) + "&remoteip=" + URLEncoder.encode(remoteAddr) +
			"&challenge=" + URLEncoder.encode(challenge, "UTF-8") + "&response=" + URLEncoder.encode(response, "UTF-8");

		String message = httpLoader.httpPost(VERIFY_URL, postParameters);

		if (message == null) {
			throw new ReCaptchaException("Null read from server.");
		}

		String[] a = message.split("\r?\n");
		if (a.length < 1) {
			throw new ReCaptchaException("No answer returned from recaptcha: "+message);
		}
		
		boolean valid = "true".equals(a[0]);
		String errorMessage = null;
		if (!valid) {
			if (a.length > 1)
				errorMessage = a[1];
			else
				errorMessage = "recaptcha4j-missing-error-message";
		}
		
		return new ReCaptchaResponse(valid, errorMessage);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String createRecaptchaHtml(String errorMessage, Properties options) {

		String errorPart = (errorMessage == null ? "" : "&amp;error=" + URLEncoder.encode(errorMessage));

		String message = fetchJSOptions(options);

		message += "<script type=\"text/javascript\" src=\"" + recaptchaServer + "/challenge?k=" + publicKey + errorPart + "\"></script>\r\n";

		if (includeNoscript) {
			String noscript = "<noscript>\r\n" + 
					"	<iframe src=\""+recaptchaServer+"/noscript?k="+publicKey + errorPart + "\" height=\"300\" width=\"500\" frameborder=\"0\"></iframe><br>\r\n" + 
					"	<textarea name=\"recaptcha_challenge_field\" rows=\"3\" cols=\"40\"></textarea>\r\n" + 
					"	<input type=\"hidden\" name=\"recaptcha_response_field\" value=\"manual_challenge\">\r\n" + 
					"</noscript>";
			message += noscript;
		}
		
		return message;
	}
	
	@Override
	public String createRecaptchaHtml(String errorMessage, String theme, Integer tabindex) {

		Properties options = new Properties();

		if (theme != null) {
			options.setProperty(PROPERTY_THEME, theme);
		}
		if (tabindex != null) {
			options.setProperty(PROPERTY_TABINDEX, String.valueOf(tabindex));
		}

		return createRecaptchaHtml(errorMessage, options);
	}

	/**
	 * Produces javascript array with the RecaptchaOptions encoded.
	 * 
	 * @param properties
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String fetchJSOptions(Properties properties) {

		if (properties == null || properties.size() == 0) {
			return "";
		}

		String jsOptions =
			"<script type=\"text/javascript\">\r\n" + 
			"var RecaptchaOptions = {";
			
		for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
			String property = (String) e.nextElement();
			
			jsOptions += property + ":'" + properties.getProperty(property)+"'";
			
			if (e.hasMoreElements()) {
				jsOptions += ",";
			}
			
		}

		jsOptions += "};\r\n</script>\r\n";

		return jsOptions;
	}
}
