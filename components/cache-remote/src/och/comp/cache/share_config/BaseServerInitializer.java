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
package och.comp.cache.share_config;

import och.comp.cache.server.CacheSever;
import och.util.share_config.BaseFileConfigInitializer;

public class BaseServerInitializer extends BaseFileConfigInitializer {

	CacheSever cacheSever;
	String lastModifiedKey; 
	String contentKey;

	public BaseServerInitializer(CacheSever cacheSever, String filePath, String lastModifiedKey, String contentKey) {
		super(filePath);
		this.cacheSever = cacheSever;
		this.lastModifiedKey = lastModifiedKey;
		this.contentKey = contentKey;
	}

	@Override
	protected void setContent(long lastModified, String content) throws Exception {
		cacheSever.putVal(lastModifiedKey, Long.toString(lastModified), 0);
		cacheSever.putVal(contentKey, content, 0);
	}

}
