

function ConfirmPayFormController(owner, val){

	var ui = Util.cloneById("paymentConfirmForm-template").createUI();

	this.init = function(){

		ui.val.text(val);

		ui.hide.click(function(){
			owner.hideConfirmForm();
		});

		ui.pay.click(function(){
			confirmPayReq();
		});

		ui.cancel.click(function(){
			cancelPayReq();
		});
	};

	this.getUI = function(){
		return ui.root;
	};

	function confirmPayReq(){
		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/billing/confirmPaypalPay",
			useCustomErrors: true,
			beforeSend: function(){
				ui.cancel.disable();
				BtnOps.disableBtnWait(ui.pay);
			},
			success: function(resp){

				GlobalMsgPopup.showInfoMsg(Labels.get("billing.msg.payConfirmed"));
				
				if(owner.isUnblockedEvent(resp)){
					GlobalMsgPopup.showWarningMsg(Labels.get("billing.msg.accUnblocked"));
				}

				owner.removeConfirmForm();

				if(resp) owner.updateData(0, resp);
			},
			complete: function(){
				ui.cancel.enable();
				BtnOps.enableBtn(ui.pay);
			}
		});
	}

	function cancelPayReq(){
		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/billing/cancelPaypalPay",
			useCustomErrors: true,
			beforeSend: function(){
				ui.pay.disable();
				BtnOps.disableBtnWait(ui.cancel);
			},
			success: function(){
				GlobalMsgPopup.showInfoMsg(Labels.get("billing.msg.payCanceled"));
				owner.removeConfirmForm();
			},
			complete: function(){
				ui.pay.enable();
				BtnOps.enableBtn(ui.cancel);
			}
		});
	}

}