<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - DoUATPExpressCheckoutPayment</title>

</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>DoUATPExpressCheckoutPayment</h3>
			<div id="apidetails">Used to make checkout payment for airline
				merchants.</div>
		</div>
		<form method="POST">
			<div id="request_form">

				<div class="params">
					<div class="param_name">PayerID*</div>
					<div class="param_value">
						<input type="text" name="payerID" value="" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">
						EC Token*(Get Token via <a href="SetExpressCheckout">SetExpressCheckout</a>)
					</div>
					<div class="param_value">
						<input type="text" name="token" value="" size="50" maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Payment Action*</div>
					<div class="param_value">
						<select name="paymentAction">
							<option value="Order">Order</option>
						</select>
					</div>
				</div>
				<div class="params">
					<div class="param_name">CurrencyID*</div>
					<div class="param_value">
						<input type="text" name="currencyID" value="" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Amount*</div>
					<div class="param_value">
						<input type="text" name="amt" value="" size="50" maxlength="260" />
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="DoUATPExpressCheckoutPaymentBtn"
						value="DoUATPExpressCheckoutPayment" /><br />
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
				<li><a href="SetExpressCheckout">SetExpressCheckout</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>