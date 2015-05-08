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
import static och.api.model.user.UserStatus.*;
import static och.util.Util.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import och.api.model.BaseBean;
import och.api.model.ValidationProcess;


public class User extends BaseBean {
	
	public static final String INVALID_LOGIN_CHARS = "@<&#\n\t\0";
	public static final String INVALID_EMAIL_CHARS = "<\n\t\0";
	public static final int LOGIN_MAX_SIZE = 30;
	public static final int EMAIL_MAX_SIZE = 60;
	
	public long id;
	public String login;
	public String email;
	protected UserStatus status = NEW;
	protected Set<UserRole> roles = emptySet();
	
	//extra
	private Map<String, Object> params = null; 
	
	public User() {
	}
	
	public User(long id) {
		this.id = id;
	}
	
	public User(long id, String login) {
		this.id = id;
		this.login = login;
	}
	
	public User(String login, String email) {
		this.login = login;
		this.email = email;
	}
	
	public User(long id, String login, String email, UserStatus status) {
		this.id = id;
		this.login = login;
		this.email = email;
		this.status = status;
	}
	
	
	@Override
	protected void checkState(ValidationProcess validation) {
		checkLogin(login, validation);
		checkEmail(email, validation);
	}
	
	public static void checkLogin(String login, ValidationProcess v){
		v.checkForText(login, "login");
		v.checkForInvalidChars(login, "login", INVALID_LOGIN_CHARS);
		v.checkForSize(login, "login", 1, LOGIN_MAX_SIZE);
	}
	
	public static void checkEmail(String email, ValidationProcess v){
		v.checkForText(email, "email");
		v.checkForInvalidChars(email, "email", INVALID_EMAIL_CHARS);
		v.checkForSize(email, "email", 1, EMAIL_MAX_SIZE);
		v.check(()-> {
			if( ! email.contains("@")) return "invalid email";
			return null;
		});
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public int getStatusCode() {
		return status.code;
	}
	
	public void setStatusCode(int statusCode) {
		this.status = tryGetEnumByCode(statusCode, UserStatus.class, NEW);
	}
	
	public UserStatus getStatus() {
		return status;
	}
	
	public void setStatus(UserStatus status) {
		this.status = status != null? status : NEW;
	}
	
	
	public void setCachedRoles(String val){
		if( ! hasText(val)) {
			setRoles(null);
			return;
		}
		
		HashSet<UserRole> set = new HashSet<>();
		StringTokenizer st = new StringTokenizer(val, ",");
		while(st.hasMoreTokens()){
			int code = tryParseInt(st.nextToken(), -1);
			UserRole role = tryGetEnumByCode(code, UserRole.class, null);
			if(role != null) set.add(role);
		}
		setRoles(set);
	}
	
	public void setRoles(Set<UserRole> set){
		if(isEmpty(set)) this.roles = emptySet();
		else this.roles = unmodifiableSet(set);
	}
	
	public Set<UserRole> getRoles(){
		return roles;
	}
	
	
	public void putParam(String key, Object val){
		if(params == null) params = new HashMap<>();
		params.put(key, val);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getParam(String key){
		return (T) params.get(key);
	}
	

	@Override
	public String toString() {
		return "User [id=" + id + ", login=" + login + "]";
	}
	

}
