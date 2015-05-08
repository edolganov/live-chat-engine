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
package och.api.remote.front;

import static och.api.remote.front.ReloadChatsModelType.*;
import static och.util.Util.*;
import och.api.model.BaseBean;
import och.api.model.ValidationProcess;

public class ReloadChatsModelReq extends BaseBean {
	
	public String reqAppId;
	public ReloadChatsModelType type;
	public String param1;
	public String param2;
	
	
	public ReloadChatsModelReq() {
	}
	
	public ReloadChatsModelReq(String reqAppId, ReloadChatsModelType type) {
		this(reqAppId, type, null, null);
	}
	
	public ReloadChatsModelReq(String reqAppId, ReloadChatsModelType type, Object param1) {
		this(reqAppId, type, param1, null);
	}
	
	public ReloadChatsModelReq(String reqAppId, ReloadChatsModelType type, Object param1, Object param2) {
		this.reqAppId = reqAppId;
		this.type = type;
		this.param1 = param1 == null? null : param1.toString();
		this.param2 = param2 == null? null : param2.toString();
	}
	
	public ReloadChatsModelType type(){
		return type != null? type : FULL_MODEL_UPDATED;
	}
	
	public Long getLongParam1(){
		return tryParseLong(param1, null);
	}
	
	public Long getLongParam2(){
		return tryParseLong(param2, null);
	}
	
	


	@Override
	protected void checkState(ValidationProcess v) {
		v.checkForText(reqAppId, "reqAppId");
		v.checkForEmpty(type, "type");
	}
	

}
