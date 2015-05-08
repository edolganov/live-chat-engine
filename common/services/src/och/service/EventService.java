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
package och.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import och.api.exception.ExpectedException;

import static och.util.Util.*;

public class EventService {
	
	private Log log = getLog(getClass());
	private HashMap<Class<?>, List<EventListener<?>>> listeners = new HashMap<>();
	
	public <T> void addListener(Class<T> type, EventListener<T> listener){
		log.info("add "+listener+" for type "+type.getName());
		List<EventListener<?>> list = listeners.get(type);
		if(list == null){
			list = new ArrayList<>();
			listeners.put(type, list);
		}
		list.add(listener);
	}
	
	public void tryFireEvent(Object event){
		try {
			fireEvent(event);
		}catch (Throwable t) {
			ExpectedException.logError(log, t, "can't process "+event);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fireEvent(Object event) throws Exception {
		List<EventListener<?>> list = listeners.get(event.getClass());
		if(isEmpty(list)) return;
		for (EventListener l : list) {
			l.onEvent(event);
		}
	}

}
