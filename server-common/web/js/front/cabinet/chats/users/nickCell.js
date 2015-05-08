

var AccUserNickCellController = function(owner, accId, user){

	var self = this;
	var ui = Util.cloneById("nicks-template").createUI();
	var security = FrontApp.security;
	var curUser = security.getUser();

	this.init = function(){

		updateViewState();

		if( ! security.isAccOwnerOrModer(accId) && curUser.id != user.id){
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
			updateNickReq();
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

	function updateViewState(){
		ui.nickLabel.text(getNickname());
	}

	function showViewMode(){
		ui.viewMode.show();
		ui.editMode.hide();
	}

	function showEditMode(){
		ui.editMode.show();
		ui.viewMode.hide();

		ui.nick.val(user.nickname? user.nickname : "");
		ui.nick.focus();
	}

	function getNickname(){
		var name = user.nickname;
		return Util.isEmptyString(name)? Labels.get("common.op") : name;
	}

	function updateNickReq(){

		if( ! ui.editMode.isVisible()){
			return;
		}

		var data = ui.root.getFormData();
		data.accId = accId;
		data.userId = user.id;

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/setNickname",
			data: data,
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.update);
			},
			success: function(){
				onNickUpdated(data);
			},
			complete: function(){
				BtnOps.enableBtn(ui.update);
			}
		});
	}

	function onNickUpdated(data){

		//update model
		user.nickname = data.nick;

		//update view
		updateViewState();
		showViewMode();
	}


}