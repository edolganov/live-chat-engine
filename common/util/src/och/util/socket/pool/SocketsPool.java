package och.util.socket.pool;

/*
 *    Copyright 2009-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import static och.util.Util.*;
import static och.util.socket.SocketUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.logging.Log;


// by: org.apache.ibatis.datasource.pooled.PooledDataSource
/**
 * This is a simple, synchronous, thread-safe socket pool.
 * <br>
 */
public class SocketsPool {

	private static final Log log = getLog(SocketsPool.class);

	private final PoolState state = new PoolState();
	
	protected String host;
	protected int port;

	protected int poolMaximumActiveConnections = 10;
	protected int poolMaximumIdleConnections = 5;
	protected int poolMaximumCheckoutTime = 20000;
	protected int poolTimeToWait = 20000;
	protected Integer socketSoTimeout;

	public SocketsPool(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public SocketConn getConnection() throws IOException {
		return popConnection();
	}
	
	public <T> T invoke(SocketConnHandler<T> handler) throws IOException {
		SocketConn conn = null;
		try {
			conn = getConnection();
			return handler.handle(conn);
		}catch (Throwable t) {
			return handler.onException(conn, t);
		}finally {
			if(conn != null) conn.close();
		}
	}


	public void setHost(String host) {
		this.host = host;
		forceCloseAll();
	}

	public void setPort(int port) {
		this.port = port;
		forceCloseAll();
	}


	/**
	 * The maximum number of active connections
	 */
	public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
		this.poolMaximumActiveConnections = poolMaximumActiveConnections;
		forceCloseAll();
	}

	/**
	 * The maximum number of idle connections
	 */
	public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
		this.poolMaximumIdleConnections = poolMaximumIdleConnections;
		forceCloseAll();
	}

	/**
	 * The maximum time a connection can be used before it *may* be given away again
	 */
	public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
		this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
		forceCloseAll();
	}

	/**
	 * The time to wait before retrying to get a connection
	 */
	public void setPoolTimeToWait(int poolTimeToWait) {
		this.poolTimeToWait = poolTimeToWait;
		forceCloseAll();
	}


	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public String getRemoteAddress(){
		return getHost()+":"+getPort();
	}


	public int getPoolMaximumActiveConnections() {
		return poolMaximumActiveConnections;
	}

	public int getPoolMaximumIdleConnections() {
		return poolMaximumIdleConnections;
	}

	public int getPoolMaximumCheckoutTime() {
		return poolMaximumCheckoutTime;
	}

	public int getPoolTimeToWait() {
		return poolTimeToWait;
	}

	public Integer getSocketSoTimeoutForNewConn() {
		return socketSoTimeout;
	}

	public void setSocketSoTimeoutForNewConn(Integer socketSoTimeout) {
		this.socketSoTimeout = socketSoTimeout;
	}

	/**
	 * Closes all active and idle connections in the pool
	 */
	public void forceCloseAll() {
		synchronized (state) {
			for (int i = state.activeConnections.size(); i > 0; i--) {
				try {
					PooledSocketConn conn = state.activeConnections.remove(i - 1);
					conn.invalidate();
					close(conn.getSocket());
				} catch (Exception e) {
					// ignore
				}
			}
			for (int i = state.idleConnections.size(); i > 0; i--) {
				try {
					PooledSocketConn conn = state.idleConnections.remove(i - 1);
					conn.invalidate();
					close(conn.getSocket());
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	public PoolState getPoolState() {
		return state;
	}


	protected void pushConnection(PooledSocketConn conn) throws IOException {

		synchronized (state) {
			state.activeConnections.remove(conn);
			if (conn.isValid()) {
				if (state.idleConnections.size() < poolMaximumIdleConnections) {
					state.accumulatedCheckoutTime += conn.getCheckoutTime();
					PooledSocketConn newConn = new PooledSocketConn(conn);
					state.idleConnections.add(newConn);
					conn.invalidate();
					state.notifyAll();
				} else {
					state.accumulatedCheckoutTime += conn.getCheckoutTime();
					conn.getSocket().close();
					conn.invalidate();
				}
			} else {
				//A bad connection attempted to return to the pool, discarding connection
				state.badConnectionCount++;
				close(conn.getSocket());
			}
		}
	}

	private PooledSocketConn popConnection() throws IOException {
		boolean countedWait = false;
		PooledSocketConn conn = null;
		long t = System.currentTimeMillis();
		int localBadConnectionCount = 0;

		//LOOP for find conn
		while (conn == null) {
			synchronized (state) {
				// Pool has available connection
				if (state.idleConnections.size() > 0) {
					conn = state.idleConnections.remove(0);
				} 
				// Pool does not have available connection
				else {
					// Can create new connection
					if (state.activeConnections.size() < poolMaximumActiveConnections) {
						conn = createConn(state.activeConnections.size() == 0);
					} 
					// Cannot create new connection
					else {
						PooledSocketConn oldestActiveConnection = state.activeConnections.get(0);
						long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
						// Can claim overdue connection
						if (longestCheckoutTime > poolMaximumCheckoutTime) {
							state.claimedOverdueConnectionCount++;
							state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
							state.accumulatedCheckoutTime += longestCheckoutTime;
							state.activeConnections.remove(oldestActiveConnection);
							conn = new PooledSocketConn(oldestActiveConnection);
							oldestActiveConnection.invalidate();
						} 
						// Must wait
						else {
							try {
								if (!countedWait) {
									state.hadToWaitCount++;
									countedWait = true;
								}
								long wt = System.currentTimeMillis();
								state.wait(poolTimeToWait);
								state.accumulatedWaitTime += System.currentTimeMillis() - wt;
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				}
				if (conn != null) {
					if (conn.isValid()) {
						conn.setCheckoutTimestamp(System.currentTimeMillis());
						conn.setLastUsedTimestamp(System.currentTimeMillis());
						state.activeConnections.add(conn);
						state.requestCount++;
						state.accumulatedRequestTime += System.currentTimeMillis() - t;
					} else {
						state.badConnectionCount++;
						localBadConnectionCount++;
						conn = null;
						if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
							throw new IllegalStateException("Could not get a good connection");
						}
					}
				}
			}

		}

		if (conn == null) {
			throw new IllegalStateException("Unknown severe error condition.  The connection pool returned a null connection.");
		}

		return conn;
	}
	
	private PooledSocketConn createConn(boolean printLog) throws IOException {
		
		Socket s = new Socket(host, port);
		if(socketSoTimeout != null) s.setSoTimeout(socketSoTimeout);
		
		InputStream is = s.getInputStream();
		OutputStream os = s.getOutputStream();
		if(printLog) log.info("Connected to "+host+":"+port+"." 
				+" Pool props ["
				+"maxActiveConnections="+poolMaximumActiveConnections
				+", maxIdleConnections="+poolMaximumIdleConnections
				+", maxCheckoutTime="+poolMaximumCheckoutTime
				+", timeToWait="+poolTimeToWait
				+"]"
				);
		return new PooledSocketConn(s, is, os, this);
	}


	@Override
	protected void finalize() throws Throwable {
		forceCloseAll();
	}
	
	public int getActiveConnectionsCount(){
		synchronized (state) {
			return state.getActiveConnectionCount();
		}
	}
	
	public int getIdleConnections(){
		synchronized (state) {
			return state.getIdleConnectionCount();
		}
	}

}
