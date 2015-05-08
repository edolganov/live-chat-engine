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
package och.api.model;

import static och.util.Util.*;

import java.util.Collection;
import java.util.Map;

public class ValidationProcess {
	
	public static interface CustomCheck {
		String check();
	}
	
	String error;
	
	public void setError(String error){
		if( ! hasError()) this.error = error;
	}
	
	public boolean hasError(){
		return error != null;
	}
	
	public void check(CustomCheck check){
		if( hasError()) return;
		String error = check.check();
		if(error != null) setError(error);
	}
	
	
	public void checkForText(String val, String valName){
		if( hasError()) return;
		if( ! hasText(val)) setError(emptyFieldMsg(valName));
	}
	
	public void checkForSize(String val, String valName, int minSize, int maxSize){
		if( hasError()) return;
		boolean hasText = hasText(val);
		if(val == null 
			|| (minSize > 0 && !hasText) 
			|| val.length() < minSize) {
			setError(invalidFieldMsg(valName, "min size: "+minSize));
		}
		else if(hasText && val.length() > maxSize) {
			setError(invalidFieldMsg(valName, "max size: "+maxSize));
		}
	}


	
	public void checkForEmpty(Object val, String valName){
		if( hasError()) return;
		if( isEmpty(val)) setError(emptyFieldMsg(valName));
	}
	
	public void checkForEmpty(Collection<?> val, String valName){
		if( hasError()) return;
		if( isEmpty(val)) setError(emptyFieldMsg(valName));
	}
	
	public void checkForEmpty(Map<?, ?> val, String valName){
		if( hasError()) return;
		if( isEmpty(val)) setError(emptyFieldMsg(valName));
	}
	
	public void checkForValid(boolean val, String valName){
		if( hasError()) return;
		if( ! val) setError(invalidFieldMsg(valName));
	}
	
	public void checkForInvalidChars(String val, String valName, String invalidChars){
		if( hasError()) return;
		if( val == null) return; 
		int invalidCharIndex = -1;
		int length = val.length();
		for (int i = 0; i < length; i++) {
			char c = val.charAt(i);
			invalidCharIndex = invalidChars.indexOf(c);
			if(invalidCharIndex > -1) break;
		}
		if(invalidCharIndex > -1){
			char invalidChar = invalidChars.charAt(invalidCharIndex);
			setError(valName+" can't contains '"+invalidChar+"'");
		}
	}
	
	
	
	public static String invalidFieldMsg(String valName) {
		return invalidFieldMsg(valName, null);
	}
	
	public static String invalidFieldMsg(String valName, String extra) {
		return "invalid field '"+valName+"'" + (extra != null? ": "+extra : "");
	}
	
	public static String emptyFieldMsg(String valName) {
		return "empty field '"+valName+"'";
	}
	
}
