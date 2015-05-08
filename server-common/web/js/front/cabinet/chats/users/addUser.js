

var AddAccUserController = function(owner, accId){

	var ui = Util.cloneById("addOp-template").createUI();

	this.init = function(){

		ui.cancel.click(function(){
			owner.hideAddUserForm();
		});

		ui.add.click(function(){
			addUserReq();
		});

		ui.isOperator.change(updateState);
		ui.isModerator.change(updateState);
		HtmlUtil.onAllInputChanges(ui.login, updateState);

		updateState();
		
	};

	this.getUI = function(){
		return ui.root;
	};


	this.reinit = function(login){

		if(FrontApp.security.isAccOwner(accId)){
			ui.isOperator.setEnable(true);
			ui.isModeratorLabel.show();
			HtmlUtil.setChecked(ui.isOperator, false);
			HtmlUtil.setChecked(ui.isModerator, false);
		} else {
			ui.isOperator.setEnable(false);
			ui.isModeratorLabel.hide();
			HtmlUtil.setChecked(ui.isOperator, true);
		}
		updateState();

		if(login){
			ui.login.val(login);
			ui.login.focus();
		}
	};
	


	var updateState = function(){
		var hasChecked = HtmlUtil.isChecked(ui.isOperator) || HtmlUtil.isChecked(ui.isModerator);
		var hasLogin = ! Util.isEmptyString(ui.login.val());
		ui.add.setEnable(hasChecked && hasLogin);
	};


	function addUserReq(){

		var data = ui.root.getFormData();
		data.accId = accId;

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/addUser",
			data: data,
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.add);
			},
			success: function(){
				ui.login.val("");
				owner.onUserAdded();
			},
			complete: function(){
				BtnOps.enableBtn(ui.add);
			}
		});

	}

};