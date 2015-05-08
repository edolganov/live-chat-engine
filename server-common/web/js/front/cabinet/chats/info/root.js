
function AccInfoController(acc){

	var self = this;
	var accId = acc.uid;
	var ui = Util.cloneById("accInfo-template").createUI();
	var tariffs = FrontApp.tariffs;
	var security = FrontApp.security;

	var updateTariffController;

	this.init = function(){

		ui.id.text(acc.uid);

		//created
		if(security.isAccOwnerOrModer(accId) && acc.created){
			ui.created.text(DateUtil.format_DD_MM_YYYY(acc.created));
		} else {
			ui.createdBlock.setVisible(false);
		}

		//name
		if(security.isAccOwner(accId)){

			ui.nameRead.setVisible(false);
			resetName();

			ui.update.click(function(){
				updateAccNameReq();
			});
		}
		else {
			ui.name.setVisible(false);
			ui.update.setVisible(false);
			if(acc.name) ui.nameRead.text(acc.name);
		}

		//tariff
		if( ! security.isAccOwnerOrModer(accId)){
			ui.tariffBlock.hide();
		} else {
			initTariffInfo();
		}


		//email notifications
		if( ! security.isAccOwnerOrModer(accId)){
			ui.emailNotifyBlock.hide();
		} else {
			initEmailNotifications();
		}

		//client code
		var codeText = ui.clientCode.text();
		codeText = codeText.replaceAll("#{ACC_ID}", accId);
		ui.clientCode.text(codeText);


		Global.bindGroup(ui.root, Msg.chats_accPausedStateUpdated, acc.uid, function(){
			initTariffLabels();
		});

	};


	function initTariffInfo(){

		//info
		initTariffLabels();
		ui.tariffOpenBilling.click(function(){
			Global.trigger(Msg.cabinet_openBilling);
		});

		//update tariff
		updateTariffController = new UpdateTariffController(self, acc);
		updateTariffController.init();
		ui.changeNextTariffForm.append(updateTariffController.getUI());
		ui.changeNextTariffForm.hide();

		ui.changeNextTariffLink.click(function(){
			if(ui.changeNextTariffForm.isVisible()){
				self.hideNextTariffForm();
			} else {
				self.openNextTariffForm();
			}
		});
	}

	function initTariffLabels(){

		var tariffId = acc.tariffId;
		var isPaused = tariffs.isPausedTariff(tariffId);
		var tariff = isPaused? tariffs.getPausedTariff() : tariffs.getTariff(acc.tariffId);

		ui.tariffName.text(tariff? tariff.name : "UNKNOWN");
		ui.tariffPrice.text(tariff? MoneyUtil.formatPrice(tariff.price) : "Unknown");
		ui.tariffMaxOperators.text(tariff? tariff.maxOperators : "Unknown");

		if(isPaused && ! ui.changeNextTariffLink.data("paused")){
			HtmlUtil.switchTexts(ui.changeNextTariffLink, ui.changeNextTariffLink_otherLabel);
			ui.changeNextTariffLink.data("paused", true);
		}
		else if( !isPaused && ui.changeNextTariffLink.data("paused")){
			HtmlUtil.switchTexts(ui.changeNextTariffLink, ui.changeNextTariffLink_otherLabel);
			ui.changeNextTariffLink.data("paused", false);
		}
	}

	this.updateTariffInfo = function(){
		initTariffLabels();
	};
	
	this.openNextTariffForm = function(){
		//updateTariffController.reinit();
		ui.changeNextTariffForm.show(500);
	};


	this.hideNextTariffForm = function(){
		ui.changeNextTariffForm.hide(500);
	};



	function initEmailNotifications(){

		HtmlUtil.setChecked(ui.feedback_notifyOpsByEmail, acc.feedback_notifyOpsByEmail);

		ui.feedback_notifyOpsByEmail.change(function(){
			//var isChecked = HtmlUtil.isChecked(ui.feedback_notifyOpsByEmail);
			//HtmlUtil.setChecked(ui.feedback_notifyOpsByEmail, ! isChecked);
			update_feedback_notifyOpsByEmailReq();
		});
	}



	this.getUI = function(){
		resetName();
		return ui.root;
	};

	function resetName(){
		if(acc.name) ui.name.val(acc.name);
	}

	function updateAccNameReq(){

		Ajax.proxyJsonPost(ui.name, {
			url: "/system-api/chat/putAccConfig",
			data: {
				accId: accId,
				key: "name",
				val: ui.name.val()
			},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.update);
			},
			success: function(){
				BrowserUtil.reloadPage();
			},
			anyError: function(){
				BtnOps.enableBtn(ui.update);
			}
		});

	}

	function update_feedback_notifyOpsByEmailReq(){

		var isChecked = HtmlUtil.isChecked(ui.feedback_notifyOpsByEmail);

		Ajax.proxyJsonPost(ui.feedback_notifyOpsByEmail, {
			name:"feedback_notifyOpsByEmail",
			url: "/system-api/chat/putAccConfig",
			singleReqDelay: 1000,
			data: {
				accId: accId,
				key: "feedback_notifyOpsByEmail",
				val: isChecked
			},
			useCustomErrors: true,
			beforeSend: function(){},
			success: function(){
				GlobalMsgPopup.showInfoMsg(Labels.get("accInfo.msg.notifyUpdated"));
			},
			anyError: function(){}
		});
	}

}