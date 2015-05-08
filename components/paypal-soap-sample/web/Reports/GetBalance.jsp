<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - GetBalance</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>GetBalance</h3>
			<div id="apidetails">GetBalance API Operation obtains the
				available balance for a PayPal account.</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">ReturnAllCurrencies(Indicates whether
						to return all currencies)</div>
					<div class="param_value">
						<select name="returnAllCurrencies">
							<option value=""></option>
							<option value="0">Return only the balance for the
								primary currency holding</option>
							<option value="1">Return the balance for each currency
								holding</option>
						</select>
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="GetBalanceBtn" value="GetBalance" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>

		<div id="relatedcalls">
			See also
			<ul>
				<li><a href="TransactionSearch">TransactionSearch</a>
				</li>
				<li><a href="GetTransactionDetails">GetTransactionDetails</a>
				</li>
				<li><a href="GetPalDetails">GetPalDetails</a>
				</li>
				<li><a href="AddressVerify">AddressVerify</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>