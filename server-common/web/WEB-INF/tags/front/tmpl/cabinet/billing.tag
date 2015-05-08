<%@tag pageEncoding="UTF-8" %>

<div id="billing-template">

	<div class="paymentTop">

		<div class="balanceRow">
			<span i18n="billing.curBal">Current balance:</span> <b><span bind="balance"></span></b>
		</div>

		<span bind="confirmBlock">
			<a bind="confirmPayment" class="pseudo" href="javascript:" i18n="billing.confirmPay">Confirm Payment</a>
			<sep></sep>
		</span>
		<a bind="payment" class="pseudo" href="javascript:" i18n="billing.newPay">New Payment</a>
		
	</div>


	<div bind="confirmForm" class="confirmForm bordered"></div>
	<div bind="paymentForm" class="paymentForm bordered"></div>

	<br>
	<br>
	<legend>
		<span i18n="billing.payments">Payments</span>
		<button bind="reload" class="btn btn-default" i18n="common.update">Update</button>
	</legend>
	<div bind="payments" class="payments"></div>
	
</div>


<div id="paymentForm-template">

	<legend i18n="billing.payment">Payment</legend>

	<select bind="payType">
		<option value="5">$5</option>
		<option value="10">$10</option>
		<option value="15">$15</option>
		<option value="20">$20</option>
		<option value="30">$30</option>
		<option value="40">$40</option>
		<option value="50">$50</option>
		<option value="100">$100</option>
	</select>

	<br>
	<button bind="pay" class="btn btn-default" i18n="billing.payByPaypal">Pay</button>
	<sep></sep>
	<a bind="cancel" href="javascript:" class="pseudo" i18n="common.hide">Hide</a>
</div>


<div id="paymentConfirmForm-template" class="confirmPayForm">

	<legend i18n="billing.confirmPay">Confirm Payment</legend>

	<div class="confirmVal"><span i18n="billing.youSelected">You selected:</span> <b>$<span bind="val"></span></b></div>

	<button bind="pay" class="btn btn-success" i18n="billing.pay">Pay</button>
	<sep></sep>
	<button bind="cancel" class="btn btn-default" i18n="common.cancel">Cancel</button>
	<sep></sep>
	<a bind="hide" href="javascript:" class="pseudo" i18n="common.hide">Hide</a>
</div>


