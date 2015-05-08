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
package och.comp.mail.common;

import och.comp.mail.SendCallback;
import och.comp.mail.SendReq.MailMsg;
import och.comp.mail.SendReq.RecipientGroup;

public class SendTask {
	
	public MailMsg msg;
	public RecipientGroup recipientGroup;
	public SendCallback callback;
	
	public Throwable t;
	public int failCount;
	public boolean stored;
	

	public SendTask(MailMsg msg, RecipientGroup recipientGroup, SendCallback callback) {
		this.msg = msg;
		this.recipientGroup = recipientGroup;
		this.callback = callback;
	}


	@Override
	public String toString() {
		return "SendTask [msg=" + msg + ", recipientGroup=" + recipientGroup
				+ ", t=" + t + ", failCount=" + failCount + ", stored="
				+ stored + "]";
	}
	

}
