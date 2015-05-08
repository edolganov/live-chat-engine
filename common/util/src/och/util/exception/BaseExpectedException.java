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
package och.util.exception;


import java.io.IOException;

import och.util.annotation.NoLog;
import och.util.socket.SocketUtil;

import org.apache.commons.logging.Log;


public abstract class BaseExpectedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public BaseExpectedException() {
		super();
	}

	public BaseExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseExpectedException(String message) {
		super(message);
	}

	public BaseExpectedException(Throwable cause) {
		super(cause);
	}
	
	
	public static void logError(Log log, Throwable t, String msg){
		
		NoLog noLog = t.getClass().getAnnotation(NoLog.class);
		if(noLog != null){
			return;
		}
		
		if(t instanceof BaseExpectedException) {
			log.error(msg+": "+t);
			return;
		}
		
		IOException socketEx = SocketUtil.findSocketException(t);
		if(socketEx != null){
			log.error(msg+": "+new ExpectedSocketException(socketEx, t));
			return;
		}
		
		//not ExpectedException
		log.error(msg, t);
	}
	
	

}
