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
package och.front.web;

import static och.api.model.PropKey.*;
import static och.comp.web.AppProviderUtil.*;
import static och.service.props.PropsOps.*;
import static och.util.Util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import och.front.service.FrontApp;
import och.service.props.Props;
import och.service.props.impl.MultiProps;
import och.service.props.impl.NetPropsClient;

import org.apache.commons.logging.Log;


public class FrontAppProvider {
	
	private static Log log = getLog(FrontAppProvider.class);
	
	public static volatile Props directProps = null;
	public static volatile FrontApp lastCreated;
	
	
	public static synchronized FrontApp get(ServletContext servletContext) throws ServletException {
		
		try {
			if(servletContext == null) throw new IllegalArgumentException("servletContext must be not null");
			
			FrontApp app = (FrontApp) servletContext.getAttribute("FrontApp");
			
			if(app == null) {
				app = createApp(servletContext);
				servletContext.setAttribute("FrontApp", app);
				lastCreated = app;
			}
			return app;
			
		}catch (Throwable t) {
			log.error("can't get app", t);
			throw new ServletException("can't init: "+t, t);
		}
	}
	
	private static FrontApp createApp(ServletContext servletContext) throws Exception{
		
		Props props = directProps;
		
		//from file and net
		if(props == null){
			String webInfPath = servletContext.getRealPath("./WEB-INF");
			
			String configPath = System.getProperty("och.chat.propsDir");
			if(configPath == null) {
				configPath = webInfPath;
			}
			
			Props startProps = createProps(configPath+"/front.properties", webInfPath);
			if(System.getProperty("och.skipNetProps") != null){
				props = startProps;
			} 
			else {
				
				NetPropsClient netPropsClient = new NetPropsClient(
						startProps.getStrVal(netProps_front_host), 
						startProps.getIntVal(netProps_front_port), 
						startProps.getStrVal(netProps_front_secureKey), 
						startProps.getBoolVal(netPropsClient_waitConnect), 
						startProps.getLongVal(netPropsClient_updateTime));
				
				addUpdateSecureKeyListener(startProps, netPropsClient, netProps_front_secureKey);
				
				Props netProps = netPropsClient.getProps();
				
				props = new MultiProps(startProps, netProps);
			}
		}
		

		return FrontApp.create(props, servletContext);
	}
	


}
