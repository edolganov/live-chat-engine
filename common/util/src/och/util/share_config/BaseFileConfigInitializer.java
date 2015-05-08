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
package och.util.share_config;

import java.io.File;

import org.apache.commons.logging.Log;

import static och.util.FileUtil.*;
import static och.util.Util.*;
import static och.util.exception.BaseExpectedException.*;



public abstract class BaseFileConfigInitializer {
	
	protected Log log = getLog(getClass());
	protected String filePath;

	public BaseFileConfigInitializer(String filePath) {
		this.filePath = filePath;
	}

	public void reinitFromFile() {
		try {
			
			if(filePath == null) return;
			
			File file = new File(filePath);
			if( ! file.exists() || ! file.isFile()){
				log.info("no file by path: "+filePath);
				return;
			}
			
			long lastModified = file.lastModified();
			String content = readFileUTF8(file);
			setContent(lastModified, content);
			log.info("reinited file: "+filePath);
			
		}catch (Throwable t) {
			logError(log, t, "can't reinitFromFile");
		}
	}
	
	protected abstract void setContent(long lastModified, String content) throws Exception;

}
