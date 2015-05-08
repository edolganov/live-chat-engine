
RestorePswController = function(){

	var ui = $("#restorePswForm").createUI();

	this.init = function(){

		ui.restore.click(restorePswReq);
	};

	this.getUI = function(){
		return ui.root;
	};

	var restorePswReq = function(){
		
		var data = ui.root.getFormData();

		Ajax.proxyJsonPost(ui.restore, {
			secure: true,
			url: "/system-api/user/psw-restore",
			data: data,
			beforeSend: function(){
				ui.restore.disable();
			},
			success: function(){
				ui.infoMsg.empty();
				ui.root.clearFormData();
				Global.trigger(Msg.user_PswRestored, [data]);
			},
			jsonError: WarningMsgUtil.showJsonErrorFn(ui.infoMsg, Labels.get("msg.cantSendPsw")),
			complete: function(){
				ui.restore.enable();
			}
		});
	};

};