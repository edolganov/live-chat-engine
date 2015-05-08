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
package och.util.socket.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static java.lang.System.*;


public class PooledSocketConn extends SocketConn {
	
	private Socket s;
	private InputStream is;
	private OutputStream os;
	private SocketsPool owner;
	
	private long createdTimestamp = currentTimeMillis();
	private long checkoutTimestamp = createdTimestamp;
	private long lastUsedTimestamp = createdTimestamp;
	private boolean valid = true;
	
	public PooledSocketConn(Socket s, InputStream is, OutputStream os, SocketsPool owner) {
		this.s = s;
		this.is = is;
		this.os = os;
		this.owner = owner;
	}

	public PooledSocketConn(PooledSocketConn other) {
		this(other.s, other.is, other.os, other.owner);
		createdTimestamp = other.createdTimestamp;
		lastUsedTimestamp = other.lastUsedTimestamp;
	}

	@Override
	public InputStream getInputStream() {
		return is;
	}

	@Override
	public OutputStream getOutputStream() {
		return os;
	}
	
	@Override
	public void close() throws IOException {
		owner.pushConnection(this);
	}
	
	protected Socket getSocket(){
		return s;
	}
	
	/***
	 * Method to see if the connection is usable
	 */
	public boolean isValid() {
		return valid && ! s.isClosed() && ! s.isInputShutdown() && ! s.isOutputShutdown();
	}
	
	
	/**
	 * Invalidates the connection
	 */
	@Override
	public void invalidate() {
		valid = false;
	}
	
	/**
	 * Getter for the timestamp that this connection was checked out
	 */
	public long getCheckoutTimestamp() {
		return checkoutTimestamp;
	}

	/**
	 * Setter for the timestamp that this connection was checked out
	 */
	public void setCheckoutTimestamp(long timestamp) {
		this.checkoutTimestamp = timestamp;
	}

	/**
	 * Getter for the time that this connection has been checked out
	 */
	public long getCheckoutTime() {
		return System.currentTimeMillis() - checkoutTimestamp;
	}
	
	/**
	 * Getter for the time that the connection was last used
	 */
	public long getLastUsedTimestamp() {
		return lastUsedTimestamp;
	}

	/**
	 * Setter for the time that the connection was last used
	 */
	public void setLastUsedTimestamp(long lastUsedTimestamp) {
		this.lastUsedTimestamp = lastUsedTimestamp;
	}
	
	/**
	 * Getter for the time that the connection was created
	 */
	public long getCreatedTimestamp() {
		return createdTimestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((s == null) ? 0 : s.hashCode());
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
		PooledSocketConn other = (PooledSocketConn) obj;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}
	
	

}
