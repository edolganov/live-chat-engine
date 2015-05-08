
CaptchaBlock = function(rootElem){

	var self = this;

	var Props = window.Props? window.Props : {};
	var enabled = Props["captcha_enabled"] !== "false";
	var global = window.captchaBlock_GlobalState;
	if(!global) {
		global = {};
		global.lastInited = null;
		window.captchaBlock_GlobalState = global;
	}


	this.init = function(callback){

		if(!enabled) return;
		
		if(global.scriptLoaded){
			createCaptcha();
			if(callback) callback();
		} else {
			rootElem.text("Loading captcha...");
			$.getScript('https://www.google.com/recaptcha/api/js/recaptcha_ajax.js', function() {
				global.scriptLoaded = true;
				createCaptcha();
				if(callback) callback();
			});
		}
	};

	function createCaptcha(){

		if(global.lastInited == self) return;

		rootElem.empty();
		Recaptcha.create(
			Props["captcha_publicKey"],
			rootElem[0],
			{theme: "white"});
		global.lastInited = self;
	}


	this.setValsToData = function(data){
		data.captchaChallenge = self.captchaChallenge();
		data.captchaResponse = self.captchaResponse();
	};

	this.captchaChallenge = function(){

		if(!enabled) return "disabled";

		return $("input#recaptcha_challenge_field", rootElem).val();
	};

	this.captchaResponse = function(){

		if(!enabled) return "disabled";

		return $("input#recaptcha_response_field", rootElem).val();
	};

	this.reload = function(){

		if(!enabled) return;

		if(window.Recaptcha) window.Recaptcha.reload();
	};

	this.reloadIfInvalidCaptchaError = function(resp, checkExtra){

		if(!enabled) return;

		var reloadCaptcha = ! resp 
			|| ! resp.msg
			|| resp.type == "och.exception.user.InvalidCaptchaException"
			|| (checkExtra && resp.msg.contains("captchaChallenge"))
			|| (checkExtra && resp.msg.contains("captchaResponse"));

		if(reloadCaptcha) self.reload();
	};

	this.remove = function(){
		
		if(!enabled) return;

		rootElem.empty();
		if(global.lastInited == self) global.lastInited = null;
	};
	

};