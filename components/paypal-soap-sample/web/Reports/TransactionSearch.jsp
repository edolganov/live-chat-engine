<%@ page import="java.text.DateFormat"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Calendar"%>
<%@ page language="java"%>

<%
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00Z");
	Calendar startDate = Calendar.getInstance();
	startDate.add(Calendar.DATE, -1);
	Calendar endDate = Calendar.getInstance();
%>

<html>
<head>
<title>PayPal SDK - TransactionSearch</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>TransactionSearch</h3>
			<div id="apidetails">TransactionSearch API searches transaction
				history for transactions that meet the specified criteria.</div>
		</div>
		<form method="POST">

			<div id="request_form">
				<div class="params">
					<div class="param_name">StartDate*</div>
					<div class="param_value">
						<input type="text" name="startDate"
							value=<%=df.format(startDate.getTime())%> size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">EndDate</div>
					<div class="param_value">
						<input type="text" name="endDate"
							value=<%=df.format(endDate.getTime())%> size="50" maxlength="260" />(Optional)
					</div>
				</div>
				<div class="params">
					<div class="param_name">
						Transaction ID(Get Transaction ID via <a
							href="../DCC/DirectPayment">Direct Payment</a> or <a
							href="../EC/SetExpressCheckout">ExpressCheckout</a>)
					</div>
					<div class="param_value">
						<input type="text" name="transactionID" value="" size="50"
							maxlength="260" />(Optional)
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="TransactionSearchBtn"
						value="TransactionSearch" /><br />
				</div>
				<a href="../index.html">Home</a>

			</div>
		</form>

		<div id="relatedcalls">
			See also
			<ul>
				<li><a href="GetTransactionDetails">GetTransactionDetails</a>
				</li>
				<li><a href="GetPalDetails">GetPalDetails</a></li>
				<li><a href="GetBalance">GetBalance</a>
				</li>
				<li><a href="AddressVerify">AddressVerify</a></li>
			</ul>
		</div>
	</div>
</body>
</html>
