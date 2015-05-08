
function HistoryChatButtons(parent, owner){

	var accId = owner.getAcc().uid;
	var ui = Util.cloneById("historyChatsButtons-template").createUI();

	this.init = function(){

		parent.append(ui.root);

		ui.date.val(DateUtil.format_DD_MM_YYYY(new Date()));
		CalendarOps.init(ui.date);

		ui.load.click(function(){
			var date = DateUtil.parseDigitsStr(ui.date.val());
			if(Util.isEmpty(date)) return;
			owner.loadHistoryReq(date);
		});


		//events
		Global.bindGroup(ui.root, Msg.chatsHist_load_Begin, accId, function(){
			BtnOps.disableBtnWait(ui.load);
		});
		Global.bindGroup(ui.root, Msg.chatsHist_load_Error, accId, function(){
			BtnOps.enableBtn(ui.load);
		});
		Global.bindGroup(ui.root, Msg.chatsHist_load_Success, accId, function(){
			BtnOps.enableBtn(ui.load);
		});

	};
	
}