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
package och.util.model;

import java.util.AbstractList;
import java.util.LinkedList;

public class LimitedQueueList<T> extends AbstractList<T>{
	
	private int limit = Integer.MAX_VALUE;
	private LinkedList<T> list = new LinkedList<T>();
	

	public LimitedQueueList(int limit) {
		super();
		setLimit(limit);
	}

	public LimitedQueueList() {
		super();
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		if(limit < 0){
			limit = 0;
		}
		this.limit = limit;
	}
	
	@Override
	public void add(int location, T object) {
		list.add(location, object);
		fixSize();
	}
	
	private void fixSize() {
		while(list.size() > limit){
			list.removeFirst();
		}
	}

	@Override
	public T set(int location, T object) {
		return list.set(location, object);
	}
	

	@Override
	public T get(int location) {
		return list.get(location);
	}

	@Override
	public int size() {
		return list.size();
	}
	

}
