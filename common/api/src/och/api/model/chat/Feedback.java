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

import java.util.Date;

public class Feedback {
	
	public ChatUser user;
	public Date created;
	public String text;
	public String ref;
	
	public Feedback(ChatUser user, Date created, String text, String ref) {
		super();
		this.created = created;
		this.user = user;
		this.text = text;
		this.ref = ref;
	}
	
	
	public static int compareByDateAsc(Feedback a, Feedback b){
		return a.created.compareTo(b.created);
	}
	

}
