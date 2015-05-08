

var AccUserPrivsCellController = function(owner, accId, user){

	var self = this;
	var ui = Util.cloneById("privs-template").createUI();
	var curUser = FrontApp.security.getUser();

	this.init = function(){
		
		updateViewState();

		if( ! FrontApp.security.isAccOwnerOrModer(accId)){
			ui.edit.hide();
			return;
		}

		TablePanelUtil.putCellController(ui.root, self);
		HtmlUtil.setVisibleByOpacityStyle(ui.edit, false);

		ui.edit.click(function(){
			showEditMode();
		});
		ui.cancel.click(function(){
			showViewMode();
		});
		ui.update.click(function(){
			updatePrivsReq();
		});

		ui.isModeratorLabel.click(function(){
			updateSaveBtnLabel();
		});
		ui.isOperatorLabel.click(function(){
			updateSaveBtnLabel();
		});
	};

	this.getUI = function(){
		return ui.root;
	};

	this.onAppended = function(tr){
		tr.mouseover(function(){
			HtmlUtil.setVisibleByOpacityStyle(ui.edit, true);
		});
		tr.mouseout(function(){
			HtmlUtil.setVisibleByOpacityStyle(ui.edit, false);
		});
	};

	function showEditMode(){
		ui.editMode.show();
		ui.viewMode.hide();

		ui.isOwnerLabel.setVisible(user.privs[AccPriv.CHAT_OWNER]);
		HtmlUtil.setChecked(ui.isModerator, user.privs[AccPriv.CHAT_MODER]);
		HtmlUtil.setChecked(ui.isOperator, user.privs[AccPriv.CHAT_OPERATOR]);

		ui.isModerator.setEnable(FrontApp.security.isAccOwner(accId));

		updateSaveBtnLabel();
	}

	function showViewMode(){
		ui.viewMode.show();
		ui.editMode.hide();
	}

	function updateViewState(){
		ui.ownerPriv.setVisible(user.privs[AccPriv.CHAT_OWNER]);
		ui.moderPriv.setVisible(user.privs[AccPriv.CHAT_MODER]);
		ui.opPriv.setVisible(user.privs[AccPriv.CHAT_OPERATOR]);
	}

	function updateSaveBtnLabel(){
		if(user.privs[AccPriv.CHAT_OWNER]) return;

		var data = ui.root.getFormData();
		var needDelete = ! data.isModerator && ! data.isOperator;

		var label = needDelete? Labels.get("common.delete") : Labels.get("common.update");
		var title = needDelete? Labels.get("users.deleteTitle") : "";
		ui.update.text(label);
		ui.update.attr("title", title);
		if(needDelete) ui.update.addClass("btn-warning");
		else ui.update.removeClass("btn-warning");
	}

	function updatePrivsReq(){

		if( ! ui.editMode.isVisible()){
			return;
		}

		var data = ui.root.getFormData();
		data.accId = accId;
		data.userId = user.id;

		var isCurModer = user.privs[AccPriv.CHAT_MODER]? true : false;
		var isCurOp = user.privs[AccPriv.CHAT_OPERATOR]? true: false;
		if(data.isModerator == isCurModer
			&& data.isOperator == isCurOp){
			showViewMode();
			return;
		}

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/updateUser",
			data: data,
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.update);
			},
			success: function(){
				onPrivsUpdated(data);
			},
			complete: function(){
				BtnOps.enableBtn(ui.update);
			}
		});

	}

	function onPrivsUpdated(data){
		if(data.isModerator){
			user.privs[AccPriv.CHAT_MODER] = true;
		} else {
			delete user.privs[AccPriv.CHAT_MODER];
		}

		if(data.isOperator){
			user.privs[AccPriv.CHAT_OPERATOR] = true;
		} else {
			delete user.privs[AccPriv.CHAT_OPERATOR];
		}

		updateViewState();
		showViewMode();
		if(Util.mapSize(user.privs) == 0){
			owner.reloadUsersList();
		}

		if(data.userId == curUser.id){
			GlobalMsgPopup.showWarningMsg(Labels.get("access.msg.reload"));
		}
	}


}