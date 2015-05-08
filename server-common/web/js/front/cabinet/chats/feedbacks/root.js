

/**
 * Таб истории чатов для данного аккаунта
 * [чаты] - [акк1] - [фидбеки*]
 */
function FeedbacksController(acc){

	var self = this;
	var log = LogFactory.getLog("FeedbacksController");
	var accId = acc.uid;
	var inited = false;

	var ui = Util.cloneById("feedbacks-template").createUI();

	var buttons;

	this.init = function(){
		
		buttons = new FeedbacksButtons(ui.topControl, self);
		buttons.init();

	};

	this.getAcc = function(){
		return acc;
	};

	this.getUI = function(){
		if(!inited){
			inited = true;
			self.loadFeedbacksReq();
		}
		return ui.root;
	};

	this.loadFeedbacksReq = function(date){

		if(!date) date = new Date();

		Ajax.proxyJsonPost(ui.root, {
			url: acc.workUrl+"/system-api/chat/feedbacks",
			data: {
				accId: acc.uid,
				date: date.getTime()
			},
			beforeSend: function(){
				Global.triggerGroup(Msg.chatsFeedbacks_load_Begin, accId);
			},
			success: function(resp){

				var feedbacks = resp? resp.list : null;

				Global.triggerGroup(Msg.chatsFeedbacks_load_Success, accId);
				updateGlobalView(feedbacks);

			},
			anyError: function(){
				GlobalMsgPopup.showInvalidReqMsg();
				Global.triggerGroup(Msg.chatsFeedbacks_load_Error, accId);
			}
		});

	};

	function updateGlobalView(feedbacks){

		ui.list.empty();

		if(Util.isEmptyArray(feedbacks)){
			ui.list.append(Labels.get("feeds.empty"));
			return;
		}

		$.each(feedbacks, function(i, item){
			var elem = createFeedbackElem(i, item);
			ui.list.append(elem);
		});
		
	}

	function createFeedbackElem(i, data){

		var user = data.user;
		var created = DateUtil.dateFrom(data.created);
		var text = data.text;
		var ref = data.ref? data.ref : "-";

		var itemUI = Util.cloneById("feedback-elem-template").createUI();

		itemUI.num.text(i+1);
		itemUI.name.text(user.userName);
		itemUI.email.text(user.userEmail);
		itemUI.created.text(DateUtil.format_DD_MM_YYYY_HH_mm_SS(created));
		itemUI.info.text(user.userId);
		itemUI.text.text(text);
		itemUI.ref.text(ref);

		return itemUI.root;
	}

}