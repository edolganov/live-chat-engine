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
package och.front.web.filter;

import static och.api.model.PropKey.*;

import java.io.File;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.front.service.FrontApp;
import och.front.web.FrontAppProvider;
import och.service.i18n.I18n;
import och.util.servlet.BaseI18nFilter;

@WebFilter(
		urlPatterns={
			"/index",
			"/index/*",
			"/enter",
			"/enter/*",
			"/cabinet", 
			"/cabinet/*",
			"/docs", 
			"/docs/*",
			"/prices", 
			"/prices/*",
			"/contacts",
			"/contacts/*",
			"/problem",
			"/problem/*",
			"/demo-look",
			"/demo-look/*",
			"/emailAds",
			"/emailAds/*",
			}
)
public class I18nFilter extends BaseI18nFilter {
	
	I18n i18n;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		
		FrontApp app = FrontAppProvider.get(filterConfig.getServletContext());
		File dir = new File(app.props.getStrVal(i18n_dirPath));
		i18n = new I18n(dir, "labels");
	}
	
	@Override
	protected String getDefaultLang() {
		return "en";
	}
	
	@Override
	protected void beforeChain(HttpServletRequest req, HttpServletResponse resp, String lang) {
		I18n.setThreadLang(lang);
		req.setAttribute("i18n", i18n);
	}
	
	@Override
	protected void afterChain(HttpServletRequest req, HttpServletResponse resp, String lang) {
		I18n.releaseThreadLang();
	}

}
