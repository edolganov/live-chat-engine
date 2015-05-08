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
package och.service.props.impl;


import static java.util.Collections.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MapProps extends BaseProps {
	
	ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
	
	public MapProps() {
	}
	
	public MapProps(Map<String, String> initVals) {
		map.putAll(initVals);
	}

	@Override
	public String getVal(Object key, String defaultVal) {
		String strKey = String.valueOf(key);
		return map.containsKey(strKey)? 
				map.get(strKey) 
				: defaultVal;
	}

	@Override
	public void putVal(Object key, String val) {
		String strKey = String.valueOf(key);
		if(val == null) map.remove(strKey);
		else map.put(strKey, val);
		
		fireChangedEvent(singleton(strKey));
	}
	
	@Override
	public Map<String, String> toMap() {
		HashMap<String, String> out = new HashMap<>(map);
		return out;
	}

}
