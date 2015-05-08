
function SyncAccPaused(){

	var ui = Util.cloneById("systemSyncAccPaused-tempalte").createUI();

	this.init = function(){
		ui.sync.click(serverReq);
	};

	this.getUI = function(){
		return ui.root;
	};

	var serverReq = function(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/admin/chat/syncPaused",
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.sync);
			},
			success: function(result){
				GlobalMsgPopup.showInfoMsg("Done");

				if(Util.isEmpty(result)){
					ui.unpaused.text("unknown");
					ui.paused.text("unknown");
				} else {
					ui.unpaused.text(result.unpaused);
					ui.paused.text(result.paused);
				}
			},
			complete: function(){
				BtnOps.enableBtn(ui.sync);
			}
		});
	};

}