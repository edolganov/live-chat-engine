
function PaggingPanel(root, initPage, initLast){

	var self = this;
	var ui = Util.cloneById("paggingPanel-template").createUI();
	var page = ! Util.isEmpty(initPage)? initPage : -1;
	var last = ! Util.isEmpty(initLast)? initLast : true;

	function init(){

		self.renderState();
		root.append(ui.root);

	}

	this.hide = function(){
		ui.root.hide();
	}

	this.show = function(){
		ui.root.show();
	}

	this.getUI = function(){
		return ui.root;
	};

	this.onPrev = function(fn){
		ui.prev.click(fn);
	};

	this.onNext = function(fn){
		ui.next.click(fn);
	};

	this.disable = function(){
		ui.next.disable();
		ui.prev.disable();
	};

	this.enable = function(){
		self.renderState();
	};

	this.setState = function(newPage, isLast){
		page = newPage;
		last = isLast;
		self.renderState();
	}

	this.renderState = function(){

		ui.prev.setEnable(page > 0);
		ui.next.setEnable( ! last);
		ui.pageNum.text(page+1);

	};



	init();

}