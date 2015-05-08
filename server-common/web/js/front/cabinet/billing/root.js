
function BillingController(){

	var self = this;
	var ui = Util.cloneById("billing-template").createUI();
	var paymentController;
	var confirmController;
	var page = 0;
	var labels = [
		Labels.get("col.type"),
		Labels.get("col.description"),
		Labels.get("col.status"),
		Labels.get("col.date"),
		Labels.get("col.amount")];
	var pagging;
	var curBalance;

	this.init = function(){

		curBalance = Util.getProp("userBalanceVal", 0);

		new PriceVal(ui.balance, curBalance);

		var confirmVal = Util.getProp("payConfirmVal");
		if(Util.isEmptyString(confirmVal) || Util.parseInt(confirmVal, 0) == 0){
			ui.confirmBlock.hide();
			ui.confirmForm.hide();
		} else {
			confirmController = new ConfirmPayFormController(self, confirmVal);
			confirmController.init();
			ui.confirmForm.append(confirmController.getUI());

			ui.confirmForm.show();
			ui.confirmPayment.click(function(){
				if(ui.confirmForm.isVisible()) self.hideConfirmForm();
				else self.showConfirmForm();
			});
		}

		
		paymentController = new PaymentFormController(self);
		paymentController.init();
		ui.paymentForm.append(paymentController.getUI());

		ui.paymentForm.hide();
		ui.payment.click(function(){
			if(ui.paymentForm.isVisible()) self.hidePaymentForm();
			else self.showPaymentForm();
		});


		ui.reload.click(function(){
			reloadPaymentsReq(page);
		});

		pagging = new PaggingPanel(ui.root);
		pagging.hide();
		pagging.onPrev(function(){
			reloadPaymentsReq(page - 1);
		});
		pagging.onNext(function(){
			reloadPaymentsReq(page + 1);
		});


		//load first data
		reloadPaymentsReq(page);

		Global.bind(ui.root, Msg.billing_Reload, function(){
			reloadPaymentsReq(0);
		});
	};

	this.hidePaymentForm = function(){
		ui.paymentForm.hide(500);
	};

	this.showPaymentForm = function(){
		ui.paymentForm.show(500);
	};

	this.hideConfirmForm = function(){
		ui.confirmForm.hide(500);
	},

	this.showConfirmForm = function(){
		ui.confirmForm.show(500);
	};

	this.removeConfirmForm = function(){
		ui.confirmBlock.hide();
		ui.confirmForm.hide();
	};


	this.getUI = function(){
		return ui.root;
	};


	function reloadPaymentsReq(newPage){
		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/billing/payments",
			data: {page: newPage},
			useCustomErrors: true,
			beforeSend: function(){
				pagging.disable();
				BtnOps.disableBtnWait(ui.reload);
			},
			success: function(resp){
				self.updateData(newPage, resp);
			},
			complete: function(){
				BtnOps.enableBtn(ui.reload);
				pagging.enable();
			}
		});
	}

	this.isUnblockedEvent = function(resp){
		var oldBalance = Util.parseFloat(curBalance, 0);
		var newBalance = Util.parseFloat(resp.balance, 0);
		return oldBalance < 0 && newBalance >= 0;
	};

	this.updateData = function(newPage, resp){

		curBalance = resp.balance;

		ui.balance.empty();
		new PriceVal(ui.balance, curBalance);

		//no items
		if(resp.list.length == 0){
			if(newPage == 0){
				page = newPage;
				showPayments(resp.list);
				pagging.hide();
			}
			pagging.setState(page, true);
			return;
		}

		page = newPage;
		pagging.setState(page, false);
		pagging.show();
		showPayments(resp.list);
	}

	function showPayments(list){

		ui.payments.empty();

		if(Util.isEmptyArray(list)){
			ui.payments.text(Labels.get("billing.emptyList"));
			return;
		}

		var table = new TablePanel(5, labels, {root: ui.payments});

		$.each(list, function(i, item){
			table.addElemsRow([
				createTypeCell(item),
				createNameCell(item),
				createStatusCell(item),
				DateUtil.format_DD_MM_YYYY(DateUtil.dateFrom(item.updated)),
				new PriceVal(null, item.amount).getUI()
			]);
		});

	}

	function createTypeCell(payment){
		if(payment.payType > 0) return $("<span class='input'>" + Labels.get("billing.inputPay") + "</span>");
		else return $("<span class='output'>" + Labels.get("billing.outputPay") + "</span>");
	}

	function createStatusCell(payment){
		return Labels.get("billing.status."+payment.status, payment.status);
	}

	function createNameCell(payment){
		if(payment.payType == 3) return Labels.get("billing.payName.bonus");
		if(payment.payType > 0) return Labels.get("billing.payName.userPay");
		else return Labels.get("billing.payName.invoice");
	}

}