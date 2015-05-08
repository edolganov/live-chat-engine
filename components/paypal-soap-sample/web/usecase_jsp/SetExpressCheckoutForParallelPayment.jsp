<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal Merchant SDK - SetExpressCheckout</title>
<link rel="stylesheet" type="text/css" href="sdk.css" />
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>SetExpressCheckout</h3>
			<div id="apidetails">Use ExpressCheckout for making payments to multiple receivers </div>
		</div>
		<br>
		<div id="request_form">
			<form method="POST" >
				<div class="params">
					<div class="param_name">Buyer email (Email address of the
						buyer as entered during checkout. PayPal uses this value to
						pre-fill the PayPal membership sign-up portion of the PayPal login
						page)</div>
					<div class="param_value">
						<input type="text" name="buyerEmail"
							value="platfo_1255077030_biz@gmail.com" size="50" maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">PaymentAction</div>
					<div class="param_value">
						<select name="paymentAction">
							<option value="Sale">Sale</option>
							<option value="Authorization">Authorization</option>
							<option value="Order">Order</option>
						</select>
					</div>
				</div>
				<div class="section_header">Payment Details</div>
				<div class="params"></div>

				<div class="params">
					<div class="param_name">CurrencyCode</div>
					<div class="param_value">
						<input type="text" name="currencyCode" value="USD" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Order Total</div>
					<div class="param_value">
						<input type="text" name="orderTotal" id="orderTotal" value="1.00" />
					</div>
				</div>
				<b/>
				<div class="param_name">
					<table>
						<tr>
							<th>Receiver Emails</th>
							<th>Request Id</th>
						</tr>
						<tr>
						    <td><input type="text" name="receiverEmail_0" value="platfo_1255170694_biz@gmail.com" size="50" maxlength="260" /></td>
							<td><input type="text" name="requestId_0" value="CART286-PAYMENT0" size="50" maxlength="260" /></td> 
						</tr>
						<tr>
						    <td><input type="text" name="receiverEmail_1" value="platfo_1255170694_biz@gmail.com" size="50" maxlength="260" /></td>
							<td><input type="text" name="requestId_1" value="CART286-PAYMENT1" size="50" maxlength="260" /></td> 
						</tr>
					</table>	
				
				
				</div>
				<div class="submit">
					<input type="submit" name="SetExpressCheckoutBtn"
						value="SetExpressCheckout" /> <br />
				</div>
				<a href="index.html">Home</a>
			</form>
		</div>
	</div>
</body>
</html>