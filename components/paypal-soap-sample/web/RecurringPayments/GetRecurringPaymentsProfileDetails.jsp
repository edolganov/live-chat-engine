<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - GetRecurringPaymentsProfileDetails</title>

</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>GetRecurringPaymentsProfileDetails</h3>
			<div id="apidetails">
				<p>GetRecurringPaymentsProfileDetails API obtains information
					about a recurring payments profile.</p>
			</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">Profile ID*(Get Profile ID via <a
							href='CreateRecurringPaymentsProfile'>CreateRecurringPaymentsProfile</a>)</div>
					<div class="param_value">
						<input type="text" name="profileID" value="" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="GetRecurringPaymentsProfileDetailsBtn"
						value="GetRecurringPaymentsProfileDetails" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls">
			See also
			<ul>
				<li><a href="CreateRecurringPaymentsProfile">CreateRecurringPaymentsProfile</a>
				</li>
				<li><a href="ManageRecurringPaymentsProfileStatus">ManageRecurringPaymentsProfileStatus</a>
				</li>
				<li><a href="UpdateRecurringPaymentsProfile">UpdateRecurringPaymentsProfile</a>
				</li>
				<li><a href="BillOutstandingAmount">BillOutstandingAmount</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>