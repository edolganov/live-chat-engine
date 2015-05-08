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
package och.util;

public class MemoryExtUtil {
	
	private static long fSLEEP_INTERVAL = 100;
	
	public static long getMemoryUse(){
	    putOutTheGarbage();
	    long totalMemory = Runtime.getRuntime().totalMemory();

	    putOutTheGarbage();
	    long freeMemory = Runtime.getRuntime().freeMemory();

	    return (totalMemory - freeMemory);
	}
	
	public static int getApproximateSize_Mb(long startMemoryUse, long endMemoryUse){
		return (int)((endMemoryUse - startMemoryUse) / 1024L / 1024L);
	}
  
	private static void putOutTheGarbage() {
	    collectGarbage();
	    collectGarbage();
	 }
	


	private static void collectGarbage() {
		try {
			System.gc();
			Thread.sleep(fSLEEP_INTERVAL);
			System.runFinalization();
			Thread.sleep(fSLEEP_INTERVAL);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

}
