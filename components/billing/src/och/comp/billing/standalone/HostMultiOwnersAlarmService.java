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
package och.comp.billing.standalone;

import static och.api.model.PropKey.*;
import static och.util.Util.*;

import java.util.List;

import och.api.model.chat.host.ClientHost;
import och.comp.cache.server.CacheServerContext;
import och.comp.cache.server.CacheServerContextHolder;
import och.comp.db.main.MainDb;
import och.comp.mail.MailService;
import och.service.props.Props;
import och.util.model.HasInitState;
import och.util.timer.TimerExt;

public class HostMultiOwnersAlarmService implements HasInitState, CacheServerContextHolder {

	private Props props;
	private MailService mailService;
	private MainDb mainDb;
	
	private TimerExt checkTimer;
	
	@Override
	public void setCacheServerContext(CacheServerContext c) {
		props = c.props;
		mailService = c.mailService;
		mainDb = c.mainDb;
	}

	@Override
	public void init() throws Exception {
		
		checkStateForEmpty(props, "props");
		checkStateForEmpty(mailService, "mailService");
		
		if( ! props.getBoolVal(chats_hosts_multiOwnersAlarmUse)){
			return;
		}
		
		checkTimer = new TimerExt("HostMultiOwnersAlarmService-check", false);
		checkTimer.tryScheduleAtFixedRate(()-> 
			doCheckWork(), 
			props.getLongVal(chats_hosts_multiOwnersAlarmDelay), 
			props.getLongVal(chats_hosts_multiOwnersAlarmDelta));
		
	}
	
	public void doCheckWork() throws Exception {
		
		if(props.getBoolVal(toolMode)) return;
		
		boolean important = true;
		Integer minVal = props.getIntVal(chats_hosts_multiOwnersAlarmVal);
		List<ClientHost> hosts = mainDb.clientHosts.getHostsWithOwners(important, minVal);
		
		sendAlarmMailToAdmin(hosts);
		
	}
	
	private void sendAlarmMailToAdmin(List<ClientHost> hosts) {
		
		if(isEmpty(hosts)) return;
		
		if(props.getBoolVal(chats_hosts_multiOwners_DisableSendErrors)) return;
		
		List<String> msgs = convert(hosts, (d)-> toJson(d, true));
		mailService.sendAsyncWarnData("Detected multi owners hosts - maybe cheating", msgs);
	}

}
