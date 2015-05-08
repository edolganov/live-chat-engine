
function UserProfileController(){

	var ui = Util.cloneById("userProfile-template").createUI();

	this.init = function(){

		initFields();
		ui.cancel.click(cancel);
		HtmlUtil.onAllInputChanges(ui.curPsw, function(){
			var val = ui.curPsw.val();
			ui.edit.setEnable( ! Util.isEmpty(val) && val.length > 0);
		});
		ui.edit.click(editReq);

		Global.bind(ui.root, Msg.security_SignedIn, initFields);
		Global.bind(ui.root, Msg.security_UserUpdated, initFields);
		Global.bind(ui.root, Msg.security_Logout, resetFields);
	};

	this.getUI = function(){
		return ui.root;
	};

	var cancel = function(){
		initFields();
		Global.trigger(Msg.settings_LoginCanceled);
	};

	var initFields = function(){
		resetFields();
		var user = FrontApp.security.getUser();
		ui.login.val(user.login);
		ui.email.val(user.email);
	};

	var resetFields = function(){
		ui.login.val("");
		ui.email.val("");
		ui.psw.val("");
		ui.curPsw.val("");
		ui.edit.disable();
		ui.cancel.enable();
	};

	var editReq = function(){

		var user = FrontApp.security.getUser();
		var data = ui.root.getFormData();
		if(data.login === user.login) delete data.login;
		if(data.email === user.email) delete data.email;
		if(Util.isEmptyString(data.psw)) delete data.psw;

		FrontApp.security.editUserReq({
			data: data,
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.edit);
				ui.cancel.disable();
			},
			success: function(){
				GlobalMsgPopup.showInfoMsg(Labels.get("common.msg.dataUpdated"));
			},
			complete: function(){
				ui.cancel.enable();
				BtnOps.enableBtn(ui.edit);
				ui.edit.setEnable( ! Util.isEmptyString(ui.curPsw.val()));
			}
		});

	};

}