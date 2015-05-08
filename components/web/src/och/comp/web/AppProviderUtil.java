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

import static och.api.model.PropKey.*;
import static och.util.Util.*;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import och.service.props.WriteProps;
import och.service.props.impl.FileProps;


public class AppProviderUtil {
	
	public static WriteProps createProps(String propsPath, String webInfPath)throws IOException {
		
		File propsFile = new File(propsPath);
		FileProps props = new FileProps(propsFile, (Properties p) -> {
	
			String httpUrlKey = httpServerUrl.name();
			String webInfKey = webInf.name();

			String serverUrl = p.getProperty(httpUrlKey);
			if( ! hasText(serverUrl)) throw new IllegalStateException("no '"+httpUrlKey+"' var in "+p);
			Properties out = new Properties();
			for(Entry<Object, Object> entry : p.entrySet()){
				String key = (String)entry.getKey();
				String val = (String)entry.getValue();
				val = val.replace("${"+webInfKey+"}", webInfPath);
				val = val.replace("${"+httpUrlKey+"}", serverUrl);
				out.put(key, val);
			}
			return out;
			
		});
		
		return props;
	}

}
