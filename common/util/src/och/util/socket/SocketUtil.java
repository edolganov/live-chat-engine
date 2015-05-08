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
package och.util.socket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import och.util.StreamUtil;
import och.util.socket.pool.SocketConn;



public class SocketUtil {
	
	
	public static BufferedReader getReaderUTF8(SocketConn conn) throws IOException{
		return getReader(conn.getInputStream(), "UTF-8");
	}

	public static BufferedReader getReaderUTF8(Socket socket) throws IOException{
		return getReader(socket.getInputStream(), "UTF-8");
	}
	
	public static BufferedReader getReader(Socket socket, String charset) throws IOException{
		return getReader(socket.getInputStream(), charset);
	}
	
	public static BufferedReader getReaderUTF8(InputStream socketIn){
		return StreamUtil.getReaderUTF8(socketIn);
	}
	
	public static BufferedReader getReader(InputStream socketIn, String charset){
		return StreamUtil.getReader(socketIn, charset);
	}
	
	
	
	public static PrintWriter getWriterUTF8(SocketConn conn) throws IOException{
		return getWriter(conn.getOutputStream(), "UTF-8");
	}
	
	public static PrintWriter getWriterUTF8(Socket socket) throws IOException{
		return getWriter(socket.getOutputStream(), "UTF-8");
	}
	
	public static PrintWriter getWriter(Socket socket, String charset) throws IOException{
		return getWriter(socket.getOutputStream(), charset);
	}
	
	public static PrintWriter getWriterUTF8(OutputStream socketOut){
		return StreamUtil.getWriterUTF8(socketOut);
	}
	
	public static PrintWriter getWriter(OutputStream socketOut, String charset){
		return StreamUtil.getWriter(socketOut, charset);
	}

	public static void close(Socket s) {
		try {
			if( s != null && ! s.isClosed()) s.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static IOException findSocketException(Throwable t){
		Throwable old = null;
		while(t != null && t != old){
			if(t instanceof SocketException) return (SocketException)t;
			else if(t instanceof SocketTimeoutException) return (SocketTimeoutException)t;
			old = t;
			t = t.getCause();
		}
		return null;
	}

}
