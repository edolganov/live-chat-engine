<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - SetExpressCheckout</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>SetExpressCheckout for Authorizing payments</h3>
			<div id="apidetails">Set the details for express checkout. PaymentAction should be set to <b>Authorization</b> 
			to create a payment authorization. Authorized payment can be captured directly using DoCapture api.</div>
		</div>
		<br>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name"><b>BuyerMail</b></div>
					<div class="param_value">
						<input type="text" name="buyerMail"
							value="platfo_1255077030_biz@gmail.com" size="50" maxlength="260" />
					</div>
				</div>
				<br>
				<div class="section_header">
					<b><u>Payment Details:</u></b>
				</div><br>
				<div class="params">
					<div class="param_name">Total Shipping costs</div>
					<div class="param_value">
						<input type="text" name="shippingTotal" id="shippingTotal"
							value="0.50" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Total insurance cost</div>
					<div class="param_value">
						<input type="text" name="insuranceTotal" id="insuranceTotal"
							value="" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Total handling cost</div>
					<div class="param_value">
						<input type="text" name="handlingTotal" id="handlingTotal"
							value="" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Total Tax</div>
					<div class="param_value">
						<input type="text" name="taxTotal" id="taxTotal" value="" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Order description</div>
					<div class="param_value">
						<textarea cols="40" rows="5" name="orderDescription">Express Checkout</textarea>
					</div>
				</div>
				<div class="params">
					<div class="param_name">CurrencyCode</div>
					<div class="param_value">
						<input type="text" name="currencyCode" value="USD" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">PaymentAction</div>
					<div class="param_value">
						<input type="text" name="paymentAction" value="Authorization"  readonly/>
					</div>
				</div>
				<div class="param_name">Item Details</div>
				<table class="params">
					<tr>
						<th class="param_name">Name</th>
						<th class="param_name">Cost</th>
						<th class="param_name">Quantity</th>
						<th class="param_name">Sales tax</th>
						<th class="param_name">Item Category</th>
						<th class="param_name">Description (optional)</th>
					</tr>

					<tr>
						<td><div class="param_value">
								<input type="text" name="itemName" id="itemName"
									value="Item Name" />
							</div></td>

						<td><div class="param_value">
								<input type="text" name="itemAmount" id="itemAmount"
									value="5.27" />
							</div></td>

						<td><div class="param_value">
								<input type="text" name="itemQuantity" id="itemQuantity"
									value="2" />
							</div></td>

						<td><div class="param_value">
								<input type="text" name="salesTax" id="salesTax" value="" />
							</div></td>

						<td><div class="param_value">
								<select name="itemCategory">
									<option Value="Physical">Physical</option>
									<option Value="Digital">Digital</option>
								</select>
							</div></td>

						<td><div class="param_value">
								<input type="text" name="itemDescription" id="itemDescription"
									value="" />
							</div></td>
					</tr>
				</table>
				<div class="params">
					<div class="param_name">IPN Notification Url (Receive IPN
						call back from PayPal)</div>
					<div class="param_value">
						<input type=text size="50" name="notifyURL" value="">
					</div>
				</div>
				<br>
				<div class="submit">
					<input type="submit" name="SetExpressCheckoutBtn"	value="SetExpressCheckout" /> <br />
				</div>
				<a href="index.html">Home</a>
			</div>
		</form>
	</div>
</body>
</html>