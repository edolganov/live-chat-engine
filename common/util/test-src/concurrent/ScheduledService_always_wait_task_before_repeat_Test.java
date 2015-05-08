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
package concurrent;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.Ignore;

@Ignore
public class ScheduledService_always_wait_task_before_repeat_Test {
	
	static int count = 10;
	static Object monitor = new Object();
	
	public static void main(String[] args) throws Exception {

		ScheduledExecutorService readers = Executors.newScheduledThreadPool(3);
		readers.scheduleWithFixedDelay(new Runnable() {
			
			@Override
			public void run() {
				
				System.out.println(Thread.currentThread().getName());
				downCount();
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
				
			}
		}, 0, 10, MILLISECONDS);
		
		
		synchronized (monitor) {
			if(count > 0) monitor.wait();
			readers.shutdown();
		}
		
	}
	
	static synchronized void downCount(){
		count--;
		if(count < 1){
			synchronized (monitor) {
				monitor.notifyAll();
			}
		}
	}

}
