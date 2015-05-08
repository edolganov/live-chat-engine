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
import och.api.exception.ValidationException;

public abstract class BaseBean {
	
	public String getErrorState(){
		ValidationProcess errors = new ValidationProcess();
		checkState(errors);
		return errors.error;
	}

	
	protected abstract void checkState(ValidationProcess v);

	
	public static String getErrorState(BaseBean obj){
		if(obj == null) return "null object";
		return obj.getErrorState();
	}
	
	public static void validateState(BaseBean obj) throws ValidationException {
		String error = getErrorState(obj);
		if(error != null) throw new ValidationException(error);
	}
	
	public static void validateForText(String str, String obName) throws ValidationException {
		if(!hasText(str)) throw new ValidationException(obName+ " is empty");
	}
	
	public static void validateForTextSize(String str, String obName, int minSize, int maxSize) throws ValidationException {
		
		if(minSize == 0 && str != null && str.length() == 0) return;
		
		validateForText(str, obName);
		
		int length = str.length();
		if(length < minSize) throw new ValidationException(obName+ " min size: "+minSize);
		else if(length > maxSize) throw new ValidationException(obName+ " max size: "+maxSize);
		
	}

	public static void validateForEmpty(Object ob, String obName) throws ValidationException{
		if(isEmpty(ob)) throw new ValidationException(obName+ " is empty");
	}
	
	public static void validateState(boolean state, String obName) throws ValidationException {
		if(!state) throw new ValidationException(obName+ " is invalid");
	}
	

}
