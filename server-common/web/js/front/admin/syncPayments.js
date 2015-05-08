
function SystemSyncPayments(){

	var ui = Util.cloneById("systemSyncPayments-tempalte").createUI();

	this.init = function(){

		ui.sync.click(syncReq);
		ui.refresh.click(refreshReq);

		loadLastSync();
	};

	this.getUI = function(){
		return ui.root;
	};

	var syncReq = function(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/admin/billing/syncPayments",
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.sync);
			},
			success: function(){
				GlobalMsgPopup.showInfoMsg("Billing sync is started. Wait 2-5 minutes and call 'Refresh' btn");
			},
			complete: function(){
				BtnOps.enableBtn(ui.sync);
			}
		});

	};

	var refreshReq = function(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/admin/billing/syncResult",
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.refresh);
			},
			success: function(result){
				if(Util.isEmptyString(result)) result = "No data";
				ui.result.text(result);
			},
			complete: function(){
				BtnOps.enableBtn(ui.refresh);
			}
		});

		loadLastSync();

	};

	function loadLastSync(){
		Ajax.proxyJsonPost(ui.lastSyncDate, {
			url: "/system-api/admin/billing/lastSyncInfo",
			useCustomErrors: false,
			success: function(result){
				if(Util.isEmpty(result)) return;
				ui.lastSyncDate.text(result.date);
				ui.lastSyncCount.text(result.updated);
			}
		});
	}

}