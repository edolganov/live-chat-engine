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

import static och.util.Util.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import och.api.exception.PropertyNotFoundException;
import och.service.props.KeyWithDef;
import och.service.props.Props;
import och.service.props.PropsChangedListener;

import org.apache.commons.logging.Log;

public class MultiProps implements Props {
	
	private static Log log = getLog(MultiProps.class);
	
	private ArrayList<Props> list = new ArrayList<Props>();
	private CopyOnWriteArrayList<PropsChangedListener> listeners = new CopyOnWriteArrayList<>();
	
	public MultiProps() {}
	
	public MultiProps(Collection<? extends Props> sources){
		for (Props source : sources) add(source);
		log.info("added sources: "+sources);
	}
	
	public MultiProps(Props... sources){
		for (Props source : sources) add(source);
		log.info("added sources: "+Arrays.toString(sources));
	}
	
	
	public void addSource(Props source){
		addSource(source, list.size());
	}

	public void addSource(Props source, int index){
		add(source, index);
		log.info("added source: "+source+", index="+index);
	}
	
	private void add(Props source){
		add(source, list.size());
	}
	
	private void add(Props source, int index){
		list.add(index, source);
		source.addChangedListener((keys)->fireChangedEvent(keys));
	}
	
	public void resetSources(Props... sources){
		
		HashSet<String> keys = new HashSet<>();
		
		//remove old
		for (Props old : list) {
			keys.addAll(old.toMap().keySet());
		}
		list.clear();
		
		//add new
		for (Props source : sources) {
			addSource(source);
			keys.addAll(source.toMap().keySet());
		}
		
		fireChangedEvent(keys);
	}
	
	public int getSourceCount(){
		return list.size();
	}
	
	@Override
	public void addChangedListener(PropsChangedListener l) {
		listeners.add(l);
	}
	
	private void fireChangedEvent(Set<String> keys) {
		for (PropsChangedListener l : listeners) {
			l.onChanged(keys);
		}
	}
	
	

	@Override
	public String findVal(Object key) throws PropertyNotFoundException {
		for (Props source : list) {
			try {
				return source.findVal(key);
			} catch(PropertyNotFoundException e){}
		}
		throw new PropertyNotFoundException(key);
	}

	@Override
	public String getVal(Object key) {
		for (Props source : list) {
			String result = source.getVal(key);
			if(result != null) return result;
		}
		return null;
	}

	@Override
	public String getVal(Object key, String defaultVal) {
		for (Props source : list) {
			String result = source.getVal(key);
			if(result != null) return result;
		}
		return defaultVal;
	}

	@Override
	public Integer getVal(Object key, Integer defaultVal) {
		for (Props source : list) {
			Integer result = source.getVal(key, (Integer)null);
			if(result != null) return result;
		}
		return defaultVal;
	}

	@Override
	public Long getVal(Object key, Long defaultVal) {
		for (Props source : list) {
			Long result = source.getVal(key, (Long)null);
			if(result != null) return result;
		}
		return defaultVal;
	}

	@Override
	public Boolean getVal(Object key, Boolean defaultVal) {
		for (Props source : list) {
			Boolean result = source.getVal(key, (Boolean)null);
			if(result != null) return result;
		}
		return defaultVal;
	}

	@Override
	public Double getVal(Object key, Double defaultVal) {
		for (Props source : list) {
			Double result = source.getVal(key, (Double)null);
			if(result != null) return result;
		}
		return defaultVal;
	}

	@Override
	public BigDecimal getVal(Object key, BigDecimal defaultVal) {
		for (Props source : list) {
			BigDecimal result = source.getVal(key, (BigDecimal)null);
			if(result != null) return result;
		}
		return defaultVal;
	}

	@Override
	public Class<?> getVal(Object key, Class<?> defaultVal) {
		for (Props source : list) {
			Class<?> result = source.getVal(key, (Class<?>)null);
			if(result != null) return result;
		}
		return defaultVal;
	}

	@Override
	public Map<String, String> toMap() {
		HashMap<String, String> out = new HashMap<>();
		for (int i = list.size()-1; i > -1; i--) {
			out.putAll(list.get(i).toMap());
		}
		return out;
	}

	@Override
	public String getStrVal(KeyWithDef prop) {
		return getVal(prop, prop.strDefVal());
	}

	@Override
	public Integer getIntVal(KeyWithDef prop) {
		return getVal(prop, prop.intDefVal());
	}

	@Override
	public Long getLongVal(KeyWithDef prop) {
		return getVal(prop, prop.longDefVal());
	}

	@Override
	public Boolean getBoolVal(KeyWithDef prop) {
		return getVal(prop, prop.boolDefVal());
	}

	@Override
	public Double getDoubleVal(KeyWithDef prop) {
		return getVal(prop, prop.doubleDefVal());
	}

	@Override
	public BigDecimal getBigDecimalVal(KeyWithDef prop) {
		return getVal(prop, prop.bigDecimalDefVal());
	}

	@Override
	public String toString() {
		return "MultiProps [list=" + list + "]";
	}
	
	

}
