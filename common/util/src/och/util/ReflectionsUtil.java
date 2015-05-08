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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.*;
import static java.lang.reflect.Modifier.*;
import static och.util.Util.*;


public class ReflectionsUtil {
	
	public static <T> Class<T> getFirstActualArgType(Class<?> target){
		return getActualArgType(target, 0);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getActualArgType(Class<?> target, int index){
		Type[] types = ((ParameterizedType) target.getGenericSuperclass()).getActualTypeArguments();
		return (Class<T>) types[index];
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T getField(Object obj, String fieldName) throws Exception {
		return (T) getFieldDef(obj.getClass(), fieldName).get(obj);
	}
	
	public static void setField(Object obj, String fieldName, Object val) throws Exception {
		getFieldDef(obj.getClass(), fieldName).set(obj, val);
	}
	
	
	public static Field getFieldDef(Class<?> clazz, String fieldName) throws Exception {
		return getFieldDef(clazz, fieldName, true);
	}
	
	
	public static Field getFieldDef(Class<?> clazz, String fieldName, boolean accessible) throws Exception {
		Field field = null;
		while(clazz != null){
			try {
				field = clazz.getDeclaredField(fieldName);
				clazz = null;
			}catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			}
		}
		if(field == null){
			throw new NoSuchFieldException(fieldName);
		}
		if(accessible) field.setAccessible(true);
		return field;
	}
	
	
	public static List<Method> getSetterMethodsDef(Class<?> clazz, String fieldName, boolean accessible) throws Exception {
		
		String methodName = "set"+toUpperCase(fieldName.charAt(0)) 
				+ (fieldName.length() > 1 ? fieldName.substring(1) : "");
		
		ArrayList<Method> methods = new ArrayList<>();
		while(clazz != null){
			Method[] decMethods = clazz.getDeclaredMethods();
			if( ! isEmpty(decMethods)){
				for (Method method : decMethods) {
					if(methodName.equals(method.getName()) 
							&& isPublic(method.getModifiers())){
						methods.add(method);
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		if(accessible){
			for (Method method : methods) {
				method.setAccessible(true);
			}
		}
		return methods;
	}
	
	public static void setFinalStatic(Class<?> clazz, String fieldName, Object newValue) throws Exception {
		setFinalStatic(getFieldDef(clazz, fieldName), newValue);
	}
	
	public static void setFinalStatic(Field field, Object newValue) throws Exception {
		
      field.setAccessible(true);

      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

      field.set(null, newValue);
   }

}
