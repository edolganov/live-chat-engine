
WarningMsg = function(parent, title, content, opt){
	
	opt = Util.extend({
		clear: true
	}, opt);

	var ui = Util.cloneById("warningMsg-template").createUI();
	ui.title.html(title);
	ui.content.html(content);

	this.show = function(){
		if(opt.clear) parent.empty();
		parent.append(ui.root);
		ui.root.show();
	};

};

WarningMsgUtil = {

	showJsonErrorFn: function(root, title){
		return function(resp){
			WarningMsgUtil.showJsonError(root, title, resp);
		};
	},

	showJsonError: function(root, title, resp){
		var errText = ServerErrorUtil.getJsonErrorMsg(resp);
		new WarningMsg(root, title, errText).show();
	}

};