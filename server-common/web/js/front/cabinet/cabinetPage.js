$(function(){
	new CabinetPage().init();
});

function CabinetPage(){

	var pingStarted = false;
	var tabs;

	var initTitle;
	var countInTitle = 0;

	this.init = function(){

		Labels.initGlobalLabels(["ServerErrLabels", "CabLabels"]);

		BrowserUtil.initIsWindowActiveListener();
		
		NotifyUtil.defaultIcon = Const.siteIcon48;

		initContext();

		initUserHeader();

		initTitle = document.title;

		//tabs
		var tabsUI = $("#sections").createUI();
		tabs = new TabPanel(tabsUI, {
			"welcome": "WelcomePageController",
			"chats": "ChatAccsController",
			"createAcc": "NewAccController",
			"addReqs": "AddReqsController",
			"profile": "UserProfileController",
			"billing": "BillingController",
			"system": "SystemController"
		});

		//events
		//показ названия акка в заголовке таба
		Global.bind(null, Msg.chats_showSingleAcc, function(e, name){
			var labelElem = $(".chatsLabel", tabsUI.chats);
			var text = labelElem.text();
			text += " [" + Util.substr(name, 20) + "]";
			labelElem.text(text);
		});

		Global.bind(null, Msg.cabinet_openBilling, function(){
			if( ! tabsUI.billing) return;
			tabs.selectItem("billing");
			HtmlUtil.scrollOnPageTop();
		});

		Global.bind(null, Msg.chats_operatorStatusForSingleAcc, function(e, val){
			if(val) TabOps.showOnlineAccTab(tabsUI.chats);
			else TabOps.showOfflineAccTab(tabsUI.chats);
		});

		initMsgNotifications();


		startPingSession();

		//select tab
		var urlParams = UrlUtil.parseDocumentUrlParams();
		if(tabsUI.billing && urlParams.confirmPayment) tabs.selectItem("billing");
		else if(tabsUI.chats) tabs.selectItem("chats");
		else tabs.selectItem("welcome");

		if(tabsUI.chats){
			NotifyUtil.permitNotifications(Labels.get("notify.msgPermitted"));
		}

		setInterval(function(){
			Global.trigger(Msg.globalMediumTimer, null, true);
		}, 4000);

	};

	function initContext(){


		FrontApp.security.init();

		FrontApp.tariffs = new TariffsService();
		FrontApp.tariffs.init();

		Ajax.init_CSRF_ProtectTokenFromDOM();

		AjaxSetup.customError = function(){
			GlobalMsgPopup.showInvalidReqMsg();
		};
		AjaxSetup.customJsonError = function(resp){
			GlobalMsgPopup.showJsonError(resp);
		};

		GlobalMsgPopup.initDefaultTheme();

		TablePanelDefaults.tableClass = "table table-hover";

	}

	function initUserHeader(){

		var ui = $("#userHeader").createUI();

		//logout
		ui.logout.click(function(){

			if(ui.logout.hasClass("notLink")) return false;
			ui.logout.addClass("notLink");

			FrontApp.security.logoutReq({
				complete: function(){
					BrowserUtil.reloadPage();
				}
			});
			return false;
		});

		//login updates
		Global.bind(null, Msg.security_UserUpdated, function(){
			var newUser = FrontApp.security.getUser();
			ui.name.text(newUser.login);
		});

	}

	function startPingSession(){
		if(pingStarted) return;
		pingStarted = true;

		var pingReqElem = HtmlUtil.createRootReqElem("ping");

		var pingFunc = function(){
			Ajax.proxyJsonPost(pingReqElem, {
				url: "/system-api/ping",
				anyError: function(errorData){
					if(ServerErrorUtil.isNeedSignInError(errorData)){
						$("#reloadPageMsg").show();
					}
				}
			});
		};

		setInterval(pingFunc, 1000*60);
	}

	function initMsgNotifications(){

		Global.bind(null, Msg.chats_newNotifyChat, function(e, accId, chat){
			var tab = tabs.getItemTab("chats");
			var count = tab.data("notify-count") || 0;
			count++;

			tab.data("notify-count", count);
			tab.addClass("notify-msg");
			updateNotificationTitle(count);

			if( ! window.isActive){
				NotifyUtil.sendNotification(Labels.get("notify.newMessage"));
			}

		});

		Global.bind(null, Msg.chats_stopNotifyChat, function(e, accId, chat){
			var tab = tabs.getItemTab("chats");
			var count = tab.data("notify-count") || 1;
			count--;
			
			tab.data("notify-count", count);
			if(count == 0) tab.removeClass("notify-msg");
			updateNotificationTitle(count);
		});

	}

	function updateNotificationTitle(count){

		if(countInTitle == count) return;
		countInTitle = count;

		if(count == 0){
			document.title = initTitle;
		} else {
			document.title = Labels.get("chats.newMessagesTitle")+" "+count+" — " + initTitle;
		}
	}

}


CabinetPageUtil = {

	getAccsCount: function(){
		return $("#chatAccs").children().length;
	}

};