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
package och.front.web.servlet.system_api.user;

import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.front.service.SecurityService.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ValidationException;
import och.api.exception.user.InvalidCaptchaException;
import och.api.exception.user.InvalidLoginDataException;
import och.api.model.user.User;
import och.front.web.JsonPostServlet;

@WebServlet("/system-api/user/login")
@SuppressWarnings("serial")
public class LoginUser extends JsonPostServlet<LoginUserExtReq, LoginUserResp>{
	
	private static final String NEED_CAPTCHA_COOKIE_NAME = "needCaptcha";
	
	
	public LoginUser() {
		this.checkXReqHeader = false;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected LoginUserResp doJsonPost(HttpServletRequest req, HttpServletResponse resp,
			LoginUserExtReq data) throws Throwable {
		
		int loginsWithoutCaptchaCount = app.props.getIntVal(users_loginsWithoutCaptchaCount);
		int curInvalidLogins = security.getInvalidLoginsCount(req, data, loginsWithoutCaptchaCount);
		
		//if more then need - use captcha
		if(curInvalidLogins >= loginsWithoutCaptchaCount || ! isEmpty(data.captchaChallenge)){
			
			//validate captcha input data
			try {
				data.validateCaptcha = true;
				validateState(data);
			}catch (ValidationException e) {
				addNeedCaptchaCookie(resp);
				throw e;
			}
			
			String remoteAddr = req.getRemoteAddr();
			boolean isValidCaptcha = app.captcha.checkAnswer(remoteAddr, data.captchaChallenge, data.captchaResponse);
			if(!isValidCaptcha) {
				addNeedCaptchaCookie(resp);
				throw new InvalidCaptchaException();
			}
		}
		
		User user = null;
		
		try {
			user = security.createUserSession(req, resp, data);
		}
		catch (InvalidLoginDataException e) {
			
			//update invalid logins count
			curInvalidLogins = curInvalidLogins > 100? 100 : curInvalidLogins+1;
			security.setInvalidLoginsCountAsync(req, data, curInvalidLogins);
			if(curInvalidLogins == loginsWithoutCaptchaCount){
				addNeedCaptchaCookie(resp);
			}
			
			throw e;
		}
		
		//clear invalid count
		if(curInvalidLogins > 0) {
			security.setInvalidLoginsCountAsync(req, data, 0);
			resp.addCookie(deletedCookie(NEED_CAPTCHA_COOKIE_NAME));
		}
		
		set_CSRF_ProtectTokenCookieFromSession(req, resp);
		String CSRF_ProtectToken = get_CSRF_ProtectTokenFromSession(req);
		
		return new LoginUserResp(user, CSRF_ProtectToken);
		
	}

	public void addNeedCaptchaCookie(HttpServletResponse resp) {
		resp.addCookie(cookie(NEED_CAPTCHA_COOKIE_NAME, "1", false, INVALID_LOGINS_CACHE_LIVETIME_SEC));
	}

}
