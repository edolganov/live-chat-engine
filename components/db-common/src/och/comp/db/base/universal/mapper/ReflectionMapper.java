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
package och.comp.db.base.universal.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static och.comp.db.base.universal.field.RowField.*;
import static och.util.ExceptionUtil.*;
import static och.util.ReflectionsUtil.*;

public class ReflectionMapper {
	
	private ConcurrentHashMap<Class<?>, HashMap<String, Method>> accessMethods = new ConcurrentHashMap<>();
	
	public Object createEntity(ResultSet rs, Class<?> type, Class<?>[] selectFields) throws Exception {
		
		boolean fromCache = true;
		HashMap<String, Method> validMethods = accessMethods.get(type);
		if(validMethods == null){
			fromCache = false;
			validMethods = new HashMap<>();
		}

		Object entity = type.newInstance();
		
		for (int i = 0; i < selectFields.length; i++) {
			String fieldName = fieldName(selectFields[i]);
			Object val = rs.getObject(fieldName);
			
			Method accessMethod = validMethods.get(fieldName);
			if(accessMethod != null){
				accessMethod.invoke(entity, val);
			} else {
				List<Method> candidats = getSetterMethodsDef(type, fieldName, true);
				for (Method method : candidats) {
					try {
						method.invoke(entity, val);
						//valid method
						validMethods.put(fieldName, method);
						break;
					}catch (IllegalAccessException | IllegalArgumentException e) {
						//wrong method
					}catch (InvocationTargetException e) {
						throw getExceptionOrThrowError(e.getCause());
					}
				}
			}
		}
		
		//save to cache
		if(!fromCache){
			accessMethods.put(type, validMethods);
		}
		
		return entity;
	}

}
