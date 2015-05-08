
//список активных чатов
function ActiveChatsList(root, owner, accId){

	var self = this;
	var buildId;

	var items = {};
	var selectedItem;

	this.init = function(){

		//нужно очитывать кнопки вверх-вниз в окне набора сообщения
		KeyUtil.onArrowUp($(document), function(){
			//TODO
			if(true) return true;
			if( ! HtmlUtil.isVisible(root)) return true;
			return changeSelectedItem(true);
		});

		KeyUtil.onArrowDown($(document), function(){
			//TODO
			if(true) return true;
			if( ! HtmlUtil.isVisible(root)) return true;
			return changeSelectedItem(false);
		});

		root.click(function(){
			self.deselectItems();
		});


		//events
		if(accId){
			Global.bind(root, Msg.chats_newNotifyChat, function(e, chatAccId, chat){
				showNotifyIfNeed(chatAccId, chat);
			});
			Global.bind(root, Msg.chats_stopNotifyChat, function(e, chatAccId, chat){
				hideNotifyIfNeed(chatAccId, chat);
			});
		}

	};

	function showNotifyIfNeed(chatAccId, chat){

		if( !accId) return;
		if( accId != chatAccId) return;

		items.each(function(){
			var item = $(this);
			var curChat = item.data("chat");
			if(!curChat) return true;

			if(curChat.id == chat.id){
				item.addClass("notify-msg");
				return false;
			}
			return true;
		});
	}

	function hideNotifyIfNeed(chatAccId, chat){
		
		if( !accId) return;
		if( accId != chatAccId) return;

		items.each(function(){
			var item = $(this);
			var curChat = item.data("chat");
			if(!curChat) return true;

			if(curChat.id == chat.id){
				item.addClass("notify-msg");
				item.removeClass("notify-msg");
				return false;
			}
			return true;
		});
	}

	this.showAllChats = function(chatsById){

		buildId = Util.generateUid();
		var curBuildId = buildId;

		self.deselectItems();
		deleteItems();

		var chats = createChatsList(chatsById);

		AsyncJob.invoke({
			name:"showAllChats",
			isAllDone: function(index){
				return index >= chats.length;
			},
			isCancel: function(){
				return curBuildId != buildId;
			},
			doJob: function(index){
				var chat = chats[index];
				var elem = createChatElem(chat);
				root.append(elem);
			},
			onDone: function(){
				reloadItems();
			}
		});

	};


	this.showNewChats = function(chatsById, newChats){

		var chats = createChatsList(chatsById);

		$.each(chats, function(i, chat){
			var isNew = ! Util.isEmpty(newChats[chat.id]);
			if(isNew){
				var elem = createChatElem(chat);
				elem.insertAt(i, root);
			}
		});

		reloadItems();

	};


	this.updateView = function(){

		items.each(function(){
			var elem = $(this);
			var chat = elem.data("chat");
			if(!chat) return;

			updateOperatorStatus(chat, elem);
			updateClosedStatus(chat, elem);
			
		});

	};

	this.selectItem = function(index){

		if( index <0 || index > items.length -1 ) return;
		var item = $(items[index]);
		var chat = item.data("chat");
		if(!chat) return;

		onChatSelected(chat, item);

	}

	this.deselectItems = function(){

		if(!selectedItem) return;

		selectedItem.removeClass("selected");
		selectedItem = null;

		owner.onChatDeselected();
	};




	function changeSelectedItem(up){
		if(!selectedItem) return true;

		var index = items.index(selectedItem);
		if(index < 0) return true;
		if(up && index == 0) return true;
		if(!up && index == items.length-1) return true;

		if(up) self.selectItem(index-1);
		else self.selectItem(index+1);
		
		return false;
	}

	function deleteItems(){
		root.empty();
		items = $();
		selectedItem = null;
	}

	function reloadItems(){
		items = $(".chatItem", root);
	}

	function createChatsList(chatsById){

		var out = [];
		$.each(chatsById, function(id, chat){
			out.push(chat);
		});

		out.sort(sortByDateAsc);

		return out;
	}


	
	function createChatElem(chat){

		var chatUI = Util.cloneById("chatItem-template").createUI();

		updateOperatorStatus(chat, chatUI.root);

		chatUI.ip.text(chat.clientIp);
		chatUI.time.text(DateUtil.format_HH_mm_SS(chat.created));

		if(chat.ended){
			var curLabel = chatUI.time.text();
			chatUI.time.text(curLabel+" - "+DateUtil.format_HH_mm_SS(chat.ended));
		}

		//events
		chatUI.root.click(function(){
			onChatSelected(chat, chatUI.root);
			return false;
		});

		chatUI.root.data("chat", chat);

		return chatUI.root;
	}

	function onChatSelected(chat, itemElem){

		if(itemElem.hasClass("selected")) return;

		if(selectedItem){
			selectedItem.removeClass("selected");
		}

		itemElem.addClass("selected");
		selectedItem = itemElem;

		owner.onChatSelected(chat);

	}






	function updateOperatorStatus(chat, itemElem){

		var opsCount = chat.users.length - 1;
		
		if(opsCount < 1 && ! itemElem.hasClass("noOperator")){
			itemElem.addClass("noOperator");
			return;
		}

		if(opsCount >= 1 && ! itemElem.hasClass("hasOperator")){
			itemElem.removeClass("noOperator");
			itemElem.addClass("hasOperator");
		}

		if( ! itemElem.hasClass("curOperator") && chat.hasCurOperator()){
			itemElem.addClass("curOperator");
		}

	}


	function updateClosedStatus(chat, itemElem){

		if(chat.closed && ! itemElem.hasClass("closed")){

			itemElem.addClass("closed");

			var chatUI = itemElem.createUI();
			chatUI.closed.show();
		}

	}






	var sortByDateAsc = function(chatA, chatB){
		return Util.ascSort(chatA.created, chatB.created);
	};

}