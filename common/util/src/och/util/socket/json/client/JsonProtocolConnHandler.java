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
package och.util.socket.json.client;


import static och.util.Util.*;
import static och.util.json.GsonUtil.*;
import static och.util.model.SecureProvider.*;
import static och.util.socket.json.server.JsonProtocolSocketHandler.*;

import java.io.IOException;

import och.util.model.SecureProvider;
import och.util.socket.json.exception.InvalidRespException;
import och.util.socket.json.exception.UnexpectedServerException;
import och.util.socket.json.exception.ValidationException;
import och.util.socket.pool.SocketConn;
import och.util.socket.pool.SocketConnHandler;


final class JsonProtocolConnHandler extends SocketConnHandler<Object> {
	
	private final String reqType;
	private final Object data;
	private final SecureProvider secureProvider;

	public JsonProtocolConnHandler(Class<?> type, Object data, SecureProvider secureProvider) {
		if(type == null) throw new IllegalArgumentException("type must be not null");
		this.reqType = type.getName();
		this.data = data;
		this.secureProvider = secureProvider == null? DUMMY_IMPL : secureProvider;
	}

	@Override
	public Object handle(SocketConn c) throws IOException {
		
		String req = PROTOCOL_PREFIX + reqType+":";
		if(data != null) req += defaultGson.toJson(data);
		req = secureProvider.encode(req);
		
		//send
		c.getWriter().println(req);
		
		//get resp
		String resp = c.getReader().readLine();
		
		if(!hasText(resp)) throw new InvalidRespException("empty resp");
		
		try {
			resp = secureProvider.decode(resp);
		}catch(Throwable t){
			throw new InvalidRespException("decode exception: "+t.getMessage());
		}
		
		if(resp.startsWith(OK)){
			String data = resp.substring(OK.length());
			
			int typeSepIndex = data.indexOf(':');
			if(typeSepIndex < 1) throw new InvalidRespException("unknown resp type: "+data);
			
			Class<?> respType = null;
			try {
				String typeStr = data.substring(0, typeSepIndex);
				respType = Class.forName(typeStr);
			}catch (Exception e) {
				throw new InvalidRespException("unknown resp type: "+data);
			}
			
			String respJson = data.substring(typeSepIndex+1);
			if( ! hasText(respJson)) return null;
			else return defaultGson.fromJson(respJson, respType);
			
		}
		if(resp.startsWith(VALIDATION_ERROR)){
			throw new ValidationException(resp.substring(VALIDATION_ERROR.length()));
		}
		if(resp.startsWith(UNEXPECTED_ERROR)){
			throw new UnexpectedServerException(resp.substring(UNEXPECTED_ERROR.length()));
		}
		throw new InvalidRespException("unknown resp: "+resp); 
	}
}
