

Security = function(loginUrl, userEditUrl, logOutUrl){

	var self = this;
	var user = null;
	var privsByAcc = {};
	var reqElem;

	this.init = function(){

		var data = $("#initData");
		var id = data.attr("p-userId");
		var login = data.attr("p-userLogin");
		var email = data.attr("p-userEmail");
		if( ! Util.isEmptyString(id)) {
			user = {id:id, login:login, email:email};
		}


		var privsByAccData = $("#privsByAcc");
		if(privsByAccData.length > 0){
			privsByAccData.children().each(function(){
				var privs = {};
				var accElem = $(this);
				accElem.children().each(function(){
					privs[$(this).attr("p-type")] = true;
				});
				privsByAcc[accElem.attr("p-uid")] = privs;
			});
		}


		reqElem = HtmlUtil.createRootReqElem("security-req-elem", true);
	};

	this.hasUser = function(){
		return self.getUser() != null;
	};

	this.getUser = function(){
		return user;
	};

	this.findUser = function(){
		if(!user) throw new Error("can't find user in security context");
		return user;
	};

	this.isAccOwner = function(accId){
		return self.hasAccPrivilege(accId, AccPriv.CHAT_OWNER);
	};

	this.isAccOwnerOrModer = function(accId){
		return self.hasAccPrivilege(accId, AccPriv.CHAT_OWNER)
			|| self.hasAccPrivilege(accId, AccPriv.CHAT_MODER);
	};

	this.isAccOperator = function(accId){
		return self.hasAccPrivilege(accId, AccPriv.CHAT_OPERATOR);
	};

	this.isOwnerOfAnyAcc = function(){

		if(!user) return false;
		var out = false;
		$.each(privsByAcc, function(accId){
			if(self.isAccOwner(accId)){
				out = true;
				return false;
			}
			return true;
		});
		return out;

	};

	this.hasAccPrivilege = function(accId, priv){
		if(!user) return false;
		var privs = privsByAcc[accId];
		if(!privs) return false;
		return privs[priv];
	}




	this.signInReq = function(ajaxOpt){

		ajaxOpt = Util.extend({}, ajaxOpt);
		ajaxOpt.secure = true;
		ajaxOpt.url = loginUrl;

		var oldSuccess = ajaxOpt.success;
		ajaxOpt.success = function(userData){
			
			user = userData;
			Ajax.set_CSRF_ProtectToken(userData.CSRF_ProtectToken);

			Global.trigger(Msg.security_SignedIn);
			if(oldSuccess) oldSuccess(userData);
		};

		Ajax.proxyJsonPost(reqElem, ajaxOpt);
	};

	this.editUserReq = function(ajaxOpt){

		ajaxOpt = Util.extend({}, ajaxOpt);
		ajaxOpt.secure = true;
		ajaxOpt.url = userEditUrl;

		var oldSuccess = ajaxOpt.success;
		ajaxOpt.success = function(userData){
			user = userData;
			Global.trigger(Msg.security_UserUpdated);
			if(oldSuccess) oldSuccess(userData);
		};

		Ajax.proxyJsonPost(reqElem, ajaxOpt);
	};

	this.logoutReq = function(ajaxOpt){

		ajaxOpt = Util.extend({}, ajaxOpt);
		ajaxOpt.url = logOutUrl;

		var oldSuccess = ajaxOpt.success;
		ajaxOpt.success = function(){
			user = null;
			Global.trigger(Msg.security_Logout);
			if(oldSuccess) oldSuccess();
		};

		Ajax.proxyAjax(reqElem, ajaxOpt);

	};
	
}