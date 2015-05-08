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
package och.front.service.chat;

import static och.util.Util.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HostsStat {
	
	private boolean canUpdate = true;
	private Map<String, Set<Long>> hostsWithOwners = new HashMap<>();
	private Map<String, Set<Long>> hostsWithAccs = new HashMap<>();
	
	public synchronized void putStat(String host, long accId, long accOwnerId){
		if( ! canUpdate) return;
		putToSetMap(hostsWithAccs, host, accId);
		putToSetMap(hostsWithOwners, host, accOwnerId);
	}
	
	public synchronized boolean isEmpty(){
		return hostsWithOwners.isEmpty();
	}
	
	public synchronized void stopUpdating(){
		canUpdate = false;
	}

	public Map<String, Set<Long>> getHostsWithOwners(){
		return hostsWithOwners;
	}
	
	public Map<String, Set<Long>> getHostsWithAccs(){
		return hostsWithAccs;
	}
}
