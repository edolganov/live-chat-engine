

var AccUserActionsCellController = function(owner, accId, user){

	var ui = Util.cloneById("accUserActions-template").createUI();
	var curUser = FrontApp.security.getUser();

	this.init = function(){

		updateViewState();

		TablePanelUtil.initActionsCell(ui);

		ui.deleteUser.click(function(){
			if(HtmlUtil.isDisabled(ui.deleteUser)) return;
			deleteUserReq();
		});


	};

	this.getUI = function(){
		return ui.root;
	};


	function updateViewState(){

		ui.deleteUser.setEnable(canDeleteUser(user));
		
	}

	function canDeleteUser(user){
		if(curUser.id == user.id && FrontApp.security.isAccOwner(accId)) return false;
		if(user.privs[AccPriv.CHAT_OWNER] && ! FrontApp.security.isAccOwner(accId)) return false;
		if(user.privs[AccPriv.CHAT_MODER] && ! FrontApp.security.isAccOwner(accId)) return false;
		return FrontApp.security.isAccOwnerOrModer(accId);
	}


	function deleteUserReq(){
		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/deleteUser",
			data: {accId: accId, userId: user.id},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableImgWait(ui.deleteUser);
			},
			success: function(){
				owner.reloadUsersList();
			},
			complete: function(){
				BtnOps.enableImg(ui.deleteUser);
			}
		});
	}

};