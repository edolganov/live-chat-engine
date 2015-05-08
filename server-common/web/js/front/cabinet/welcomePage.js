
function WelcomePageController(){

	var ui = Util.cloneById("welcomePage-template").createUI();

	this.init = function(){
		
	};

	this.getUI = function(){
		return ui.root;
	};

}