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
package och.util.timer;

import static och.util.ExceptionUtil.*;
import static och.util.Util.*;

import java.util.Timer;
import java.util.TimerTask;

import och.util.model.CallableVoid;

import org.apache.commons.logging.Log;

public class TimerExt extends Timer {
	
	private Log log = getLog(getClass());
	private String name;

	public TimerExt() {
		super();
	}

	public TimerExt(boolean isDaemon) {
		super(isDaemon);
	}

	public TimerExt(String name, boolean isDaemon) {
		super(name, isDaemon);
		this.name = name;
	}

	public TimerExt(String name) {
		super(name);
		this.name = name;
	}
	
	
	
	
	public void trySchedule(CallableVoid body, long delay) {
		super.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					body.call();
				}catch(Throwable t){
					log.error(getLogPrefix() + "can't schedule: "+t);
				}
			}
		}, delay);
		
		log.info(getLogPrefix() + "timer started: delay="+delay);
	}
	
	
	public void schedule(CallableVoid body, long delay) {
		super.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					body.call();
				}catch(Exception e){
					throw getRuntimeExceptionOrThrowError(e);
				}
			}
		}, delay);
		
		log.info(getLogPrefix() + "timer started: delay="+delay);
	}
	
	public void tryScheduleAtFixedRate(CallableVoid body, long delay, long period) {
		
		super.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				try {
					body.call();
				}catch(Throwable t){
					log.error(getLogPrefix() + "can't schedule: "+t);
				}
			}
		}, delay, period);
		
		log.info(getLogPrefix() + "timer started: delay="+delay+", period="+period);
	}
	
	
	public void scheduleAtFixedRate(CallableVoid body, long delay, long period) {
		
		super.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				try {
					body.call();
				}catch(Exception e){
					throw getRuntimeExceptionOrThrowError(e);
				}
			}
		}, delay, period);
		
		log.info(getLogPrefix() + "timer started: delay="+delay+", period="+period);
	}

	private String getLogPrefix() {
		return name != null? name + ": " : "";
	}
	
	

}
