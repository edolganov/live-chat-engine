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
package och.comp.cache.impl;

import java.util.Date;

public class CacheItem {
	
	public final Object key;
	public final Object val;
	public final int liveTime;
	public final long liveUntil;
	
	public CacheItem(Object key, Object val, int liveTime) {
		super();
		this.key = key;
		this.val = val;
		this.liveTime = liveTime < 1? 0 : liveTime;
		this.liveUntil = liveTime < 1? 0 : System.currentTimeMillis() + liveTime;
	}
	
	public boolean isNeedRemove(){
		return liveUntil > 0 && liveUntil < System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return "CacheItem [key=" + key + ", val=" + val + ", liveTime="
				+ liveTime + ", liveUntil=" + (liveUntil > 0? new Date(liveUntil) : "0") + "]";
	}
	
	

}
