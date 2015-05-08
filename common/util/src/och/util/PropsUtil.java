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
package och.util;

import static och.util.Util.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropsUtil {
	
	public static Properties loadPropsFromFile(File file) throws IOException {
		FileReader reader = new FileReader(file);
		Properties props = new Properties();
		try {
			props.load(reader);
		} finally {
			reader.close();
		}
		return props;
	}
	
	
	public static String getNotEmptyProperty(Properties props, String key){
		String out = props.getProperty(key);
		if(isEmpty(out)) throw new IllegalStateException("empty property by key "+key);
		return out;
	}
	
	public static Integer getIntProperty(Properties props, String key, Integer defaultVal){
		String str = props.getProperty(key);
		return tryParseInt(str, defaultVal);
	}

}
