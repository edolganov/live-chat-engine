<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - Bill User</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>Bill User</h3>
			<div id="apidetails">Initiate a "merchant pull" (or
				"prepparoved payment") transaction - transfer money from the user's
				account to the merchant's account in accordance with a previously
				established billing agreement.</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">
						Billing Agreement ID*(Get Billing Agreement ID via <a
							href="../EC/SetExpressCheckout">ExpressCheckout</a>)
					</div>
					<div class="param_value">
						<input type="text" name="billingAgreementID" value="" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Payment Type</div>
					<div class="param_value">
						<select name=paymentCodeType>
							<option value=Any>Any</option>
							<option value=InstantOnly>InstantOnly</option>
							<option value=EcheckOnly>EcheckOnly</option>
						</select>
					</div>
				</div>
				<div class="section_header">Item Details</div>
				<table class="params">
					<tr>
						<th class="param_name">Item Name</th>
						<th class="param_name">Item Number</th>
					</tr>
					<tr>
						<td class="param_value"><input type="text" name="itemName"
							value="Goods" />
						</td>
						<td class="param_value"><input type="text" name="itemNum"
							value="1" />
						</td>
					</tr>
				</table>
				<table class="params">
					<tr>
						<th class="param_name">Amount to be charged*</th>
						<th class="param_name">Shipping cost</th>
						<th class="param_name">Handling cost</th>
						<th class="param_name">Tax Amount</th>
						<th class="param_name">Currency Code*</th>
					</tr>
					<tr>
						<td class="param_value"><input type="text" name="amt"
							value="0.50" />
						</td>
						<td class="param_value"><input type="text" name="shipping"
							value="0.50" />
						</td>
						<td class="param_value"><input type="text" name="handling"
							value="0.00" />
						</td>
						<td class="param_value"><input type="text" name="tax"
							value="0.0" />
						</td>
						<td class="param_value"><input type="text" name="currencyID"
							value="USD" />
						</td>
					</tr>
				</table>
				<div class="params">
					<div class="param_name">Subject in email</div>
					<div class="param_value">
						<input type="text" name="mailSubject" value="BillUser" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Memo</div>
					<div class="param_value">
						<input type="text" name="memo" value="BillUser" />
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="BillUserBtn" value="BillUser" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls">
			See also:
			<ul>
				<li><a href="../EC/SetExpressCheckout">SetExpressCheckout</a></li>
				<li><a href="GetBillingAgreementCustomerDetails">GetBillingAgreementCustomerDetails</a>
				</li>
				<li><a href="BillAgreementUpdate">BillAgreementUpdate</a></li>
				<li><a href="DoReferenceTransaction">DoReferenceTransaction</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>