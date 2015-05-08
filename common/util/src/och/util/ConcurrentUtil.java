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

import java.util.Collection;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;

public class ConcurrentUtil {
	
	private static Log log = getLog(ConcurrentUtil.class);
	
	public static void getAndClearAllFutures(Collection<Future<?>> futures){
		getAndClearAllFutures(futures, true);
	}
	
	public static void getAndClearAllFutures(Collection<Future<?>> futures, boolean logOnError){
		if(isEmpty(futures)) return;
		for (Future<?> future : futures) {
			try {
				future.get();
			}catch(Exception e){
				if(logOnError) log.error("can't get future", e);
			}
		}
		futures.clear();
	}

}
