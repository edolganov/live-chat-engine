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
package och.comp.chats;

import java.util.List;

import och.api.exception.ValidationException;
import och.api.model.chat.ChatOperator;
import och.api.model.chat.Message;
import och.api.model.chat.config.AccConfig;
import och.api.model.client.ClientSession;
import och.comp.chats.model.Chat;
import och.comp.chats.model.Chat.AddClientCommentRes;

public final class ChatsAccListenerStub implements ChatsAccListener {
	
	public static final ChatsAccListenerStub STUB_INSTANCE = new ChatsAccListenerStub();

	@Override
	public void beforeAddClientMsg(String chatId, int clientIndex,
			List<Message> curMsgs, String newMsg) throws ValidationException {
	}

	@Override
	public void beforeAddOperatorMsg(String chatId, int operatorIndex,
			List<Message> curMsgs, String newMsg) throws ValidationException {
	}

	@Override
	public void checkCanCreateChat(String accId, ClientSession client)
			throws ValidationException {
	}

	@Override
	public void onChatCreated(String accId, Chat chat, ClientSession client) {
	}

	@Override
	public void onChatClientMsgAdded(String accId, Chat chat, AddClientCommentRes result) {
	}

	@Override
	public void onChatOperatorAdded(String accId, Chat chat, ChatOperator operator) {
	}

	@Override
	public void onOperatorsUpdate(String accId, List<ChatOperator> operators) {
	}

	@Override
	public void onChatClose(String accId, Chat chat) {
	}

	@Override
	public void onConfigSetted(String accId, AccConfig config) {
	}

	@Override
	public void onChatOperatorMsgAdded(String accId, Chat chat, Message result) {
	}

}
