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

import java.math.BigDecimal;
import java.util.Map;

import och.api.exception.PropertyNotFoundException;
import och.service.props.KeyWithDef;
import och.service.props.Props;
import och.service.props.PropsChangedListener;

public class PropsWrapper implements Props {
	
	private Props real;
	
	public PropsWrapper(Props real) {
		super();
		this.real = real;
	}
	
	@Override
	public void addChangedListener(PropsChangedListener l) {
		real.addChangedListener((keys)-> l.onChanged(keys));
	}

	@Override
	public String findVal(Object key) throws PropertyNotFoundException {
		return real.findVal(key);
	}

	@Override
	public String getVal(Object key) {
		return real.getVal(key);
	}

	@Override
	public String getVal(Object key, String defaultVal) {
		return real.getVal(key, defaultVal);
	}

	@Override
	public Integer getVal(Object key, Integer defaultVal) {
		return real.getVal(key, defaultVal);
	}

	@Override
	public Long getVal(Object key, Long defaultVal) {
		return real.getVal(key, defaultVal);
	}

	@Override
	public Boolean getVal(Object key, Boolean defaultVal) {
		return real.getVal(key, defaultVal);
	}
	
	@Override
	public Double getVal(Object key, Double defaultVal) {
		return real.getVal(key, defaultVal);
	}
	
	@Override
	public BigDecimal getVal(Object key, BigDecimal defaultVal) {
		return real.getVal(key, defaultVal);
	}

	@Override
	public Class<?> getVal(Object key, Class<?> defaultVal) {
		return real.getVal(key, defaultVal);
	}

	@Override
	public Map<String, String> toMap() {
		return real.toMap();
	}
	
	@Override
	public String getStrVal(KeyWithDef prop){
		return real.getStrVal(prop);
	}
	@Override
	public Integer getIntVal(KeyWithDef prop){
		return real.getIntVal(prop);
	}
	@Override
	public Long getLongVal(KeyWithDef prop){
		return real.getLongVal(prop);
	}
	@Override
	public Boolean getBoolVal(KeyWithDef prop){
		return real.getBoolVal(prop);
	}

	@Override
	public Double getDoubleVal(KeyWithDef prop) {
		return real.getDoubleVal(prop);
	}
	
	@Override
	public BigDecimal getBigDecimalVal(KeyWithDef prop) {
		return real.getBigDecimalVal(prop);
	}

	@Override
	public String toString() {
		return "PropsWrapper [real=" + real + "]";
	}

}
