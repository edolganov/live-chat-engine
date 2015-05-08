
function FeedbacksButtons(parent, owner){

	var accId = owner.getAcc().uid;
	var ui = Util.cloneById("feedbacksButtons-template").createUI();

	this.init = function(){

		parent.append(ui.root);

		ui.date.val(DateUtil.format_DD_MM_YYYY(new Date()));
		CalendarOps.init(ui.date);

		ui.load.click(function(){
			var date = DateUtil.parseDigitsStr(ui.date.val());
			if(Util.isEmpty(date)) return;
			owner.loadFeedbacksReq(date);
		});


		//events
		Global.bindGroup(ui.root, Msg.chatsFeedbacks_load_Begin, accId, function(){
			BtnOps.disableBtnWait(ui.load);
		});
		Global.bindGroup(ui.root, Msg.chatsFeedbacks_load_Error, accId, function(){
			BtnOps.enableBtn(ui.load);
		});
		Global.bindGroup(ui.root, Msg.chatsFeedbacks_load_Success, accId, function(){
			BtnOps.enableBtn(ui.load);
		});

	};

}