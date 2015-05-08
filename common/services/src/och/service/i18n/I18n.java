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
package och.service.i18n;

import static och.util.FileUtil.*;
import static och.util.Util.*;

import java.io.File;
import java.util.HashMap;

import och.service.props.impl.FileProps;

import org.apache.commons.logging.Log;

public class I18n {
	
	private static final Log log = getLog(I18n.class);
	public static final String SEP = "_";
	
	
	private static ThreadLocal<String> threadLocalLang = new ThreadLocal<>();
	
	public static void setThreadLang(String lang){
		threadLocalLang.set(lang);
	}
	
	public static void releaseThreadLang(){
		threadLocalLang.remove();
	}
	
	public static String getThreadLang(){
		return threadLocalLang.get();
	}
	
	public static boolean isThreadLang_EN(){
		return "en".equals(getThreadLang());
	}
	
	public static boolean isThreadLang_RU(){
		return "ru".equals(getThreadLang());
	}
	
	
	private FileProps def;
	private HashMap<String, FileProps> byLang = new HashMap<String, FileProps>();
	
	public I18n(File root, String defName) {
		
		File[] files = root.listFiles();
		if( isEmpty(files)) return;
		
		
		for (File file : files) {
			if(file.isDirectory())continue;
			String name = file.getName();
			if( ! name.startsWith(defName)) continue;
			
			name = getFileNameWithoutType(name);
			if(name.equals(defName)){
				def = new FileProps(file);
				log.info("found def lang for '"+defName+"'");
			} 
			else {
				
				String lang = name.substring(defName.length());
				if(lang.startsWith(SEP)) lang = lang.substring(SEP.length());
				
				byLang.put(lang, new FileProps(file));
				log.info("found '"+lang+"' lang for '"+defName+"'");
			}
		}
	}
	
	
	public String label(String key){
		return getLabel(getThreadLang(), key, null);
	}
	
	public String label(String key, String defVal){
		return getLabel(getThreadLang(), key, defVal);
	}
	
	public String labelFor(String lang, String key){
		return getLabel(lang, key, null);
	}
	
	public String labelFor(String lang, String key, String defVal){
		return getLabel(lang, key, defVal);
	}
	
	public String getLabel(String lang, String key){
		return getLabel(lang, key, null);
	}
	

	public String getLabel(String lang, String key, String defVal){
		
		if( ! hasText(key)) return defVal;
		if( defVal == null) defVal = "'" + key + "'";
		
		FileProps props = byLang.get(lang);
		if(props == null) props = def;
		if(props == null) return defVal;
		
		String val = props.getVal(key);
		if(val == null && props != def) val = def.getVal(key);
		if(val == null) return defVal;
		return val;
	}
	


}
