
/**
 * Таб аккаунта чатов:
 * [чаты] - [акк1*][акк2]
 */
function AccTabController(acc){

	var self = this;
	var accId = acc.uid;
	var ui = Util.cloneById("chatAccPanel-template").createUI();
	var activeChats;
	var history;
	var info;
	var users;
	var feedbacks;
	var isSingleAcc = CabinetPageUtil.getAccsCount() == 1;
	var tariffs = FrontApp.tariffs;
	var security = FrontApp.security;

	this.retryListener = null;

	this.init = function(){

		$(".reconnectLink", ui.root).click(function(){
			if(self.retryListener) self.retryListener();
		});

		ui.blockedMsg.setVisible(acc.blocked);

		initPausedBlock();
		updatePausedState();

		activeChats = new ActiveChatsController(acc);
		activeChats.init();

		history = new HistoryChatsController(acc);
		history.init();

		info = new AccInfoController(acc);
		info.init();

		users = new AccUsersController(acc);
		users.init();

		feedbacks = new FeedbacksController(acc);
		feedbacks.init();


		//common tabs
		var controllers = {
			"activeChats": activeChats,
			"history": history,
			"feedbacks": feedbacks,
			"info": info,
			"users": users
		};

		var tabs = new TabPanel(ui, controllers, {
			contentRoot: "accTabContent"
		});


		if(isSingleAcc && UrlUtil.parseDocumentUrlParams().showInfo){
			tabs.selectItem("info");
		} else {
			tabs.selectItem("activeChats");
		}

		Global.bind(ui.root, Msg.chats_openUsers, function(){
			if( ! ui.root.isVisible()) return;
			tabs.selectItem("users");
			HtmlUtil.scrollOnPageTop();
		});

		Global.bind(ui.root, Msg.chats_openInfo, function(){
			if( ! ui.root.isVisible()) return;
			tabs.selectItem("info");
			HtmlUtil.scrollOnPageTop();
		});


		Global.bindGroup(ui.root, Msg.chats_accPausedStateUpdated, accId, function(){
			updatePausedState();
		});
	};

	this.getUI = function(){
		return ui.root;
	};

	this.showLoadingState = function(){
		
		ui.initMsg.show();

		ui.errorInitMsg.hide();
		ui.reconnectMsg.hide();
		ui.content.hide();
	};

	this.showErrorState = function(){
		
		ui.errorInitMsg.show();

		ui.initMsg.hide();
		ui.content.hide();
		ui.reconnectMsg.hide();
	};

	this.showAccContent = function(){
		
		ui.content.show();

		ui.initMsg.hide();
		ui.errorInitMsg.hide();
		ui.reconnectMsg.hide();
	};

	this.showActiveChats = function(chats){
		activeChats.showData(chats);
	};

	this.showOperatorStatus = function(val){
		activeChats.updateOperatorStatus(val);
	}

	this.showOpsActiveCount = function(val){
		activeChats.showOpsActiveCount(val);
	}

	this.updateActiveChats = function(chats){
		activeChats.update(chats);
	}

	this.showReconnectToAccMsg = function(){
		if(ui.errorInitMsg.isVisible()) return;
		ui.reconnectMsg.show();
	}

	this.getChatsUpdateMap = function(){
		return activeChats.getChatsUpdateMap();
	};

	function initPausedBlock(){

		if( ! security.isAccOwner(accId)){
			ui.unpausedBlock.hide();
			return;
		}

		ui.unpause.setVisible(true);
		ui.unpause.click(function(){
			unpauseReq();
		});

	}


	function updatePausedState(){

		var isPaused = tariffs.isPausedTariff(acc.tariffId);
		ui.pausedMsg.setVisible(isPaused);

	}

	function unpauseReq(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/pauseAcc",
			data: {accId: acc.uid, val: false},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.unpause);
			},
			success: function(resp){

				//update model
				acc.tariffId = resp.newTariffId;

				GlobalMsgPopup.showInfoMsg(Labels.get("tariffChange.msg.unpaused"));


				//udpdated event
				Global.triggerGroup(Msg.chats_accPausedStateUpdated, acc.uid, [self]);
				
			},
			complete: function(){
				BtnOps.enableBtn(ui.unpause);
			}
		});

	}



}