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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapUtil {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set getUpdatedKeys(Map old, Map cur) {
		
		HashSet updatedKeys = new HashSet();
		
		for (Object e : old.entrySet()) {
			
			Map.Entry oldEntry = (Map.Entry) e;
			
			Object oldKey = oldEntry.getKey();
			if( ! cur.containsKey(oldKey)){
				updatedKeys.add(oldKey);
				continue;
			}
			
			Object oldVal = oldEntry.getValue();
			Object newVal = cur.get(oldKey);
			if(oldVal == null && newVal != null){
				updatedKeys.add(oldKey);
				continue;
			}
			if( ! oldVal.equals(newVal)){
				updatedKeys.add(oldKey);
				continue;
			}
			
		}
		for (Object e : cur.entrySet()) {
			
			Map.Entry newEntry = (Map.Entry) e;
			
			Object newKey = newEntry.getKey();
			if( ! old.containsKey(newKey)){
				updatedKeys.add(newKey);
				continue;
			}
		}
		return updatedKeys;
	}

}
