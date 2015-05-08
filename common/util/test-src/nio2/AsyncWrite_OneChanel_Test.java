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
package nio2;

import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.junit.Ignore;

/**
 * Тест конкур.асинхр записи
 * Результат: запись идет блокирующая - последовательная (но это не гарантируется спецификацией)
 *
 */
@Ignore
public class AsyncWrite_OneChanel_Test {
	
	static Object endObject = new Object();
	
	static class Task {
		
		int index;
		String data;

		public Task(int index) {
			this.index = index;
			this.data = ""+index+stubData;
		}
		
		void writeAsync(AsynchronousFileChannel fileChannel){
			
			ByteBuffer byteBuffer = ByteBuffer.wrap(data.getBytes());
			fileChannel.write(byteBuffer, 0l, null, new CompletionHandler<Integer, Object>() {

				@Override
				public void completed(Integer result, Object attachment) {
					System.out.println("done for " + index);
					mayBeExit();
				}

				@Override
				public void failed(Throwable exc, Object attachment) {
					System.out.println("error in " + index+":"+exc);
					mayBeExit();
				}
			});
		}

		
		
		
	}
	
	static int endCount = 0;
	static int taskCount = 20;
	
	public static void main(String[] args) throws Exception {
		
		
		Path path = Paths.get("./test-out/nio-test.txt");
		File file = path.toFile();
		file.delete();
		file.createNewFile();
		
		ArrayList<Task> list = new ArrayList<>();
		for (int i = 0; i < taskCount; i++) {
			list.add(new Task(i+1));
		}
		
		try(AsynchronousFileChannel fileChannel = 
				AsynchronousFileChannel.open(path, READ, WRITE)){
			
			for (Task task : list) {
				task.writeAsync(fileChannel);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		//wait end
		synchronized (endObject) {
			try {
				if(endCount != taskCount) endObject.wait();
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("end");
		
	}
	

	protected static synchronized void mayBeExit() {
		endCount++;
		if(endCount == taskCount){
			synchronized (endObject) {
				try {
					endObject.notifyAll();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	
	
	
	
	static String stubData = "__фтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвф" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт" +
			"твфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфтвфт\n\n\n";

}
