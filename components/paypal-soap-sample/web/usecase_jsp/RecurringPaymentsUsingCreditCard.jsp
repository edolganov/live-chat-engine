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
			<h3>CreateRecurringPaymentsProfile Using Credit Card</h3>
			<div id="apidetails">
				<p>The CreateRecurringPaymentsProfile API operation creates a
					recurring payments profile. You can directly use Credit Card for
					creating a profile</p>
			</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">
						<b><u>Credit Card *</u>
						</b>
					</div>
				</div>
				<br>
				<table class="params">
					<tr>
						<th class="param_name">Credit Card number</th>
						<th class="param_name">Expiry date</th>
						<th class="param_name">Buyer Email Id</th>
						<th class="param_name">Credit Card type</th>
						<th class="param_name">CVV</th>
					</tr>
					<tr>
						<td><div class="param_value">
								<input type="text" name="creditCardNumber" id="creditCardNumber"
									value="4917760970795152" />
							</div>
						</td>
						<td><div class="param_value">
								<select name="expMonth">
									<option value="1">Jan</option>
									<option value="2">Feb</option>
									<option value="3">Mar</option>
									<option value="4">Apr</option>
									<option value="5">May</option>
									<option value="6">Jun</option>
									<option value="7">Jul</option>
									<option value="8">Aug</option>
									<option value="9">Sep</option>
									<option value="10">Oct</option>
									<option value="11">Nov</option>
									<option value="12">Dec</option>
								</select> <select name="expYear">
									<option value="2011">2011</option>
									<option value="2012">2012</option>
									<option value="2013">2013</option>
									<option value="2014" selected="selected">2014</option>
									<option value="2015">2015</option>
									<option value="2016">2016</option>
								</select>
							</div>
						</td>
						<td><div class="param_value">
								<input type="text" name="BuyerEmailId" id="BuyerEmailId"
									value="" />
							</div>
						</td>
						<td><div class="param_value">
								<select name="creditCardType">
									<option value="Visa">Visa</option>
									<option value="MasterCard">MasterCard</option>
									<option value="Discover">Discover</option>
									<option value="Amex">Amex</option>
								</select>
							</div>
						</td>
						<td><div class="param_value">
								<input type="text" name="cvv" id="cvv" value="962"/>
							</div>
						</td>
					</tr>
				</table>

				<div class="section_header">Recurring payments profile details</div>
				
				<div class="param_name">Billing start date</div>
				<div class="param_value">
					<input type="text" name="billingStartDate" id="billingStartDate"
						value=<%=df.format(startDate.getTime())%> />
				</div>
				
				<div class="section_header">
					<b><u>Schedule Details:</u> </b>
				</div>
				<div class="params">
					<div class="param_name">Description* </div>
					<div class="param_value">
						<textarea rows="5" cols="60" name="profileDescription">Recurring Payments Using Credit Card</textarea>
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
				<br>
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