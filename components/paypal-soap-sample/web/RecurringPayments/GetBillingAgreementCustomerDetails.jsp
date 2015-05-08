<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<title>PayPal SDK - GetBillingAgreementCustomerDetails</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>GetBillingAgreementCustomerDetails</h3>
			<div id="GetBillingAgreementCustomerDetails">
				<p>
					<b>GetBillingAgreementCustomerDetails API operation obtains
						information about a billing agreement's PayPal account holder.</b>
				</p>
			</div>
		</div>
		<form id="form1" method="post">
			<div id="request_form">
				<div class="params">
					<div class="param_name">
						Token:(Get Token via <a href="../EC/SetExpressCheckout">ExpressCheckout</a>)
					</div>
					<div class="param_value">
						<input type="text" name="token" value="" />
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="GetBillingAgreementCustomerDetailsBtn"
						value="GetBillingAgreementCustomerDetails" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls">
			See also:
			<ul>
				<li><a href="RT/BillAgreementUpdate">BillAgreementUpdate</a>
				</li>
				<li><a href="RT/DoReferenceTransaction">DoReferenceTransaction</a>
				</li>
				
			</ul>
		</div>
	</div>
</body>
</html>
