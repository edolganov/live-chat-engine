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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import och.api.exception.PropertyNotFoundException;
import och.service.props.KeyWithDef;
import och.service.props.PropsChangedListener;
import och.service.props.WriteProps;

import org.apache.commons.logging.Log;

public abstract class BaseProps implements WriteProps {
	
	protected Log log = getLog(getClass());
	
	private CopyOnWriteArrayList<PropsChangedListener> listeners = new CopyOnWriteArrayList<>();
	
	@Override
	public void addChangedListener(PropsChangedListener l) {
		listeners.add(l);
	}
	
	protected void fireChangedEvent(Set<String> keys){
		for (PropsChangedListener l : listeners) {
			l.onChanged(keys);
		}
	}
	
	@Override
	public String getVal(Object key) {
		return getVal(key, (String)null);
	}
	
	@Override
	public String findVal(Object key) {
		String out = getVal(key, (String)null);
		if(out == null) throw new PropertyNotFoundException(key);
		return out;
	}
	
	
	@Override
	public Integer getVal(Object key, Integer defaultVal) {
		return tryParseInt(getVal(key), defaultVal);
	}

	@Override
	public Long getVal(Object key, Long defaultVal) {
		return tryParseLong(getVal(key), defaultVal);
	}

	@Override
	public Boolean getVal(Object key, Boolean defaultVal) {
		return tryParseBool(getVal(key), defaultVal);
	}
	
	@Override
	public Double getVal(Object key, Double defaultVal) {
		return tryParseDouble(getVal(key), defaultVal);
	}
	
	@Override
	public BigDecimal getVal(Object key, BigDecimal defaultVal) {
		return tryParseBigDecimal(getVal(key), defaultVal);
	}
	
	@Override
	public Class<?> getVal(Object key, Class<?> defaultVal) {
		String className = getVal(key);
		if(className == null) return defaultVal;
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			log.warn("ClassNotFoundException for key: "+key);
			return defaultVal;
		}
	}
	
	
	@Override
	public void putVal(Object key, Integer val) {
		putVal(key, val == null? (String) null : val.toString());
	}

	@Override
	public void putVal(Object key, Long val) {
		putVal(key, val == null? (String) null : val.toString());
	}

	@Override
	public void putVal(Object key, Boolean val) {
		putVal(key, val == null? (String) null : val.toString());
	}
	
	@Override
	public void putVal(Object key, Class<?> val) {
		putVal(key, val == null? null : val.getName());
	}
	
	@Override
	public void removeVal(Object key) {
		putVal(key, (String)null);
	}
	
	@Override
	public String getStrVal(KeyWithDef key){
		return getVal(key, key.strDefVal());
	}
	@Override
	public Integer getIntVal(KeyWithDef key){
		return getVal(key, key.intDefVal());
	}
	@Override
	public Long getLongVal(KeyWithDef key){
		return getVal(key, key.longDefVal());
	}
	@Override
	public Boolean getBoolVal(KeyWithDef key){
		return getVal(key, key.boolDefVal());
	}
	
	@Override
	public Double getDoubleVal(KeyWithDef key) {
		return getVal(key, key.doubleDefVal());
	}
	
	@Override
	public BigDecimal getBigDecimalVal(KeyWithDef key) {
		return getVal(key, key.bigDecimalDefVal());
	}
}
