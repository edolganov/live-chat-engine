

var UpdateTariffController = function(owner, acc){

	var self = this;
	var ui = Util.cloneById("updateTariff-template").createUI();
	var tariffs = FrontApp.tariffs;

	this.init = function(){

		new TariffSelect(ui.tariffSelect);

		updateViewState();

		ui.cancel.click(function(){
			owner.hideNextTariffForm();
		});

		ui.update.click(function(){
			updateReq();
		});

		ui.pause.click(function(){
			pauseReq();
		});


		//events
		Global.bindGroup(ui.root, Msg.chats_accPausedStateUpdated, acc.uid, function(){
			updateViewState();
		});

	};

	this.getUI = function(){
		return ui.root;
	};

	function updateViewState(){
		
		var isPaused = tariffs.isPausedTariff(acc.tariffId);

		ui.update.setEnable( ! isPaused);

		if(isPaused && ! ui.pause.data("paused")){
			HtmlUtil.switchTexts(ui.pause, ui.pause_otherLabel);
			ui.pause.data("paused", true);
			ui.pause.addClass("btn-success").removeClass("btn-warning");
		}
		else if( !isPaused && ui.pause.data("paused")){
			HtmlUtil.switchTexts(ui.pause, ui.pause_otherLabel);
			ui.pause.data("paused", false);
			ui.pause.addClass("btn-warning").removeClass("btn-success");
		}

	}

	function updateReq(){

		var data = ui.root.getFormData();
		data.accId = acc.uid;

		if(data.tariff == acc.tariffId){
			GlobalMsgPopup.showInfoMsg(Labels.get("tariffChange.already"));
			return;
		}

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/updateTariff",
			data: data,
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.update);
				ui.pause.disable();
			},
			success: function(priceVal){
				
				acc.tariffId = data.tariff;
				owner.updateTariffInfo();

				var msg = Labels.get("tariffChange.msg.updated");
				if(priceVal > 0){
					msg += getPriceValMsg(priceVal);
					Global.trigger(Msg.billing_Reload);
				}
				GlobalMsgPopup.showInfoMsg(msg);
				

				owner.hideNextTariffForm();
			},
			complete: function(){
				BtnOps.enableBtn(ui.update);
				ui.pause.enable();
			}
		});
	}


	function pauseReq(){

		var isPaused = tariffs.isPausedTariff(acc.tariffId);

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/pauseAcc",
			data: {accId: acc.uid, val: !isPaused},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.pause);
				ui.update.disable();
			},
			success: function(resp){

				//update model
				acc.tariffId = resp.newTariffId;

				//update views
				BtnOps.enableBtn(ui.pause);
				ui.update.enable();

				var isPausedNow = tariffs.isPausedTariff(acc.tariffId);

				var msg = isPausedNow ? Labels.get("tariffChange.msg.paused") : Labels.get("tariffChange.msg.unpaused");
				var priceVal = resp.prevTariffCost;
				if(priceVal > 0){
					msg += getPriceValMsg(priceVal);
					Global.trigger(Msg.billing_Reload);
				}
				GlobalMsgPopup.showInfoMsg(msg);

				owner.hideNextTariffForm();

				//udpdated event
				Global.triggerGroup(Msg.chats_accPausedStateUpdated, acc.uid, [self]);
			},
			anyError: function(){
				BtnOps.enableBtn(ui.pause);
				ui.update.enable();
			}
		});

	}

	function getPriceValMsg(priceVal){
		return "<br>"+Labels.get("tariffChange.msg.updatedDescBegin")+" $"+MoneyUtil.formatPrice(priceVal)+"."
			+"<br>"+Labels.get("tariffChange.msg.updatedDescEnd")
			+" <a href='javascript:' onclick='if(Global) Global.trigger(Msg.cabinet_openBilling);'>"
			+ Labels.get("tariffChange.msg.billingLink")
			+"</a>";
	}

};