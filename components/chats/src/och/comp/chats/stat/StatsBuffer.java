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
package och.comp.chats.stat;

import static och.util.Util.*;

import java.util.HashMap;
import java.util.Map;

import och.api.model.chat.ChatOperator;
import och.api.model.chat.Message;
import och.api.model.client.ClientInfo;
import och.api.model.client.ClientSession;
import och.comp.chats.model.Chat;
import och.comp.chats.model.Chat.AddClientCommentRes;
import och.util.geoip.GeoIp;


public class StatsBuffer {
	
	public static interface StatsBufferListener {
		void onStat(Map<String, AccStatData> map);
	}
	
	public final long liveTime;
	
	private volatile long lastTime;
	private volatile Map<String, AccStatData> statsByAcc;
	private StatsBufferListener listener;
	private GeoIp geoIp;

	public StatsBuffer(long liveTime, StatsBufferListener listener) {
		this.liveTime = liveTime;
		this.listener = listener;
		resetStat();
	}

	public void setGeoIp(GeoIp geoIp) {
		this.geoIp = geoIp;
	}

	public Map<String, AccStatData> resetStat() {
		
		Map<String, AccStatData> out = statsByAcc;
		
		//reset models
		statsByAcc = new HashMap<>();
		lastTime = System.currentTimeMillis() + liveTime;

		return out;
	}

	public boolean isFull() {
		return lastTime < System.currentTimeMillis();
	}

	public void flushAndSaveIfNeed() {
		if( ! isFull()) return;
		
		Map<String, AccStatData> map = resetStat();
		if(isEmpty(map)) return;
		
		listener.onStat(map);
	}
	
	
	
	public void addFeedbackStat(String accId, ClientInfo info) {
		AccStatData stat = getAccStat(accId);
		stat.feedbacks++;
	}
	
	public void createChatStat(String accId, Chat chat, ClientSession client) {
		AccStatData stat = getAccStat(accId);
		stat.chats++;
		
		if(geoIp != null){
			stat.incCountry(geoIp.getCountry(client.info.ip));
		}
	}


	public void addClientMsgStat(String accId, Chat chat, AddClientCommentRes result) {
		AccStatData stat = getAccStat(accId);
		stat.userComments++;
	}
	
	public void addOperatorStat(String accId, Chat chat, ChatOperator op) {
		AccStatData stat = getAccStat(accId);
		stat.incOperatorChatsStat(op.id);
	}
	
	public void addOperatorMsgStat(String accId, Chat chat, Message result) {
		AccStatData stat = getAccStat(accId);
		stat.operatorComments++;
		
		Long opId = chat.getOperatorId(result.userIndex);
		stat.incOperatorMsgStat(opId);
	}
	
	public void closeChatStat(String accId, Chat chat) {
		AccStatData stat = getAccStat(accId);
		if(chat.getOperatorsCount() == 0){
			stat.noAnswerChats++;
		}
	}

	
	
	
	private AccStatData getAccStat(String accId) {
		Map<String, AccStatData> map = statsByAcc;
		AccStatData out = map.get(accId);
		if(out == null){
			out = new AccStatData(accId);
			map.put(accId, out);
		}
		return out;
	}












	
	

}
