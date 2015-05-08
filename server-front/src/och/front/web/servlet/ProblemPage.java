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
package och.front.web.servlet;

import static och.service.i18n.I18n.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.front.web.SimpleFrontServlet;

@WebServlet(value="/problem")
@SuppressWarnings("serial")
public class ProblemPage extends SimpleFrontServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String errorMsgText = req.getParameter("errorMsg");
		req.setAttribute("errorMsg", errorMsgText);
		
		String en = "/WEB-INF/jsp/front/problem.jsp";
		String ru = "/WEB-INF/jsp/front/problem_ru.jsp";
		
		if(isThreadLang_EN()) forward(req, resp, en);
		else if(isThreadLang_RU()) forward(req, resp, ru);
		else forward(req, resp, en);
	}

}
