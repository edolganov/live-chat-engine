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
package och.service.props.impl;


import static java.util.Collections.*;
import static och.util.FileUtil.*;
import static och.util.MapUtil.*;
import static och.util.Util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import och.util.concurrent.ExecutorsUtil;

import org.apache.commons.logging.Log;


public class FileProps extends BaseProps {
	
	private static Log log = getLog(FileProps.class);
	
	private static ScheduledExecutorService syncService;
	private static synchronized ScheduledExecutorService getExecutorService(){
		if(syncService == null){
			syncService = ExecutorsUtil.newScheduledThreadPool("FileProps-sync", 1);
		}
		return syncService;
	}
	
	
	
	public static interface PropsModifier {
		Properties modify(Properties props);
	}
	
	public static FileProps createPropsWithoutUpdate(File propsFile){
		return new FileProps(propsFile, 0, null);
	}
	
	public static List<FileProps> createFileProps(Collection<String> paths){
		LinkedList<FileProps> list = new LinkedList<>();
		for (String path : paths) {
			File file = new File(path);
			if( ! file.exists()){
				log.error("can't find file by path: "+path);
				continue;
			}
			list.addFirst(new FileProps(file));
		}
		return list;
	}

	
	private final File file;
	private final PropsModifier propsModifier;
	private volatile long curModified;
	private volatile Properties state = new Properties();
	
	public FileProps(String propsPath){
		this(new File(propsPath));
	}
	
	public FileProps(File propsFile){
		this(propsFile, null);
	}
	
	public FileProps(File propsFile, PropsModifier propsModifier){
		this(propsFile, 1000*60, propsModifier);
	}
	
	public FileProps(File propsFile, long updateTimeMs, PropsModifier propsModifier){
		
		this.file = propsFile;
		this.propsModifier = propsModifier;
		
		updateFromFileIfNeed();
		
		updateTimeMs = updateTimeMs > 0 && updateTimeMs < 50? 50 : updateTimeMs;
		if(updateTimeMs > 0){
			getExecutorService().scheduleWithFixedDelay(() -> 
				updateFromFileIfNeed(), updateTimeMs, updateTimeMs, TimeUnit.MILLISECONDS);
		}

		
	}


	public File getFile(){
		return file;
	}

	@Override
	public String getVal(Object oKey, String defaultVal) {
		String key = String.valueOf(oKey);
		Properties props = state;
		return props.containsKey(key)? 
				(String)props.get(key) 
				: defaultVal;
	}
	

	@Override
	public void putVal(Object oKey, String val) {
		String key = String.valueOf(oKey);
		putObjVal(key, val);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, String> toMap() {
		HashMap<String, String> out = new HashMap<>((Map)state);
		return out;
	}


	
	private synchronized void putObjVal(String key, String val){
		
		Properties props = state;
		
		Properties newProps = new Properties();
		newProps.putAll(props);
		if(val == null) newProps.remove(key);
		else newProps.put(key, val);
		saveToFile(newProps);
		
		fireChangedEvent(singleton(key));
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized void updateFromFileIfNeed() {
		long lastModified = file.lastModified();
		if(curModified == lastModified) return;
		
		log.info("load props from "+file.getAbsolutePath());
		Properties props = null;
		try {
			
			FileInputStream is = new FileInputStream(file);
			Properties fromFile = new Properties();
			fromFile.load(new InputStreamReader(is, "UTF-8"));
			is.close();
			
			props = fromFile;
			
		}
		//empty props
		catch(FileNotFoundException e){
			props = new Properties();
		}
		//invalid read
		catch (Exception e) {
			props = null;
			log.error("can't read props", e);
		}
		
		if(props == null) return;
		Properties newState = modifyProps(props);
		Properties oldState = state;
		
		this.curModified = lastModified;
		this.state = newState;
		
		Set<String> keys = getUpdatedKeys(oldState, newState);
		if( ! isEmpty(keys)) {
			fireChangedEvent(keys);
		}
	}


	private void saveToFile(Properties newProps) {
		
		log.info("save props to "+file.getAbsolutePath());
		
		Properties newState = modifyProps(newProps);
		
		StringBuilder sb = new StringBuilder();
		sb.append("#Props from app");
		for (Entry<Object, Object> entry : newProps.entrySet()) {
			sb.append("\n"+entry.getKey()+"="+entry.getValue());
		}
		
		
		try {
			replaceFileUTF8(file, sb.toString());
		}catch (Exception e) {
			log.error("can't saveToFile", e);
			return;
		}
		
		this.curModified = file.lastModified();
		this.state = newState;
	}
	
	public Properties modifyProps(Properties props){
		PropsModifier curModifier = this.propsModifier;
		if(curModifier == null) return props;
		return curModifier.modify(props);
	}

	@Override
	public String toString() {
		return "FileProps [file=" + file + "]";
	}

	
	
}
