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

import static och.api.model.PropKey.*;
import static och.util.Util.*;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import och.comp.cache.impl.CacheImpl;
import och.comp.chats.ChatsAccService;
import och.comp.mail.MailService;
import och.comp.template.Templates;
import och.service.AsyncService;
import och.service.BaseApp;
import och.service.EventService;
import och.service.props.Props;
import och.service.web.SessionsCounterService;
import och.service.web.SessionsHolderService;
import och.util.geoip.GeoIp;


public class ChatsApp extends BaseApp{
	
	private ChatsAppContext c;
	
	public final String id;
	public final SecurityService security; 
	public final SessionsCounterService sessionsCounter;
	public final SessionsHolderService sessionsHolder;
	public final ChatsService chats;
	public final GeoIp geoIp;
	

	public ChatsApp(ChatsAppContext context) {
		super(context);
		this.c = context;
		this.id = c.props.getVal(chatApp_id, randomSimpleId());
		c.root = this;
		
		this.security = new SecurityService(c);
		this.chats = new ChatsService(c);
		this.sessionsCounter = c.sessionsCounter;
		this.sessionsHolder = c.sessionsHolder;
		this.geoIp = c.geoIp;
	}
	
	
	
	public static ChatsApp create(Props props, ServletContext servletContext) throws IOException {
		return create(props, servletContext, null, null);
	}


	public static ChatsApp create(
			Props props, 
			ServletContext servletContext,
			ChatsAccService accs,
			MailService mails) throws IOException {
		
		AsyncService asyncService = new AsyncService();
		EventService events = new EventService();
		SessionsCounterService sessionsCounter = new SessionsCounterService();
		SessionsHolderService sessionsHolder = new SessionsHolderService();
		GeoIp geoIp = new GeoIp(props.getStrVal(geo_ip_dbPath), props.getBoolVal(geo_ip_dbInRAM));
		
		if(accs == null) accs = new ChatsAccService(new File(props.getStrVal(chats_rootDir)), props);
		accs.setGeoIp(geoIp);
		
		if(mails == null) mails = new MailService(props);
		
		Templates templates = new Templates(props);
		
		CacheImpl cache = new CacheImpl(props.getLongVal(cache_cleanTime));
		cache.run();
		

		
		ChatsAppContext context = new ChatsAppContext(
				props,
				asyncService,
				events,
				sessionsCounter,
				accs,
				cache,
				sessionsHolder,
				mails,
				templates,
				geoIp);
		
		ChatsApp out = new ChatsApp(context);
		
		//extra web init
		sessionsCounter.init(servletContext);
		sessionsHolder.init(servletContext);
		out.security.init(servletContext);
		
		return out;
	}



}
