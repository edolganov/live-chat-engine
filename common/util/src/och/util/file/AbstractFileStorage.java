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
package och.util.file;


import static och.util.Util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import och.util.FileUtil;
import och.util.StreamUtil;
import och.util.Util;

import org.apache.commons.logging.Log;





public abstract class AbstractFileStorage {

	
	private LockManager lockManager = LockManager.instance;
	
	public abstract File getFile(String path) throws Exception;

	
	public String readFileUTF8(String path) throws Exception {
		return readFile(path, UTF8);
	}
	
	public void writeFileUTF8(String path, boolean overrideIfExists, String content) throws Exception {
		writeFile(path, overrideIfExists, content, UTF8);
	}


	public String readFile(String path, final String charset) throws Exception {
		
		final String[] out = new String[1];
		
		readFile(path, new InputStreamCallback() {
			
			@Override
			public void onOpenStream(FileInputStream is) throws Exception {
				String content = FileUtil.readFile(is, charset);
				out[0] = content;
			}
		});
		
		return out[0];
	}
	
	private void writeFile(String path, boolean overrideIfExists, final String content, final String charset) throws Exception {
		
		writeFile(path, overrideIfExists, new OutputStreamCallback() {
			
			@Override
			public void onOpenStream(FileOutputStream os) throws Exception {
				FileUtil.writeFile(os, content, charset);
			}
		});
		
	}
	
	public List<String> getChidrenNames(String path) throws Exception {
		
		lockManager.globalReadLock.lock();
		try {
			
			File dir = getFile(path);
			if( ! dir.exists() || ! dir.isDirectory()){
				return Collections.emptyList();
			}
			
			String[] names = dir.list();
			return Util.list(names);
			
		}finally{
			lockManager.globalReadLock.unlock();
		}
	}
	
	public void deleteFileOrDir(String path) throws Exception{
		lockManager.globalWriteLock.lock();
		try {
			File file = getFile(path);
			FileUtil.deleteFileOrDirRecursive(file);
		}finally{
			lockManager.globalWriteLock.unlock();
		}
	}

	public void readFile(String path, InputStreamCallback callback) throws Exception {
		
		lockManager.globalReadLock.lock();
		try {
		
			Object lock = lockManager.getFileLock(path);
			synchronized (lock) {
				try {
					
					File file = getFile(path);
					if( ! file.exists()){
						return;
					}
					FileInputStream is = new FileInputStream(file);
					callback.onOpenStream(is);
					StreamUtil.close(is);
	
				}finally {
					lockManager.releaseFileLock(path);
				}
			}
			
		}finally{
			lockManager.globalReadLock.unlock();
		}
	}
	
	public void writeFile(String path, boolean overrideIfExists, OutputStreamCallback callback) throws Exception {
		
		lockManager.globalReadLock.lock();
		try {
		
			Object lock = lockManager.getFileLock(path);
			synchronized (lock) {
				try {
					
					File file = getFile(path);
					if( file.exists() && ! overrideIfExists){
						return;
					}
					
					file.getParentFile().mkdirs();
					file.delete();
					file.createNewFile();
					
					FileOutputStream os = new FileOutputStream(file);
					callback.onOpenStream(os);
					StreamUtil.close(os);
					
				}finally {
					lockManager.releaseFileLock(path);
				}
			}
		
		}finally{
			lockManager.globalReadLock.unlock();
		}
		
	}


	public void globalLock(GlobalLockCallback callback) throws Exception {
		
		lockManager.globalWriteLock.lock();
		try {
			callback.onLock();
		}finally{
			lockManager.globalWriteLock.unlock();
		}
	}



}

class LockManager {
	
	private static class FileLockObject {
		int clientsCount;
	}
	
	static final LockManager instance = new LockManager();
	
	private Log log = getLog(LockManager.class);
	private HashMap<String, FileLockObject> locks = new HashMap<String, FileLockObject>();
	private ReadWriteLock rw = new ReentrantReadWriteLock();
	
	final Lock globalReadLock = rw.readLock();
	final Lock globalWriteLock = rw.writeLock();
	
	
	private LockManager() {
		super();
	}
	
	public synchronized Object getFileLock(String path) {
		
		FileLockObject lock = locks.get(path);
		if(lock == null){
			lock = new FileLockObject();
			locks.put(path, lock);
		}
		lock.clientsCount++;
		return lock;
	}
	
	public synchronized void releaseFileLock(String path) {
		FileLockObject lock = locks.get(path);
		if(lock == null){
			log.warn("error locks state: can't find lock to release by path '"+path+"'");
			return;
		}
		
		lock.clientsCount--;
		if(lock.clientsCount == 0){
			locks.remove(path);
		}
		
	}
	
}
