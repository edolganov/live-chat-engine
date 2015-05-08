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
package och.comp.db.base.exception;


import java.io.IOException;

import och.api.exception.ExpectedException;
import och.util.socket.SocketUtil;

public class ConnectionProblemException extends ExpectedException {
	
	private static final long serialVersionUID = 1L;
	
	
	public ConnectionProblemException(String url, Throwable t) {
		super("url="+url+", cause="+t, t);
	}
	
	
	public static ConnectionProblemException tryFindConnProblem(String url, Throwable t){
		
		IOException socketEx = SocketUtil.findSocketException(t);
		if(socketEx != null) return new ConnectionProblemException(url, t);
		
		String msg = t.getMessage();
		if(msg != null && msg.contains("connection has been closed")){
			return new ConnectionProblemException(url, t);
		}
		
		//not ConnectionProblemException
		return null;
	}

}
