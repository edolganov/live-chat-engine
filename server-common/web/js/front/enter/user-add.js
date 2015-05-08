

UserAddController = function(){

	var ui = $("#newUserForm").createUI();
	var captcha;


	this.init = function(){
		
		//terms of use
		new CollapseBlock({root:ui.termOfUseBlock});

		captcha = new CaptchaBlock($(".captcha", ui.root));

		ui.create.click(createReq);
	};

	this.getUI = function(){
		captcha.init();
		return ui.root;
	};


	var createReq = function(){
		
		var data = ui.root.getFormData();
		captcha.setValsToData(data);

		Ajax.proxyJsonPost(ui.create, {
			secure: true,
			url: "/system-api/user/add",
			data: data,
			beforeSend: function(){
				ui.create.disable();
			},
			success: function(){
				ui.infoMsg.empty();
				ui.root.clearFormData();
				Global.trigger(Msg.user_Added, [data]);
			},
			jsonError: function(resp){
				WarningMsgUtil.showJsonError(ui.infoMsg, Labels.get("msg.cantAddUser"), resp);
				captcha.reloadIfInvalidCaptchaError(resp);
			},
			complete: function(){
				ui.create.enable();
			}
		});
	};

};