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
package och.comp.web;

import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ExpectedException;
import och.api.exception.user.AccessDeniedException;
import och.api.exception.web.Show404PageExcepion;

import org.apache.commons.logging.Log;

public abstract class BaseErrorsServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
    public static final String DEFAULT_ERROR_JSP_PATH = "/WEB-INF/jsp/error/error.jsp";
    
    
	public static final String ERROR_EXCEPTION = "javax.servlet.error.exception";
    public static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    
    Log log = getLog(getClass());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try {
			
			Integer statusCode = (Integer) req.getAttribute(ERROR_STATUS_CODE);
	        if (statusCode != null) {
	        	setStatus(req, resp, statusCode);
	        }
			
            Throwable t = (Throwable) req.getAttribute(ERROR_EXCEPTION);
            if(t != null){
                processException(t, req, resp);
                ExpectedException.logError(log, t, "error while doing request");
            }
            
        } catch (Throwable e) {
            if (log.isErrorEnabled()) log.error("Secondary exception", e);
        }
		
		
		String forwardPath = getForwardPath();
		try {
			forward(req, resp, forwardPath);
		}catch (Exception e) {
			if (log.isErrorEnabled()) log.error("Can't show errors by path: "+forwardPath, e);
		}
		
	}
	
	
	protected String getForwardPath(){
		return DEFAULT_ERROR_JSP_PATH;
	}
	
	
	
	private void processException(Throwable t, HttpServletRequest req, HttpServletResponse resp) {
    	
    	if(t instanceof Show404PageExcepion){
        	set404Status(req, resp);
        	return;
    	}
    	
    	
    	// Подменяет HTTP статус на 403 - доступ запрещен
        if(t instanceof AccessDeniedException) {
        	set403Status(req, resp);
        	return;
        }
        
        
    }
	
	public static void set404Status(HttpServletRequest req, HttpServletResponse resp) {
		setStatus(req, resp, HttpServletResponse.SC_NOT_FOUND);
	}
	
	public static void set403Status(HttpServletRequest req, HttpServletResponse resp) {
		setStatus(req, resp, HttpServletResponse.SC_FORBIDDEN);
	}
	
	public static void set503Status(HttpServletRequest req, HttpServletResponse resp) {
		setStatus(req, resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
	}
	
	public static void setStatus(HttpServletRequest req, HttpServletResponse resp, int code){
		req.setAttribute("statusCode", code);
		resp.setStatus(code);
	}

}
