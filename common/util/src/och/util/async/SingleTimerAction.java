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
package och.util.async;


import java.util.Timer;
import java.util.TimerTask;

import och.util.model.Shutdownable;





public class SingleTimerAction implements Shutdownable {
	
    private static long timerId;

    private synchronized static long nextId() {
        return timerId++;
    }
	
	private volatile String saveReqId;
	private Timer timer = new Timer("SingleTimerAction-"+nextId());
	private long delay;
	
	public SingleTimerAction(long delay) {
		super();
		this.delay = delay;
	}



	public void doSingleAction(final Runnable r){
		
		final String curReqId = System.currentTimeMillis()+"-"+System.nanoTime();
		saveReqId = curReqId;
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				//check for valid req
				String saveReqId = SingleTimerAction.this.saveReqId;
				if( curReqId != saveReqId){
					return;
				}
				
				//action
				r.run();
			}
			
		}, delay);
	}
	
	@Override
	public void shutdown(){
		timer.cancel();
	}

}
