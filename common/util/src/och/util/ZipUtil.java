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
package och.util;

import static och.util.StreamUtil.*;
import static och.util.Util.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import och.util.model.Pair;

public class ZipUtil {
	
	
	public static void zipSingleFile(File source, File destFile) throws IOException{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destFile));
		ZipEntry ze = new ZipEntry(source.getName());
		zos.putNextEntry(ze);
		StreamUtil.copy(bis, zos, false);
		bis.close();
		zos.closeEntry();
		zos.close();
	}


	public static void zipFiles(List<File> files, File destFile) throws IOException {
		ZipOutputStream zip = null;
		FileOutputStream fos = null;
		try {
			
			fos = new FileOutputStream(destFile);
			zip = new ZipOutputStream(fos);
			
			LinkedList<Pair<String, File>> queue = new LinkedList<>();
			for (File file : files) queue.addLast(new Pair<>("", file));
			
			while( ! queue.isEmpty()){
				
				Pair<String, File> pair = queue.removeFirst();
				String path = pair.first;
				File file = pair.second;
				if(file.isFile()){
			        FileInputStream in = new FileInputStream(file);
			        byte[] buf = new byte[2048];
			        int len;
			        zip.putNextEntry(new ZipEntry(path + "/" + file.getName()));
			        while ((len = in.read(buf)) > 0) {
			            zip.write(buf, 0, len);
			        }
			        in.close();
					continue;
				}
				
				File[] children = file.listFiles();
				if(isEmpty(children)) {
					zip.putNextEntry(new ZipEntry(path + "/" + file.getName() + "/"));
					continue;
				}
				
				for (File child : children) {
					queue.addLast(new Pair<>(path + "/" + file.getName(), child));
				}
				
			}
			
		}finally {
			close(zip);
			close(fos);
		}
	}
	
	
	public static void unzip(File zipFile, File destDir) throws IOException {
		
		int BUFFER = 2048;

	    ZipFile zip = new ZipFile(zipFile);

	    destDir.mkdirs();
	    
	    Enumeration<? extends ZipEntry> elems = zip.entries();
	    while (elems.hasMoreElements()){
	        
	    	ZipEntry entry = elems.nextElement();
	        String currentEntry = entry.getName();
	        
	        File destFile = new File(destDir, currentEntry);
	        
	        destFile.getParentFile().mkdirs();

	        if ( ! entry.isDirectory()){
	            BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
	            int currentByte;
	            byte data[] = new byte[BUFFER];
	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
	            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
	        } 
	        else {
	        	destFile.mkdir();
	        }
	    }
	    
	    zip.close();
	}


	
}
