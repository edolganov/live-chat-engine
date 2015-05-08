
$(function() {
	new EnterPage().init();
});

function EnterPage(){

	var ui;
	var tabPanel;
	var canFakeSubmit = false;

	this.init = function(){

		Labels.initGlobalLabels(["ServerErrLabels", "EnterLabels"]);

		ui = $("#userActions").createUI();
		var controllers = {
			"signIn":"UserSignInController",
			"restore":"RestorePswController",
			"newUser":"UserAddController"
		}
		tabPanel = new TabPanel(ui, controllers);

		var fakeSubmitForLoginSave = $("#fakeSubmitFormForLoginSave");
		fakeSubmitForLoginSave.submit(function(){
			if( ! canFakeSubmit) return false;
			return true;
		});

		Global.bind(ui.root, Msg.user_Added, showSignIn);
		Global.bind(ui.root, Msg.user_PswRestored, showSignIn);
		Global.bind(ui.root, Msg.security_SignedIn, function(){

			canFakeSubmit = true;
			fakeSubmitForLoginSave.submit();
			
		});


		var urlParams = UrlUtil.parseDocumentUrlParams();
		var selectTab = urlParams.tab;
		if(controllers[selectTab]){
			tabPanel.selectItem(selectTab);
		} else {
			showSignIn();
		}

	};

	var showSignIn = function (){
		tabPanel.selectItem("signIn");
	};

}