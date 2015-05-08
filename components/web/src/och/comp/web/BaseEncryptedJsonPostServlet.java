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

import javax.servlet.http.HttpServletResponse;

import och.api.model.BaseBean;

public abstract class BaseEncryptedJsonPostServlet<I extends BaseBean, O> extends BaseJsonPostServlet<I, O>  {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public OutputWrapper createOutputWrapper(HttpServletResponse resp) throws IOException {
		
		return new OutputWrapper(resp.getOutputStream(), new OutputWrapper.Converter() {
			@Override
			public String convert(String out) {
				String key = getEncryptedKey();
				return isEmpty(key) ? out : encodeToken(out, key);
			}
		});
	}

	@Override
	protected I convertReqDataToObject(String encoded) {
		
		if(encoded == null) return null;
		
		String key = getEncryptedKey();
		String decoded = isEmpty(key) ? encoded : decodeToken(encoded, key);
		return super.convertReqDataToObject(decoded);
	}
	
	protected abstract String getEncryptedKey();

	
}
