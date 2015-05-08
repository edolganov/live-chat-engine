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

import static och.api.model.PropKey.*;
import static och.comp.chats.common.StoreOps.*;
import static och.util.DateUtil.*;
import static och.util.FileUtil.*;
import static och.util.Util.*;
import static och.util.concurrent.AsyncListener.*;
import static och.util.json.GsonUtil.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import och.api.model.chat.ChatOperator;
import och.api.model.chat.Message;
import och.api.model.client.ClientInfo;
import och.api.model.client.ClientSession;
import och.comp.chats.model.Chat;
import och.comp.chats.model.Chat.AddClientCommentRes;
import och.service.props.Props;
import och.util.concurrent.AsyncListener;
import och.util.concurrent.ExecutorsUtil;
import och.util.geoip.GeoIp;
import och.util.timer.TimerExt;

import org.apache.commons.logging.Log;

public class AccsStat {
	
	public static final String STAT_DIR = "_stat";

	private Log log = getLog(getClass());
	
	private AsyncListener asyncListener;
	private File statRoot;
	private Props props;
	
	private boolean externalExecutor;
	private boolean externalTimer;
	private ExecutorService updateExecutor;
	private TimerExt timer;
	
	private ArrayList<StatsBuffer> buffers = new ArrayList<StatsBuffer>();
	
	public AccsStat(
			File accsRoot, 
			Props props, 
			ExecutorService updateExecutor, 
			AsyncListener asyncListener,
			TimerExt timer) {
		
		this.statRoot = new File(accsRoot, STAT_DIR);
		this.props = props;
		this.asyncListener = asyncListener;
		
		if(updateExecutor == null) updateExecutor = ExecutorsUtil.newSingleThreadExecutor("AccsStat-update-stat-thread");
		else externalExecutor = true;
		this.updateExecutor = updateExecutor;
		
		if(props.getBoolVal(chats_useStat)){
			
			buffers.add(new StatsBuffer(ONE_MINUTE, (map)-> rewriteStat(map, "_minute.stat")));
			buffers.add(new StatsBuffer(ONE_MINUTE*30, (map)->rewriteStat(map, "_minute-30.stat")));
			buffers.add(new StatsBuffer(ONE_HOUR, (map)->rewriteStat(map, "_hour.stat")));
			buffers.add(new StatsBuffer(ONE_HOUR*6, (map)->rewriteStat(map, "_hour-6.stat")));
			buffers.add(new StatsBuffer(ONE_DAY, (map)->rewriteDayStat(map)));
			
			if(timer == null) timer = new TimerExt("AccsStat-write-timer", false);
			else externalTimer = true;
			this.timer = timer;
			timer.tryScheduleAtFixedRate(() -> flushAndSaveStat(), 
					props.getLongVal(chats_stat_Delay), 
					props.getLongVal(chats_stat_Delay));		
		}
	}
	
	public void setGeoIp(GeoIp geoIp) {
		for(StatsBuffer buffer : buffers) buffer.setGeoIp(geoIp);
	}

	public void shutdown() {
		if(!externalExecutor) updateExecutor.shutdown();
		if(!externalTimer) timer.cancel();
	}
	

	public void flushAndSaveStat(){
		if( ! props.getBoolVal(chats_useStat)) return;
		
		statRoot.mkdirs();
		for(StatsBuffer buffer : buffers){
			buffer.flushAndSaveIfNeed();
		}
		
	}

	public void addFeedbackStatAsync(String accId, ClientInfo info) {
		asyncEvent((buffer)->buffer.addFeedbackStat(accId, info));
	}
	
	public void createChatStatAsync(String accId, Chat chat, ClientSession client) {
		asyncEvent((buffer)->buffer.createChatStat(accId, chat, client));
	}

	public void addClientMsgStatAsync(String accId, Chat chat, AddClientCommentRes result) {
		asyncEvent((buffer)-> buffer.addClientMsgStat(accId, chat, result));
	}
	

	public void addOperatorStatAsync(String accId, Chat chat, ChatOperator operator) {
		asyncEvent((buffer)-> buffer.addOperatorStat(accId, chat, operator));
	}
	
	public void addOperatorMsgStatAsync(String accId, Chat chat, Message result) {
		asyncEvent((buffer)-> buffer.addOperatorMsgStat(accId, chat, result));
	}
	
	public void closeChatStatAsync(String accId, Chat chat) {
		asyncEvent((buffer)-> buffer.closeChatStat(accId, chat));
	}
	
	
	
	
	private static interface StatsBufferEvent {
		void onEvent(StatsBuffer buffer);
	}
	
	private void asyncEvent(StatsBufferEvent e){
		if( ! props.getBoolVal(chats_useStat)) return;
		Future<?> f = updateExecutor.submit(()-> {
			for(StatsBuffer buffer : buffers) {
				if(buffer.isFull()) continue;
				e.onEvent(buffer);
			}
		});
		fireAsyncEvent(asyncListener, f);
	}


	private void rewriteDayStat(Map<String, AccStatData> map) {
		String date = formatDate(new Date(), DAY_FORMAT);
		rewriteStat(map, date+".stat");
	}
	

	private void rewriteStat(Map<String, AccStatData> map, String fileName) {
		
		if(isEmpty(map)) return;
		
		for (Entry<String, AccStatData> entry : map.entrySet()) {
			
			AccStatData data = entry.getValue();
			String accId = data.accId;
			File dir = getAccDir(statRoot, accId);
			dir.mkdirs();
			
			try {
				
				data.prepareToSave();
				
				String json = defaultGsonPrettyPrinting.toJson(data);
				writeFileUTF8(new File(dir, fileName), json);
				
			}catch(Exception e){
				log.error("can't rewriteStat for "+fileName+": "+e);
			}
			
		}
	
	}









}
