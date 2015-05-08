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

import static och.util.model.IntWrap.*;

import java.util.HashMap;
import java.util.Map;

import och.util.model.IntWrap;

public class AccStatData {
	
	public String accId;
	public int feedbacks;
	public int chats;
	public int noAnswerChats;
	public int userComments;
	public int operatorComments;
	public Map<Long, Integer> opsChats;
	public Map<Long, Integer> opsComments;
	public Map<String, Integer> countries;
	
	
	private Map<Long, IntWrap> temp_opsChats;
	private Map<Long, IntWrap> temp_opsComments;
	private Map<String, IntWrap> temp_countries;
	
	
	public AccStatData() {
		super();
	}
	
	public AccStatData(String accId) {
		this.accId = accId;
	}
	
	public void prepareToSave(){
		
		opsChats = unwrapMap(temp_opsChats);
		opsComments = unwrapMap(temp_opsComments);
		countries = unwrapMap(temp_countries);
		
		temp_opsChats = null;
		temp_opsComments = null;
		temp_countries = null;
	}

	public void incOperatorChatsStat(Long opId) {
		if(opId == null) return;
		if(temp_opsChats == null) temp_opsChats = new HashMap<>();
		
		IntWrap data = temp_opsChats.get(opId);
		if(data == null) {
			data = new IntWrap();
			temp_opsChats.put(opId, data);
		}
		data.val++;
	}

	public void incOperatorMsgStat(Long opId) {
		if(opId == null) return;
		if(temp_opsComments == null) temp_opsComments = new HashMap<>();
		
		IntWrap data = temp_opsComments.get(opId);
		if(data == null) {
			data = new IntWrap();
			temp_opsComments.put(opId, data);
		}
		data.val++;
	}

	public void incCountry(String country) {
		if(country == null) country = "Unknown";
		if(temp_countries == null) temp_countries = new HashMap<>();
		
		IntWrap data = temp_countries.get(country);
		if(data == null) {
			data = new IntWrap();
			temp_countries.put(country, data);
		}
		data.val++;
	}



}
