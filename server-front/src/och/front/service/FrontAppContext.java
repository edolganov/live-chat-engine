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

import och.comp.cache.client.CacheClient;
import och.comp.captcha.Captcha;
import och.comp.db.main.MainDb;
import och.comp.mail.MailService;
import och.comp.paypal.PaypalClient;
import och.comp.template.Templates;
import och.comp.tocheckout.ToCheckoutProvider;
import och.service.AsyncService;
import och.service.BaseContext;
import och.service.EventService;
import och.service.props.Props;
import och.util.geoip.GeoIp;

public class FrontAppContext extends BaseContext {

	public final EventService events;
	public final MainDb db;
	public final CacheClient cache;
	public final MailService mails;
	public final Templates templates;
	public final Captcha captcha;
	public final PaypalClient paypalClient;
	public final ToCheckoutProvider toCheckoutProvider;
	public final GeoIp geoIp;
	
	public FrontApp root;

	public FrontAppContext(
			Props props, 
			AsyncService async, 
			EventService events, 
			MainDb db,
			CacheClient cache,
			MailService mails,
			Templates templates,
			Captcha captcha,
			PaypalClient paypalClient,
			ToCheckoutProvider toCheckoutProvider,
			GeoIp geoIp) {
		super(props, async);
		this.events = events;
		this.db = db;
		this.cache = cache;
		this.mails = mails;
		this.templates = templates;
		this.captcha = captcha;
		this.paypalClient = paypalClient;
		this.toCheckoutProvider = toCheckoutProvider;
		this.geoIp = geoIp;
	}
	
	
}
