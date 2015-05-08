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
package och.comp.db.base.universal.field;

import och.comp.db.base.universal.annotation.FieldName;

public abstract class RowField<T> {
	
	private static final String FIELD_NAME_SUFFIX = "Field";
	
	public final T value;
	
	public RowField(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+" [field=" + fieldName() + ", value=" + value + "]";
	}
	
	public String fieldName(){
		return fieldName(getClass());
	}
	
	public static String fieldName(Class<?> type){
		String name = null;
		
		//from Annotation
		FieldName nameAnn = type.getAnnotation(FieldName.class);
		if(nameAnn != null) name = nameAnn.value();
		
		//from class name
		if( name == null){
			String typeName = type.getSimpleName();
			if(typeName.length() == 1) name = typeName.toLowerCase();
			else {
				name = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
				if(name.endsWith(FIELD_NAME_SUFFIX)) name = name.substring(0, name.length()-FIELD_NAME_SUFFIX.length());
			}
		}
		return name;
	}


	
}
