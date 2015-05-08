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
package och.chat.service;

import och.comp.cache.impl.CacheImpl;
import och.comp.chats.ChatsAccService;
import och.comp.mail.MailService;
import och.comp.template.Templates;
import och.service.AsyncService;
import och.service.BaseContext;
import och.service.EventService;
import och.service.props.Props;
import och.service.web.SessionsCounterService;
import och.service.web.SessionsHolderService;
import och.util.geoip.GeoIp;

public class ChatsAppContext extends BaseContext {
	
	
	public final EventService events; 
	public final SessionsCounterService sessionsCounter;
	public final SessionsHolderService sessionsHolder;
	public final ChatsAccService accs;
	public final CacheImpl cache;
	public final MailService mails;
	public final Templates templates;
	public final GeoIp geoIp;
	
	public ChatsApp root;

	public ChatsAppContext(
			Props props,
			AsyncService asyncService,
			EventService events,
			SessionsCounterService sessionsCounter,
			ChatsAccService accs,
			CacheImpl cache,
			SessionsHolderService sessionsHolder,
			MailService mail,
			Templates templates,
			GeoIp geoIp) {
		super(props, asyncService);
		this.events = events;
		this.sessionsCounter = sessionsCounter;
		this.accs = accs;
		this.cache = cache;
		this.sessionsHolder = sessionsHolder;
		this.mails = mail;
		this.templates = templates;
		this.geoIp = geoIp;
	}

}
