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
package och.api.model.chat.config;

import static och.util.Util.*;

import java.math.BigDecimal;

public enum Key {
	
	name(),
	feedback_notifyOpsByEmail(true),
	
	
	
	;
	
	private final String defVal;
	
	private Key(){
		this(null);
	}
	
	private Key(Object defVal){
		this.defVal = defVal == null? null : defVal.toString();
	}
	
	public String strDefVal(){
		return defVal;
	}
	
	public Boolean boolDefVal(){
		return tryParseBool(defVal, null);
	}
	
	public Integer intDefVal(){
		return tryParseInt(defVal, null);
	}
	
	public Long longDefVal(){
		return tryParseLong(defVal, null);
	}
	
	public Double doubleVal(){
		return tryParseDouble(defVal, null);
	}
	
	public BigDecimal bigDeciamlVal(){
		return tryParseBigDecimal(defVal, null);
	}
}
