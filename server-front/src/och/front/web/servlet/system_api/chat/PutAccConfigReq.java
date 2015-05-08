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
package och.front.web.servlet.system_api.chat;

import static och.api.model.chat.account.ChatAccount.*;
import static och.api.model.chat.config.Key.*;
import static och.util.Util.*;
import och.api.model.BaseBean;
import och.api.model.ValidationProcess;
import och.api.model.chat.config.Key;

public class PutAccConfigReq extends BaseBean {
	
	public String accId;
	public String key;
	public String val;
	
	public Key keyVal;

	@Override
	protected void checkState(ValidationProcess v) {
		
		v.checkForText(accId, "accId");
		
		keyVal = key();
		v.checkForEmpty(keyVal, "key");
		
		
		//val validation
		if(keyVal == name){
			v.checkForSize(val, "name", 1, MAX_NAME_SIZE);			
		}
		
		
	}
	
	
	public Key key(){
		return tryParseEnum(key, Key.class);
	}
	
	

}
