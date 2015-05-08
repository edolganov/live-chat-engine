
function PaymentFormController(owner){

	var ui = Util.cloneById("paymentForm-template").createUI();

	this.init = function(){

		ui.cancel.click(function(){
			owner.hidePaymentForm();
		});

		ui.pay.click(function(){
			payReq_2checkout();
		});
	};

	this.getUI = function(){
		return ui.root;
	};


	function payReq_paypal(){

		var val = ui.payType.val();

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/billing/pay",
			data: {val: val, provider:'paypal'},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.pay);
			},
			success: function(resp){
				GlobalMsgPopup.showInfoMsg(Labels.get("billing.msg.redirectToPaypal"));
				setTimeout(function(){
					BrowserUtil.loadPage(resp.redirectUrl);
				}, 1000);

			},
			anyError: function(){
				BtnOps.enableBtn(ui.pay);
			}
		});
	}

	function payReq_2checkout(){

		var val = ui.payType.val();

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/billing/pay",
			data: {val: val, provider:'2checkout'},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.pay);
			},
			success: function(resp){
				GlobalMsgPopup.showInfoMsg(Labels.get("billing.msg.redirectTo2Checkout"));
				setTimeout(function(){
					FormUtil.submitDynamicForm(resp.redirectUrl, {
						sid:resp.accId,
						och_token:resp.token,
						mode:'2CO',
						fixed:'Y',
						li_0_type:'product',
						li_0_name:'Live chat Account Payment',
						li_0_quantity:'1',
						li_0_price:val,
						li_0_tangible:'N',
						currency_code:'USD'
					});
				}, 1000);

			},
			anyError: function(){
				BtnOps.enableBtn(ui.pay);
			}
		});
	}


	function directPaymentExmpl(){
//		BtnOps.disableBtnWait(ui.pay);
//
//		var actionUrl = "https://www.sandbox.paypal.com/cgi-bin/webscr";
//		var paypalAcc = "";
//		FormUtil.submitDynamicForm(actionUrl, {
//			cmd: "_xclick",
//			business:paypalAcc,
//			item_name: "Online Chat Payment",
//			amount: val,
//			currency_code:"USD",
//			no_shipping:1,
//			rm: 1,
//			"return":"https://127.0.0.1:10643/cabinet?successPayment",
//			cancel_return:"https://127.0.0.1:10643/cabinet?failPayment"
//		});
	}

}