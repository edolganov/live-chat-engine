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
package och.comp.chats.common;

import static och.util.Util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StoreOps {
	
	public static final String ACC_DIR_NAME_PREFIX = "acc-";
	
	public static File getAccDir(File root, String accId) {
		return new File(root, getAccountDirName(accId));
	}
	
	public static String getAccountDirName(String accId) {
		return ACC_DIR_NAME_PREFIX+accId;
	}
	
	public static List<String> getAccIdsNames(File root){
		
		int prefixLength = ACC_DIR_NAME_PREFIX.length();
		ArrayList<String> out = new ArrayList<>();
		
		File[] files = root.listFiles();
		if( ! isEmpty(files)){
			for (File file : files) {
				String name = file.getName();
				if( file.isDirectory() 
						&& name.startsWith(ACC_DIR_NAME_PREFIX)
						&& name.length() > prefixLength){
					String accountId = name.substring(prefixLength);
					out.add(accountId);
				}
			}
		}
		
		return out;
		
	}

}
