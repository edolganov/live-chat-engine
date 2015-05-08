

/**
 * Таб истории чатов для данного аккаунта
 * [чаты] - [акк1] - [история*]
 */
function HistoryChatsController(acc){

	var self = this;
	var log = LogFactory.getLog("HistoryChatsController");
	var accId = acc.uid;

	var userId = FrontApp.security.findUser().id;
	var ui = Util.cloneById("acviteChats-template").createUI();

	var buttons;
	var list;
	var chatLog;
	var info;

	this.init = function(){

		ui.root.addClass("history");
		
		buttons = new HistoryChatButtons(ui.topControl, self);
		buttons.init();

		list = new ActiveChatsList(ui.chats, self);
		list.init();

		chatLog = new ChatLogController(ui.log, self, {showFooter: false});
		chatLog.init();

		info = new ActiveChatInfo(ui.chatInfo, self);
		info.init();

	};

	this.getAcc = function(){
		return acc;
	};

	this.getUI = function(){
		return ui.root;
	};

	this.loadHistoryReq = function(date){

		Ajax.proxyJsonPost(ui.root, {
			url: acc.workUrl+"/system-api/chat/history",
			data: {
				accId: acc.uid,
				date: date.getTime()
			},
			beforeSend: function(){
				Global.triggerGroup(Msg.chatsHist_load_Begin, accId);
			},
			success: function(resp){

				var chats = resp? resp.list : null;

				Global.triggerGroup(Msg.chatsHist_load_Success, accId);
				updateGlobalView(chats);

			},
			anyError: function(){
				GlobalMsgPopup.showInvalidReqMsg();
				Global.triggerGroup(Msg.chatsHist_load_Error, accId);
			}
		});

	};

	this.onChatSelected = function(chat){
		chatLog.showView(chat);
		info.showView(chat);
	};

	this.onChatDeselected = function(){
		chatLog.removeChatView();
		info.removeChatView();
	};

	function updateGlobalView(chats){
		var chatsById = {};
		if( ! Util.isEmpty(chats)){
			$.each(chats, function(i, chat){
				try {
					ChatUtil.initHistChat(chat, userId);
					chatsById[chat.id] = chat;
				}catch(e){
					log.error("can't init hist chat", e);
				}
				
			});
		}
		list.showAllChats(chatsById);
	}

}