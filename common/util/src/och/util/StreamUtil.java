/*
 * Copyright 2012 Evgeny Dolganov (evgenij.dolganov@gmail.com).
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

import static och.util.Util.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;


public class StreamUtil {

	public static final int DEFAULT_BUFFER_SIZE = 4096;

	public static abstract class ExceptionHandler {

		public void onInputException(Exception e) throws IOException {
			defaultHandle(e);
		}

		public void onOutputException(Exception e) throws IOException {
			defaultHandle(e);
		}

		private void defaultHandle(Exception e) throws IOException {
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			throw ExceptionUtil.getRuntimeExceptionOrThrowError(e);
		}
	}

	public static final void copyAndClose(InputStream in, OutputStream out)
			throws IOException {
		copy(in, out, DEFAULT_BUFFER_SIZE, true, null);
	}

	public static final void copyAndClose(InputStream in, OutputStream out,
			ExceptionHandler exceptionHandler) throws IOException {
		copy(in, out, DEFAULT_BUFFER_SIZE, true, exceptionHandler);
	}

	public static final void copyAndClose(InputStream in, OutputStream out,
			int bufferSize) throws IOException {
		copy(in, out, bufferSize, true, null);
	}

	public static final void copyAndClose(InputStream in, OutputStream out,
			int bufferSize, ExceptionHandler exceptionHandler)
			throws IOException {
		copy(in, out, bufferSize, true, exceptionHandler);
	}

	public static final void copy(InputStream in, OutputStream out,
			boolean close) throws IOException {
		copy(in, out, DEFAULT_BUFFER_SIZE, close, null);
	}

	public static final void copy(InputStream in, OutputStream out,
			boolean close, ExceptionHandler exceptionHandler)
			throws IOException {
		copy(in, out, DEFAULT_BUFFER_SIZE, close, exceptionHandler);
	}

	public static final void copy(InputStream in, OutputStream out,
			int bufferSize, boolean close) throws IOException {
		copy(in, out, bufferSize, close, null);
	}

	public static final void copy(InputStream in, OutputStream out,
			int bufferSize, boolean close, ExceptionHandler exceptionHandler)
			throws IOException {

		if (Util.isEmpty(exceptionHandler)) {
			exceptionHandler = new ExceptionHandler() {};
		}

		try {

			byte[] buffer = new byte[bufferSize];
			int len = 0;
			while (true) {

				// try read
				try {
					len = in.read(buffer);
					if (len < 0) {
						break;
					}
				} catch (Exception e) {
					exceptionHandler.onInputException(e);
					break;
				}

				// try write
				try {
					out.write(buffer, 0, len);
				} catch (Exception e) {
					exceptionHandler.onOutputException(e);
					break;
				}
			}

		} finally {
			if (close) {
				close(in);
				close(out);
			}
		}

	}
	
	/**
	 * @return readed count
	 */
	public static int copyFullBuffer(InputStream is, OutputStream os, int readLimit, int bufferSize) throws IOException {
		int bufferCount = readLimit / bufferSize;
		int readed = 0;
		if(bufferCount > 0){
			byte[] buffer = new byte[bufferSize];
			for (int i = 0; i < bufferCount; i++) {
				is.read(buffer);
				readed += bufferSize;
				os.write(buffer);
			}
		}
		return readed;
	}
	
	
	

	public static final void close(Closeable c) {

		if (c == null) {
			return;
		}

		try {
			c.close();
		} catch (Exception e) {
			System.err.println("can't close "+c+": " + e);
		}
	}
	
	public static String streamToStr(InputStream is) throws IOException {
		return streamToStr(is, UTF8);
	}
	
    public static String streamToStr(InputStream is, String charset) throws IOException {
        InputStreamReader r = new InputStreamReader(is, charset);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[1024];
        try {
            for (int n; (n = r.read(buffer)) != -1;)
                sw.write(buffer, 0, n);
        }
        finally{
            try {
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sw.toString();
    }
    
	public static InputStream strToStream(String str){
		return null;
	}
	
	
	public static BufferedReader getReaderUTF8(InputStream is){
		return getReader(is, UTF8);
	}
	
	public static BufferedReader getReader(InputStream is, String charset){
		return new BufferedReader(new InputStreamReader(is, Charset.forName(charset)));
	}
	
	public static PrintWriter getWriterUTF8(OutputStream os){
		return getWriter(os, UTF8);
	}
	
	public static PrintWriter getWriter(OutputStream os, String charset){
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName(charset)));
		return new PrintWriter(writer, true);
	}

}
