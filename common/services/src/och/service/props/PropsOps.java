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

import static och.util.Util.*;
import och.api.exception.ValidationException;
import och.util.model.SecureKeyHolder;

import org.apache.commons.logging.Log;

public class PropsOps {
	
	
	private static Log log = getLog(PropsOps.class);
	
	
	public static void addUpdateSecureKeyListener(Props keyProps, SecureKeyHolder keyHolder, KeyWithDef keyProp){
		keyProps.addChangedListener((keys)->{
			if(keys.contains(keyProp.toString())){
				String newKeyVal = keyProps.getStrVal(keyProp);
				keyHolder.setSecureKey(newKeyVal);
			}
		});
	}
	
	public static void checkMaxPropsVal(Props props, KeyWithDef key, String customSufix, int curVal, 
			Class<? extends ValidationException> exceptionType){
		
		int max = props.getVal(key+"_"+customSufix, -1);
		if(max < 0) max = props.getIntVal(key);
		
		if(curVal >= max){
			ValidationException ex = null;
			try {
				ex = exceptionType.newInstance();
			}catch(Exception e){
				log.error("can't create intance of "+exceptionType+": "+e);
				ex = new ValidationException("max value of "+key);
			}
			throw ex;
		}
			
	}

}
