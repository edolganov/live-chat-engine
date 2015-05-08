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
package och.service.props.net;

import static java.util.Collections.*;
import static och.util.Util.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GetUpdateResp {
	
	public boolean full;
	public Map<String, String> updated = emptyMap();
	public Set<String> deleted = null;
	public long delta;
	
	
	public GetUpdateResp() {
		super();
	}
	
	public GetUpdateResp(boolean full, Map<String, String> updated, long delta) {
		this.full = full;
		this.updated = updated;
		this.delta = delta;
		
		if( ! isEmpty(updated)){
			for (Entry<String, String> entry : updated.entrySet()) {
				if(entry.getValue() == null){
					if(deleted == null) deleted = new HashSet<>();
					deleted.add(entry.getKey());
				}
			}
		}
	}
	
	public Set<String> getAllKeys(){
		HashSet<String> out = new HashSet<>();
		if( ! isEmpty(updated)) out.addAll(updated.keySet());
		if( ! isEmpty(deleted)) out.addAll(deleted);
		return out;
	}
	
	public GetUpdateResp putDeletedKeysToUpdates(){
		
		if(isEmpty(deleted)) return this;
		
		if(isEmpty(updated)) updated = new HashMap<>();
		for(String key : deleted){
			updated.put(key, null);
		}
		
		return this;
	}
	

}
