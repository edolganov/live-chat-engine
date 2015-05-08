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
package och.api.model.user;

import static java.util.Collections.*;
import static och.api.model.user.UserRole.*;
import static och.api.model.user.UserStatus.*;

import java.util.ArrayList;
import java.util.Set;

import och.api.exception.user.AccessDeniedException;
import och.util.model.CallableVoid;

public class SecurityContext {
	
	private static class ThreadData {
		User curUser;
		ArrayList<User> oldUsers;
		public ThreadData(User curUser) {
			push(curUser);
		}
		public void push(User user) {
			User oldUser = curUser;
			curUser = user;
			if(oldUser != null) {
				if(oldUsers == null) oldUsers = new ArrayList<>();
				oldUsers.add(oldUser);
			}
		}
		public void pop() {
			curUser = null;
			if(oldUsers != null){
				User oldUser = oldUsers.remove(oldUsers.size()-1);
				curUser = oldUser;
				if(oldUsers.isEmpty()) oldUsers = null;
			}
		}
	}
	
	private static final ThreadLocal<ThreadData> threadLocal = new ThreadLocal<>();
	
	
	
	public static void pushToSecurityContext_SYSTEM_USER(CallableVoid body) throws Exception {
		pushToSecurityContext_SYSTEM_USER();
		try {
			body.call();
		} finally {
			popUserFromSecurityContext();
		}
	}
	
	
	public static void pushToSecurityContext_SYSTEM_USER(){
		User system = new User(-1, "#SYSTEM_USER#", "no-email", ACTIVATED);
		system.setRoles(singleton(ADMIN));
		pushToSecurityContext(system);
	}
	
	public static void pushToSecurityContext(User user){
		
		if(user == null) return;
		
		ThreadData data = threadLocal.get();
		if(data == null) data = new ThreadData(user);
		else data.push(user);
		threadLocal.set(data);
	}
	
	public static void popUserFromSecurityContext(){
		ThreadData data = threadLocal.get();
		if(data == null) return;
		
		data.pop();
		if(data.curUser == null){
			threadLocal.remove();
		}
		
	}
	
	public static void checkAccessFor_ADMIN() throws AccessDeniedException {
		checkAccessFor(ADMIN);
	}
	
	public static void checkAccessFor_MODERATOR() throws AccessDeniedException {
		checkAccessFor(MODERATOR);
	}
	
	public static void checkAccessFor(UserRole...roles) throws AccessDeniedException {
		if(!hasAccessFor(roles)) throw new AccessDeniedException();
	}
	
	public static boolean hasAccessFor(UserRole...roles){
		ThreadData data = threadLocal.get();
		if(data == null) return false;
		
		Set<UserRole> curRoles = data.curUser.getRoles();
		if(curRoles.contains(ADMIN)) return true;
		
		for (UserRole role : roles) {
			if(curRoles.contains(role)){
				return true;
			}
		}
		return false;
	}
	
	public static User getUserFromSecurityContext(){
		ThreadData data = threadLocal.get();
		return data == null? null : data.curUser;
	}
	
	public static User findUserFromSecurityContext() throws AccessDeniedException {
		User out = getUserFromSecurityContext();
		if(out == null) throw new AccessDeniedException();
		return out;
	}
	
	public static long findUserIdFromSecurityContext() throws AccessDeniedException {
		return findUserFromSecurityContext().id;
	}
	
	

}
