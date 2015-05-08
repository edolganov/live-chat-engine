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


import static och.comp.web.JsonOps.*;
import static och.util.ExceptionUtil.*;
import static och.util.Util.*;
import static och.util.json.GsonUtil.*;
import static och.util.servlet.WebUtil.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ExpectedException;
import och.api.exception.ValidationException;
import och.api.model.BaseBean;
import och.util.ReflectionsUtil;
import och.util.sql.ConcurrentUpdateSqlException;



public abstract class BaseJsonPostServlet<I extends BaseBean, O> extends BaseJsonServlet {
	
	private static final long serialVersionUID = 1L;
	

	/** protect post-ajax from CSRF with form-post */
	protected boolean checkXReqHeader = true;
	protected boolean checkCSRFToken = true;
	protected boolean checkInputDataForEmpty = true;
	protected boolean printError = false;
	
	private Class<I> respType;
	
	public BaseJsonPostServlet() {
		respType = ReflectionsUtil.getFirstActualArgType(getClass());
		if(respType.equals(Object.class)) throw new IllegalStateException("can't get actual input type for "+getClass());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		OutputWrapper out = createOutputWrapper(resp);
		
		if(checkXReqHeader){
			if( ! hasXReqHeader(req)){
				out.write(jsonValidationError("Expected only Ajax requests from the original site"));
				return;
			}
		}
		if(checkCSRFToken){
			if( ! isValid_CSRF_ProtectTokenInReq(req)){
				out.write(jsonValidationError("Invalid req token"));
				return;
			}
		}
		

		String data = req.getParameter("data");
		boolean isEmptyData = ! hasText(data) || "null".equals(data);
		if( isEmptyData && checkInputDataForEmpty){
			out.write(jsonValidationError("Empty json data"));
			return;
		}
		
		I dataObj = null;
		try {
			dataObj = isEmptyData? null : convertReqDataToObject(data);
		}catch (Throwable t) {
			out.write(jsonValidationError("Can't parse json data"));
			return;
		}
		
		if(dataObj != null){
			String errorMsg = dataObj.getErrorState();
			if(!isEmpty(errorMsg)){
				out.write(jsonValidationError(errorMsg));
				return;
			}
		}
		
		try {
			doJsonPost(req, resp, dataObj, out);
		}
		catch (ValidationException | ConcurrentUpdateSqlException e) {
			out.write(jsonValidationError(e));
			return;
		}
		catch (Throwable t) {
			ExpectedException.logError(log, t, "can't invoke doPost");
			out.write(jsonUnexpectedError( ! printError? "Server error" : "Server error:\n "+stackTraceToString(t)));
			return;
		}
	}

	public OutputWrapper createOutputWrapper(HttpServletResponse resp) throws IOException {
		return new OutputWrapper(resp.getOutputStream());
	}
	
	protected I convertReqDataToObject(String data){
		return data != null? defaultGson.fromJson(data, respType) : null;
	}
	
	
	protected void doJsonPost(HttpServletRequest req, HttpServletResponse resp, I data, OutputWrapper out) throws Throwable {
		O outData = doJsonPost(req, resp, data);
		if(outData == null) out.write(jsonOk());
		else out.write(jsonOk(outData));
	}
	
	protected O doJsonPost(HttpServletRequest req, HttpServletResponse resp, I data) throws Throwable {
		return null;
	}
	

	

	

}
