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
package och.util.geoip;

import static och.util.Util.*;
import static och.util.ZipUtil.*;
import static och.util.servlet.WebUtil.*;
import com.maxmind.geoip.Country;
import com.maxmind.geoip.LookupService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;


public class GeoIp {
	
	Log log = getLog(getClass());
	LookupService ip4Mapper;
	LookupService ip6Mapper;
	

	public GeoIp(String dataDirPath) {
		this(dataDirPath, true);
	}
	
	public GeoIp(String dataDirPath, boolean inMemory) {
		
		try {
			File dir = new File(dataDirPath);
			if( ! dir.exists()) throw new FileNotFoundException("Can't find GeoIP dir: "+dataDirPath);
			
			File ip4File = new File(dir, "GeoIP.dat");
			File ip6File = new File(dir, "GeoIPv6.dat");
			if( ! ip4File.exists() && ! ip6File.exists()){
				File zipFile = new File(dir, "GeoIP-db.zip");
				if( ! zipFile.exists()) throw new FileNotFoundException("Can't find GeoIP db: "+zipFile);
				unzip(zipFile, dir);
			}
			
			int readMode = inMemory? LookupService.GEOIP_MEMORY_CACHE : LookupService.GEOIP_STANDARD;
			if( ip4File.exists()) ip4Mapper = new LookupService(ip4File, readMode);
			if( ip6File.exists()) ip6Mapper = new LookupService(ip6File, readMode); 

		}catch(IOException e){
			log.error("can't load get ip files: "+e);
		}
	}


	public String getCountry(String ip){
		Country out = getCountryData(ip);
		return out == null? null : out.getName();
	}
	
	public String getCountryCode(String ip){
		Country out = getCountryData(ip);
		return out == null? null : out.getCode();
	}

	public Country getCountryData(String ip) {
		if( ! hasText(ip)) return null;
		try {
			Country out = null;
			
			if( ! isIp6_SimpleCheck(ip)) out = ip4Mapper != null? ip4Mapper.getCountry(ip) : null;
			else out = ip6Mapper != null? ip6Mapper.getCountryV6(ip) : null;
			
			if(out != null && "--".equals(out.getCode())) 
				out = null;
			
			return out;
			
		}catch(Exception e){
			log.error("can't getCountry for ip "+ip+": "+e);
			return null;
		}
	}

}
