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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.user.InvalidCaptchaException;
import och.front.web.JsonPostServlet;

@WebServlet("/system-api/user/add")
@SuppressWarnings("serial")
public class AddUser extends JsonPostServlet<AddUserReq, Void> {
	
	public AddUser() {
		this.checkXReqHeader = false;
	}
	
	@Override
	protected Void doJsonPost(HttpServletRequest req, HttpServletResponse resp, 
			AddUserReq data) throws Throwable {
		
		String remoteAddr = req.getRemoteAddr();
		
		boolean isValidCaptcha = app.captcha.checkAnswer(remoteAddr, data.captchaChallenge, data.captchaResponse);
		if(!isValidCaptcha) throw new InvalidCaptchaException();
		
		app.users.createUser(data.getUser(), data.psw);
		return null;
	}

}
