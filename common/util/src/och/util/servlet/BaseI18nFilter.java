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
package och.util.servlet;

import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BaseI18nFilter extends BaseFilter {
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}


	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		
		String paramLang = req.getParameter("lang");
		String cookieLang = getCookieVal(req, "lang");
		
		String lang = paramLang;
		if( ! hasText(lang)) lang = cookieLang;
		if( ! hasText(lang)) lang = getDefaultLang();
		
		if( hasText(lang) && ! lang.equals(cookieLang)){
			resp.addCookie(cookieForYear("lang", lang, false));
		}
		
		req.setAttribute("lang", lang);
		
		beforeChain(req, resp, lang);
		try {
			chain.doFilter(req, resp);
		} finally {
			afterChain(req, resp, lang);
		}
		
	}

	protected String getDefaultLang() {
		return null;
	}
	
	/**
	 * @param lang - can be null
	 */
	protected void beforeChain(HttpServletRequest req, HttpServletResponse resp, String lang) {}
	
	/**
	 * @param lang - can be null
	 */
	protected void afterChain(HttpServletRequest req, HttpServletResponse resp, String lang) {}

}
