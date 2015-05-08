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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class BaseSetupAjaxFromHttpToHttpsFilter extends BaseFilter {
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	protected abstract String getOrigin(HttpServletRequest req, HttpServletResponse resp);
	
	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		
		String origin = getOrigin(req, resp);
		if(origin != null){
			resp.setHeader("Access-Control-Allow-Origin", origin);
			resp.setHeader("Access-Control-Allow-Methods", "POST");
			resp.setHeader("Access-Control-Allow-Credentials", "true");
		}
        
		chain.doFilter(req, resp);
	}

	@Override
	public void destroy() {}



}
