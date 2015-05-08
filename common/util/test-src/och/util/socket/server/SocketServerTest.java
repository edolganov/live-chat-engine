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
package och.util.socket.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


import och.junit.AssertExt;
import och.util.concurrent.ExecutorsUtil;
import och.util.socket.server.SocketServer;
import och.util.socket.server.SocketWriterHander;

import org.junit.Test;

import static och.util.socket.SocketUtil.*;

public class SocketServerTest extends AssertExt {
	
	
	@Test
	public void test_max_connections() throws Exception {
		
		final CopyOnWriteArrayList<Throwable> serverErrors = new CopyOnWriteArrayList<>();
		final CopyOnWriteArrayList<String> clientErrors = new CopyOnWriteArrayList<>();
		final ConcurrentHashMap<String, Object> serverThreads = new ConcurrentHashMap<>();
		
		final int port = 11002;
		int maxThreads = 10;
		SocketServer server = new SocketServer(port, maxThreads, new SocketWriterHander() {
			
			@Override
			protected void process(Socket openedSocket, BufferedReader socketReader, PrintWriter socketWriter, SocketServer owner) throws Throwable {
				
				serverThreads.put(Thread.currentThread().getName(), Boolean.TRUE);
				
				try {
					while( ! owner.wasShutdown()){
						String line = socketReader.readLine();
						if(line == null || owner.wasShutdown()) break;
						
						try {
							Thread.sleep(10);
						}catch (Exception e) {
							serverErrors.add(e);
						}
						
						socketWriter.println("echo:"+line);
						socketWriter.flush();
					}
				}catch (IOException e) {
					//client closed connection
				}
			}
		});
		server.runAsync();
		
		final String req = "привет мир";
		final String resp = "echo:"+req;
		
		ArrayList<Future<?>> calls = new ArrayList<>();
		int callCount = maxThreads * 2;
		ExecutorService pool = ExecutorsUtil.newFixedThreadPool("test-thred", callCount);
		for (int i = 0; i < callCount; i++) {
			Future<?> future = pool.submit(new Runnable() {
				@Override
				public void run() {
					
					try(Socket s = new Socket("localhost", port)){
						BufferedReader reader = getReaderUTF8(s);
						PrintWriter writer = getWriterUTF8(s);
						req(writer, reader);
						req(writer, reader);
						req(writer, reader);
					} catch (Exception e) {
						serverErrors.add(e);
					}
				}
				
				private void req(PrintWriter writer, BufferedReader reader) throws IOException{
					writer.println(req);
					writer.flush();
					String result = reader.readLine();
					if( ! resp.equals(result)) clientErrors.add("invalid result: "+result);
				}
			});
			calls.add(future);
		}
		for (Future<?> future : calls) future.get();
		server.shutdownWait();
		
		assertEquals("client errors:"+clientErrors, 0, clientErrors.size());
		assertEquals("server errors:"+serverErrors, 0, serverErrors.size());
		assertEquals(maxThreads, serverThreads.size());
		
	}
	
	
	
	@Test
	public void test_work() throws Exception{
		
		final ArrayList<String> serverData = new ArrayList<>();
		
		int port = 11001;
		SocketServer server = new SocketServer(port, 10, new SocketWriterHander() {
			
			@Override
			protected void process(Socket openedSocket, BufferedReader socketReader, PrintWriter socketWriter, SocketServer owner) throws Throwable {
				
				while(true){
					String line = socketReader.readLine();
					if(line == null) break;
					
					serverData.add(line);
					socketWriter.println("echo:"+line);
				}
				openedSocket.close();
			}
		});
		server.runAsync();
		
		String req = "привет мир";
		String resp = "echo:"+req;
		
		Socket s = new Socket("localhost", port);
		BufferedReader reader = getReaderUTF8(s);
		PrintWriter writer = getWriterUTF8(s);
		{
			writer.println(req);
			String answer = reader.readLine();
			assertEquals(resp, answer);
			assertEquals(1, serverData.size());
			assertEquals(req, serverData.get(0));
		}
		{
			writer.println(req);
			String answer = reader.readLine();
			assertEquals(resp, answer);
			assertEquals(2, serverData.size());
			assertEquals(req, serverData.get(1));
		}
		s.close();
		
		server.shutdownWait();
		

		
		
	}

}
