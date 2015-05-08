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
package och.util.stopwatch;

import static java.util.Collections.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.List;

import och.util.model.Pair;

public class ThreadStopwatch {
	
	
	private static class State {
		ThreadStopwatch root;
		ThreadStopwatch last;
		State(ThreadStopwatch root) {
			this.root = root;
			this.last = root;
		}
	}
	
	private static ThreadLocal<State> threadLocal = new ThreadLocal<>();
	
	
	public static ThreadStopwatch startThreadStopwatch(String name){
		return startThreadStopwatch(name, true);
	}
	
	public static ThreadStopwatch startThreadStopwatch(String name, boolean enable){
		
		ThreadStopwatch sw = new ThreadStopwatch(name, enable);
		
		State state = threadLocal.get();
		if(state == null){
			state = new State(sw);
			threadLocal.set(state);
			return sw;
		}
		
		sw.setParent(state.last);
		state.last = sw;
		return sw;
	}
	
	public static void removeThreadStopwatch(ThreadStopwatch sw){
		
		if(sw.removed) return;
		sw.removed = true;
		
		//root sw
		if(sw.parent == null) {
			threadLocal.remove();
			return;
		}
		
		//child sw
		State state = threadLocal.get();
		if(state == null) return;
		state.last = sw.parent;
	}
	
	public static void removeAllThreadStopwatchs(){
		threadLocal.remove();
	}
	
	public static ThreadStopwatch getRootThreadStopwatch(){
		State state = threadLocal.get();
		return state == null? null : state.root;
	}
	
	
	
	
	public final String name;
	
	private boolean enable;
	private long start;
	private long worktime = -1;
	private ArrayList<Pair<String, String>> params;
	
	//tree elem
	private ThreadStopwatch parent;
	private List<ThreadStopwatch> children;
	private boolean removed;
	
	
	private ThreadStopwatch(String name, boolean enable){
		this.name = name;
		this.enable = enable;
		this.start = System.currentTimeMillis();
	}
	
	private void setParent(ThreadStopwatch parent){
		this.parent = parent;
		this.enable = parent.enable;
		parent.addChild(this);
	}
	
	private void addChild(ThreadStopwatch child) {
		if(children == null) children = new ArrayList<>();
		children.add(child);
	}

	public void addExternalChild(ThreadStopwatch child){
		child.enable = this.enable;
		addChild(child);
	}
	
	public List<ThreadStopwatch> getChildren(){
		if(children == null) return emptyList();
		return unmodifiableList(children);
	}
	
	public void addInfo(String key, Object ob) {
		if(!enable) return;
		if(params == null) params = new ArrayList<>();
		params.add(new Pair<>(key, String.valueOf(ob)));
	}
	
	public String stopAndGetLog(){
		stop();
		return getLog();
	}

	public void stop(){
		if(worktime == -1) worktime = System.currentTimeMillis() - start;
	}
	
	public String getLog(){
		return getLog(0);
	}
	
	public void remove(){
		stop();
		removeThreadStopwatch(this);
	}
	

	private String getLog(int level){
		
		if(!enable) return "stopwatch not enabled";
		
		StringBuilder sb = new StringBuilder();
		if(level == 0)sb.append("stopwatch tree: \n");
		
		sb.append(createLevelPrefix(level));
		if(level> 0)sb.append(' ');
		
		sb.append(name);
		sb.append(" time=").append((worktime/1000.)).append("sec");
		if( ! isEmpty(params)){
			for (Pair<String, String> pair : params) {
				sb.append(", ").append(pair.first).append('=').append(pair.second);
			}
		}
		if(! isEmpty(children)){
			int nextLevel = level+1;
			for (ThreadStopwatch child : children) {
				sb.append('\n').append(child.getLog(nextLevel));
			}
		}
		
		return sb.toString();
	}

	private String createLevelPrefix(int level) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append('-');
		}
		return sb.toString();
	}
}
