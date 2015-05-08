
function PriceVal(parent, val){

	var ui = Util.cloneById("price-template").createUI();
	var negative = val < 0;

	function init(){
		
		ui.val.text(MoneyUtil.formatPrice(negative? -val : val));
		ui.neg.setVisible(negative);
		if(negative) ui.root.addClass("neg");

		if(parent) parent.append(ui.root);
	}

	this.getUI = function(){
		return ui.root;
	};


	init();

}