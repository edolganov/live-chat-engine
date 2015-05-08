

function SystemController(){

	var ui = Util.cloneById("system-template").createUI();

	this.init = function(){
		
		Util.initAndAppend("SystemReloadModels", ui.root);
		ui.root.append("<br>");
		Util.initAndAppend("SystemSyncPayments", ui.root);
		ui.root.append("<br>");
		Util.initAndAppend("SyncAccBlocked", ui.root);
		ui.root.append("<br>");
		Util.initAndAppend("SyncAccPaused", ui.root);

	};

	this.getUI = function(){
		return ui.root;
	};
}