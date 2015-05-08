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
			<h3>SetExpressCheckout</h3>
			<div id="apidetails">Set the details for express checkout, can
				set up billing agreements for reference transactions and recurring
				payments. It only initiates an Express Checkout transaction.</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">BuyerMail</div>
					<div class="param_value">
						<input type="text" name="buyerMail"
							value="platfo_1255077030_biz@gmail.com" size="50" maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Requires shipping</div>
					<div class="param_value">
						<select name="noShipping">
							<option value="0">Display shipping address in PayPal
								pages</option>
							<option value="1">Do not display shipping address in
								PayPal pages</option>
							<option value="2">If shipping address not passed, use
								value in buyer's profile</option>
						</select>
					</div>
				</div>
				<div class="section_header"><b>Payment Details</b></div>
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
						<textarea cols="40" rows="5" name="orderDescription"></textarea>
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
					<div class="param_name">PaymentType</div>
					<div class="param_value">
						<select name="paymentType">
							<option value="Sale">Sale</option>
							<option value="Authorization">Authorization</option>
							<option value="Order">Order</option>
						</select>
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
							</div>
						</td>

						<td><div class="param_value">
								<input type="text" name="itemAmount" id="itemAmount"
									value="5.27" />
							</div>
						</td>

						<td><div class="param_value">
								<input type="text" name="itemQuantity" id="itemQuantity"
									value="2" />
							</div>
						</td>

						<td><div class="param_value">
								<input type="text" name="salesTax" id="salesTax" value="" />
							</div>
						</td>

						<td><div class="param_value">
								<select name="itemCategory">
									<option Value="Physical">Physical</option>
									<option Value="Digital">Digital</option>
								</select>
							</div>
						</td>

						<td><div class="param_value">
								<input type="text" name="itemDescription" id="itemDescription"
									value="" />
							</div>
						</td>
					</tr>
				</table>
				<div class="params">
					<div class="param_name">IPN Notification Url (Receive IPN call back from PayPal)</div>
					<div class="param_value">
						<input type=text size="50"  name="notifyURL" value="">
					</div>
				</div>
				<br>
				<div class="section_header"><b>Ship To Address:</b></div>
				<div class="param_name">Require buyer's PayPal Shipping address to be a confirmed address</div> 
		        <div class="param_value"> 
	                <select name="reqConfirmShipping"> 
	                    <option value="0" >No</option> 
	                    <option value="1">Yes</option> 
	                </select> 
		        </div>
		        <div class="param_name">Allow Address override</div> 
	            <div class="param_value"> 
	                <select name="addressoverride"> 
	                    <option value="">-Select a value-</option>
	                     <option value="0">No - PayPal should not display shipping address</option>
	                     <option value="1">Yes - PayPal should display shipping address</option>
	                </select> 
	            </div>
						
				<div class="params">
					<div class="param_name">Name</div>
					<div class="param_value">
						<input type="text" name="name" value="John" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">street1</div>
					<div class="param_value">
						<input type="text" name="street1" value="1,Main St" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">street2</div>
					<div class="param_value">
						<input type="text" name="street2" value="" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">City</div>
					<div class="param_value">
						<input type="text" name="city" value="Austin" size="50"
							maxlength="260" />
					</div>

				</div>
				<div class="params">
					<div class="param_name">State</div>
					<div class="param_value">
						<input type="text" name="state" value="TX" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">PostalCode</div>
					<div class="param_value">
						<input type="text" name="postalCode" value="78750" size="50"
							maxlength="260" />
					</div>
				</div>

				<div class="params">
					<div class="param_name">CountryCode</div>
					<div class="param_value">
						<input type="text" name="countryCode" value="US" size="50"
							maxlength="260" />
					</div>
				</div>
				<div class="section_header"><b>Billing Agreement (Required for
					Recurring payments/Reference transactions only)</b></div>
				<div class="param_name">Billing Agreement Description *</div>
				<div class="param_value">
					<textarea cols="80" rows="10" name="billingAgreementText"></textarea>
				</div>
				<div class="param_name">Billing type *</div>
				<div class="param_value">
					<select name="billingType">
						<option value="None">None</option>
						<option value="MerchantInitiatedBilling">Merchant
							Initiated Billing</option>
						<option value="RecurringPayments">Recurring Payments</option>
						<option value="MerchantInitiatedBillingSingleAgreement">Merchant
							Initiated Billing Single Agreement</option>
						<option value="ChannelInitiatedBilling">Channel Initiated
							Billing</option>
					</select>
				</div>

				<!-- PayPal page styling attributes -->
				<div class="section_header"><b>PayPal page styling attributes
					(optional)</b></div>
				<div class="param_name">Business name to display in the PayPal
					account on the PayPal hosted checkout pages</div>
				<div class="param_value">
					<input type="text" name="brandName" id="brandName" value="" />
				</div>
				<div class="param_name">Custom page style for payment pages
					(as configured in Merchant's account profile)</div>
				<div class="param_value">
					<input type="text" name="pageStyle" id="pageStyle" value="" />
				</div>
				<div class="param_name">URL for header image</div>
				<div class="param_value">
					<input type="text" name="cppheaderimage" id="cppheaderimage"
						value="" />
				</div>
				<div class="param_name">Border color around header</div>
				<div class="param_value">
					<input type="text" name="cppheaderbordercolor"
						id="cppheaderbordercolor" value="" />
				</div>
				<div class="param_name">Background color for header</div>
				<div class="param_value">
					<input type="text" name="cppheaderbackcolor"
						id="cppheaderbackcolor" value="" />
				</div>
				<div class="param_name">Background color for payment page</div>
				<div class="param_value">
					<input type="text" name="cpppayflowcolor" id="cpppayflowcolor"
						value="" />
				</div>



				<!-- Advanced features -->
				<div class="section_header"><b>Advanced features (optional)</b></div>
				<div class="param_name">Allow buyer to enter note to merchant
					on PayPal pages</div>
				<div class="param_value">
					<select name="allowNote">
						<option value="0">False</option>
						<option value="1">True</option>
					</select>
				</div>
				<div class="submit">
					<input type="submit" name="SetExpressCheckoutBtn"
						value="SetExpressCheckout" /> <br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>

		<div id="relatedcalls">
			See also
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