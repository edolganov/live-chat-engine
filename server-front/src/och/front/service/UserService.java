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
package och.front.service;

import static java.util.Collections.*;
import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.user.SecurityContext.*;
import static och.api.model.user.UserRole.*;
import static och.api.model.user.UserStatus.*;
import static och.api.model.web.ReqInfo.*;
import static och.comp.db.main.table.MainTables.*;
import static och.util.ExceptionUtil.*;
import static och.util.Util.*;
import static och.util.servlet.WebUtil.*;
import static och.util.sql.SingleTx.*;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import och.api.annotation.Secured;
import och.api.exception.ValidationException;
import och.api.exception.user.BannedUserException;
import och.api.exception.user.DuplicateUserDataException;
import och.api.exception.user.InvalidLoginDataForUpdateException;
import och.api.exception.user.InvalidUserActivationCodeException;
import och.api.exception.user.NotActivatedUserException;
import och.api.exception.user.UnmodifiableAdminUserException;
import och.api.exception.user.UserActivationExpiredException;
import och.api.exception.user.UserNotFoundException;
import och.api.model.user.UpdateUserReq;
import och.api.model.user.User;
import och.api.model.user.UserExt;
import och.api.model.user.UserRole;
import och.api.model.user.UserStatus;
import och.comp.db.base.universal.BaseUpdateOp;
import och.comp.db.base.universal.SelectRows;
import och.comp.db.base.universal.field.RowField;
import och.comp.db.main.table._f.ActivationCode;
import och.comp.db.main.table._f.ActivationStateDate;
import och.comp.db.main.table._f.CachedRoles;
import och.comp.db.main.table._f.Email;
import och.comp.db.main.table._f.Login;
import och.comp.db.main.table._f.PswHash;
import och.comp.db.main.table._f.PswSalt;
import och.comp.db.main.table._f.StatusCode;
import och.comp.db.main.table.user.CreateUser;
import och.comp.db.main.table.user.SelectUserByEmail;
import och.comp.db.main.table.user.SelectUserById;
import och.comp.db.main.table.user.SelectUserByIds;
import och.comp.db.main.table.user.SelectUserByLogin;
import och.comp.db.main.table.user.SelectUserByLoginOrEmail;
import och.comp.db.main.table.user.UpdateUserById;
import och.comp.db.main.table.user_role.CreateUserRole;
import och.comp.db.main.table.user_role.DeleteAllUserRoles;
import och.comp.mail.SendReq;
import och.front.service.event.user.UserBannedEvent;
import och.front.service.event.user.UserUnbannedEvent;
import och.front.service.event.user.UserUpdateTxEvent;
import och.util.servlet.WebUtil;


public class UserService extends BaseFrontService {

	
	public UserService(FrontAppContext c) {
		super(c);
	}
	
	
	public long createUser(User user, String psw) throws Exception {
		
		if( ! props.getBoolVal(users_autoActivation)) 
			return createUserInner(user, psw, true);

		//auto activate
		pushToSecurityContext_SYSTEM_USER();
		try {
			long userId = createUserInner(user, psw, false);
			activateUser(userId);
			return userId;
		} finally {
			popUserFromSecurityContext();
		}
		
	}
	
	@Secured
	public long createUser(User user, String psw, boolean sendEmail) throws Exception {
		
		checkAccessFor_MODERATOR();
		
		return createUserInner(user, psw, sendEmail);
	}
	
	private long createUserInner(User user, String psw, boolean sendEmail) throws Exception {
		
		validateState(user);
		validateForText(psw, "psw");
		
		//create user
		String pswSalt = createSalt();
		byte[] pswHash = getHash(psw, pswSalt);
		Date activationDate = new Date();
		String activationCode = createActivationCode();
		long id = universal.nextSeqFor(users);
		try {
			universal.update(new CreateUser(new UserExt(id, user.login, user.email, NEW, pswHash, pswSalt, activationDate, activationCode)));
		}catch (SQLException e) {
			if(containsAnyTextInMessage(e, 
					"users_login_key", 
					"users_email_key", 
					"unique index")){
				throw new DuplicateUserDataException();
			}
			throw e;
		}
		
		//send async email
		if(sendEmail) sendActivationEmailAsync(user.email, activationCode);
		
		log.info("user created: id="+id
				+", login="+user.login
				+", req="+getReqInfoStr());
		
		return id;
	}
	
	public void activateUser(String email, String code) throws UserActivationExpiredException, InvalidUserActivationCodeException, Exception{
		
		checkArgumentForEmpty(email, "email");
		checkArgumentForEmpty(code, "code");
		
		//check status
		UserExt user = universal.selectOne(new SelectUserByEmail(email));
		if(user == null || user.getStatus() != NEW) return;
		
		//check date
		user.checkActivateExpiredTime(getActivateExpiredTime());
		
		//check code
		if( ! code.equals(user.activationCode))
			throw new InvalidUserActivationCodeException(email, code);
		
		//activate
		activateUserInner(user.id);
		
		log.info("user activated: id="+user.id
				+", login="+user.login
				+", req="+getReqInfoStr());
	}
	
	@Secured
	public void activateUser(long id) throws Exception{
		
		checkAccessFor_MODERATOR();
		
		activateUserInner(id);
		
		c.events.tryFireEvent(new UserUnbannedEvent(id));
		
		log.info("user activated/unbanned: id="+id
				+", req="+getReqInfoStr());
	}
	
	private void activateUserInner(long id) throws Exception{
		
		universal.update(new UpdateUserById(id, 
				new StatusCode(ACTIVATED),
				new ActivationStateDate(new Date()),
				new ActivationCode(null)));
	}
	
	
	@Secured
	public void sendActivationEmailAgain(String email) throws UserActivationExpiredException, Exception {
		
		checkAccessFor_MODERATOR();
		checkArgumentForEmpty(email, "email");
		
		//check status
		UserExt user = universal.selectOne(new SelectUserByEmail(email));
		if(user == null || user.getStatus() != NEW) return;
		
		//check date
		user.checkActivateExpiredTime(getActivateExpiredTime());
		
		//send async email
		sendActivationEmailAsync(email, user.activationCode);
	}
	
	/**
	 * Try find user by login or email 
	 * and check status and psw
	 */
	public User checkEmailOrLoginAndPsw(String loginOrEmail, String psw) throws NotActivatedUserException, BannedUserException, Exception {
		
		UserExt user = universal.selectOne(new SelectUserByLoginOrEmail(loginOrEmail));
		if(user == null) return null;
		
		checkStatus(user, loginOrEmail);
		return equalsPsw(user, psw)? user.getUser() : null;
	}

	/**
	 * Try find user by id
	 * and check status
	 */
	public User checkClientUser(long id, boolean exceptionIfInvalidStatus) throws NotActivatedUserException, BannedUserException, Exception {
		
		UserExt user = universal.selectOne(new SelectUserById(id));
		if(user == null) return null;
		
		try {
			checkStatus(user, id);
		}catch (ValidationException e) {
			if(exceptionIfInvalidStatus) throw e;
			else return null;
		}
		
		return user.getUser();
	}
	

	
	
	
	public User getUserByLogin(String login) throws Exception {
		UserExt userExt = universal.selectOne(new SelectUserByLogin(login));
		return userExt == null? null : userExt.getUser();
	}
	
	public User getUserByLoginOrEmail(String val) throws Exception {
		UserExt userExt = universal.selectOne(new SelectUserByLoginOrEmail(val));
		return userExt == null? null : userExt.getUser();
	}
	
	public User getUserById(long id) throws Exception {
		List<User> list = getUsersByIds(list(id));
		return isEmpty(list)? null : list.get(0);
	}
	
	public User findUserById(long id) throws Exception{
		User user = getUserById(id);
		if(user == null) throw new UserNotFoundException();
		return user;
	}
	
	public List<User> getUsersByIds(Collection<Long> ids) throws Exception {
		if(isEmpty(ids)) return emptyList();
		SelectRows<UserExt> select = ids.size() == 1? new SelectUserById(firstFrom(ids)) : new SelectUserByIds(ids);
		List<UserExt> list = universal.select(select);
		return convert(list, (item) -> item.getUser());
	}
	
	public void generateNewPassword(String email) throws Exception {
		generateNewPasswordInner(email, true);
	}
	
	@Secured
	public void generateNewPassword(String email, boolean sendEmail) throws Exception {
		
		checkAccessFor_MODERATOR();
		
		generateNewPasswordInner(email, sendEmail);
	}
	
	private void generateNewPasswordInner(String email, boolean sendEmail) throws Exception {
		
		UserExt user = universal.selectOne(new SelectUserByEmail(email));
		if(user == null) return;
		
		String pswSalt = createSalt();
		String psw = generateRandomPsw(6);
		byte[] pswHash = getHash(psw, pswSalt);
		universal.update(new UpdateUserById(user.id, 
				new PswHash(pswHash), 
				new PswSalt(pswSalt)));
		
		//send async email
		if(sendEmail) sendNewPswEmailAsync(user.email, user.login, psw);
		
		log.info("user took new password: id="+user.id
				+", login="+user.login
				+", req="+getReqInfoStr());
		
	}
	
	@Secured
	public User updateUser(long id, String curPsw, UpdateUserReq req) throws Exception {
		
		checkAccessFor_ADMIN();
		
		validateState(req);
		validateForText(curPsw, "psw");
		
		UserExt userExt = universal.selectOne(new SelectUserById(id));
		if(userExt == null) throw new UserNotFoundException();
		if( ! equalsPsw(userExt, curPsw)) throw new InvalidLoginDataForUpdateException();
		
		//convert psw to hash
		req.pswHash = null;
		if(req.psw != null){
			String pswSalt = createSalt();
			req.pswHash = getHash(req.psw, pswSalt);
			req.pswSalt = pswSalt;
			req.psw = null;
		}
		
		String oldLogin = null;
		boolean oldPsw = false;
		
		ArrayList<RowField<?>> fields = new ArrayList<>();
		if( isUpdateNotEmptyVal(userExt.email, req.email)) fields.add(new Email(req.email));
		if( isUpdateNotEmptyVal(userExt.login, req.login)) {
			fields.add(new Login(req.login));
			oldLogin = userExt.login;
		}
		if( ! isEmpty(req.pswHash)) {
			fields.add(new PswHash(req.pswHash));
			fields.add(new PswSalt(req.pswSalt));
			oldPsw = true;
		}
		
		//update tx
		setSingleTxMode();
		try {
			
			universal.update(new UpdateUserById(id, array(fields, RowField.class)));
			
			c.events.fireEvent(new UserUpdateTxEvent(userExt, req));
			
		}catch (Exception e) {
			
			rollbackSingleTx();
			
			if(e instanceof SQLException){
				if(containsAnyTextInMessage(e, "email", "login", "unique index")) throw new DuplicateUserDataException();
			}
			throw e;				

		} finally {
			closeSingleTx();
		}
		
		
		userExt.update(req);
		
		log.info("user updated values: id="+id+", login="+userExt.login 
				+ (oldLogin != null? ", oldLogin="+oldLogin : "")
				+ (oldPsw ? ", psw changed" : "")
				+", req="+getReqInfoStr());
		
		return userExt.getUser();
	}
	
	@Secured
	public void setRoles(long userId, Set<UserRole> roles)throws Exception {
		
		checkAccessFor_MODERATOR();
		
		if(roles == null) roles = emptySet();
		ArrayList<BaseUpdateOp> ops = new ArrayList<>();
		
		//update roles ops
		ops.add(new DeleteAllUserRoles(userId));
		for (UserRole role : roles) ops.add(new CreateUserRole(userId, role));
		
		//update cache op
		ops.add(new UpdateUserById(userId, new CachedRoles(roles)));
		
		//invoke
		universal.update(array(ops, BaseUpdateOp.class));
		
		log.info("user updated roles: id="+userId
				+", roles="+roles
				+", req="+getReqInfoStr());
	}
	

	@Secured
	public void banUser(long id) throws Exception {
		
		checkAccessFor_MODERATOR();
		
		User user = getUserById(id);
		if(user == null) return;
		
		//can't ban ADMIN
		if(user.getRoles().contains(ADMIN))
			throw new UnmodifiableAdminUserException(user);
		
		universal.update(new UpdateUserById(id, 
				new StatusCode(BANNED),
				new ActivationStateDate(new Date()),
				new ActivationCode(null)));
		
		c.events.tryFireEvent(new UserBannedEvent(id));
		
		log.info("user banned: id="+id
				+", login="+user.login
				+", req="+getReqInfoStr());
	}
		
	
	private void sendActivationEmailAsync(String email, String activationCode) {
		try {
			
			String subject = c.templates.fromTemplate("user-activation-subject.ftl");
			String html = c.templates.fromTemplate("user-activation-text.ftl", 
					map(
						"activationUrl", c.props.getVal(users_activationUrl),
						"email", URLEncoder.encode(email, "UTF-8"),
						"code", URLEncoder.encode(activationCode, "UTF-8")
					)
				);
			c.mails.sendAsync(new SendReq(email, subject, html));
			
		} catch (Exception e) {
			log.error("can't send email", e);
		}
	}
	
	private void sendNewPswEmailAsync(String email, String login, String newPsw) {
		try {
			
			String subject = c.templates.fromTemplate("user-change-psw-subject.ftl");
			String html = c.templates.fromTemplate("user-change-psw-text.ftl", 
					map(
						"login", login,
						"psw", newPsw
					)
				);
			c.mails.sendAsync(new SendReq(email, subject, html));
			
		}catch (Exception e) {
			log.error("can't send email", e);
		}
	}
	
	public long getActivateExpiredTime(){
		//by time prop
		Long byTimePropVal = props.getLongVal(users_expiredTime);
		if(byTimePropVal != null) return byTimePropVal;
		//by day prop
		return props.getLongVal(users_expiredDays) * 1000*60*60*24;
	}
	
	
	public static boolean equalsPsw(UserExt user, String psw) {
		return Arrays.equals(user.pswHash, WebUtil.getHash(psw, user.pswSalt));
	}
	
	public static void checkStatus(UserExt user, Object errorData) throws ValidationException {
		UserStatus status = user.getStatus();
		if(status == NEW) throw new NotActivatedUserException(errorData);
		if(status == BANNED) throw new BannedUserException(errorData);
	}
	

}
