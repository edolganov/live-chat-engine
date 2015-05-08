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

import static och.api.model.PropKey.*;
import static och.api.model.user.SecurityContext.*;
import static och.api.model.user.UserRole.*;
import static och.service.props.PropsOps.*;
import static och.util.ExceptionUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.sql.SingleTx.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import och.api.model.PropKey;
import och.api.model.tariff.Tariff;
import och.api.model.user.User;
import och.api.model.user.UserRole;
import och.comp.cache.client.CacheClient;
import och.comp.captcha.Captcha;
import och.comp.db.base.BaseDb;
import och.comp.db.main.MainDb;
import och.comp.mail.MailService;
import och.comp.paypal.PaypalClient;
import och.comp.paypal.PaypalClientStub;
import och.comp.paypal.PaypalSoapClient;
import och.comp.template.Templates;
import och.comp.tocheckout.ToCheckoutProvider;
import och.front.service.event.DBInitedEvent;
import och.service.AsyncService;
import och.service.BaseApp;
import och.service.EventService;
import och.service.props.Props;
import och.util.Util;
import och.util.geoip.GeoIp;

import org.apache.commons.logging.Log;


public class FrontApp extends BaseApp {
	
	public final static String DEFAULT_INIT_CHAT_ACC_1 = "demo";
	public final static String DEFAULT_INIT_CHAT_ACC_2 = "demo2";
	
	private Log log = getLog(getClass());
	
	FrontAppContext c;
	
	public final String id;
	
	public final SecurityService security;
	public final ChatService chats;
	public final UserService users;
	public final Captcha captcha;
	public final BillingService billing;
	
	public final AdminService admin;
	
	

	public FrontApp(FrontAppContext context) throws Exception {
		super(context);
		this.c = context;
		c.root = this;
		this.id = c.props.getVal(frontApp_id, Util.randomSimpleId());
		
		ArrayList<BaseFrontService> services = new ArrayList<>();
		
		captcha = c.captcha;
		admin = addToList(services, new AdminService(c));
		security = addToList(services, new SecurityService(c));
		chats = addToList(services, new ChatService(c));
		users = addToList(services, new UserService(c));
		billing = addToList(services, new BillingService(c));
		
		//final init
		for (BaseFrontService s : services) s.init();
		
		
		initDB();
		
		c.events.fireEvent(new DBInitedEvent());
	}
	
	private <T extends BaseFrontService> T addToList(List<BaseFrontService> services, T s){
		services.add(s);
		return s;
	}


	private void initDB() throws Exception {
		
		if( ! c.db.isNewTables()){
			return;
		}
		
		pushToSecurityContext_SYSTEM_USER();
		setSingleTxMode();
		try {
			
			//servers
			String serversInitVal = props.getStrVal(chats_server_init_urls);
			List<String> serversData = strToList(serversInitVal, ",");
			for (String data : serversData) {
				List<String> dataList = strToList(data," ");
				if(dataList.size() < 2){
					log.error("invalid server data: "+data);
					continue;
				}
				String httpUrl = dataList.get(0);
				String httpsUrl = dataList.get(1);
				chats.createServer(httpUrl, httpsUrl);
			}
			
			
			//admins
			List<Long> adminsIds = initUsers(users_init_adminLogins, users_init_adminPsws, ADMIN);
			initUsers(users_init_moderLogins, users_init_moderPsws, MODERATOR);
			
			
			//tariffs
			{
				//pause tariff
				chats.createTariff(props.getBigDecimalVal(tariff_pausePrice), false, 0, Tariff.PAUSE_TARIFF_ID);
				
				//other tariffs
				BigDecimal curTariffPrice = new BigDecimal(props.getLongVal(tariffs_init_val));
				int delta = props.getIntVal(tariffs_init_delta);
				int count = props.getIntVal(tariffs_init_count);
				String publicTariffsVal = props.getStrVal(tariffs_init_publicIds);
				Set<Long> publicTariffs = new HashSet<>(convert(strToList(publicTariffsVal), (s)-> tryParseLong(s, 0L)));
				
				HashMap<Long, Integer> maxOperatorsById = new HashMap<Long, Integer>();
				List<String> maxOps = strToList(props.getStrVal(tariffs_maxOperators));
				for(String pair : maxOps){
					List<String> idCount = strToList(pair, "-");
					if(isEmpty(idCount) || idCount.size() < 2) continue;
					maxOperatorsById.put(tryParseLong(idCount.get(0), -1L), tryParseInt(idCount.get(1), 0));
				}
				
				for(int i=0; i < count; i++){
					
					long expectedId = i+1;
					boolean isPublic = publicTariffs.contains(expectedId);
					Integer maxOperators = maxOperatorsById.get(expectedId);
					if(maxOperators == null) maxOperators = 0;
					chats.createTariff(curTariffPrice, isPublic, maxOperators);
					
					curTariffPrice = curTariffPrice.add(new BigDecimal(delta));
				}
			}
			
			
			ArrayList<String> chatsUids = new ArrayList<>();
			
			if( ! isEmpty(adminsIds)){
				
				Long chatOwnerId = adminsIds.get(0);
				
				//chats
				String chatsInitVal = props.getVal(chats_init_accounts, "1 "+DEFAULT_INIT_CHAT_ACC_1+",1 "+DEFAULT_INIT_CHAT_ACC_2);
				long tariffId = props.getLongVal(chats_init_tariff);
				List<String> chatsData = strToList(chatsInitVal, ",");
				for (String data : chatsData) {
					List<String> dataList = strToList(data," ");
					if(dataList.size() < 2){
						log.error("invalid chat data: "+data);
						continue;
					}
					long serverId = tryParseLong(dataList.get(0), 0L);
					if(serverId == 0L){
						log.error("invalid serverId for chat: "+data);
						continue;
					}
					
					String uid = dataList.get(1);
					String chatName = null;
					boolean adminNotify = false;
					chats.createAcc(serverId, uid, chatOwnerId, chatName, tariffId, adminNotify);
					chatsUids.add(uid);
				}
			}
			

			
			//owner operators for chats
			if( ! isEmpty(chatsUids) && ! isEmpty(adminsIds)){
				for (String uid : chatsUids) {					
					for (Long userId : adminsIds) {
						chats.setOperatorForAcc(uid, userId);
					}
				}
			}
			
		}catch (Throwable t) {
			rollbackSingleTx();
			throw getExceptionOrThrowError(t);
		} 
		finally {
			popUserFromSecurityContext();
			closeSingleTx();
		}
	}
	

	
	private List<Long> initUsers(PropKey usersProp, PropKey pswsProp, UserRole... roles) throws Exception {
		List<String> logins = strToList(props.getStrVal(usersProp));
		List<String> psws = strToList(props.getStrVal(pswsProp));
		if(logins.size() == 0 || logins.size() != psws.size()){
			throw new IllegalStateException("can't init users with invalid data:"+logins+", "+psws);
		}
		
		ArrayList<Long> ids = new ArrayList<>();
		for (int i = 0; i < logins.size(); i++) {
			String login = logins.get(i);
			String psw = psws.get(i);
			String email = login + "@system";
			long id = users.createUser(new User(login, email), psw, false);
			users.activateUser(id);
			users.setRoles(id, set(roles));
			log.info("created init user ["+login+"] with roles: "+set(roles));
			ids.add(id);
		}
		return ids;
	}
	
	
	
	
	
	public static FrontApp create(Props props, ServletContext servletContext) throws Exception {
		return create(props, servletContext, null, null);
	}


	public static FrontApp create(
			Props props, 
			ServletContext servletContext, 
			MailService mails,
			PaypalClient paypalClient) throws Exception {
		
		DataSource ds = BaseDb.createDataSource(props);
		MainDb db = new MainDb(ds, props);
		AsyncService async = new AsyncService();
		EventService events = new EventService();
		CacheClient cache = new CacheClient(props);
		Templates templates = new Templates(props);
		Captcha captcha = new Captcha(props);
		
		if(mails == null) mails = new MailService(props);
		if(paypalClient == null) paypalClient = props.getBoolVal(paypal_clientStub) ? 
				new PaypalClientStub() : PaypalSoapClient.create(props);
				
		ToCheckoutProvider toCheckoutProvider = new ToCheckoutProvider(props); 
		GeoIp geoIp = new GeoIp(props.getStrVal(geo_ip_dbPath), props.getBoolVal(geo_ip_dbInRAM));
		
		addUpdateSecureKeyListener(props, cache, cache_encyptedKey);
		
		FrontAppContext context = new FrontAppContext(
				props, 
				async, 
				events, 
				db, 
				cache, 
				mails,
				templates,
				captcha,
				paypalClient,
				toCheckoutProvider,
				geoIp);
		
		FrontApp out = new FrontApp(context);
		out.security.init(servletContext);
		
		return out;
	}

}
