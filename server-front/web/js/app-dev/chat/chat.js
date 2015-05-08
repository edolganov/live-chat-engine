

function Chat(){

	var self = this;
	var log = LogFactory.getLog("Chat");

	var inited = false;
	var reloadingTimerId;
	

	var ui;
	var showListeners = [];
	var hideListeners = [];
	var beforeSendListeners = [];

	var size = 0;

	this.onShow = function(fn){
		showListeners.push(fn);
	};

	this.onHide = function(fn){
		hideListeners.push(fn);
	};

	this.beforeSend = function(fn){
		beforeSendListeners.push(fn);
	}

	this.show = function(){
		init();
		ui.root.show();
		fireEvent(showListeners, "show");
	};

	this.hide = function(){
		if( ! ui) return;
		ui.root.hide();
		fireEvent(hideListeners, "hide");
	};

	this.reinit = function(){
		cancelOldReloading();
		inited = false;
		self.show();
	};

	this.appendLocalMsg = function(msgText, ops){

		ops = Util.extend({
			isUser: false,
			operatorNick: "System",
			noScroll: true,
			date : new Date()
		}, ops);

		var log = {operators:{"sys" : ops.operatorNick}};

		var msg = {user:{}};
		msg.user.operatorId = ops.isUser? null : "sys";
		msg.date = ops.date;
		msg.text = msgText;

		appendMsg(log, 0, msg, ops.noScroll);
	};

	function init(){

		if(inited) {
			return;
		}

		if( Util.isEmpty(ui)){
			initUI();
		}

		if(Global.demoMode){
			initDemoMode();
			return;
		}

		var data = {id: Global.clientProps.id, ref: Global.ref};
		var reqUrl = Global.url + "/api/status";
		//ie post data fix
		if(BrowserUtil.isIE_8_9()){
			var jsonStr = JSON.stringify(data);
			reqUrl += "?data="+encodeURI(jsonStr);
		}

		Ajax.proxyJsonPost(ui.root, {
			singleReq: true,
			url: reqUrl,
			data: data,
			beforeSend: function(){
				showConnectingMsg();
			},
			success: function(chatInfo){
				beginChat(chatInfo);
			},
			anyError: function(errorData){

				if(errorData.ajax && errorData.resp){
					if(errorData.resp.type == ServerExp.HOST_BLOCKED){
						showConnectingErrorMsg("Chat account is blocked for "+Global.ref+". Please contact with Live Chat admin.");
						return;
					}
				}

				showConnectingErrorMsg();
			}
		});
	}

	function initDemoMode(){
		Global.props = {};
		Global.props.msgMaxSize = 300;
		showChatContent({});
		inited = true;
	}

	function initUI(){

		ui = $("#oChatRoot", Global.layout).createUI();

		var position = Global.clientProps.chatPosition;
		if(position == "top") ui.root.addClass("oChat-root-top");
		else if(position == "bottom") ui.root.addClass("oChat-root-bottom");
		else ui.root.addClass("oChat-root-bottom");

		if(Global.clientProps.chatPositionFixed) ui.root.addClass("oChat-root-fixed");
		else ui.root.addClass("oChat-root-absolute");
		

		ui.root.appendTo($("body"));

		ui.hide.click(function(){
			self.hide();
		});

		ui.connectAgain.click(function(){
			init();
		});

		ui.send.click(function(){
			sendMsg();
		});

		KeyUtil.onCtrlEnter(ui.msgText, function(){
			sendMsg();
		});

		ui.feedbackSend.click(function(){
			sendFeedback();
		});

		$(".reinitLink", ui.root).click(function(){
			self.reinit();
		});
	}

	function showConnectingMsg(){
		
		ui.connectError.hide();
		ui.content.hide();
		ui.feedback.hide();
		ui.sendError.hide();

		ui.startConnectMsg.show();
	}

	function showConnectingErrorMsg(text){

		inited = false;
		
		ui.startConnectMsg.hide();
		ui.content.hide();
		ui.feedback.hide();
		ui.sendError.hide();

		ui.connectError.show();

		if(Util.isEmpty(text)){
			ui.connectErrorCommonText.show();
			ui.connectErrorCustomText.hide();
		} else {
			ui.connectErrorCommonText.hide();
			ui.connectErrorCustomText.show();
			ui.connectErrorCustomText.text(text);
		}
	}

	function beginChat(chatInfo){

		inited = true;
		
		Global.chatUrl = Util.isHttps()? chatInfo.chatHttpsUrl : chatInfo.chatHttpUrl;
		Global.props = {};
		Global.props.msgMaxSize = chatInfo.msgMaxSize;

		new ChatProvider(Global.chatUrl, Global.ref).init();

		log.info("Connecting to chat url: " + Global.chatUrl);

		Ajax.proxyJsonPost(ui.root, {
			singleReq: true,
			url: Global.chatUrl + "/api/chat/start",
			data: {id: Global.clientProps.id, oldChatId: restoreOldChatId()},
			beforeSend: function(){
				showConnectingMsg();
			},
			success: function(chatState){
				showChatContent(chatState);
			},
			anyError: function(errorData){

				var customErrText = null;
				if(errorData.ajax && errorData.resp){

					//label by err type
					customErrText = Global.labels["err."+errorData.resp.type];

					//No operators - show feedback form
					if(errorData.resp.type == ServerExp.NO_ACTIVE_OPS){
						showFeedback();
						return;
					}

					//Paused - show feedback form
					if(errorData.resp.type == ServerExp.CHAT_PAUSED){
						showFeedback();
						return;
					}

					//Blocked - show msg
					if(errorData.resp.type == ServerExp.CHAT_BLOCKED){
						if(Util.isEmpty(customErrText)) customErrText = "Chat account is not avalibale";
						showConnectingErrorMsg(customErrText);
						return;
					}
				}

				//unknown situation
				customErrText = Global.labels["err.unknown"];
				if(Util.isEmpty(customErrText)) customErrText = "Connection problem";
				showConnectingErrorMsg(customErrText);
				
			}
		});
	}

	function showChatContent(chatState){
		
		ui.startConnectMsg.hide();
		ui.connectError.hide();
		ui.feedback.hide();
		ui.sendError.hide();

		if(Global.props.msgMaxSize > 0){
			ui.msgText.attr("maxlength", Global.props.msgMaxSize);
		}

		clearMsgs();
		ui.content.show();

		var log = chatState.log;
		if(Util.isEmpty(log)) return;

		appendLog(log, true);
		startReloadChat();
		saveChatId(log.id);
		
	}

	function appendLog(log, noScroll){

		if(Util.isEmpty(log)) return;
		
		var messages = log.messages;
		if(Util.isEmpty(messages)) return;

		var fromIndex = log.fromIndex;
		if(Util.isEmpty(fromIndex)) fromIndex = 0;
		
		ChatUtil.setUsersToMsgsForChat(log);

		if(size == 0){
			ui.chatLog.empty();
		}

		$.each(messages, function(i, msg){
			var globalIndex = fromIndex + i;
			if(globalIndex < size) return;
			appendMsg(log, i, msg, noScroll);
			size++;
		});
	}

	function appendMsg(log, i, msg, noScroll){

		var operators = log.operators;
		if(!operators) operators = {};

		var elem = null;
		var user = msg.user;
		var isOperator = ! Util.isEmpty(user.operatorId);
		if( ! isOperator){
			elem = Util.cloneById("oChatUserMsg-template", {root: Global.layout});
		} else {
			elem = Util.cloneById("oChatOperatorMsg-template", {root: Global.layout});
		}
		var elemUI = elem.createUI();
		elemUI.content.text(msg.text);

		if(isOperator){
			var nick = operators[user.operatorId];
			if( ! Util.isEmptyString(nick)){
				elemUI.nick.text(nick);
			}
		}

		var date = DateUtil.dateFrom(msg.date);
		elemUI.time.text(DateUtil.format_HH_mm(date));

		
		ui.chatLog.append(elem);

		if(!noScroll) 
			ui.chatLog.scrollTo(elem);

	}

	function clearMsgs(){
		size = 0;
		ui.chatLog.empty();
	}



	function sendMsg(){

		var text = ui.msgText.val();
		
		if(Util.isEmptyString(text)) {
			ui.msgText.focus();
			return;
		}

		fireEvent(beforeSendListeners, "beforeSend", text);

		if(Global.demoMode) {
			ui.msgText.val("");
			ui.msgText.focus();
			return;
		}

		Ajax.proxyJsonPost(ui.msgText, {
			singleReq: true,
			url: Global.chatUrl + "/api/chat/add",
			data: {id: Global.clientProps.id, updateData:{fromIndex:size}, text:text},
			beforeSend: function(){
				ui.send.disable();
				ui.sendLoading.show();
				hideSendError();
			},
			success: function(resp){
				
				appendLog(resp.log);
				saveChatId(resp.log.id);

				ui.msgText.val("");
				ui.msgText.focus();
				
				startReloadChat();
			},
			anyError: function(errorData){

				$(".oChat-errorText", ui.sendError).hide();
				var errorTaken = false;
				if(errorData.ajax && errorData.resp){

					if(errorData.resp.type == ServerExp.NO_CLIENT_SESSION){
						self.reinit();
						return;
					}

					if(errorData.resp.type == ServerExp.NO_ACTIVE_OPS){
						ui.sendErrorNoOperators.show();
						errorTaken = true;
					}
				}

				if(!errorTaken) ui.sendErrorCommon.show();
				ui.sendError.show();
				
			},
			complete: function(){
				ui.send.enable();
				ui.sendLoading.hide();
			}
		});

	}

	//одноразовый запуск получения обновлений чата
	function startReloadChat(){

		if(Global.demoMode) return;
		if(reloadingTimerId) return;

		log.info("Start get chat updates from: "+Global.chatUrl);

		var reload = function(){

			Ajax.proxyJsonPost(ui.root, {
				singleReq: true,
				url: Global.chatUrl + "/api/chat/updates",
				data: {id: Global.clientProps.id, updateData:{fromIndex:size}, compact: true},
				success: function(resp){
					if(!resp || !resp.log) return;
					appendLog(resp.log);
				},
				anyError: function(errorData){
					if(errorData.ajax && errorData.resp){
						if(errorData.resp.type == ServerExp.NO_CLIENT_SESSION){
							self.reinit();
							return;
						}
					}
				}
			});

		};

		reloadingTimerId = setInterval(reload, 1000*8);

	}

	function cancelOldReloading(){
		
		if( ! reloadingTimerId) return;

		log.info("Stop get chat updates");
		
		clearInterval(reloadingTimerId);
		reloadingTimerId = null;
	}


	function showFeedback(){

		inited = false;
		
		ui.startConnectMsg.hide();
		ui.content.hide();
		ui.connectError.hide();

		ui.feedback.show();
		ui.feedBackEmptyMsg.hide();

		ui.feedbackText.val("");
		ui.feedbackDone.hide();
		ui.feedbackForm.show();

		
	}

	function sendFeedback(){

		if(Global.demoMode) return;
		var text = ui.feedbackText.val();
		var email = ui.feedbackEmail.val();

		if(Util.isEmptyString(text) || Util.isEmptyString(email)) {
			ui.feedBackEmptyMsg.show();
			return;
		} else {
			ui.feedBackEmptyMsg.hide();
		}

		var name = ui.feedbackName.val();

		Ajax.proxyJsonPost(ui.msgText, {
			singleReq: true,
			url: Global.chatUrl + "/api/chat/feedback",
			data: {id: Global.clientProps.id, email:email, name:name, text:text},
			beforeSend: function(){
				ui.feedbackSend.disable();
				ui.sendLoading.show();
				hideSendError();
			},
			success: function(){
				ui.feedbackForm.hide();
				ui.feedbackDone.show();
			},
			anyError: function(){
				showCommonSendError();
			},
			complete: function(){
				ui.feedbackSend.enable();
				ui.sendLoading.hide();
			}
		});
	}

	function showCommonSendError(){
		$(".oChat-sendErrorMsg", ui.sendError).hide();
		ui.sendErrorCommon.show();
		ui.sendError.show();
	}

	function hideSendError(){
		$(".oChat-sendErrorMsg", ui.sendError).hide();
		ui.sendError.hide();
	}

	function saveChatId(chatId){
		if(!chatId) return;
		try {
			CookieUtil.addCookie("oChat-restore", chatId, {path: "/"});
		}catch(e){}
	}

	function restoreOldChatId(){
		return CookieUtil.getCookieVal("oChat-restore");
	}




	function fireEvent(listeners, eventName, val){
		$.each(listeners, function(i, fn){
			try {
				fn(val);
			}catch(e){
				log.error("error in "+eventName+" listener", e);
			}
		});
	}

}

