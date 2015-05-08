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
package och.api.model.chat.account;

import static och.util.Util.*;

import java.util.Date;
import java.util.Map;

import och.api.model.server.ServerRow;


public class ChatAccount implements Cloneable, Comparable<ChatAccount> {
	
	public static final int MAX_NAME_SIZE = 30;
	
	public long id;
	public long serverId;
	public String uid;
	public String name;
	public Date created;
	public long tariffId;
	public Date tariffStart;
	public Date tariffLastPay;
	public int tariffChangedInDay;
	public Long tariffPrevId;
	public boolean feedback_notifyOpsByEmail = true;
	
	//extra
	public ServerRow server;
	public boolean blocked;
	public Map<String, Object> params;
	
	
	public ChatAccount() {
		super();
	}
	
	public ChatAccount(long id, long serverId, String uid, String name, Date created, long tariffId) {
		this.id = id;
		this.serverId = serverId;
		this.uid = uid;
		this.name = name;
		this.created = created;
		this.tariffId = tariffId;
		this.tariffStart = created;
		this.tariffLastPay = created;
	}
	
	@Override
	public ChatAccount clone() {
		try {
			return (ChatAccount)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("can't clone", e);
		}
	}
	
	@Override
	public int compareTo(ChatAccount o) {
		return compareByIdAsc(this, o);
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatAccount other = (ChatAccount) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChatAccount [id=" + id + ", serverId=" + serverId + ", uid="
				+ uid + ", name=" + name + ", created=" + created
				+ ", tariffId=" + tariffId + ", tariffStart=" + tariffStart
				+ ", tariffLastPay=" + tariffLastPay + "]";
	}
	
	public static int compareByIdAsc(ChatAccount a, ChatAccount b){
		return Long.compare(a.id, b.id);
	}
	
	public static int compareByNameAsc(ChatAccount a, ChatAccount b){
		String nameA = hasText(a.name)? a.name : a.uid;
		String nameB = hasText(b.name)? b.name : b.uid;
		return nameA.compareTo(nameB);
	}

	public static int compareByNameDesc(ChatAccount a, ChatAccount b){
		return -1 * compareByNameAsc(a, b);
	}
	
	public static int compareByDateAsc(ChatAccount a, ChatAccount b){
		long dateA = a.created != null? a.created.getTime() : 0;
		long dateB = b.created != null? b.created.getTime() : 0;
		return Long.compare(dateA, dateB);
	}
	
	public static int compareByDateDesc(ChatAccount a, ChatAccount b){
		return -1 * compareByDateAsc(a, b);
	}
	
	
	
	
	
	

	public void setId(long id) {
		this.id = id;
	}
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ServerRow getServer() {
		return server;
	}

	public void setServer(ServerRow server) {
		this.server = server;
	}

	public long getId() {
		return id;
	}

	public long getServerId() {
		return serverId;
	}

	public String getUid() {
		return uid;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	public void setTariffId(long tariffId) {
		this.tariffId = tariffId;
	}
	
	public long getTariffId() {
		return tariffId;
	}
	
	public void setTariffStart(Date tariffStart) {
		this.tariffStart = tariffStart;
	}

	public void setTariffLastPay(Date tariffLastPay) {
		this.tariffLastPay = tariffLastPay;
	}

	public int getTariffChangedInDay() {
		return tariffChangedInDay;
	}

	public void setTariffChangedInDay(Integer tariffChangedInDay) {
		this.tariffChangedInDay = tariffChangedInDay == null? 0 : tariffChangedInDay;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setTariffPrevId(Long tariffPrevId) {
		this.tariffPrevId = tariffPrevId;
	}

	public boolean isFeedback_notifyOpsByEmail() {
		return feedback_notifyOpsByEmail;
	}

	public void setFeedback_notifyOpsByEmail(Boolean feedback_notifyOpsByEmail) {
		if(feedback_notifyOpsByEmail == null) feedback_notifyOpsByEmail = true;
		this.feedback_notifyOpsByEmail = feedback_notifyOpsByEmail;
	}
	
	
	
	
	
}
