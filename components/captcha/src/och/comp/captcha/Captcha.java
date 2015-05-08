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
package och.comp.captcha;

import static och.api.model.PropKey.*;
import static och.util.Util.*;
import och.comp.captcha.impl.recaptcha.ReCaptcha;
import och.comp.captcha.impl.recaptcha.ReCaptchaFactory;
import och.comp.captcha.impl.recaptcha.ReCaptchaResponse;
import och.service.props.Props;

import org.apache.commons.logging.Log;

public class Captcha {
	
	private final static Log log = getLog(Captcha.class);
	
	Props props;
	
	public Captcha(Props props) {
		this.props = props;
	}

	
	public boolean checkAnswer(String remoteAddr, String challenge, String response){
		try {
			
			if( ! props.getBoolVal(captcha_enabled)){
				return true;
			}
			
			if(isEmpty(remoteAddr) 
					|| isEmpty(challenge) 
					|| isEmpty(response)) return false;
			
			ReCaptchaResponse answer = getReCaptcha().checkAnswer(remoteAddr, challenge, response);
			return answer.isValid();
			
		}catch (Throwable t) {
			
			log.error("can't check captcha: "+ t);
			//TODO send email to admin
			
			return false;
		}
	}


	private ReCaptcha getReCaptcha() {
		String publicKey = props.findVal(captcha_publicKey);
		String privateKey = props.findVal(captcha_privateKey);
		boolean includeNoscript = false;
		return ReCaptchaFactory.newReCaptcha(publicKey, privateKey, includeNoscript);
	}

}
