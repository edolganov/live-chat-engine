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
package och.comp.db.base.universal.query;

import och.comp.db.base.universal.field.RowField;
import och.util.model.Pair;


public class SortCondition {
	
	private Pair<Class<? extends RowField<?>>, Boolean>[] fields;
	
	public SortCondition(Class<? extends RowField<?>> field, boolean ask) {
		this(
			new Pair<Class<? extends RowField<?>>, Boolean>(field, ask));
	}
	
	public SortCondition(Class<? extends RowField<?>> field1, boolean ask1, Class<? extends RowField<?>> field2, boolean ask2) {
		this(
			new Pair<Class<? extends RowField<?>>, Boolean>(field1, ask1), 
			new Pair<Class<? extends RowField<?>>, Boolean>(field2, ask2));
	}

	@SafeVarargs
	public SortCondition(Pair<Class<? extends RowField<?>>, Boolean>... fields) {
		this.fields = fields;
	}

	public Pair<Class<? extends RowField<?>>, Boolean>[] values() {
		return fields;
	}

}
