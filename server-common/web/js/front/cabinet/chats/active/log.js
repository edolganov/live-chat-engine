
/**
 * Логи выбранного активного чата
 */
function ChatLogController(parent, owner, opt){

	opt = Util.extend({
		showFooter: true
	}, opt);

	var ui;
	var curChat;
	var lastAddedIndex = -1;
	var accId = owner.getAcc().uid;
	var isOperator = FrontApp.security.isAccOperator(accId);

	this.init = function(){

		ui = Util.cloneById("chatLogPanel-template").createUI();
		parent.append(ui.root);

		//ограничение чатов
		if(Props.chats_maxMsgSize > 0){
			ui.text.attr("maxlength", Props.chats_maxMsgSize);
		}


		ui.footer.hide();

		if( ! opt.showFooter){
			return;
		}

		ui.takeChat.click(function(){
			if( ! curChat) return;
			owner.takeChatReq(curChat);	
		});

		ui.send.click(function(){
			sendReq();
		});

		KeyUtil.onCtrlEnter(ui.text, function(){
			sendReq();
		});

		ui.takeChatPanel.setVisible(isOperator);

		//events
		var anyReqList = function(e, chat){
			if( ! ChatUtil.isSameChats(curChat, chat)) return;
			updateReqStateView();
		};
		Global.bindGroup(ui.root, Msg.chats_takeChatReq_Begin, accId, anyReqList);
		Global.bindGroup(ui.root, Msg.chats_takeChatReq_Error, accId, anyReqList);
		Global.bindGroup(ui.root, Msg.chats_takeChatReq_Success, accId, anyReqList);

		Global.bindGroup(ui.root, Msg.chats_addMsgReq_Begin, accId, anyReqList);
		Global.bindGroup(ui.root, Msg.chats_addMsgReq_Error, accId, anyReqList);
		Global.bindGroup(ui.root, Msg.chats_addMsgReq_Success, accId, anyReqList);
	};

	function sendReq(){
		if( ! curChat) return;
		owner.sendMsgReq(curChat, ui.text.val());
	}

	this.showView = function(chat){
		if(ChatUtil.isSameChats(curChat, chat)){
			return;
		}

		clearAll();

		curChat = chat;
		updateReqStateView();
		appendMsgs(false);
		showFooter();

		//restore last text
		if(curChat.extra.lastText){
			ui.text.val(curChat.extra.lastText);
		} else {
			ui.text.val("");
		}
		
	};

	this.removeChatView = function(){
		clearAll();
	};

	this.updateView = function(){
		if( ! curChat) return;
		appendMsgs(true);
		showFooter();
	};

	function clearAll(){

		//save last text
		if(curChat){
			curChat.extra.lastText = ui.text.val();
		}

		curChat = null;
		lastAddedIndex = -1;

		ui.msgs.empty();
		ui.footer.hide();
	}


	function appendMsgs(scroll){

		var messages = curChat.messages;
		if(Util.isEmpty(messages)) return;

		$.each(messages, function(i, msg){
			if(i <= lastAddedIndex) return;
			appendMsg(i, msg, scroll);
			lastAddedIndex++;
		});
	}

	function appendMsg(i, msg, scroll){
		
		var elem = null;

		var user = msg.user;
		var isOperator = ! Util.isEmpty(user.operatorId)
		if( ! isOperator){
			elem = Util.cloneById("userMsg-template");
		} else {
			elem = Util.cloneById("operatorMsg-template");
		}
		
		var elemUI = elem.createUI();
		elemUI.content.text(msg.text);

		if(isOperator){
			var nick = user.operatorNick;
			if( ! Util.isEmptyString(nick)){
				elemUI.nick.text(nick);
			} else {
				elemUI.nick.text(Labels.get("common.op")+"#"+user.operatorId);
			}
		}

		var date = DateUtil.dateFrom(msg.date);
		elemUI.time.text(DateUtil.format_HH_mm(date));

		if(curChat.clientRefs[i]){

			var ref = curChat.clientRefs[i];
			var refUI = Util.cloneById("clientRef-template").createUI();
			refUI.root.text(Labels.get("chat.from") + ref);
			refUI.root.attr("title", ref);

			elem.prepend(refUI.root);
			elem.addClass("hasRef");
		}

		ui.msgs.append(elem);
		
		if(scroll) {
			ui.msgs.scrollTo(elem);
		}
	}

	function showFooter(){

		if( ! opt.showFooter) return;

		ui.takeChatPanel.hide();
		ui.addMsgPanel.hide();
		ui.closedInfo.hide();
		ui.footer.show();

		if(curChat.closed){
			ui.closedInfo.show();
			ui.msgs.removeClass("compact");
			return;
		}

		if(curChat.hasCurOperator()){
			ui.addMsgPanel.show();
			ui.msgs.addClass("compact");
			return;
		}

		ui.takeFirstMsg.setVisible(curChat.users.length === 1);
		ui.takeNextMsg.setVisible(curChat.users.length > 1);
		ui.takeChatPanel.setVisible(isOperator);
		ui.msgs.removeClass("compact");
	}


	function updateReqStateView(){
		
		var takeState = curChat.extra.takeState;
		if(takeState === Const.state_ReqBegin){
			BtnOps.disableBtnWait(ui.takeChat);
		} else {
			BtnOps.enableBtn(ui.takeChat);
		}

		var addMsgState = curChat.extra.addMsgState;
		if(addMsgState === Const.state_ReqBegin){
			BtnOps.disableBtnWait(ui.send);
		}
		else if(addMsgState === Const.state_ReqSuccess){
			BtnOps.enableBtn(ui.send);
			ui.text.val("");
		}
		else {
			BtnOps.enableBtn(ui.send);
		}

	}

	

}