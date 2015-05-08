
function ActiveChatButtons(parent, owner){

	var self = this;

	var accId = owner.getAcc().uid;
	var ui = Util.cloneById("acviteChatsButtons-template").createUI();
	var curChat;
	var isOperator = FrontApp.security.isAccOperator(accId);
	var isOperatorActive;

	this.init = function(){

		parent.append(ui.root);

		ui.changeOnlineStatus.setVisible(isOperator);
		ui.statusSetOnline.click(function(){
			changeStatusReq(true);
		});
		ui.statusSetOffline.click(function(){
			changeStatusReq(false);
		});
		
		ui.sortControl.show();
		ui.noOperatorControl.hide();

		ui.takeChat.click(function(){
			if(curChat) {
				owner.takeChatReq(curChat);
			}
		});
		ui.takeChat.setVisible(isOperator);


		//events
		var anyReqList = function(e, chat){
			if( ! ChatUtil.isSameChats(curChat, chat)) return;
			updateReqStateView();
		};
		Global.bindGroup(ui.root, Msg.chats_takeChatReq_Begin, accId, anyReqList);
		Global.bindGroup(ui.root, Msg.chats_takeChatReq_Error, accId, anyReqList);
		Global.bindGroup(ui.root, Msg.chats_takeChatReq_Success, accId, anyReqList);

		Global.bind(ui.root, Msg.chats_operatorStatusChange, function(e, val){
			changeStatusReq(val);
		});

	};

	this.showView = function(chat){
		curChat = chat;
		updateReqStateView();
		updatePanelsView();
	};

	this.removeChatView = function(){
		curChat = null;
		updatePanelsView();
	};


	this.updateOperatorStatus = function(val){
		if(Util.isEmpty(val)) return;
		if( ! isOperator) return;

		isOperatorActive = val;
		if(val) showOnlineStatus();
		else showOfflineStatus();
	}

	this.showOpsActiveCount = function(val){
		if(Util.isEmpty(val) || val < 0) return;

		ui.opsOnlineRoot.show();
		ui.opsOnline.text(val);
	}

	this.onUpdateStatusReq = function(){
		ui.statusLabelWait.show();
		ui.statusLabelOffline.hide();
		ui.statusLabelOnline.hide();

		ui.statusLabelRoot.removeClass("btn-danger").removeClass("btn-success");
	};

	this.refreshStatus = function(){
		self.updateOperatorStatus(isOperatorActive);
	};

	function showOnlineStatus(){
		ui.statusLabelOnline.show();
		ui.statusLabelOffline.hide();
		ui.statusLabelWait.hide();

		ui.statusLabelRoot.addClass("btn-success").removeClass("btn-danger");
	}

	function showOfflineStatus(){
		ui.statusLabelOffline.show();
		ui.statusLabelOnline.hide();
		ui.statusLabelWait.hide();

		ui.statusLabelRoot.addClass("btn-danger").removeClass("btn-success");
	}


	function changeStatusReq(val){
		if(!isOperator) return;
		if(val == isOperatorActive) return;
		owner.changeStatusReq(val, ui.changeOnlineStatus);
	}



	this.updateView = function(){
		updatePanelsView();
	};

	function updatePanelsView(){

		if(!curChat) {
			ui.noOperatorControl.hide();
			return;
		}

		//closed
		if(curChat.closed){
			ui.noOperatorControl.hide();
			return;
		}

		//no operators
		if(curChat.users.length == 1){
			ui.noOperatorControl.show();
			return;
		}

		
		ui.noOperatorControl.hide();
		
	}

	function updateReqStateView(){

		var takeState = curChat.extra.takeState;
		if(takeState === Const.state_ReqBegin){
			BtnOps.disableBtnWait(ui.takeChat);
		} else {
			BtnOps.enableBtn(ui.takeChat);
		}

	}


}