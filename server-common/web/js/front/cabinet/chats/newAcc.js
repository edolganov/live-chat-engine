
function NewAccController(){

	var ui = Util.cloneById("newAcc-template").createUI();
	var isFirst = CabinetPageUtil.getAccsCount() == 0;

	this.init = function(){

		new TariffSelect(ui.tariffSelect);

		ui.create.click(function(){
			createAccReq();
		});
		
	};

	this.getUI = function(){
		return ui.root;
	};

	function createAccReq(){

		var data = ui.root.getFormData();

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/createAcc",
			data: data,
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.create);
			},
			success: function(){
				var url = "/cabinet";
				if(isFirst) url += "?showInfo";
				BrowserUtil.loadPage(url);
			},
			anyError: function(){
				BtnOps.enableBtn(ui.create);
			}
		});
	}

}