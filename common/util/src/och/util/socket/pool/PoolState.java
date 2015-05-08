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

import java.util.ArrayList;
import java.util.List;


public class PoolState {

	protected final List<PooledSocketConn> idleConnections = new ArrayList<>();
	protected final List<PooledSocketConn> activeConnections = new ArrayList<>();
	
	protected long requestCount = 0;
	protected long accumulatedRequestTime = 0;
	protected long accumulatedCheckoutTime = 0;
	protected long claimedOverdueConnectionCount = 0;
	protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
	protected long accumulatedWaitTime = 0;
	protected long hadToWaitCount = 0;
	protected long badConnectionCount = 0;


	public synchronized long getRequestCount() {
		return requestCount;
	}

	public synchronized long getAverageRequestTime() {
		return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
	}

	public synchronized long getAverageWaitTime() {
		return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;

	}

	public synchronized long getHadToWaitCount() {
		return hadToWaitCount;
	}

	public synchronized long getBadConnectionCount() {
		return badConnectionCount;
	}

	public synchronized long getClaimedOverdueConnectionCount() {
		return claimedOverdueConnectionCount;
	}

	public synchronized long getAverageOverdueCheckoutTime() {
		return claimedOverdueConnectionCount == 0 ? 0
				: accumulatedCheckoutTimeOfOverdueConnections
						/ claimedOverdueConnectionCount;
	}

	public synchronized long getAverageCheckoutTime() {
		return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
	}

	public synchronized int getIdleConnectionCount() {
		return idleConnections.size();
	}

	public synchronized int getActiveConnectionCount() {
		return activeConnections.size();
	}

	@Override
	public synchronized String toString() {
		return "PoolState [idleConnections="+ idleConnections 
				+ ", activeConnections=" + activeConnections
				+ ", requestCount=" + requestCount
				+ ", accumulatedRequestTime=" + accumulatedRequestTime
				+ ", accumulatedCheckoutTime=" + accumulatedCheckoutTime
				+ ", claimedOverdueConnectionCount="
				+ claimedOverdueConnectionCount
				+ ", accumulatedCheckoutTimeOfOverdueConnections="
				+ accumulatedCheckoutTimeOfOverdueConnections
				+ ", accumulatedWaitTime=" + accumulatedWaitTime
				+ ", hadToWaitCount=" + hadToWaitCount
				+ ", badConnectionCount=" + badConnectionCount + "]";
	}

	

}
