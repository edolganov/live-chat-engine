
function SystemReloadModels(){

	var ui = Util.cloneById("systemReloadModels-template").createUI();

	this.init = function(){
		ui.reload.click(serverReq);
	};

	this.getUI = function(){
		return ui.root;
	};

	var serverReq = function(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/admin/reloadModels",
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.reload);
			},
			success: function(){
				GlobalMsgPopup.showInfoMsg("Done");
			},
			complete: function(){
				BtnOps.enableBtn(ui.reload);
			}
		});
	};

}