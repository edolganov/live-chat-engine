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
package och.util.web.rest;

import static java.util.Collections.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

public class RestUtil {
	
	
	public static List<String> getRestElems(HttpServletRequest req){
		return getRestElems(req.getPathInfo());
	}
	
	public static List<String> getRestElems(String path){
		if(isEmpty(path) || "/".equals(path)){
			return emptyList();
		}
		
		ArrayList<String> elems = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(path, "/");
		while(st.hasMoreTokens()){
			String nextToken = st.nextToken();
			if(isEmpty(nextToken)) continue;
			elems.add(nextToken);
		}
		return elems;
		
	}

}
