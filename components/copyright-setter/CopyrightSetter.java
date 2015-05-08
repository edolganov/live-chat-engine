import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;


public class CopyrightSetter {
	
	public final static String copyrightText = 
	"/*\n"
	+" * Copyright 2015 Evgeny Dolganov (evgenij.dolganov@gmail.com).\n"
	+" *\n"
	+" * Licensed under the Apache License, Version 2.0 (the \"License\");\n"
	+" * you may not use this file except in compliance with the License.\n"
	+" * You may obtain a copy of the License at\n"
	+" *\n"
	+" *      http://www.apache.org/licenses/LICENSE-2.0\n"
	+" *\n"
	+" * Unless required by applicable law or agreed to in writing, software\n"
	+" * distributed under the License is distributed on an \"AS IS\" BASIS,\n"
	+" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
	+" * See the License for the specific language governing permissions and\n"
	+" * limitations under the License.\n"
	+" */\n";
	

	public static void main(String[] args) throws Exception {
		String root = ".";
		if(args.length > 0){
			root = args[0];
		}
		
		File rootDir = new File(root);
		
		LinkedList<File> queue = new LinkedList<File>();
		if(rootDir.isDirectory()){
			queue.addLast(rootDir);
		}
		
		log("begin. \nscan dir: "+rootDir.getAbsolutePath());
		while( ! queue.isEmpty()){
			File dir = queue.removeFirst();
			File[] children = dir.listFiles();
			if(children != null){
				for (File child : children) {
					if(child.isDirectory()){
						queue.addLast(child);
					} else {
						updateIfNeed(child);
					}
				}
			}
		}
		log("end");
		
		
	}
	
	private static void updateIfNeed(File file) {
		String name = file.getName();
		if(name.endsWith(".java")){
			try {
				update(file);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void update(File file) throws Exception {
		
		boolean hasCopyright = false;
		
		StringBuilder oldContent = new StringBuilder();
		
		FileReader fileReader = new FileReader(file);
		BufferedReader br = new BufferedReader(fileReader);
		String line = null;
		while((line = br.readLine())!= null){
			
			String normalStr = line.toLowerCase();
			if(normalStr.contains(" copyright ") && normalStr.contains("*")){
				hasCopyright = true;
				break;
			}
			
			appendLine(oldContent, line);
			
		}
		fileReader.close();
		
		
		if( ! hasCopyright){
			
			String header = copyrightText;
			String body = oldContent.toString();
			//if(body.endsWith("\n")){
			//	body = body.substring(0, body.length()-1);
			//}
			
			File bakFile = new File(file.getParent(),file.getName()+".BAK");
			boolean backuped = file.renameTo(bakFile);
			if( ! backuped){
				throw new IllegalStateException("can't rename for backup "+file);
			}
			
			FileWriter fileWriter = new FileWriter(file);

			fileWriter.write(header);
			fileWriter.write(body);
			fileWriter.close();
			
			bakFile.delete();
			
			log("updated: "+file.getName());
		}
	}

	private static void appendLine(StringBuilder sb, String line) {
		sb.append(line).append('\n');
	}

	private static void log(String msg){
		System.out.println(msg);
	}

}
