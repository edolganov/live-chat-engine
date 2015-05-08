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
package och.api.model.chat.config;

import static och.util.Util.*;

import java.util.HashMap;


public class AccConfig implements Cloneable, AccConfigRead{
	
	private HashMap<String, String> map;
	
	
	public void putVal(Key key, Object val){
		if(map == null) map = new HashMap<>();
		map.put(key.name(), val == null? null : val.toString());
	}
	
	@Override
	public String getStrVal(Key key) {
		String out = getVal(key);
		return out != null? out : key.strDefVal();
	}


	@Override
	public Boolean getBoolVal(Key key) {
		return tryParseBool(getVal(key), key.boolDefVal());
	}


	@Override
	public Integer getIntVal(Key key) {
		return tryParseInt(getVal(key), key.intDefVal());
	}
	
	

	
	private String getVal(Key key) {
		return map == null? null : map.get(key.name());
	}
	
	@Override
	public AccConfig clone() {
		try {
			AccConfig out = new AccConfig();
			if(map != null) out.map = new HashMap<>(map);
			return out;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}








}
