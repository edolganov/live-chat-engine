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
package och.service.props;

import java.math.BigDecimal;
import java.util.Map;

import och.api.exception.PropertyNotFoundException;

public interface Props {
	
	String findVal(Object key) throws PropertyNotFoundException;
	
	String getVal(Object key);
	
	String getVal(Object key, String defaultVal);

	Integer getVal(Object key, Integer defaultVal);
	
	Long getVal(Object key, Long defaultVal);
	
	Boolean getVal(Object key, Boolean defaultVal);
	
	Double getVal(Object key, Double defaultVal);
	
	BigDecimal getVal(Object key, BigDecimal defaultVal);
	
	Class<?> getVal(Object key, Class<?> defaultVal);
	
	Map<String, String> toMap();
	
	void addChangedListener(PropsChangedListener l);
	
	
	//Key with defaults api
	String getStrVal(KeyWithDef key);
	
	Integer getIntVal(KeyWithDef key);
	
	Long getLongVal(KeyWithDef key);
	
	Boolean getBoolVal(KeyWithDef key);
	
	Double getDoubleVal(KeyWithDef key);
	
	BigDecimal getBigDecimalVal(KeyWithDef key);

}
