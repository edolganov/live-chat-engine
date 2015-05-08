<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - DoExpressCheckout</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<%
		String token = request.getParameter("token");
		String payerId = request.getParameter("PayerID");
	%>
	<div id="wrapper">
		<div id="header">
			<h3>DoExpressCheckout</h3>
			<div id="apidetails">Used to make checkout payment. Select the appropriate paymentAction below, as what you set in SetExpressCheckout.</div>
		</div><br>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">
						Token*(Get Token via SetExpressCheckout)
					</div>
					<div class="param_value">
						<input type="text" name="token" value="<%= token %>" size="50" maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Payer ID*</div>
					<div class="param_value">
						<input type="text" name="payerID" value="<%= payerId %>" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">PaymentAction* (Select the paymentAction that you had set in SetExpressCheckout)</div>
					<div class="param_value">
						<select name="paymentAction">
							<option value="Sale" selected="selected">Sale</option>
							<option value="Authorization">Authorization</option>
							<option value="Order">Order</option>
						</select>
					</div>
				</div>
				<div class="section_header">Payment Details</div>
				<div class="params"></div>

				<div class="params">
					<div class="param_name">CurrencyCode</div>
					<div class="param_value">
						<input type="text" name="currencyCode" value="USD" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Order Total</div>
					<div class="param_value">
						<input type="text" name="orderTotal" id="orderTotal" value="1.00" />
					</div>
				</div>
				<div class="param_name">
					<table>
						<tr>
							<th>Receiver Emails</th>
							<th>Request Id</th>
						</tr>
						<tr>
						    <td><input type="text" name="receiverEmail_0" value="platfo_1255170694_biz@gmail.com" size="50" maxlength="260" /></td>
							<td><input type="text" name="requestId_0" value="CART286-PAYMENT0" size="50" maxlength="260" /></td> 
						</tr>
						<tr>
						    <td><input type="text" name="receiverEmail_1" value="platfo_1255170694_biz@gmail.com" size="50" maxlength="260" /></td>
							<td><input type="text" name="requestId_1" value="CART286-PAYMENT1" size="50" maxlength="260" /></td> 
						</tr>
					</table>	
				
				
				</div>
				<div class="params">
					<div class="param_name">IPN Notification Url (Receive IPN call back from PayPal)</div>
					<div class="param_value">
						<input type=text size="50"  name="notifyURL" value="">
					</div>
				</div>
				<br>
				<div class="submit">
					<input type="submit" name="DoExpressCheckoutBtn"
						value="DoExpressCheckout" /> <br />
				</div>
				<a href="index.html">Home</a>
			</div>
		</form>
	</div>
</body>
</html>