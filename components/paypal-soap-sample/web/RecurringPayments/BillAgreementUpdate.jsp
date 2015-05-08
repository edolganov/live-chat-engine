<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<title>PayPal SDK - BillAgreementUpdate</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>BillAgreementUpdate</h3>
			<div id="header">
				<p>
					<b>BAUpdate API operation updates or deletes a billing
						agreement.</b>
				</p>
			</div>
		</div>
		<form id="form1" method="post">
			<div id="request_form">
				<div class="params">
					<div class="param_name">
						ReferenceID*(Billing agreement ID or a reference transaction ID
						that is associated with a billing agreement, get it via <a
							href="../EC/SetExpressCheckout">ExpressCheckout</a>)
					</div>
					<div class="param_value">
						<input type="text" name="referenceID" value="" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">BillingAgreement Status*</div>
					<div class="param_value">
						<select name="billingAgreementStatus">
							<option value="Active">Active</option>
							<option value="Canceled">Canceled</option>
						</select>
					</div>
				</div>
				<div class="params">
					<div class="param_name">BillingAgreement Description*</div>
					<div class="param_value">
						<textarea name="billingAgreementDescription" rows="10" cols="50"></textarea>
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="BillAgreementUpdateBtn"
						value="BillAgreementUpdate" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls">
			See also:
			<ul>
				<li><a href="RT/GetBillingAgreementCustomerDetails">GetBillingAgreementCustomerDetails</a>
				</li>

				<li><a href="RT/DoReferenceTransaction">DoReferenceTransaction</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>
