<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - ExternalRememberMeOptOut</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>ExternalRememberMeOptOut</h3>
			<div id="apidetails">
				<p>ExternalRememberMeOptOut API Operation invalidates particular
					external remember me id.</p>
			</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">External Remember Me ID*</div>
					<div class="param_value">
						<input type=text size=30 maxlength=32 name="externalRememberMeId"
							value="">
					</div>
				</div>
				<div class="params">
					<div class="param_name">External Remember Me Owner ID Type*</div>
					<div class="param_value">
						<select name="ownerIDType">
							<option value="None">None</option>
							<option value="Email">Email</option>
							<option value="SecureMerchantAccountID">SecureMerchantAccountID</option>
						</select>
					</div>
				</div>
				<div class="params">
					<div class="param_name">External Remember Me Owner ID*</div>
					<div class="param_value">
						<input type=text size=30 maxlength=32 name="ownerID" value="">
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="ExternalRememberMeOptOutBtn"
						value="ExternalRememberMeOptOut" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls"> See also
		<ul>
			<li><a href="GetExpressCheckout">GetExpressCheckout</a>
			</li>
			<li><a href="DoExpressCheckout">DoExpressCheckout</a>
			</li>
		
		</ul>
	</div>
	</div>

</body>
</html>