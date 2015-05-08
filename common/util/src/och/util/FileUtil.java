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


import static och.util.NumberUtil.*;
import static och.util.Util.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;

import och.util.StreamUtil.ExceptionHandler;
import och.util.io.OpenByteArrayOutputStream;

import org.apache.commons.logging.Log;



public class FileUtil {

	private final static Log log = getLog(FileUtil.class);
	private final static String SLASH = "/";
	private final static String INVERSE_SLASH = "\\";
	private final static char DOT = '.';
	
	
	public static boolean deleteFileOrDirRecursive(File file) {
		if(file.isFile()){
			return file.delete();
		} else {
			return deleteDirRecursive(file);
		}

	}

	public static boolean deleteDirRecursive(String path) {
		return deleteDirRecursive(new File(path));
	}

	public static boolean deleteDirRecursive(File dir) {
		if (dir.exists()) {
			
			if( ! dir.isDirectory()){
				throw new IllegalArgumentException(""+dir+" is not directory");
			}
			
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirRecursive(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (dir.delete());
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.getParentFile().mkdirs();
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
	
	public static String readFileUTF8(FileInputStream fis) throws IOException {
		return readFile(fis, UTF8);
	}
	
	public static String tryReadFileUTF8(File file){
		try {
			return readFileUTF8(file);
		}catch(Throwable t){
			log.error("can't readFileUTF8: "+t);
			return null;
		}
	}

	public static String readFileUTF8(File file) throws IOException {
		return readFile(file, UTF8);
	}

	public static String readFile(File file, String charset) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		return readFile(fis, charset);
	}

	public static String readFile(FileInputStream fis, String charset) throws UnsupportedEncodingException, IOException {
		InputStreamReader r = null;
		OutputStreamWriter w = null;

		try {
			r = new InputStreamReader(fis, charset);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			w = new OutputStreamWriter(out, charset);
			char[] buff = new char[1024 * 4];
			int i;
			while ((i = r.read(buff)) > 0) {
				w.write(buff, 0, i);
			}
			w.flush();
			return out.toString(charset);
		} finally {
			if (r != null)
				try {
					r.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			if (w != null)
				try {
					w.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	
	
	public static byte[] readFile(File file) throws IOException {
		long length = file.length();
		if(length > Integer.MAX_VALUE) throw new IllegalStateException("File to large to byte[] array: "+length+" bytes");
		if(length == 0) return new byte[0];
		int size = (int) length;
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		OpenByteArrayOutputStream bos = new OpenByteArrayOutputStream(size);
		StreamUtil.copy(bis, bos, true);
		return bos.getBuf();
	}
	
	
	public static void replaceFileUTF8(File file, String text) throws IOException {
		
		File bak = null;
		
		if(file.exists()) {
			File parent = file.getParentFile();
			bak = new File(parent, file.getName()+"-"+randomSimpleId()+".BAK");
			copyFile(file, bak);
		}
		
		writeFileUTF8(file, text);
		
		if(bak != null) bak.delete();
		
	}
	
	
	public static void tryWriteFileUTF8(File file, String text) {
		try {
			file.createNewFile();
			writeFileUTF8(file, text);
		}catch(Throwable t){
			log.error("can't writeFileUTF8: "+t);
		}
	}

	public static void writeFileUTF8(File file, String text) throws IOException {
		writeFile(file, text, UTF8, false);
	}
	
	public static void writeFileUTF8(File file, String text, boolean append) throws IOException {
		writeFile(file, text, UTF8, append);
	}


	public static void writeFile(File file, String text, String charset) throws IOException {
		writeFile(file, text, charset, false);
	}

	public static void writeFile(File file, String text, String charset, boolean append) throws IOException {
		FileOutputStream fos = new FileOutputStream(file, append);
		writeFile(fos, text, charset);
	}
	
	public static void writeFileUTF8(FileOutputStream fos, String text) throws IOException {
		writeFile(fos, text, UTF8);
	}
	

	public static void writeFile(FileOutputStream fos, String text, String charset) throws UnsupportedEncodingException, IOException {
		byte[] bytes = text.getBytes(charset);
		fos.write(bytes);
		fos.close();
	}
	
	public static void writeFile(File file, byte[] bytes) throws IOException {
		writeFile(file, bytes, false);
	}
	
	public static void writeFile(File file, byte[] bytes, boolean append) throws IOException {
		if( ! file.exists()) file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file, append);
		fos.write(bytes);
		fos.close();
	}
	
	

	/**
	 * получить имя файла из пути
	 * 
	 * @param path
	 *            путь до файла с прямыми, либо обратными слешами
	 * @return строка после последнего слеша в path, либо (если слешей нет) сам
	 *         path
	 */
	public static String getFileNameFromPath(String path) {
		String name = path;
		int index = Math.max(path.lastIndexOf(SLASH),
				path.lastIndexOf(INVERSE_SLASH));
		if (index > -1) {
			name = path.substring(index + 1);
		}
		return name;
	}

	public static String toUnixPath(String path) {
		return path.replace(INVERSE_SLASH, SLASH);
	}

	public static String getFileType(File file) {
		return getFileType(file.getName());
	}

	/**
	 * получить тип файла из пути
	 * 
	 * @param path
	 *            - "путь/имя.тип"
	 * @return "тип", либо null если не нашел точку в конце строки
	 */
	public static String getFileType(String path) {
		int lastDot = path.lastIndexOf(DOT);
		if (lastDot < 0)
			return null;
		else
			return path.substring(lastDot + 1);
	}

	/**
	 * получить тип файла из пути
	 * 
	 * @param id
	 *            - "имя.тип"
	 */
	public static String getFileNameWithoutType(String name) {
		int lastDot = name.lastIndexOf(DOT);
		if (lastDot < 0)
			return name;
		else
			return name.substring(0, lastDot);
	}

	public static BufferedReader getBufferedReaderUTF8(File file)
			throws IOException {
		return getBufferedReader(file, "utf-8");
	}

	public static BufferedReader getBufferedReader(File file, String charset)
			throws IOException {
		FileInputStream is = new FileInputStream(file);
		InputStreamReader fileReader = new InputStreamReader(is, charset);
		return new BufferedReader(fileReader);
	}

	public static BufferedOutputStream getBufferedOutputStream(File destFile)
			throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(destFile));
	}

	public static void writeFile(Object obj, File file) throws Exception {

		FileOutputStream os = new FileOutputStream(file, false);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		try {

			oos.writeObject(obj);

		} catch (Exception e) {
			file.delete();
			throw e;
		} finally {
			StreamUtil.close(oos);
		}
	}


	public static void writeFile(InputStream in, File file,
			ExceptionHandler exceptionHandler) throws IOException {
		try {

			OutputStream os = new FileOutputStream(file, false);
			StreamUtil.copy(in, os, StreamUtil.DEFAULT_BUFFER_SIZE, true,
					exceptionHandler);

		} catch (Exception e) {

			file.delete();

			if (e instanceof IOException) {
				throw (IOException) e;
			} else {
				throw ExceptionUtil.getRuntimeExceptionOrThrowError(e);
			}
		}
	}
	
	
	public static File[] listFilesWithNameStart(File root, final String nameStart){
		return root.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.getName().startsWith(nameStart);
			}
		});
	}

	/**
	 * get index from name like "some{separator}{[000]index}[.type]"
	 */
	public static int getFileNameIndex(String name, String separator) {
		name = getFileNameWithoutType(name);
		int sepIndex = name.lastIndexOf(separator);
		if(sepIndex < 0) return 0;
		return (int)getLongFromZeroFormattedStr(name.substring(sepIndex+1));
	}

	public static void createDir(String path) {
		new File(path).mkdirs();
	}
	
	public static void tryCreateNewFile(File file){
		try {
			file.createNewFile();
		}catch(Throwable t){
			log.error("can't createNewFile", t);
		}
	}

}
