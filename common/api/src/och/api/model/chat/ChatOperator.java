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
package och.api.model.chat;

public class ChatOperator implements Cloneable {
	
	public long id;
	public String name;
	public String email;
	
	
	public ChatOperator() {
		super();
	}

	public ChatOperator(long id) {
		this(id, null, null);
	}
	
	public ChatOperator(long id, String name) {
		this(id, name, null);
	}
	
	public ChatOperator(long id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}
	
	@Override
	public ChatOperator clone() {
		try {
			return (ChatOperator) super.clone();
		}catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	

}
