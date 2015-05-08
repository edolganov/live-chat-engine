<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - ExecuteCheckoutOperations</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>ExecuteCheckoutOperations</h3>
			<div id="apidetails">Creation of billing agreements without the
				web flow.</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="section_header">Billing Approval Details*</div>
				<table class="params">
					<tr>
						<th class="param_name">BillingApprovalType</th>
						<th class="param_name">BillingApprovalSubType</th>
						<th class="param_name">BillingAmount</th>
						<th class="param_name">CurrencyID</th>
						<th class="param_name">Payment type</th>
					</tr>
					<tr>
						<td class="param_value"><select name="billingApprovalType">
								<option value="BillingAgreement">BillingAgreement</option>
								<option value="Profile">Profile</option>
						</select>
						</td>
						<td class="param_value"><select name="billingApprovalSubType">
								<option value="None">None</option>
								<option value="MerchantInitiatedBilling">MerchantInitiatedBilling</option>
						</select>
						</td>
						<td class="param_value"><input type="text" name="amt"
							value="2.00" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="currencyID"
							value="USD" maxlength="260" />
						</td>
						<td class="param_value"><select name="paymentType">
								<option value="Any">Any</option>
								<option value="InstantOnly">InstantOnly</option>
								<option value="EcheckOnly">EcheckOnly</option>
						</select>
						</td>
					</tr>
				</table>
				<div class="params">
					<div class="param_name">Need Authorization?</div>
					<div class="param_value">
						<select name="isRequested">
							<option value="False">No</option>
							<option value="True">Yes</option>
						</select>
					</div>
				</div>
				<div class="section_header">Identification Info(Mandatory if
					Authorization is Requested)</div>
				<div class="note">Set either MobileSessionToken or
					ExternalRememberMeID</div>
				<table class="params">
					<tr>
						<th class="param_name">Mobile Session Token</th>
						<th class="param_name">External Remember Me ID</th>
					</tr>
					<tr>
						<td class="param_value"><input type="text"
							name="sessionToken" value="" maxlength="260" /></td>
						<td class="param_value"><input type="text"
							name="externalRememberMeID" value="" maxlength="260" /></td>
					</tr>
				</table>
				<div class="params">
					<div class="param_name">Require Billing Address in
						GetExpressCheckout Response</div>
					<div class="param_value">
						<select name="reqBillingAddress">
							<option value="0">No</option>
							<option value="1">Yes</option>
						</select>
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="ExecuteCheckoutOperationsBtn"
						value="ExecuteCheckoutOperations" /> <br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls">
			See also
			<ul>
				<li><a href="GetExpressCheckout">GetExpressCheckout</a>
				</li>
				<li><a href="DoExpressCheckout">DoExpressCheckout</a>
				</li>
				<li><a href="DoUATPExpressCheckoutPayment">DoUATPExpressCheckout</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>