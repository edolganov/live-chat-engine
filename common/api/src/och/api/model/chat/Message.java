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

public class Message {
	
	public Date date;
	public byte userIndex;
	public String text;
	
	public Message() {
		super();
	}

	public Message(int userIndex, String text) {
		this(new Date(), userIndex, text);
	}

	public Message(Date date, int userIndex, String text) {
		super();
		this.date = date;
		this.userIndex = (byte)userIndex;
		this.text = text;
	}

}
