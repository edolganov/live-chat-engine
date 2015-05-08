

/**
 * Таб активных чатов для данного аккаунта
 * [чаты] - [акк1] - [активные чаты*]
 */
function ActiveChatsController(acc){

	var self = this;
	var log = LogFactory.getLog("ActiveChatsController");
	var accId = acc.uid;
	var security = FrontApp.security;

	//model
	var userId = security.findUser().id;
	var chatsById = {};

	//view
	var ui = Util.cloneById("acviteChats-template").createUI();
	var buttons;
	var chatsList;
	var chatLog;
	var info;

	var selectedChat;

	this.init = function(){

		buttons = new ActiveChatButtons(ui.topControl, self);
		buttons.init();

		chatsList = new ActiveChatsList(ui.chats, self, accId);
		chatsList.init();

		chatLog = new ChatLogController(ui.log, self);
		chatLog.init();

		info = new ActiveChatInfo(ui.chatInfo, self);
		info.init();

		//events
		Global.bind(ui.root, Msg.globalMediumTimer, function(){
			if(HtmlUtil.isVisible(ui.root)){
				stopNotifySelectedChat();
			}
		});
		
	};

	this.getAcc = function(){
		return acc;
	};

	this.hasAcc = function(accId){
		return acc.uid == accId;
	};

	this.getUI = function(){
		return ui.root;
	};

	this.updateOperatorStatus = function(val, updateActiveCount){
		buttons.updateOperatorStatus(val);
		acc.isOperatorActive = val;
		Global.trigger(Msg.chats_operatorStatus, [accId, val]);

		if(!updateActiveCount) return;

		var activeCount = 0 + acc.activeCount;
		if(Util.isEmpty(activeCount)) return;
		activeCount += val? 1 : -1;
		if(activeCount < 0) activeCount = 0;
		self.showOpsActiveCount(activeCount);
		
	};

	this.showOpsActiveCount = function(val){
		buttons.showOpsActiveCount(val);
		acc.activeCount = val;
	};
	

	this.showData = function(chats){

		var notifyChats = [];

		chatsById = {};
		$.each(chats, function(i, chat){
			try {
				ChatUtil.initChat(chat, userId);
				chatsById[chat.id] = chat;

				if( isNotifyChat(chat)) {
					notifyChats.push(chat);
				}
			}catch(e){
				log.error("can't init chat", e);
			}

		});
		chatsList.showAllChats(chatsById);

		//events
		$.each(notifyChats, function(i, chat){
			Global.trigger(Msg.chats_newNotifyChat, [accId, chat]);
		});
	};

	this.update = function(chats){

		if(Util.isEmpty(chats)) return;

		var notifyChats = [];
		var unnotifyChats = [];
		var newChats = {};

		//new and old items
		$.each(chats, function(i, chat){

			var oldChat = chatsById[chat.id];

			//new chat
			if(Util.isEmpty(oldChat)){
				ChatUtil.initChat(chat, userId);
				newChats[chat.id] = chat;
				chatsById[chat.id] = chat;
				if( isNotifyChat(chat)) {
					notifyChats.push(chat);
				}
			}
			//old chat
			else {
				mergeChat(oldChat, chat);
				
				if(isNotifyChat(oldChat)){
					notifyChats.push(oldChat);
				}
				else if(isUnnotifyChat(oldChat)){
					unnotifyChats.push(oldChat);
				}
			}
		});

		//update view
		chatsList.showNewChats(chatsById, newChats);
		updateGlobalView();

		//events
		$.each(notifyChats, function(i, chat){
			Global.trigger(Msg.chats_newNotifyChat, [accId, chat]);
		});
		$.each(unnotifyChats, function(i, chat){
			Global.trigger(Msg.chats_stopNotifyChat, [accId, chat]);
		});

	};

	function isNotifyChat(chat){

		if( ! security.isAccOperator(accId)) return false;
		
		if(chat.hasNotification) return false;
		if(chat.closed) return false;

		var selectedChatId = selectedChat? selectedChat.id : null;
		if(window.isActive && chat.id == selectedChatId && HtmlUtil.isVisible(ui.root)){
			return false;
		}

		var hasOps = chat.users.length > 1;
		if( ! chat.noOpsAlreadyNotified && ! hasOps) {
			chat.hasNotification = true;
			chat.noOpsAlreadyNotified = true;
			return true;
		}
		
		if( hasOps && ! chat.hasCurOperator()) return false;

		var lastIndex = chat.messages.length - 1;
		var lastMsg = chat.messages[lastIndex];
		if( ! lastMsg.alreadyNotified && userId != lastMsg.user.operatorId){
			chat.hasNotification = true;
			lastMsg.alreadyNotified = true;
			return true;
		}
		return false;
	}


	function isUnnotifyChat(chat){
		if( ! window.isActive) return false;
		if( ! security.isAccOperator(accId)) return false;

		if( ! chat.hasNotification) return false;
		if(chat.closed) return false;

		var hasOps = chat.users.length > 1;
		if( ! hasOps) return false;
		if(chat.hasCurOperator()) return false;

		var hasMsgFromOp = false;
		$.each(chat.messages, function(i, msg){
			if( ! Util.isEmpty(msg.user.operatorId)){
				hasMsgFromOp = true;
				return false;
			}
			return true;
		})
		if(hasMsgFromOp){
			chat.hasNotification = false;
			return true;
		}

		return false;
	}


	this.getChatsUpdateMap = function(){
		var out = {};
		$.each(chatsById, function(id, chat){
			if(chat.closed) return;
			out[id] = {
				fromIndex: chat.messages.length,
				usersCount: chat.users.length
			};
		});
		return out;
	};

	this.changeStatusReq = function(val, reqElem){
		Ajax.proxyJsonPost(reqElem, {
			url: acc.workUrl+"/system-api/chat/opStatus",
			data: {
				accId: acc.uid,
				val: val
			},
			beforeSend: function(){
				buttons.onUpdateStatusReq();
			},
			success: function(newVal){
				if(newVal != val) return;
				self.updateOperatorStatus(newVal, true);
			},
			anyError: function(){
				GlobalMsgPopup.showErrorMsg(Labels.get("status.cantChange")+" '"+(acc.name? acc.name : acc.uid)+"'");
				buttons.refreshStatus();
			}
		});
	}


	this.takeChatReq = function(chat){

		var reqElem = HtmlUtil.createRootReqElem("chatReq_take-"+chat.id, true);

		Ajax.proxyJsonPost(reqElem, {
			url: acc.workUrl+"/system-api/chat/take",
			data: {
				accId: acc.uid,
				chatId: chat.id,
				state: {
					fromIndex: chat.messages.length,
					usersCount: chat.users.length
				}
			},
			beforeSend: function(){
				chat.extra.takeState = Const.state_ReqBegin;
				Global.triggerGroup(Msg.chats_takeChatReq_Begin, accId, [chat]);
			},
			success: function(resp){

				if(Util.isEmpty(resp) || Util.isEmpty(resp.chatLog)){
					GlobalMsgPopup.showInvalidReqMsg();
					chat.extra.takeState = Const.state_ReqError;
					Global.triggerGroup(Msg.chats_takeChatReq_Error, accId, [chat]);
					return;
				}

				//merge data
				mergeChat(chat, resp.chatLog);

				if(resp.concurrentError){
					GlobalMsgPopup.showErrorMsg('Chat alredy been taken by other operator');
					chat.extra.takeState = Const.state_ReqError;
					Global.triggerGroup(Msg.chats_takeChatReq_Error, accId, [chat]);
				} else {
					chat.extra.takeState = Const.state_ReqSuccess;
					Global.triggerGroup(Msg.chats_takeChatReq_Success, accId, [chat]);
				}

				updateGlobalView();
				
			},
			anyError: function(errorData){

				GlobalMsgPopup.showInvalidReqMsgForAnyError(errorData);

				chat.extra.takeState = Const.state_ReqError;
				Global.triggerGroup(Msg.chats_takeChatReq_Error, accId, [chat]);
			},
			complete: function(){
				chat.extra.takeState = Const.state_ReqEnd;
			}
		});

	};


	this.sendMsgReq = function(chat, text){

		if(Util.isEmptyString(text)) return;

		var reqElem = HtmlUtil.createRootReqElem("chatReq_addMsg-"+chat.id, true);

		Ajax.proxyJsonPost(reqElem, {
			url: acc.workUrl+"/system-api/chat/addMsg",
			data: {
				accId: acc.uid,
				chatId: chat.id,
				text:text,
				state: {
					fromIndex: chat.messages.length,
					usersCount: chat.users.length
				}
			},
			beforeSend: function(){
				chat.extra.addMsgState = Const.state_ReqBegin;
				Global.triggerGroup(Msg.chats_addMsgReq_Begin, accId, [chat]);
			},
			success: function(resp){

				if(Util.isEmpty(resp) || Util.isEmpty(resp.chatLog)){
					GlobalMsgPopup.showInvalidReqMsg();
					chat.extra.addMsgState = Const.state_ReqError;
					Global.triggerGroup(Msg.chats_addMsgReq_Error, accId, [chat]);
					return;
				}

				//merge data
				mergeChat(chat, resp.chatLog);

				chat.extra.addMsgState = Const.state_ReqSuccess;
				Global.triggerGroup(Msg.chats_addMsgReq_Success, accId, [chat]);
				updateGlobalView();
			},
			anyError: function(errorData){

				GlobalMsgPopup.showInvalidReqMsgForAnyError(errorData);
				
				chat.extra.addMsgState = Const.state_ReqError;
				Global.triggerGroup(Msg.chats_addMsgReq_Error, accId, [chat]);
			},
			complete: function(){
				chat.extra.addMsgState = Const.state_ReqEnd;
			}
		});
	};





	this.onChatSelected = function(chat){

		selectedChat = chat;
		
		buttons.showView(chat);
		chatLog.showView(chat);
		info.showView(chat);

		stopNotifySelectedChat();
		
	};

	this.onChatDeselected = function(){

		selectedChat = null;

		buttons.removeChatView();
		chatLog.removeChatView();
		info.removeChatView();
	};

	function mergeChat(oldChat, chat){

		if(chat.closed){
			oldChat.closed = true;
			return;
		}

		var curMsgsSize = oldChat.messages.length;
		if(chat.fromIndex > curMsgsSize){
			return;
		}
		
		ChatUtil.setUsersToMsgs(
			chat.messages,
			chat.users,
			chat.operators,
			oldChat.directClient);

		$.each(chat.messages, function(i, msg){
			var globalIndex = chat.fromIndex + i;
			if(globalIndex < curMsgsSize) return;
			oldChat.messages.push(msg);
		});

		if( ! Util.isEmptyArray(chat.operators)){
			oldChat.operators = chat.operators;
		}

		if( ! Util.isEmptyArray(chat.users)){
			oldChat.users = chat.users;
		}

		if( ! Util.isEmpty(chat.clientRefs)){
			Util.extend(oldChat.clientRefs, chat.clientRefs);
		}
		
	}

	function updateGlobalView(){
		chatsList.updateView();
		buttons.updateView();
		chatLog.updateView();
		info.updateView();
	}

	function stopNotifySelectedChat(){

		if( ! window.isActive) return;
		if( ! selectedChat) return;
		if( ! selectedChat.hasNotification) return;

		selectedChat.hasNotification = false;
		Global.trigger(Msg.chats_stopNotifyChat, [accId, selectedChat]);
	}

}