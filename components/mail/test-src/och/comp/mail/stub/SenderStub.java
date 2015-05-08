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
package och.comp.mail.stub;

import java.util.concurrent.CopyOnWriteArrayList;

import och.comp.mail.Sender;
import och.comp.mail.common.SendTask;
import och.service.props.Props;

public class SenderStub implements Sender {
	
	public CopyOnWriteArrayList<SendTask> tasks = new CopyOnWriteArrayList<>();

	@Override
	public void send(SendTask task, Props config) throws Exception {
		tasks.add(task);
	}
	
}
