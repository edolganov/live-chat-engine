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
package och.util.log;

import static och.util.Util.*;

import org.apache.commons.logging.Log;

public class LogUtil {
	
	private static final String USE_ASYNC_LOG = "useAsyncLog";

	public static Log getAsyncLogIfNeed(Log log){
		
		if( ! tryParseBool(System.getProperty(USE_ASYNC_LOG), false)){
			return log;
		}

		return AsyncLogFactory.createAsyncLog(log);
	}

}
