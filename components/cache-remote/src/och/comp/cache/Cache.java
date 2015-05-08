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
package och.comp.cache;

import java.io.IOException;
import java.util.concurrent.Future;

public interface Cache {
	
	Future<?> putCacheAsync(String key, String val);
	
	Future<?> putCacheAsync(String key, String val, int liveTime);
	
	void tryPutCache(String key, String val);
	
	void tryPutCache(String key, String val, int liveTime);
	
	void putCache(String key, String val) throws IOException;
	
	void putCache(String key, String val, int liveTime) throws IOException;
	
	String tryGetVal(String key);
	
	String tryGetVal(String key, String defaultVal);
	
	String getVal(String key) throws IOException;
	
	String tryRemoveCache(String key);
	
	String removeCache(String key) throws IOException;
	
	Future<?> removeCacheAsync(String key);

}
