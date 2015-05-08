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
package och.api.model;

public class RemoteChats {
	
	//chat
	public static final String URL_CHAT_CREATE_ACC 	= "/remote/chat/createAcc";
	public static final String URL_CHAT_PUT_OP = "/remote/chat/putOperator";
	public static final String URL_CHAT_REMOVE_OP = "/remote/chat/removeOperator";
	public static final String URL_CHAT_UPDATE_SESSIONS = "/remote/chat/updateSessions";
	public static final String URL_CHAT_BLOKED = "/remote/chat/setBlocked";
	public static final String URL_CHAT_GET_UNBLOKED = "/remote/chat/getUnblocked";
	public static final String URL_CHAT_PAUSED = "/remote/chat/setPaused";
	public static final String URL_CHAT_GET_PAUSED_STATE = "/remote/chat/getPausedState";
	public static final String URL_CHAT_UPDATE_USER_CONTACT = "/remote/chat/updateUserContact";
	public static final String URL_CHAT_PUT_ACC_CONFIG = "/remote/chat/putAccConfig";
	
	//user
	public static final String URL_USER_INIT_TOKEN = "/remote/user/initToken";
	public static final String URL_USER_REMOVE_SESSION = "/remote/user/removeSession";
	
	
	public static final String stub = "";

}
