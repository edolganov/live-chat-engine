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

import static och.util.concurrent.DoneFuture.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import och.comp.cache.Cache;

public class CacheImpl implements Cache {
	
	//private Log log = getLog(getClass());
	private long removeOldDeltaTime;
	
	private ConcurrentHashMap<Object, CacheItem> map = new ConcurrentHashMap<>();
	private Timer cleanDeadItemsTimer;
	
	
	public CacheImpl(long removeOldDeltaTime) {
		this.removeOldDeltaTime = removeOldDeltaTime;
	}
	
	public void run(){
		
		cleanDeadItemsTimer = new Timer("CacheImpl-cleanDeadItemsTimer", false);
		cleanDeadItemsTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				cleanDeadItems();
			}
		}, removeOldDeltaTime, removeOldDeltaTime);
		
	}

	public void shutdown(){
		cleanDeadItemsTimer.cancel();
	}
	
	
	public void putObjVal(Object key, Object val, int liveTime) {
		map.put(key, new CacheItem(key, val, liveTime));
	}
	
	public Object getObjVal(Object key) {
		CacheItem item = map.get(key);
		if(item == null) return null;
		
		if(item.isNeedRemove()){
			map.remove(key);
			return null;
		}
		
		return item.val;
	}
	
	public Object removeObjVal(Object key) {
		CacheItem item = map.remove(key);
		if(item == null) return null;
		return item.val;
	}
	
	
	public Integer getItemLivetime(Object key){
		CacheItem cacheItem = map.get(key);
		return cacheItem == null? null : cacheItem.liveTime;
	}
	
	private void cleanDeadItems(){
		
		LinkedList<Object> toDelete = new LinkedList<>();
		
		Set<Entry<Object, CacheItem>> entrySet = map.entrySet();
		for (Entry<Object, CacheItem> entry : entrySet) {
			if(entry.getValue().isNeedRemove()) toDelete.add(entry.getKey());
		}
		
		if(toDelete.size() == 0) return;
		
		//log.info("Found items to delete: "+toDelete.size());
		for (Object key : toDelete) {
			map.remove(key);
		}
	}

	@Override
	public Future<?> putCacheAsync(String key, String val) {
		putObjVal(key, val, 0);
		return EMPTY_DONE_FUTURE;
	}

	@Override
	public Future<?> putCacheAsync(String key, String val, int liveTime) {
		putObjVal(key, val, liveTime);
		return EMPTY_DONE_FUTURE;
	}

	@Override
	public void tryPutCache(String key, String val) {
		putObjVal(key, val, 0);
	}

	@Override
	public void tryPutCache(String key, String val, int liveTime) {
		putObjVal(key, val, liveTime);
	}

	@Override
	public void putCache(String key, String val) throws IOException {
		putObjVal(key, val, 0);
	}

	@Override
	public void putCache(String key, String val, int liveTime) throws IOException {
		putObjVal(key, val, liveTime);
	}

	@Override
	public String tryGetVal(String key) {
		return (String)getObjVal(key);
	}

	@Override
	public String tryGetVal(String key, String defaultVal) {
		String out = tryGetVal(key);
		if(out == null) return defaultVal;
		return out;
	}

	@Override
	public String getVal(String key) throws IOException {
		return tryGetVal(key);
	}

	@Override
	public String tryRemoveCache(String key) {
		return (String)removeObjVal(key);
	}

	@Override
	public String removeCache(String key) throws IOException {
		return tryRemoveCache(key);
	}

	@Override
	public Future<?> removeCacheAsync(String key) {
		tryRemoveCache(key);
		return EMPTY_DONE_FUTURE;
	}
}
