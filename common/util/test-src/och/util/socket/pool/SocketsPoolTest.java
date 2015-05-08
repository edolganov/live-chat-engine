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
package och.util.socket.pool;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;


import och.junit.AssertExt;
import och.util.socket.pool.SocketConn;
import och.util.socket.pool.SocketConnHandler;
import och.util.socket.pool.SocketsPool;
import och.util.socket.server.SocketServer;
import och.util.socket.server.SocketWriterHander;

import org.junit.Ignore;
import org.junit.Test;

import test.TestException;
import static och.util.socket.SocketUtil.*;

public class SocketsPoolTest extends AssertExt {
	
	String host = "localhost";
	int port = 11001;
	int maxThreads = 10;
	
	
	@Test
	public void test_client_exception() throws Exception {
		
		SocketServer server = createEchoServer(port, 1);
		server.runAsync();

		try {
			SocketsPool pool = new SocketsPool(host, port);
			pool.setPoolMaximumActiveConnections(1);
			pool.setPoolMaximumIdleConnections(1);
			
			try {
				pool.invoke(new SocketConnHandler<Void>() {
					@Override
					public Void handle(SocketConn c) throws IOException {
						c.getWriter().println("test1");
						throw new TestException();
					}
				});
				fail_exception_expected();
			}catch (TestException e) {
				//ok
			}
			
			assertEquals(0, pool.getIdleConnections());
			
			pool.invoke(new SocketConnHandler<Void>() {
				@Override
				public Void handle(SocketConn c) throws IOException {
					c.getWriter().println("test2");
					assertEquals("test2", c.getReader().readLine());
					return null;
				}
			});
			
			assertEquals(1, pool.getIdleConnections());
			
			
			
		} finally {
			server.shutdownWait();
		}
	}
	
	
	@Test
	public void test_client_SocketTimeoutException_fix() throws Exception {
		
		class Context {
			volatile boolean needSleep = true;
		}
		
		final Context cx = new Context();
		final int maxSleep = 100;
		final int waitSleep = 50;
		SocketServer server = new SocketServer(port, maxThreads, new SocketWriterHander() {
			@Override
			protected void process(Socket openedSocket, BufferedReader socketReader, PrintWriter socketWriter, SocketServer owner) throws Throwable {
				while( ! owner.wasShutdown()){
					String line = socketReader.readLine();
					
					try {
						if(cx.needSleep) Thread.sleep(maxSleep);
					}catch (Exception e) {
						e.printStackTrace();
					}
					
					if(line == null || owner.wasShutdown()) break;
					socketWriter.println(line);
				}
			}
		});
		
		server.runAsync();
		
		try {
			SocketsPool pool = new SocketsPool(host, port);
			pool.setSocketSoTimeoutForNewConn(waitSleep);
			
			try {
				pool.invoke(new SocketConnHandler<Void>() {
					@Override
					public Void handle(SocketConn c) throws IOException {
						c.getWriter().println("test1");
						c.getReader().readLine();
						return null;
					}
				});
				fail_exception_expected();
			}catch (SocketTimeoutException e) {
				//ok
			}
			
			assertEquals(0, pool.getIdleConnections()); //<------------------ no active connections after exception
			
			
			
		} finally {
			server.shutdownWait();
		}
	}
	
	
	@Ignore
	@Test
	public void test_client_SocketTimeoutException_wrong_conn_data() throws Exception {
		
		class Context {
			volatile boolean needSleep = true;
		}
		
		final Context cx = new Context();
		final int maxSleep = 300;
		final int waitSleep = 200;
		SocketServer server = new SocketServer(port, maxThreads, new SocketWriterHander() {
			@Override
			protected void process(Socket openedSocket, BufferedReader socketReader, PrintWriter socketWriter, SocketServer owner) throws Throwable {
				while( ! owner.wasShutdown()){
					String line = socketReader.readLine();
					
					try {
						if(cx.needSleep) Thread.sleep(maxSleep);
					}catch (Exception e) {
						e.printStackTrace();
					}
					
					if(line == null || owner.wasShutdown()) break;
					socketWriter.println(line);
				}
			}
		});
		
		server.runAsync();
		
		try {
			SocketsPool pool = new SocketsPool(host, port);
			pool.setSocketSoTimeoutForNewConn(waitSleep);
			
			try(SocketConn c = pool.getConnection()){
				c.getWriter().println("test1");
				c.getReader().readLine();
				fail_exception_expected();
			} catch (SocketTimeoutException e) {
				//ok
			}
			
			assertEquals(1, pool.getIdleConnections());
			cx.needSleep = false;
			
			try(SocketConn c = pool.getConnection()){
				c.getWriter().println("test2");
				String oldData = c.getReader().readLine();
				assertEquals("test1", oldData); //<-------------------------- !!!
			}
			
			
		} finally {
			server.shutdownWait();
		}
		
	}
	
	
	
	@Test
	public void test_lost_conn_and_get_again() throws Exception{
		
		SocketServer server = createEchoServer(port, maxThreads);
		server.runAsync();
		
		SocketsPool pool = new SocketsPool(host, port);
		pool.setPoolMaximumActiveConnections(1);
		pool.setPoolMaximumIdleConnections(1);
		
		try(SocketConn c = pool.getConnection()){
			
			PrintWriter w = c.getWriter();
			BufferedReader r = c.getReader();

			w.println("1");
			assertEquals("1", r.readLine());
			
			//shutdown server
			server.shutdownWait();
			
			w.println("1");
			assertNull(r.readLine());
		}catch (SocketException e) {
			//ok
		}
		
		SocketConn c = pool.getConnection();
		try{
			c.getWriter().println("1");
			assertNull(c.getReader().readLine());
		}catch (SocketException e) {
			//ok
		}finally {
			c.invalidate(); //<-----------------------------
			c.close();
		}
		
		SocketServer server2 = createEchoServer(port, maxThreads);
		server2.runAsync();
		
		try(SocketConn c2 = pool.getConnection()){
			c2.getWriter().println("1");
			assertEquals("1", c2.getReader().readLine());
		}finally {
			server2.shutdownWait();
		}
		
		
	}
	
	@Ignore
	@Test
	public void test_lost_conn_and_get_new() throws Exception{
		
		SocketServer server = createEchoServer(port, maxThreads);
		server.runAsync();
		
		SocketsPool pool = new SocketsPool(host, port);
		SocketConn c = pool.getConnection();
		getWriterUTF8(c.getOutputStream()).println("hello");
		assertEquals("hello", getReaderUTF8(c.getInputStream()).readLine());
		
		server.shutdownWait();
		try {
			pool.getConnection();
			fail_exception_expected();
		}catch (ConnectException e) {
			//ok
		}
		
		//server is available again
		server = createEchoServer(port, 10);
		server.runAsync();
		SocketConn c2 = pool.getConnection();
		getWriterUTF8(c2.getOutputStream()).println("hello");
		assertEquals("hello", getReaderUTF8(c2.getInputStream()).readLine());
		server.shutdownWait();
		
		
	}
	
	
	
	@Ignore
	@Test(expected=ConnectException.class)
	public void test_wrong_url() throws Exception {
		SocketsPool pool = new SocketsPool(host, port+1);
		pool.getConnection();
	}
	
	
	
	@Test
	public void test_more_than_max_client_conns() throws Exception{
		SocketServer server = createEchoServer(port, maxThreads);
		server.runAsync();
		
		SocketsPool pool = new SocketsPool(host, port);
		int maxConns = maxThreads;
		pool.setPoolMaximumIdleConnections(maxConns);
		pool.setPoolMaximumActiveConnections(maxConns);
		pool.setPoolTimeToWait(5);
		pool.setPoolMaximumCheckoutTime(5);
		
		//max conn
		HashSet<SocketConn> openConns = new HashSet<>();
		for (int i = 0; i < maxConns; i++) {
			SocketConn c = pool.getConnection();
			boolean isNew = openConns.add(c);
			assertTrue(c.toString(), isNew);
		}
		//more then max - get conn from other client
		SocketConn moreThanMax = pool.getConnection();
		assertTrue(openConns.contains(moreThanMax));
		
		server.shutdownWait();
		
	}
	
	
	
	
	@Test
	public void test_get_conn() throws Exception{
		
		SocketServer server = createEchoServer(port, maxThreads);
		server.runAsync();
		assertEquals(0, server.getActiveConnectionsCount());
		
		SocketsPool pool = new SocketsPool(host, port);
		int maxConns = maxThreads;
		pool.setPoolMaximumIdleConnections(maxConns);
		pool.setPoolMaximumActiveConnections(maxConns);
		
		//single conn
		try(SocketConn c = pool.getConnection()){
			getWriterUTF8(c.getOutputStream()).println("hello");
			assertEquals("hello", getReaderUTF8(c.getInputStream()).readLine());
			assertEquals(1, server.getActiveConnectionsCount());
		}
		assertEquals(1, server.getActiveConnectionsCount());
		try(SocketConn c = pool.getConnection()){
			getWriterUTF8(c.getOutputStream()).println("2");
			assertEquals("2", getReaderUTF8(c.getInputStream()).readLine());
			assertEquals(1, server.getActiveConnectionsCount());
		}
		
		//max conn
		ArrayList<SocketConn> openConns = new ArrayList<>();
		for (int i = 0; i < maxConns; i++) {
			openConns.add(pool.getConnection());
		}
		for (int i = 0; i < maxConns; i++) {
			SocketConn c = openConns.get(i);
			try{
				String msg = "привет-"+i;
				getWriterUTF8(c.getOutputStream()).println(msg);
				assertEquals(msg, getReaderUTF8(c.getInputStream()).readLine());
			}finally {
				c.close();
			}
		}
		assertEquals(maxConns, server.getActiveConnectionsCount());
		
		
		
		
		server.shutdownWait();
	}
	

	public SocketServer createEchoServer(int port, int maxThreads) {
		SocketServer server = new SocketServer(port, maxThreads, new SocketWriterHander() {
			@Override
			protected void process(Socket openedSocket, BufferedReader socketReader, PrintWriter socketWriter, SocketServer owner) throws Throwable {
				while( ! owner.wasShutdown()){
					String line = socketReader.readLine();
					if(line == null || owner.wasShutdown()) break;
					socketWriter.println(line);
				}
			}
		});
		return server;
	}

}
