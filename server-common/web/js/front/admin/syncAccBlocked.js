
function SyncAccBlocked(){

	var ui = Util.cloneById("systemSyncAccBlocked-tempalte").createUI();

	this.init = function(){
		ui.sync.click(serverReq);
	};

	this.getUI = function(){
		return ui.root;
	};

	var serverReq = function(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/admin/chat/syncBlocked",
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.sync);
			},
			success: function(result){
				GlobalMsgPopup.showInfoMsg("Done");

				if(Util.isEmpty(result)){
					ui.unblocked.text("unknown");
					ui.blocked.text("unknown");
				} else {
					ui.unblocked.text(result.unblocked);
					ui.blocked.text(result.blocked);
				}
			},
			complete: function(){
				BtnOps.enableBtn(ui.sync);
			}
		});
	};

}