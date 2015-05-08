

function StartPanel(chat){

	var inited = false;
	var ui;

	this.init = function(){

		if(inited) return;
		inited = true;

		ui = Util.cloneById("oChatStartPanel", {root: Global.layout}).createUI();
		ui.root.appendTo($("body"));

		chat.onShow(function(){
			ui.root.hide();
		});

		chat.onHide(function(){
			ui.root.show();
		});

		ui.root.click(function(){
			chat.show();
		})

	};

}