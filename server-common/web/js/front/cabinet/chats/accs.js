
/**
 * Список аккаунтов оператора
 * [чаты] - [табы акканутов*]
 */
function ChatAccsController(){

	var log = LogFactory.getLog("ChatAccsController");
	var ui = $("#chatServersPanel").createUI();
	var tabPanel;
	var changeGlobalStatusUI;
	
	var sessionToken;
	var allAccs = [];
	var serversByUrl = {};
	var isSingleAcc = false;

	var needAutoRecreateSessions = true;

	this.init = function(){

		//load accs data
		$("#chatAccs").children().each(function(i, elem){

			var acc = createAccFromData($(elem));

			allAccs.push(acc);

			var url = acc.workUrl;
			var server = serversByUrl[url];
			if(!server){

				var idFromUrl = url.replaceAll("/", "").replaceAll(":", "_");

				server = {
					url:url,
					inited: false,
					accs:[],
					reqElemState: HtmlUtil.createRootReqElem("server-state-"+idFromUrl, true),
					reqElemUpdate: HtmlUtil.createRootReqElem("server-update-"+idFromUrl, true)
				};
				serversByUrl[url] = server;
			}
			server.accs.push(acc);
		});

		ui.retryInit.click(function(){
			initAccSessions();
		});

		initAccSessions();

		Global.bind(ui.root, Msg.chats_operatorStatus, function(e, accId, val){
			onOperatorStatusChanged(accId, val);
		});

		Global.bind(ui.root, Msg.chats_newNotifyChat, function(e, accId, chat){
			showNotifyMsgIfNeed(accId, chat);
		});

		Global.bind(ui.root, Msg.chats_stopNotifyChat, function(e, accId, chat){
			hideNotifyMsgIfNeed(accId, chat);
		})

	};

	this.getUI = function(){
		return ui.root;
	};

	function createAccFromData(elem){
		var acc = {};
		acc.uid = elem.attr("p-uid");
		acc.name = elem.attr("p-name");
		acc.httpUrl = elem.attr("p-httpUrl");
		acc.httpsUrl = elem.attr("p-httpsUrl");
		acc.created = DateUtil.dateFrom(elem.attr("p-created"));
		acc.tariffId = elem.attr("p-tariffId");
		acc.blocked = elem.attr("p-blocked") === "true";
		acc.feedback_notifyOpsByEmail = elem.attr("p-feedback_notifyOpsByEmail") !== "false"
		acc.serverId = elem.attr("p-serverId");

		acc.workUrl = acc.httpUrl;
		if(Props["frontApp_cabinet_useHttpUrlsForChats"] !== "true"
			&& ! Util.isEmptyString(acc.httpsUrl)){
			acc.workUrl = acc.httpsUrl;
		}

		return acc;
	}


	function initAccSessions(){

		var data = {};
		if( ! Util.isEmpty(sessionToken)){
			
			data.full = true;

			data.fullCheckAccs = {};
			$.each(allAccs, function(i, acc){
				if(Util.isEmpty(acc.serverId)) return;
				var list = data.fullCheckAccs[acc.serverId];
				if(!list){
					list = [];
					data.fullCheckAccs[acc.serverId] = list;
				}
				list.push(acc.uid);
			});
		}

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/initAccSession",
			data: data,
			beforeSend: function(){
				showInitMsg();
			},
			success: function(token){
				sessionToken = token;
				showAccsContent();
			},
			anyError: function(errorData){

				var resp = errorData.resp;
				if(resp && resp.type == "och.api.exception.chat.OutdatedChatAccountsException"){
					BrowserUtil.reloadPage();
					return;
				}

				showErrorMsg();
			}
		});
	}

	function showAccsContent(){
		showContent();
		showAccs();
	}

	function showInitMsg(){
		ui.initMsg.show();
		ui.errorInitMsg.hide();
		ui.content.hide();
	}

	function showErrorMsg(){
		ui.errorInitMsg.show();
		ui.initMsg.hide();
		ui.content.hide();
	}

	function showContent(){
		ui.content.show();
		ui.initMsg.hide();
		ui.errorInitMsg.hide();
	}


	//показываем аккаунты в виде табов
	function showAccs(){

		//init providers
		//it's normal to call it second time (some providers mayed be unavalable last time)
		initChatProviders();

		//clear old view
		ui.content.empty();

		var tabUI = TabPanelUtil.createTabRootUI(null, "accsNav");
		ui.content.append(tabUI.root);

		tabPanel = new TabPanel(tabUI);
		var firstUid = null;

		$.each(allAccs, function(i, acc){

			if(i == 0) firstUid = acc.uid;

			var tabItem = TabPanelUtil.createTabItem(getAccName(acc)+" ");
			var notifyElem = Util.cloneById("newMsgIcon-template");
			$("a", tabItem).append(notifyElem);
			notifyElem.attr("style", "");

			var tabController = new AccTabController(acc);
			tabController.init();
			tabController.retryListener = function(){
				initAccSessions();
			};

			acc.tab = tabController;
			acc.tabItem = tabItem;
			tabPanel.addItem(acc.uid, tabItem, tabController);

		});


		changeGlobalStatusUI = Util.cloneById("changeGlobalActiveStatus-template").createUI();
		initChangeGlobalStatus();
		changeGlobalStatusUI.root.hide();
		tabPanel.addItem("changeAll", changeGlobalStatusUI.root);

		if( ! Util.isEmpty(firstUid)){
			tabPanel.selectItem(firstUid);
		}

		if(allAccs.length == 1){
			isSingleAcc = true;
			showSingleAcc(tabUI);
		}

		initUserSessionForAllAccs();
	}

	function showSingleAcc(tabUI){

		tabUI.nav.hide();

		var acc = allAccs[0];
		var name = getAccName(acc);
		Global.trigger(Msg.chats_showSingleAcc, [name]);
		
	}

	function getAccName(acc){
		return acc.name? acc.name : acc.uid;
	}

	function initChatProviders(){
		$.each(serversByUrl, function(url){
			new ChatProvider(url).init();
		});
	}


	function initUserSessionForAllAccs(){
		$.each(serversByUrl, function(url, server){
			initUserSessionForServer(server);
		});
	}

	function initUserSessionForServer(server){

		var extraData = {};
		if( ! server.inited){
			extraData.sessionToken = sessionToken;
		}

		var uids = [];
		$.each(server.accs, function(i, acc){
			uids.push(acc.uid);
		});

		Ajax.proxyJsonPost(server.reqElemState, {
			url: server.url+"/system-api/chat/state",
			data: {uids:uids},
			extraData: extraData,
			beforeSend: function(){
				$.each(server.accs, function(i, acc){
					acc.tab.showLoadingState();
				});
			},
			success: function(state){
				needAutoRecreateSessions = false;
				server.inited = true;
				showAccsContentForSever(server, state);
				startLoadUpdatesForServer(server);
			},
			anyError: function(errorData){

				if(ServerErrorUtil.isNeedSignInError(errorData) && needAutoRecreateSessions){
					needAutoRecreateSessions = false;
					initAccSessions();
					return;
				}
				showErrorState(server);
			}
		});
	}

	function startLoadUpdatesForServer(server){
		if(server.updatedStarted) return;
		server.updatedStarted = true;

		var reload = function(){

			var data = {};
			data.compact = true;
			data.updates = {};
			$.each(server.accs, function(i, acc){
				var accInfo = {};
				data.updates[acc.uid] = accInfo;
				accInfo.chats = acc.tab.getChatsUpdateMap();
				accInfo.opActive = acc.isOperatorActive;
				accInfo.activeCount = acc.activeCount;
			});

			Ajax.proxyJsonPost(server.reqElemUpdate, {
				url: server.url + "/system-api/chat/updates",
				data: data,
				success: function(updateState){
					if(Util.isEmpty(updateState)) return;
					updateAccsContentForServer(server, updateState);
				},
				anyError: function(errorData){
					if(ServerErrorUtil.isNeedSignInError(errorData)){
						server.inited = false;
						showReconnectToAccMsg(server);
					}
				}
			});

		};

		setInterval(reload, 1000*8);

	}


	//показ контента для данного сервера
	function showAccsContentForSever(server, state){
		$.each(server.accs, function(i, acc){
			var accInfo = state.infoByAcc[acc.uid];
			if(!accInfo) return;
			
			var tab = acc.tab;

			tab.showAccContent();
			tab.showActiveChats(accInfo.chats);
			tab.showOperatorStatus(accInfo.opActive);
			tab.showOpsActiveCount(accInfo.activeCount);

		});
	}

	function updateAccsContentForServer(server, updateState){

		if(Util.isEmpty(updateState) || Util.isEmpty(updateState.infoByAcc)) return;

		$.each(server.accs, function(i, acc){

			var updateInfo = updateState.infoByAcc[acc.uid];
			if(Util.isEmpty(updateInfo)) return;

			var chats = updateInfo.chats;
			if( ! Util.isEmpty(chats)){
				acc.tab.updateActiveChats(chats);
			}
			
			var newActiveVal = updateInfo.opActive;
			if( ! Util.isEmpty(newActiveVal)){
				acc.tab.showOperatorStatus(newActiveVal);
			}

			var newOpsActiveCount = updateInfo.activeCount;
			if( ! Util.isEmpty(newOpsActiveCount)){
				acc.tab.showOpsActiveCount(newOpsActiveCount);
			}

		});
	}

	function showErrorState(server){
		$.each(server.accs, function(i, acc){
			acc.tab.showErrorState();
		});
	}

	function showReconnectToAccMsg(server){
		$.each(server.accs, function(i, acc){
			acc.tab.showReconnectToAccMsg();
		});
	}

	function onOperatorStatusChanged(accId, val){

		var elem = tabPanel.getItemUI(accId);
		if( ! elem) return;

		var isOperator = FrontApp.security.isAccOperator(accId);
		if( ! isOperator) return;

		if(val) TabOps.showOnlineAccTab(elem);
		else TabOps.showOfflineAccTab(elem);

		showChangeAllActivitiesLink();

		if(isSingleAcc){
			Global.trigger(Msg.chats_operatorStatusForSingleAcc, [val]);
		}
		
	}

	function initChangeGlobalStatus(){
		changeGlobalStatusUI.online.click(function(){
			Global.trigger(Msg.chats_operatorStatusChange, [true]);
		});
		changeGlobalStatusUI.offline.click(function(){
			Global.trigger(Msg.chats_operatorStatusChange, [false]);
		});
	}

	function showChangeAllActivitiesLink(){
		changeGlobalStatusUI.root.show();
	}

	function showNotifyMsgIfNeed(accId, chat){
		
		var acc = getAccByUid(accId);
		if(!acc) return;

		var tabItem = acc.tabItem;
		if(!tabItem) return;

		var count = tabItem.data("notify-count") || 0;
		tabItem.data("notify-count", ++count);
		tabItem.addClass("notify-msg");
	}

	function hideNotifyMsgIfNeed(accId, chat){

		var acc = getAccByUid(accId);
		if(!acc) return;

		var tabItem = acc.tabItem;
		if(!tabItem) return;

		var count = tabItem.data("notify-count") || 1;
		tabItem.data("notify-count", --count);
		if(count == 0){
			tabItem.removeClass("notify-msg");
		}
	}

	function getAccByUid(accUid){
		var out = null;
		$.each(allAccs, function(i, acc){
			if(acc.uid == accUid) {
				out = acc;
				return false;
			}
			return true;
		});
		return out;
	}

}