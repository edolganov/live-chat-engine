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
package och.comp.chats.history;

import static och.comp.chats.ChatsAccService.*;
import static och.comp.chats.common.StoreOps.*;
import static och.util.DateUtil.*;
import static och.util.Util.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import och.util.DateUtil;
import och.util.FileUtil;
import och.util.ZipUtil;

import org.apache.commons.logging.Log;

public class LogsArchiveImpl implements LogsArchive {

	public static final String LOGS_ARC_FILE_PREFIX = "logs_arc_";
	public static final String ARC_EXT = ".zip";
	public static final long ARCH_SCAN_PERIOD = 1000*60*60*24;
	public static final String YYYY_MM = "yyyy-MM";
	public static final int MONTH_BACK = 2;

	Log log = getLog(getClass());
	
	@Override
	public Date tryCreateArcsIfNeed(File root, String accId, Date lastScanned){
		
		Date now = new Date();
		if(lastScanned != null 
				&& (now.getTime() - lastScanned.getTime()) <= ARCH_SCAN_PERIOD){
			return lastScanned;
		}
		
		try {
			
			File accDir = new File(root, getAccountDirName(accId));
			String[] names = accDir.list();
			if(isEmpty(names)) return now;
			
			SimpleDateFormat yearMonthFormat = new SimpleDateFormat(YYYY_MM);
			Date curMonth = monthStart(now);
			Date needArcMonth = addMonths(curMonth, -MONTH_BACK);
			Date needArcDate = dateEnd(monthEnd(needArcMonth));
			long needArcTime = needArcDate.getTime();
			
			//dirs to arc
			HashMap<String, List<String>> toArcs = new HashMap<>();
			for (String name : names) {
				Date date = parseDate(name);
				if(date == null) continue;
				if(date.getTime() > needArcTime) continue;

				String arcId = LOGS_ARC_FILE_PREFIX + yearMonthFormat.format(date) + ARC_EXT;
				putToListMap(toArcs, arcId, name);
			}
			
			//create arcs
			for (Entry<String, List<String>> entry : toArcs.entrySet()) {
				String arcName = entry.getKey();
				List<String> dirs = entry.getValue();
				createArc(accDir, arcName, dirs);
			}
			
		}catch (Throwable t) {
			log.error("can't create chat archives: "+t);
		}
		
		return now;
	}
	
	
	
	
	

	private static void createArc(File accDir, String arcName, List<String> dirs) throws IOException {
		
		File zipFile = new File(accDir, arcName);
		if(zipFile.exists()){
			zipFile.renameTo(new File(accDir, zipFile.getName()+"-"+System.currentTimeMillis()+".BAK"));
			zipFile.delete();
		}
		
		List<File> files = new ArrayList<>();
		for(String name : dirs) files.add(new File(accDir, name));
		
		ZipUtil.zipFiles(files, zipFile);
		
		//remove files
		for(File file : files) FileUtil.deleteDirRecursive(file);
		
	}

	public static Date parseDate(String name) {
		try {
			
			if( ! name.startsWith(LOGS_DIR_PREFIX)) return null;
			
			String dateStr = name.substring(LOGS_DIR_PREFIX.length());
			return DateUtil.parseDate(dateStr, YYYY_MM_DD);
			
		}catch (Exception e) {
			return null;
		}
	}

}
