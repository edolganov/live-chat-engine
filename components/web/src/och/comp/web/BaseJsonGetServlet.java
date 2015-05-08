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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import och.api.exception.ExpectedException;
import och.api.exception.ValidationException;

public abstract class BaseJsonGetServlet<O> extends BaseJsonServlet {
	
	private static final long serialVersionUID = 1L;
	
	private boolean printError;
	
	
	public BaseJsonGetServlet() {
		this(false);
	}

	public BaseJsonGetServlet(boolean printError) {
		this.printError = printError;
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		OutputWrapper out = new OutputWrapper(resp.getOutputStream());
		try {
			doJsonGet(req, resp, out);
		}
		catch (ValidationException e) {
			out.write(jsonValidationError(e));
		}
		catch (Throwable t) {
			ExpectedException.logError(log, t, "can't invoke doGet");
			out.write(jsonUnexpectedError( ! printError? "server error" : "server error:\n "+stackTraceToString(t)));
		}
		
	}
	
	protected void doJsonGet(HttpServletRequest req, HttpServletResponse resp, OutputWrapper out) throws Throwable {
		O outData = doJsonGet(req, resp);
		if(outData == null) out.write(jsonOk());
		else out.write(jsonOk(outData));
	}
	
	protected O doJsonGet(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
		return null;
	}
	


}
