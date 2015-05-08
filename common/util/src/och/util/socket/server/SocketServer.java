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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;

import static och.util.Util.*;
import static och.util.concurrent.ExecutorsUtil.*;


public class SocketServer implements UncaughtExceptionHandler {
	
	private Log log = getLog(getClass());
	
	private String name;
	private int waitConnectTimeout = 500;
	private int port;
	private int maxThreads;
	private SocketHandler socketHandler;
	
	
	private ExecutorService waitConnectThread;
	private ServerSocket serverSocket;
	private Future<?> endFuture;
	private ExecutorService connectProcessingPool;
	
	private volatile boolean shutdownFlag;
	private AtomicInteger activeConnsCount = new AtomicInteger(0);
	
	public SocketServer(int port, int maxThreads, SocketHandler socketController) {
		this(null, port, maxThreads, socketController);
	}
	
	public SocketServer(String name, int port, int maxThreads, SocketHandler socketHandler) {
		this.name = name != null? name : "SocketServer";
		this.port = port;
		this.socketHandler = socketHandler;
		this.maxThreads = maxThreads;
	}
	
	public void runAsync() throws IOException{
		init();
	}
	
	public void runWait() throws IOException{
		init();
		try {
			endFuture.get();
		}catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if(cause instanceof IOException) throw (IOException)cause;
			else log.error("error while "+name+" work", cause);
		} catch (Exception e) {
			//ok
		}
	}
	
	
	private void init() throws IOException {
		
		log.info("Start "+name+" [port:"+port+", threadsCount:"+maxThreads+"]");
		
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(waitConnectTimeout);
		
		//connect threads
		connectProcessingPool = newFixedThreadPool("SocketServer-process-connect", maxThreads, this);
		
		//wait connect thread
		waitConnectThread = newSingleThreadExecutor("SocketServer-wait-connect");
		endFuture = waitConnectThread.submit(new Runnable() {
			@Override
			public void run() {
				try {
					while( ! shutdownFlag){
						Socket socket = null;
						try {
							socket = serverSocket.accept();
							connectProcessingPool.submit(new ProcessSocketTask(socket));
						}
						//queue is full
						catch (RejectedExecutionException e) {
							try { 
								socket.close(); 
							} catch (Exception ignore) {}
						}
						//stoped waiting
						catch (SocketTimeoutException e) {/*ok*/}
					}
				}catch (Throwable t) {
					log.error("error in wait connect loop", t);
				} finally {
					shutdownAll();
				}
			}
		});
	}
	
	private class ProcessSocketTask implements Runnable {
		
		Socket socket;
		
		public ProcessSocketTask(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				
				if(socketHandler == null) {
					log.error("empty socketController");
					return;
				}
				
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				
				int active = activeConnsCount.incrementAndGet();
				if(active % 50 == 0) log.info("active connections: "+active+" (and more)");
				
				socketHandler.process(socket, is, os, SocketServer.this);
				
			}
			//Connection reset
			catch (SocketException e) {
				//ok
			}
			//Other
			catch (Throwable t) {
				log.error("can't process socket", t);
			}
			//Close socket at finaly
			finally {
				try {
					if( ! socket.isClosed()) socket.close();
				}catch (Exception e) {
					log.error("can't close socket", e);
				}
				int active = activeConnsCount.decrementAndGet();
				if(active % 50 == 0) log.info("active connections: "+active+(active > 0? " (and less)" : ""));
			}
		}
		
	}
	
	public boolean wasShutdown(){
		return shutdownFlag;
	}
	
	public void shutdownAsync(){
		shutdownFlag = true;
	}
	
	public void shutdownWait(){
		shutdownFlag = true;
		try {
			endFuture.get();
		}catch (Exception e) {
			log.error("error while wait shutdown", e);
		}
	}
	
	private synchronized void shutdownAll(){
		
		log.info("shutdown "+name+"...");
		
		if(serverSocket != null && ! serverSocket.isClosed()){
			try {
				serverSocket.close();
				serverSocket = null;
			}catch (Exception e) {
				log.error("can't close serverSocket", e);
			}
		}
		waitConnectThread.shutdownNow();
		connectProcessingPool.shutdownNow();
		
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("error in "+t, e);
	}

	public int getActiveConnectionsCount() {
		return activeConnsCount.get();
	}

}
