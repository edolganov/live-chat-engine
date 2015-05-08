<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.text.DateFormat"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Calendar"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - CreateRecurringPaymentsProfile</title>
<%
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	Calendar startDate = Calendar.getInstance();
%>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>CreateRecurringPaymentsProfile Using PayPal ExpressCheckout Token</h3>
			<div id="apidetails">
				<p>The CreateRecurringPaymentsProfile API operation creates a
					recurring payments profile.There is a one-to-one correspondence
					between billing agreements and recurring payments profiles. To
					associate a recurring payments profile with its billing agreement,
					you must ensure that the description in the recurring payments
					profile matches the description of a billing agreement. For version
					54.0 and later, use SetExpressCheckout to initiate creation of a
					billing agreement.</p>
			</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="note">You must set ExpressCheckout Token</div><br>
				<div class="params">
					<div class="param_name">Express Checkout Token *(Token
						generated via ExpressCheckout)</div>
					<div class="param_value">
						<input type="text" name="token" id="token"
							value="<%=(session.getAttribute("ecToken") != null) ? ((String) session.getAttribute("ecToken")) : ""%>" />
						<%	session.removeAttribute("ecToken");	%>
					</div>
				</div>

				<div class="section_header">Recurring payments profile details</div>
				<div class="param_name">Billing start date</div>
				<div class="param_value">
					<input type="text" name="billingStartDate" id="billingStartDate"
						value=<%=df.format(startDate.getTime())%> />
				</div>
				
				<div class="section_header">
					<b><u>Schedule Details:</u>
					</b>
				</div>
				<div class="params">
					<div class="param_name">Description* (must match billing
						agreement if using Express Checkout token)</div>
					<div class="param_value">
						<textarea rows="5" cols="60" name="profileDescription">Recurring Payments Using Express Checkout</textarea>
					</div>
				</div>

				<div class="section_header">
					<b>Payment Period *</b>
				</div>
				<table class="params_name">
					<tr>
						<th>Billing frequency</th>
						<th>Billing period</th>
						<th>Total billing cycles</th>
						<th>Per billing cycle amount</th>
						<th>Shipping amount</th>
						<th>Tax</th>
					</tr>
					<tr>
						<td><span class="param_value"> <input type="text"
								id="billingFrequency" name="billingFrequency" value="10" /> </span>
						</td>
						<td><span class="param_value"> <select
								name="billingPeriod">
									<option value="Day">Day</option>
									<option value="Week">Week</option>
									<option value="SemiMonth">SemiMonth</option>
									<option value="Month">Month</option>
									<option value="Year">Year</option>
							</select> </span>
						</td>
						<td><span class="param_value"> <input type="text"
								id="totalBillingCycles" name="totalBillingCycles" value="8" />
						</span>
						</td>
						<td><span class="param_value"> <input type="text"
								id="billingAmount" name="billingAmount" value="5.0" /> </span>
						</td>
						<td><span class="param_value"> <input type="text"
								id="shippingAmount" name="shippingAmount" value="1.0" /> </span>
						</td>
						<td><span class="param_value"> <input type="text"
								id="taxAmount" name="taxAmount" value="0.0" /> </span>
						</td>
					</tr>
				</table>
				
				<div class="submit">
					<input type="submit" name="CreateRecurringPaymentsProfileBtn"
						value="CreateRecurringPaymentsProfile" /><br />
				</div>
				<a href="index.html">Home</a> <br /> <br />
			</div>
		</form>
	</div>
</body>
</html>