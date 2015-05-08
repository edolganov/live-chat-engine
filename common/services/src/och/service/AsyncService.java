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
package och.service;

import static och.util.ExceptionUtil.*;
import static och.util.Util.*;
import static och.util.concurrent.AsyncListener.*;
import static och.util.concurrent.ExecutorsUtil.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import och.api.exception.ExpectedException;
import och.util.concurrent.AsyncListener;
import och.util.model.CallableVoid;

import org.apache.commons.logging.Log;

public class AsyncService implements UncaughtExceptionHandler {
	
	private Log log = getLog(getClass());
	private ArrayList<AsyncListener> listeners = new ArrayList<>();
	private ArrayList<AsyncListener> scheduleListeners = new ArrayList<>();
	
	private ExecutorService workThreads;
	private ScheduledExecutorService scheduledService;
	
	
	public AsyncService() {
		this(10, 2);
	}
	
	public AsyncService(int asyncThreads, int scheduleThreads) {
		workThreads = newFixedThreadPool("AsyncService", asyncThreads, this);
		scheduledService = newScheduledThreadPool("AsyncService-scheduled", scheduleThreads, this);
	}
	
	public void addListener(AsyncListener l){
		listeners.add(l);
	}
	
	public void addScheduleListener(AsyncListener l){
		scheduleListeners.add(l);
	}
	
	public <T> Future<T> invoke(final Callable<T> task){
		Future<T> future = workThreads.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				try {
					
					return task.call();
					
				}catch (Throwable t) {
					ExpectedException.logError(log, t, "can't invoke async");
					throw getExceptionOrThrowError(t);
				}
			}
		});
		fireAsyncEvent(listeners, future);
		return future;
	}
	
	/**
	 * Good for simple cases
	 * <pre>
	 * task --- delay --- task
	 * </pre>
	 */
	public void scheduleWithFixedDelay(String commandName, Runnable command, long initialDelayMs, long delayMs){
		
		logScheduleWithFixedDelay(commandName, delayMs);
		
		ScheduledFuture<?> future = scheduledService.scheduleWithFixedDelay(command, initialDelayMs, delayMs, TimeUnit.MILLISECONDS);
		fireScheduleEvent(future);
	}
	
	/**
	 * Good for simple cases
	 * <pre>
	 * task --- delay --- task
	 * </pre>
	 */
	public void tryScheduleWithFixedDelay(String commandName, CallableVoid command, long initialDelayMs, long delayMs){
		
		logScheduleWithFixedDelay(commandName, delayMs);
		
		ScheduledFuture<?> future = scheduledService.scheduleWithFixedDelay(()->{
			try {
				command.call();
			}catch(Throwable t){
				log.error("can't scheduleWithFixedDelay", t);
			}
		}, initialDelayMs, delayMs, TimeUnit.MILLISECONDS);
		fireScheduleEvent(future);
	}
	
	private void logScheduleWithFixedDelay(String commandName, long delayMs){
		log.info("scheduleWithFixedDelay: '"+commandName+"' with delayMs="+delayMs);
		if(delayMs < 60_000L) log.warn("very small delay "+delayMs+"ms for '"+commandName+"'");
	}
	
	
	
	/**
	 * Good for time periods:
	 * <pre>
	 * period --- period --- period --- period
	 * taaaaaaaaaaask                   taaaaaaaaaaask
	 * </pre>
	 */
	public void scheduleAtFixedRate(String commandName, Runnable command, long initialDelayMs, long periodMs){
		
		logScheduleAtFixedRate(commandName, periodMs);
		
		ScheduledFuture<?> future = scheduledService.scheduleAtFixedRate(command, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
		fireScheduleEvent(future);
	}
	
	
	/**
	 * Good for time periods:
	 * <pre>
	 * period --- period --- period --- period
	 * taaaaaaaaaaask                   taaaaaaaaaaask
	 * </pre>
	 */
	public void tryScheduleAtFixedRate(String commandName, CallableVoid command, long initialDelayMs, long periodMs){
		
		logScheduleAtFixedRate(commandName, periodMs);
		
		ScheduledFuture<?> future = scheduledService.scheduleAtFixedRate(()->{
			try {
				command.call();
			}catch(Throwable t){
				log.error("can't scheduleAtFixedRate", t);
			}
		}, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
		fireScheduleEvent(future);
	}
	
	private void logScheduleAtFixedRate(String commandName, long periodMs){
		log.info("scheduleAtFixedRate: '"+commandName+"' with periodMs="+periodMs);
		if(periodMs < 60_000L) log.warn("very small period "+periodMs+"ms for '"+commandName+"'");
	}

	
	private void fireScheduleEvent(Future<?> future) {
		for (AsyncListener l : scheduleListeners) {
			try {
				l.onFutureEvent(future);
			}catch (Throwable t) {
				log.error("schedule listener error", t);
			}
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("can't invoke async", e);
	}

}
