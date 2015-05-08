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
package heap;

import static java.lang.System.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import och.util.concurrent.ExecutorsUtil;

public class SortTest {
	
	public static void main(String[] args) {
		
		Random r = new Random();
		
		System.out.println("create data...");
		
		int size = 500_000;
		//size = 100;
		int[] data1 = new int[size];
		int[] data2 = new int[size];
		int[] data3 = new int[size];
		for (int i = 0; i < data1.length; i++) {
			int val = Math.abs(r.nextInt(size+1));
			data1[i] = val;
			data2[i] = val;
			data3[i] = val;
		}
		
		
		System.out.println("done");
		
		long start = currentTimeMillis();
		sortQuick(data2);
		System.out.println("quick sort: "+ ((currentTimeMillis()-start)/1000.));
		
		for (int i = 0; i < data2.length-1; i++) {
			if(data2[i] > data2[i+1]) {
				System.out.println("! invalid quick sort");
				break;
			}
		}
		
		System.out.println("done");
		
		
		
		start = currentTimeMillis();
		sortQuickAsync(data3);
		System.out.println("quick sort async: "+ ((currentTimeMillis()-start)/1000.));
		
		for (int i = 0; i < data3.length-1; i++) {
			if(data3[i] > data3[i+1]) {
				System.out.println("! invalid quick sort async");
				break;
			}
		}
		
		System.out.println("done");
		
		
		
		
		if(true){
			return;
		}
		
		start = currentTimeMillis();
		sortBubble(data1);
		System.out.println("bubble sort: "+ ((currentTimeMillis()-start)/1000.));
		
		for (int i = 0; i < data1.length-1; i++) {
			if(data1[i] > data1[i+1]) {
				System.out.println("! invalid bubble sort");
				break;
			}
		}
		
		System.out.println("done");
		
	}
	




	private static void sortBubble(int[] data) {
		int temp;
		boolean allDone;
		for (int i = 0; i < data.length-1; i++) {
			allDone = true;
			for (int j = 0; j < data.length-1; j++) {
				if(data[j] > data[j+1]){
					temp = data[j+1];
					data[j+1] = data[j];
					data[j] = temp;
					allDone = false;
				}				
			}
			if(allDone) break;
		}
	}
	
	static class QuickData {
		int from;
		int last;
		public QuickData(int from, int last) {
			this.from = from;
			this.last = last;
		}
	}
	
	private static void sortQuick(int[] data) {
		
		LinkedList<QuickData> queue = new LinkedList<>();
		queue.addLast(new QuickData(0, data.length-1));
		
		int curSize;
		int sepIndex;
		int sepData;
		LinkedList<Integer> toR = new LinkedList<>();
		LinkedList<Integer> toL = new LinkedList<>();
		while( ! queue.isEmpty()){
			
			QuickData cur = queue.removeFirst();
			
			curSize = cur.last + 1 - cur.from;
			sepIndex = cur.from + curSize / 2;
			sepData = data[sepIndex];
			toR.clear();
			toL.clear();
			
			for (int i = 0+cur.from; i < sepIndex; i++) {
				if(data[i] > sepData){
					toR.add(i);
				} else {
					toL.add(i);
				}
			}
			for(int i = sepIndex+1; i < cur.last+1; i++){
				if(data[i] < sepData){
					toL.add(i);
				} else {
					toR.add(i);
				}
			}
			
			boolean sepDone = false;
			for(int i = cur.from; i < cur.last+1; i++){
				if(toL.size() > 0) data[i] = toL.removeFirst();
				else if( ! sepDone) {
					data[i] = sepData;
					sepDone = true;
				}
				else data[i] = toR.removeFirst();
			}
			if(curSize > 2){
				queue.addLast(new QuickData(cur.from, sepIndex));
				queue.addLast(new QuickData(sepIndex+1, cur.last));
			}
			
		}
	}
	
	
	private static void sortQuickAsync(int[] data) {
		
		ExecutorService executor = ExecutorsUtil.newFixedThreadPool("sortTest", Runtime.getRuntime().availableProcessors());
		
		ArrayList<Future<?>> futures = new ArrayList<>();
		
	}

}
