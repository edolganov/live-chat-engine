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
package och.comp.ops;

import static och.api.model.PropKey.*;
import och.api.exception.chat.HostBlockedException;
import och.api.exception.user.AccessDeniedException;
import och.service.props.Props;

public class SecurityOps {
	
	public static void checkBlockedByIp(Props props, String ip) throws AccessDeniedException {
		
		if(props.getVal(chats_blockClientByIp+"_"+ip, false))
			throw new AccessDeniedException();
	}
	
	public static void checkBlockedHost(Props props, String host, long accsOwner, String uid) throws HostBlockedException {
		
		if(props.getVal(chats_blockByHost+"_"+host, false)){
			throw new HostBlockedException();
		}
		
		if(props.getVal(chats_blockByHost+"_"+host+"_owner_"+accsOwner, false)){
			throw new HostBlockedException();
		}
		
		if(props.getVal(chats_blockByHost+"_"+host+"_"+uid, false)){
			throw new HostBlockedException();
		}
	}

}
