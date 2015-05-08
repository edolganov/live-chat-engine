
function ActiveChatInfo(parent, owner){

	var ui;
	var curChat;

	var inited = false;
	var opsLabels;

	this.init = function(){

		ui = Util.cloneById("chatInfo-template").createUI();
		ui.root.hide();
		parent.append(ui.root);

	};

	this.showView = function(chat){
		
		curChat = chat;
		inited = false;
		
		updatePanelsView();
		inited = true;
	};

	this.removeChatView = function(){

		curChat = null;
		inited = false;

		updatePanelsView();
	};


	this.updateView = function(){
		updatePanelsView();
	};

	function updatePanelsView(){

		if(!curChat) {
			ui.root.hide();
			return;
		}

		//first look
		if(!inited){

			ui.users.empty();

			var clientInfo = curChat.directClient;
			new LabelDetails(ui.users, Labels.get("common.client"), clientInfo.userId).show();

			opsLabels = [];
			ui.root.show();
		}

		//updates
		var opsSize = opsLabels.length;
		for (var i = 1; i < curChat.users.length; i++) {
			var opIndex = i-1;
			var op = curChat.users[i];
			var id = op.operatorId;
			var nick = curChat.operators[id];
			if(Util.isEmptyString(nick)) nick = Labels.get("chat.noNickname");

			//new
			if(opIndex >= opsSize){
				var label = new LabelDetails(ui.users, "#"+id, nick);
				label.detailsCache = nick;
				label.show();
				opsLabels.push(label);
			}
			//update
			else {
				var oldLabel = opsLabels[opIndex];
				if(oldLabel.detailsCache != nick){
					oldLabel.detailsCache = nick;
					oldLabel.setDetails(nick);
				}
			}
		}

	}

}