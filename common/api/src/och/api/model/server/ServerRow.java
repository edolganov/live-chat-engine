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
package och.api.model.server;

public class ServerRow implements Cloneable, Comparable<ServerRow> {
	
	public long id;
	public String httpUrl;
	public String httpsUrl;
	public boolean isFull;
	
	
	public ServerRow() {
		super();
	}
	
	public ServerRow(long id, String httpUrl, String httpsUrl) {
		this.id = id;
		this.httpUrl = httpUrl;
		this.httpsUrl = httpsUrl;
	}
	
	

	
	public String createUrl(String req){
		return createUrl(httpUrl, req);
	}
	
	public static String createUrl(String httpUrl, String req){
		return httpUrl + req;
	}
	
	
	@Override
	public ServerRow clone() {
		try {
			return (ServerRow)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("can't clone", e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((httpUrl == null) ? 0 : httpUrl.hashCode());
		result = prime * result
				+ ((httpsUrl == null) ? 0 : httpsUrl.hashCode());
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
		ServerRow other = (ServerRow) obj;
		if (httpUrl == null) {
			if (other.httpUrl != null)
				return false;
		} else if (!httpUrl.equals(other.httpUrl))
			return false;
		if (httpsUrl == null) {
			if (other.httpsUrl != null)
				return false;
		} else if (!httpsUrl.equals(other.httpsUrl))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int compareTo(ServerRow o) {
		return Long.compare(id, o.id);
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getHttpUrl() {
		return httpUrl;
	}
	
	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}
	
	public String getHttpsUrl() {
		return httpsUrl;
	}
	
	public void setHttpsUrl(String httpsUrl) {
		this.httpsUrl = httpsUrl;
	}

	public boolean isFull() {
		return isFull;
	}
	
	public void setIsFull(Boolean isFull) {
		setFull(isFull);
	}
	
	public void setFull(Boolean isFull) {
		if(isFull == null) isFull = false;
		this.isFull = isFull;
	}

	public void setFull(boolean isFull) {
		this.isFull = isFull;
	}




	
	
	
	
	

}
