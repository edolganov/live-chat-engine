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
package och.front.service;

import static och.api.model.RemoteCache.*;
import static och.api.model.user.SecurityContext.*;
import static och.util.Util.*;

import java.util.Date;

import och.api.annotation.Secured;
import och.api.model.billing.LastSyncInfo;
import och.comp.ops.BillingOps;
import och.front.service.event.admin.UpdateModelsEvent;
import och.util.model.Pair;

public class AdminService extends BaseFrontService {

	public AdminService(FrontAppContext c) {
		super(c);
	}
	
	
	@Secured
	public void reloadModels() throws Exception{
		
		checkAccessFor_ADMIN();
		
		log.info("reloadModels");
		
		c.events.fireEvent(new UpdateModelsEvent());
		
		log.info("done");
	}
	
	
	@Secured
	public void syncPayments() throws Exception{
		
		checkAccessFor_ADMIN();
		
		c.cache.tryPutCache(BILLING_SYNC_REQ, new Date().toString());
	}
	
	@Secured
	public String getSyncPaymentsResult() throws Exception {
		
		checkAccessFor_ADMIN();
		
		return c.cache.tryGetVal(BILLING_SYNC_RESP, null);
	}
	
	@Secured
	public LastSyncInfo getLastSyncInfo() throws Exception {
		
		checkAccessFor_ADMIN();
		
		return tryParseJson(c.cache.tryGetVal(BILLING_LAST_SYNC), LastSyncInfo.class);
	}
	
	
	@Secured
	public Pair<Integer, Integer> syncAccBlocked() throws Exception {
		
		checkAccessFor_ADMIN();
		
		return BillingOps.reinitAccsBlocked(props, db, c.cache);
	}

	
	@Secured
	public Pair<Integer, Integer> syncAccPaused() {
		
		checkAccessFor_ADMIN();
		
		return BillingOps.reinitAccsPaused(props, db);
	}

}
