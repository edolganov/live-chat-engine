
UserSignInController = function(){

	var Props = window.Props? window.Props : {};
	var autoActivation = Props["users_autoActivation"] === "true";

	var ui = $("#signInForm").createUI();
	var signIn = ui.signIn;
	var infoMsg = ui.infoMsg;
	var captchaMode = false;
	var captcha;

	this.init = function(){
		
		signIn.click(signInReq);

		captcha = new CaptchaBlock($(".captcha", ui.root));

		Global.bind(ui.root, Msg.user_Added, onUserAdded);
		Global.bind(ui.root, Msg.user_PswRestored, onPswRestored);
		Global.bind(ui.root, Msg.security_Logout, onLoggedOut);

	};

	this.getUI = function(){
		setupCaptchaMode();
		return ui.root;
	};

	var onUserAdded = function(event, data){

		if(data){
			ui.login.val(data.login);
			ui.psw.val(data.psw);
		}

		if( ! autoActivation){
			new WarningMsg(infoMsg,
				Labels.get("msg.waitEmail"),
				Labels.get("msg.waitEmailActivation")).show();
		} else {
			signInReq();
		}
		
	};

	var onPswRestored = function(event, data){
		
		if(data){
			ui.login.val(data.login);
			ui.psw.val("");
		}

		new WarningMsg(infoMsg,
			Labels.get("msg.waitEmail"),
			Labels.get("msg.waitEmailRestore")).show();

	};

	var signInReq = function(){

		var data = ui.root.getFormData();
		if(captchaMode) captcha.setValsToData(data);

		FrontApp.security.signInReq({
			data: data,
			beforeSend: function(){
				signIn.disable();
			},
			success: function(user){
				removeCaptchaCookie();
				setupCaptchaMode();
				infoMsg.empty();
			},
			jsonError: function(resp){
				WarningMsgUtil.showJsonError(ui.infoMsg, Labels.get("msg.cantSignIn"), resp);
				setupCaptchaMode(function(){
					captcha.reloadIfInvalidCaptchaError(resp, true);
				});
			},
			complete: function(){
				signIn.enable();
			}
		});

	};

	var onLoggedOut = function(){
		ui.root.clearFormData();
	};


	function removeCaptchaCookie(){
		$.removeCookie('needCaptcha');
	}

	function setupCaptchaMode(callback){
		var needCaptcha = ! Util.isEmpty($.cookie('needCaptcha'));
		if(needCaptcha) enableCaptchaMode(callback);
		else disableCaptchaMode();
		
	}

	function enableCaptchaMode(callback){
		captchaMode = true;
		captcha.init(callback);
	}

	function disableCaptchaMode(){
		captchaMode = false;
		captcha.remove();
	}




};