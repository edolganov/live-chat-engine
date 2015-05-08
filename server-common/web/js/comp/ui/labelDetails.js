
function LabelDetails(parent, label, details, opt){

	opt = Util.extend({
		html: false
	}, opt);

	var ui = Util.cloneById("labelDetails-template").createUI();

	var setFn = opt.html? 'html' : 'text';
	ui.label[setFn](label);
	ui.details[setFn](details);

	this.show = function(){
		parent.append(ui.root);
	};

	this.setDetails = function(details){
		ui.details[setFn](details);
	};

}