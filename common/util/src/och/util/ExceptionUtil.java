/*
 * Copyright 2012 Evgeny Dolganov (evgenij.dolganov@gmail.com).
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
package och.util;

import static och.util.StringUtil.*;
import static och.util.Util.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

public class ExceptionUtil {
	
	public static interface ExceptionHander {
		public void handle(Throwable t) throws Exception;
	}
	

	public static Exception getExceptionOrThrowError(Throwable t) {
		if (t instanceof Exception) {
			return (Exception) t;
		}
		if (t instanceof Error) {
			throw (Error) t;
		} else {
			throw new IllegalStateException("Unknow type of Throwable", t);
		}
	}

	public static RuntimeException getRuntimeExceptionOrThrowError(Throwable t) {
		return getRuntimeExceptionOrThrowError(t, "Wrap not-runtime exception");
	}
	
	public static RuntimeException getRuntimeExceptionOrThrowError(Throwable t, String msg) {
		Exception e = getExceptionOrThrowError(t);
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(msg, e);
		}
	}
	
	public static String stackTraceToString(Throwable t){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	
	public static String getMessageOrType(Throwable t){
        String msg = t.getLocalizedMessage();
        return msg != null ? msg : t.getClass().getSimpleName();
	}
	
	public static Throwable unwrapThrowable(Throwable wrapped) {
		Throwable unwrapped = wrapped;
		while (true) {
			if(unwrapped instanceof ExecutionException) return unwrapped.getCause();
			if(unwrapped instanceof InvocationTargetException) {
				unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
			} else if (unwrapped instanceof UndeclaredThrowableException) {
				unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
			} else {
				return unwrapped;
			}
		}
	}
	
	public static IOException unwrapIOException(Throwable t) {
		Throwable real = unwrapThrowable(t);
		if(real instanceof IOException) return (IOException) real;
		throw getRuntimeExceptionOrThrowError(real);
	}
	
	
	public static boolean containsAnyTextInMessage(Throwable t, String... texts){
		
		if( ! isEmpty(texts)){
			for (int i = 0; i < texts.length; i++) {
				texts[i] = texts[i] == null? null : texts[i].toLowerCase();
			}
		}
		
		Throwable prev = null;
		while(t != null && t != prev){
			
			String msg = t.getMessage();
			if(msg == null) msg = "";
			msg = msg.toLowerCase();
			
			if(containsAny(msg, texts)){
				return true;
			}
			
			prev = t;
			t = t.getCause();
		}
		return false;
		
	}
	
	public static String printStackTrace(Throwable t){
		
		String header = t.toString();
		
		StringWriter errors = new StringWriter();
		t.printStackTrace(new PrintWriter(errors));
		String stack = errors.toString();
		
		return header+"\n"+stack;
	}

}
