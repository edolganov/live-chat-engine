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
import static och.comp.chats.history.LogsArchiveImpl.*;
import static och.util.DateUtil.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import och.util.ZipUtil;

import org.junit.Test;

import test.BaseTest;

public class LogsArchiveImplTest extends BaseTest {
	
	@Test
	public void test_scanPeriod(){
		
		long scanPeriod = ARCH_SCAN_PERIOD;
		assertEquals(1000*60*60*24, scanPeriod);
		
		Date now = new Date();
		Date noNeedScanDate = new Date(now.getTime() - scanPeriod + 5000);
		Date needScanDate = new Date(now.getTime() - scanPeriod - 5000);
		
		LogsArchiveImpl arc = new LogsArchiveImpl();
		assertNotNull(arc.tryCreateArcsIfNeed(TEST_DIR, "test", null));
		assertEquals(now, arc.tryCreateArcsIfNeed(TEST_DIR, "test", now));
		assertEquals(noNeedScanDate, arc.tryCreateArcsIfNeed(TEST_DIR, "test", noNeedScanDate));
		
		Date newDate = arc.tryCreateArcsIfNeed(TEST_DIR, "test", needScanDate);
		assertTrue(newDate+" "+now, newDate.compareTo(now) > -1);
		
	}
	
	@Test
	public void test_work() throws Exception{
		
		SimpleDateFormat dayFormat = new SimpleDateFormat(YYYY_MM_DD);
		SimpleDateFormat monthFormat = new SimpleDateFormat(YYYY_MM);
		
		Date now = new Date();
		Date curMonth = monthStart(now);
		Date needArcMonth = addMonths(curMonth, -MONTH_BACK);
		Date needArcDate1 = dateEnd(monthEnd(needArcMonth));
		Date needArcDate2 = addDays(needArcDate1, -1);
		Date noNeedArcDate = addDays(needArcDate1, 1);
		
		File accDir = new File(TEST_DIR, "acc-test");
		accDir.mkdir();
		
		File logDir1 = new File(accDir, "logs_"+dayFormat.format(needArcDate1));
		logDir1.mkdir();
		File fileA = new File(logDir1, "fileA");
		fileA.createNewFile();
		
		File logDir2 = new File(accDir, "logs_"+dayFormat.format(needArcDate2));
		logDir2.mkdir();
		File fileB = new File(logDir2, "fileB");
		fileB.createNewFile();
		
		File logDir3 = new File(accDir, "logs_"+dayFormat.format(noNeedArcDate));
		logDir3.mkdir();
		File fileC = new File(logDir3, "fileC");
		fileC.createNewFile();
		
		LogsArchiveImpl arc = new LogsArchiveImpl();
		arc.tryCreateArcsIfNeed(TEST_DIR, "test", null);
		
		File zipFile = new File(accDir, LOGS_ARC_FILE_PREFIX + monthFormat.format(needArcDate1) + ARC_EXT);
		
		assertTrue(zipFile.exists());
		assertFalse(logDir1.exists());
		assertFalse(logDir2.exists());
		assertTrue(logDir3.exists());
		
		File unzipDir = new File(accDir, "unzip");
		ZipUtil.unzip(zipFile, unzipDir);
		
		assertTrue(new File(path(unzipDir, logDir1, fileA)).exists());
		assertTrue(new File(path(unzipDir, logDir2, fileB)).exists());
		assertTrue(fileC.exists());
	}
	

}
