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


import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import och.util.concurrent.ExecutorsUtil;



/**
 * http://stackoverflow.com/questions/2082304/what-causes-scheduled-threads-not-to-run-in-java
 * http://stackoverflow.com/questions/6844575/scheduleatfixedrate-slow-late
 */
public class ScheduledService_atFixedRate {
	
	public static void main(String[] args) throws IOException {
		
		ScheduledExecutorService pool = ExecutorsUtil.newScheduledThreadPool("test", 2);
		pool.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("1000");
				try {
					Thread.sleep(1000);
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}, 100, 1000, TimeUnit.MILLISECONDS);
		pool.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("500");
				try {
					Thread.sleep(500);
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}, 100, 500, TimeUnit.MILLISECONDS);
		pool.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("100");
				try {
					Thread.sleep(100);
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}, 100, 100, TimeUnit.MILLISECONDS);
		
		System.in.read();
	}

}
