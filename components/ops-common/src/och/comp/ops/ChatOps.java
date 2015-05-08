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
package och.comp.ops;

import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.service.props.PropsOps.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;

import java.util.List;

import och.api.exception.chat.MaxChatsForAccPerDayException;
import och.api.exception.chat.MaxChatsFromIpPerDayException;
import och.api.exception.chat.MaxFeedbacksForAccPerDayException;
import och.api.exception.chat.MaxFeedbacksFromIpPerDayException;
import och.api.exception.chat.MsgsPerChatLimitException;
import och.api.exception.chat.SimgleMsgsPerTimeLimitException;
import och.api.model.chat.Message;
import och.service.props.Props;

public class ChatOps {
	
	public static void checkNewMsgToAdd(Props props, String newMsg) {
		validateForTextSize(newMsg, "msg", 1, props.getIntVal(chats_maxMsgSize));
	}
	
	
	public static void checkMaxMsgsPerChat(Props props, int size) {
		if(size >= props.getIntVal(chats_maxMsgsPerChat)) 
			throw new MsgsPerChatLimitException();
	}
	
	
	public static void checkMaxSingleMsgsPerTime(Props props, List<Message> curMsgs, int userIndex){
		
		byte index = (byte) userIndex;
		
		int size = curMsgs.size();
		int curUserMsgsPerTime = 0;
		for (int i = size-1; i > -1; i--) {
			if(curMsgs.get(i).userIndex == index) curUserMsgsPerTime++;
			else break;
		}
		
		if(curUserMsgsPerTime >= props.getIntVal(chats_maxSingleMsgsPerTime)){
			throw new SimgleMsgsPerTimeLimitException();
		}
	}
	
	
	

	
	public static void checkMaxChatsFromIpPerDay(Props props, String ip, int createdPerDay){
		checkMaxPropsVal(props, chats_maxChatsFromIpPerDay, ip, createdPerDay, 
				MaxChatsFromIpPerDayException.class);
	}
	
	public static void checkMaxChatsForAccPerDay(Props props, String accId, int createdPerDay){
		checkMaxPropsVal(props, chats_maxChatsForAccPerDay, accId, createdPerDay, 
				MaxChatsForAccPerDayException.class);
	}
	
	
	public static void checkMaxFeedbacksFromIpPerDay(Props props, String ip, int createdPerDay){
		checkMaxPropsVal(props, chats_maxFeedbacksFromIpPerDay, ip, createdPerDay, 
				MaxFeedbacksFromIpPerDayException.class);
	}
	
	public static void checkMaxFeedbacksForAccPerDay(Props props, String accId, int createdPerDay){
		checkMaxPropsVal(props, chats_maxFeedbacksForAccPerDay, accId, createdPerDay, 
				MaxFeedbacksForAccPerDayException.class);
	}
	
	
	public static boolean getHostImportantFlag(Props props, String url) {
		if( ! hasText(url)) return false;
		
		List<String> urlElems = strToList(url, ".");
		
		String urlBegin = chats_hosts_unimportant.name() + "_";
		
		boolean first = true;
		for(String urlElem : urlElems){
			
			if(first) first = false;
			else urlBegin += ".";
			
			urlBegin += urlElem;
			
			if(props.getVal(urlBegin) != null){
				return false;
			}
		}
		
		return true;
	}

}
