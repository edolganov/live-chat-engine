<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>PayPal SDK - DoReferenceTransaction</title>
<script type="text/javascript">
	function display() {
		document.getElementById("id2").style.display = "";
	}
</script>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>DoReferenceTransaction</h3>
			<div id="header">
				<p>The DoReferenceTransaction API operation processes a payment
					from a buyers account, which is identified by a previous
					transaction.</p>
			</div>
		</div>
		<form method="post">
			<div id="request_form">
				<div class="params">
					<div class="param_name">Payment Action*</div>
					<div class="param_value">
						<select name=paymentAction>
							<option value=Sale selected>Sale</option>
							<option value=Authorization>Authorization</option>
						</select>
					</div>
				</div>
				<div class="params">
					<div class="param_name">Payment Type</div>
					<div class="param_value">
						<select name=paymentType>
							<option value=Any>Any</option>
							<option value=InstantOnly>InstantOnly</option>
							<option value=EcheckOnly>EcheckOnly</option>
						</select>
					</div>
				</div>

				<div class="section_header">
					<b>Payment Details*</b>
				</div>

				<div class="params">
					<div class="param_name">Amount</div>
					<div class="param_value">
						<input type="text" name="amount" id="amount" value="1.00" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">CurrencyID</div>
					<div class="param_value">
						<input type="text" name="currencyID" id="currencyID" value="USD" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">Address</div>
				</div>

				<div class="params">
					<div class="param_name">First Name</div>
					<div class="param_value">
						<input type="text" name="firstName" id="firstName" value="John" />
					</div>
				</div>

				<div class="params">
					<div class="param_name">Last Name</div>
					<div class="param_value">
						<input type="text" name="lastName" id="lastName" value="Doe" />
					</div>
				</div>

				<div class="params">
					<div class="param_name">Address Line 1</div>
					<div class="param_value">
						<input type="text" name="address1" id="address1" value="1 Main St" />
					</div>
				</div>

				<div class="params">
					<div class="param_name">Address Line 2</div>
					<div class="param_value">
						<input type="text" name="address2" id="address2" />
					</div>
				</div>

				<div class="params">
					<div class="param_name">City</div>
					<div class="param_value">
						<input type="text" name="city" id="city" value="San Jose" />
					</div>
				</div>

				<div class="params">
					<div class="param_name">Country</div>
					<div class="param_value">United States</div>
				</div>

				<div class="params">
					<div class="param_name">State</div>
					<div class="param_value">
						<select id="state" name="state">
							<option value=></option>
							<option value=AK>AK</option>
							<option value=AL>AL</option>
							<option value=AR>AR</option>
							<option value=AZ>AZ</option>
							<option value=CA selected>CA</option>
							<option value=CO>CO</option>
							<option value=CT>CT</option>
							<option value=DC>DC</option>
							<option value=DE>DE</option>
							<option value=FL>FL</option>
							<option value=GA>GA</option>
							<option value=HI>HI</option>
							<option value=IA>IA</option>
							<option value=ID>ID</option>
							<option value=IL>IL</option>
							<option value=IN>IN</option>
							<option value=KS>KS</option>
							<option value=KY>KY</option>
							<option value=LA>LA</option>
							<option value=MA>MA</option>
							<option value=MD>MD</option>
							<option value=ME>ME</option>
							<option value=MI>MI</option>
							<option value=MN>MN</option>
							<option value=MO>MO</option>
							<option value=MS>MS</option>
							<option value=MT>MT</option>
							<option value=NC>NC</option>
							<option value=ND>ND</option>
							<option value=NE>NE</option>
							<option value=NH>NH</option>
							<option value=NJ>NJ</option>
							<option value=NM>NM</option>
							<option value=NV>NV</option>
							<option value=NY>NY</option>
							<option value=OH>OH</option>
							<option value=OK>OK</option>
							<option value=OR>OR</option>
							<option value=PA>PA</option>
							<option value=RI>RI</option>
							<option value=SC>SC</option>
							<option value=SD>SD</option>
							<option value=TN>TN</option>
							<option value=TX>TX</option>
							<option value=UT>UT</option>
							<option value=VA>VA</option>
							<option value=VT>VT</option>
							<option value=WA>WA</option>
							<option value=WI>WI</option>
							<option value=WV>WV</option>
							<option value=WY>WY</option>
							<option value=AA>AA</option>
							<option value=AE>AE</option>
							<option value=AP>AP</option>
							<option value=AS>AS</option>
							<option value=FM>FM</option>
							<option value=GU>GU</option>
							<option value=MH>MH</option>
							<option value=MP>MP</option>
							<option value=PR>PR</option>
							<option value=PW>PW</option>
							<option value=VI>VI</option>
						</select>
					</div>
				</div>

				<div class="params">
					<div class="param_name">Postal Code</div>
					<div class="param_value">
						<input type="text" name="zip" id="zip" value="95131" />
					</div>
				</div>
				<div class="params">
					<div class="param_name">
						<b>Reference TransactionID(Billing agreement ID or a reference
							transaction ID that is associated with a billing agreement, get
							it via <a href="../EC/SetExpressCheckout">ExpressCheckout</a>)</b>
					</div>
					<div class="param_value">
						<input type="text" name="referenceID" id="referenceID" />
					</div>
				</div>

				<div class="params">
						<div class="param_name">IPN Notification Url (Receive IPN call back from PayPal)</div>
						<div class="param_value">
							<input type=text size="50"  name="notifyURL" value="">
				</div>
					
				<table class="params">
					<tr>
						<th class="param_name">Override CreditCard Details</th>
						<td class="param_value"><input type="checkbox"
							name="ReferenceCreditCardDetails" value="ON" onclick="display()" />
						</td>
					</tr>
				</table>


				<div id="id2" class="param_value" style="display: none">
					<div class="section_header">
						<b>CreditCard Details</b>
					</div>
					<div class="params">
						<div class="param_name">First Name</div>
						<div class="param_value">
							<input type="text" name="firstName" id="firstName" value="John" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">Last Name</div>
						<div class="param_value">
							<input type="text" name="lastName" id="lastName" value="Doe" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">CreditCard Number</div>
						<div class="param_value">
							<input type="text" name="creditCardNumber" id="creditCardNumber"
								value="4904969011809253" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">CreditCardType</div>
						<div class="param_value">
							<select name=creditCardType>
								<option value=Visa selected>Visa</option>
								<option value=MasterCard>MasterCard</option>
								<option value=Discover>Discover</option>
								<option value=Amex>American Express</option>
							</select>
						</div>
					</div>

					<div class="params">
						<div class="param_name">CVV2</div>
						<div class="param_value">
							<input type="text" name="CVV2" id="CVV2" value="962" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">Expiry Month</div>
						<div class="param_value">
							<input type="text" name="expMonth" id="expMonth" value="11" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">Expiry Year</div>
						<div class="param_value">
							<input type="text" name="expYear" id="expYear" value="2012" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">Billing Address</div>
					</div>

					<div class="params">
						<div class="param_name">First Name</div>
						<div class="param_value">
							<input type="text" name="firstName" id="firstName" value="John" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">Last Name</div>
						<div class="param_value">
							<input type="text" name="lastName" id="lastName" value="Doe" />
						</div>
					</div>
					<div class="params">
						<div class="param_name">Address Line 1</div>
						<div class="param_value">
							<input type="text" name="address1" id="address1"
								value="1 Main St" />
						</div>
					</div>

					<div class="params">
						<div class="param_name">Address Line 2</div>
						<div class="param_value">
							<input type="text" name="address2" id="address2" />
						</div>
					</div>
					<div class="params">
						<div class="param_name">City</div>
						<div class="param_value">
							<input type="text" name="city" id="city" value="San Jose" />
						</div>
					</div>
					<div class="params">
						<div class="param_name">Country</div>
						<div class="param_value">United States</div>
					</div>
					<div class="params">
						<div class="param_name">State</div>
						<div class="param_value">
							<select id=state name=state>
								<option value=></option>
								<option value=AK>AK</option>
								<option value=AL>AL</option>
								<option value=AR>AR</option>
								<option value=AZ>AZ</option>
								<option value=CA selected>CA</option>
								<option value=CO>CO</option>
								<option value=CT>CT</option>
								<option value=DC>DC</option>
								<option value=DE>DE</option>
								<option value=FL>FL</option>
								<option value=GA>GA</option>
								<option value=HI>HI</option>
								<option value=IA>IA</option>
								<option value=ID>ID</option>
								<option value=IL>IL</option>
								<option value=IN>IN</option>
								<option value=KS>KS</option>
								<option value=KY>KY</option>
								<option value=LA>LA</option>
								<option value=MA>MA</option>
								<option value=MD>MD</option>
								<option value=ME>ME</option>
								<option value=MI>MI</option>
								<option value=MN>MN</option>
								<option value=MO>MO</option>
								<option value=MS>MS</option>
								<option value=MT>MT</option>
								<option value=NC>NC</option>
								<option value=ND>ND</option>
								<option value=NE>NE</option>
								<option value=NH>NH</option>
								<option value=NJ>NJ</option>
								<option value=NM>NM</option>
								<option value=NV>NV</option>
								<option value=NY>NY</option>
								<option value=OH>OH</option>
								<option value=OK>OK</option>
								<option value=OR>OR</option>
								<option value=PA>PA</option>
								<option value=RI>RI</option>
								<option value=SC>SC</option>
								<option value=SD>SD</option>
								<option value=TN>TN</option>
								<option value=TX>TX</option>
								<option value=UT>UT</option>
								<option value=VA>VA</option>
								<option value=VT>VT</option>
								<option value=WA>WA</option>
								<option value=WI>WI</option>
								<option value=WV>WV</option>
								<option value=WY>WY</option>
								<option value=AA>AA</option>
								<option value=AE>AE</option>
								<option value=AP>AP</option>
								<option value=AS>AS</option>
								<option value=FM>FM</option>
								<option value=GU>GU</option>
								<option value=MH>MH</option>
								<option value=MP>MP</option>
								<option value=PR>PR</option>
								<option value=PW>PW</option>
								<option value=VI>VI</option>
							</select>
						</div>
					</div>
					<div class="params">
						<div class="param_name">Postal Code</div>
						<div class="param_value">
							<input type="text" name="zip" id="zip" value="95131" />
						</div>
					</div>
					
				</div>
				</div>
				<div class="submit">
					<input type="submit" name="DoReferenceTransactionBtn"
						value="DoReferenceTransaction" /><br />
				</div>
				<a href="../index.html">Home</a>
			</div>

		</form>
		<div id="relatedcalls">
			See also:
			<ul>
				<li><a href="../RP/CreateRecurringPaymentsProfile">RecurringPayments</a>
				</li>
				<li><a href="BillAgreementUpdate">BillAgreementUpdate</a></li>
				<li><a href="GetBillingAgreementCustomerDetails">GetBillingAgreementCustomerDetails</a>
				</li>
				
			</ul>
		</div>
	</div>
</body>
</html>
