<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - AddressVerify</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>AddressVerify</h3>
			<div id="apidetails">AddressVerify API operation confirms
				whether a postal address and postal code match those of the
				specified PayPal account holder.</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">Mail*</div>
					<div class="param_value">
						<input type="text" name="mail" value="" size="50" maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Street*</div>
					<div class="param_value">
						<input type="text" name="street" value="" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Zip*</div>
					<div class="param_value">
						<input type="text" name="zip" value="" size="50" maxlength="260" />
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="AddressVerifyBtn" value="AddressVerify" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls">
			See also
			<ul>
				<li><a href="TransactionSearch">TransactionSearch</a></li>
				<li><a href="GetPalDetails">GetPalDetails</a></li>
				<li><a href="GetBalance">GetBalance</a>
				</li>
				<li><a href="GetTransactionDetails">GetTransactionDetails</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>