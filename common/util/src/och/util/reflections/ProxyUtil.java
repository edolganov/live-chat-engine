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
package och.util.reflections;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyUtil {
	
	public static Object createProxy(Class<?> classLoaderSource, Class<?> inter, InvocationHandler h){
		return createProxy(classLoaderSource, new Class[] {inter}, h);
	}
	
	public static Object createProxy(Class<?> classLoaderSource, Class<?>[] interfaces, InvocationHandler h){
		return Proxy.newProxyInstance(classLoaderSource.getClassLoader(), interfaces, h);
	}
	
	
	public static Object invokeReal(Object real, Method m, Object[] args) throws Throwable {
		try {
			return m.invoke(real, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
		}
	}

}
