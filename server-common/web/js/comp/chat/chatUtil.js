

var ChatUtil = {

	initChat: function(chat, userId){

		if(chat.created){
			chat.created = DateUtil.dateFrom(chat.created);
		}

		if(Util.isEmpty(chat.messages)) chat.messages = [];
		if(Util.isEmpty(chat.clientRefs)) chat.clientRefs = {};


		ChatUtil.setUsersToMsgsForChat(chat);

		chat.directClient = ChatUtil.getChatClient(chat);
		chat.clientIp = ChatUtil.getClientIp(chat.directClient);

		chat.hasCurOperator = ChatUtil._hasCurOperatorFunc;

		chat.extra = {};
		chat.extra.curUserId = userId;

	},

	initHistChat: function(chat, userId){
		ChatUtil.initChat(chat, userId);

		if(chat.ended){
			chat.ended = DateUtil.dateFrom(chat.ended);
		}
	},


	getChatClient: function(chat){
		var client = null;
		$.each(chat.users, function(i, item){
			if(item.userId){
				client = item;
				return false;
			}
			return true;
		});
		return client;
	},

	/**
	 * Проставляем в каждом сообщении данные по автору сообщения
	 */
	setUsersToMsgsForChat: function(chat){
		var users = chat.users;
		var messages = chat.messages;
		var operators = chat.operators;
		ChatUtil.setUsersToMsgs(messages, users, operators);
	},

	/**
	 * Проставляем в каждом сообщении данные по автору сообщения
	 */
	setUsersToMsgs: function(messages, users, operatorsMap, directClient){
		
		if(Util.isEmpty(messages)) return;
		if(Util.isEmpty(users)) users = [];
		if(Util.isEmpty(operatorsMap)) operatorsMap = {};

		$.each(messages, function(i, msg){
			var user = null;
			var userIndex = msg.userIndex;
			if(userIndex >= users.length){
				//неизвестный автор
				user = {operatorId:null};
			} else {
				//известный автор
				user = users[userIndex];

				//подменяем инфу о клиенте на заранее заданную, если нужно
				if(Util.isEmpty(user.operatorId) && directClient){
					user = directClient;
				}
			}
			if( ! Util.isEmpty(user.operatorId)){
				user.operatorNick = operatorsMap[user.operatorId];
			}
			msg.user = user;
		});
	},

	isSameChats: function(chatA, chatB){
		if(Util.isEmpty(chatA) || Util.isEmpty(chatB)) return false;
		return chatA.id == chatB.id;
	},

	getClientIp: function(client){
		if(Util.isEmpty(client) 
			|| Util.isEmpty(client.userId)) return null;

		var userId = client.userId;
		var terms = userId.split("#");
		if(terms.length > 0) return terms[0];
		return null;
		
	},



	
	_hasCurOperatorFunc: function(){
		var chat = this;
		for (var i = 1; i < chat.users.length; i++) {
			if(chat.users[i].operatorId == chat.extra.curUserId){
				return true;
			}
		}
		return false;
	}

};