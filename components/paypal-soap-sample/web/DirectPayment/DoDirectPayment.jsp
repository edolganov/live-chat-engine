<html>
<head>
<title>PayPal SDK - DoDirectPayment</title>
<script language="JavaScript">
	function generateCC(){
		var cc_number = new Array(16);
		var cc_len = 16;
		var start = 0;
		var rand_number = Math.random();
		
		switch(document.frmDCC.creditCardType.value)
        {
			case "Visa":
				cc_number[start++] = 4;
				break;
			case "Discover":
				cc_number[start++] = 6;
				cc_number[start++] = 0;
				cc_number[start++] = 1;
				cc_number[start++] = 1;
				break;
			case "MasterCard":
				cc_number[start++] = 5;
				cc_number[start++] = Math.floor(Math.random() * 5) + 1;
				break;
			case "Amex":
				cc_number[start++] = 3;
				cc_number[start++] = Math.round(Math.random()) ? 7 : 4 ;
				cc_len = 15;
				break;
        }
        
        for (var i = start; i < (cc_len - 1); i++) {
			cc_number[i] = Math.floor(Math.random() * 10);
        }
		
		var sum = 0;
		for (var j = 0; j < (cc_len - 1); j++) {
			var digit = cc_number[j];
			if ((j & 1) == (cc_len & 1)) digit *= 2;
			if (digit > 9) digit -= 9;
			sum += digit;
		}
		
		var check_digit = new Array(0, 9, 8, 7, 6, 5, 4, 3, 2, 1);
		cc_number[cc_len - 1] = check_digit[sum % 10];
		
		document.frmDCC.creditCardNumber.value = "";
		for (var k = 0; k < cc_len; k++) {
			document.frmDCC.creditCardNumber.value += cc_number[k];
		}
	}
	
	
</script>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>DoDirectPayment</h3>
			<div id="apidetails">
				<p>DoDirectPayment API Operation enables you to process a credit
					card payment.</p>
			</div>
		</div>
		<form method="POST" name="frmDCC">
			<div id="request_form">
				<div class="params">
					<div class="param_name">First Name*</div>
					<div class="param_value">
						<input type=text size=30 maxlength=32 name=firstName value=John>
					</div>
				</div>
				<div class="params">
					<div class="param_name">Last Name*</div>
					<div class="param_value">
						<input type=text size=30 maxlength=32 name=lastName value=Doe>
					</div>
				</div>
				<div class="section_header">Credit Card Details*</div>
				<div class="params">
					<div class="param_name">Card Type</div>
					<div class="param_value">
						<select name="creditCardType">
							<option value=Visa selected>Visa</option>
							<option value=MasterCard>MasterCard</option>
							<option value=Discover>Discover</option>
							<option value=Amex>American Express</option>
						</select>
					</div>
				</div>
				<div class="params">
					<div class="param_name">Card Number</div>
					<div class="param_value">
						<input type=text size=19 maxlength=19 name="creditCardNumber">
					</div>
				</div>
				<div class="params">
					<div class="param_name">Expiration Date</div>
					<div class="param_value">
						<select name=expDateMonth>
							<option value=1>01</option>
							<option value=2>02</option>
							<option value=3>03</option>
							<option value=4>04</option>
							<option value=5>05</option>
							<option value=6>06</option>
							<option value=7>07</option>
							<option value=8>08</option>
							<option value=9>09</option>
							<option value=10>10</option>
							<option value=11>11</option>
							<option value=12>12</option>
						</select> <select name=expDateYear>
							<option value=2005>2005</option>
							<option value=2006>2006</option>
							<option value=2007>2007</option>
							<option value=2008>2008</option>
							<option value=2009>2009</option>
							<option value=2010>2010</option>
							<option value=2011>2011</option>
							<option value=2012 selected>2012</option>
							<option value=2013>2013</option>
							<option value=2014>2014</option>
							<option value=2015>2015</option>
						</select>
					</div>
				</div>
				<div class="params">
					<div class="param_name">Card Verification Number</div>
					<div class="param_value">
						<input type=text size=3 maxlength=4 name=cvv2Number value=962>
					</div>
				</div>
				<div class="params">
					<div class="param_name">Payment Type*</div>
					<div class="param_value">
						<select name=paymentType>
							<option value=Sale selected>Sale</option>
							<option value=Authorization>Authorization</option>
						</select>
					</div>
				</div>
				<div class="params">
					<div class="param_name">IPN Notification Url (Receive IPN call back from PayPal)</div>
					<div class="param_value">
						<input type=text size="50"  name="notifyURL" value="">
					</div>
				</div>
				<br> <b>Billing Address*</b>
				<div class="params">
					<div class="param_name">Address Line 1</div>
					<div class="param_value">
						<input type=text size=25 maxlength=100 name=address1
							value="1 Main St">
					</div>
				</div>
				<div class="params">
					<div class="param_name">Address Line 2</div>
					<div class="param_value">
						<input type=text size=25 maxlength=100 name=address2>(optional)
					</div>
				</div>
				<div class="params">
					<div class="param_name">City</div>
					<div class="param_value">
						<input type=text size=25 maxlength=40 name=city value="San Jose">
					</div>
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
					<div class="param_name">ZIP Code</div>
					<div class="param_value">
						<input type=text size=10 maxlength=10 name=zip value=95131>(5
						or 9 digits)
					</div>
				</div>
				<div class="params">
					<div class="param_name">CountryCode</div>
					<div class="param_value">
						<input type=text size=4 maxlength=7 name=countryCode value=US>
					</div>
				</div>
				<div class="params">
					<div class="param_name">Amount*</div>
					<div class="param_value">
						<input type=text size=4 maxlength=7 name=amount value=1.00>
					</div>
				</div>
				<div class="params">
					<div class="param_name">CurrencyCode*</div>
					<div class="param_value">
						<input type=text size=4 maxlength=7 name=currencyCode value=USD>
					</div>
				</div>
				<div class="submit">
					<input type="submit" name="DirectPaymentBtn" value="DirectPayment" /><br />
				</div>
				<a href="../index.html">Home</a> <br /> <br />
			</div>
		</form>
		<div id="relatedcalls">
			See also
			<ul>
				<li><a href="../RT/DoReferenceTransaction">DoReferenceTransaction</a>
				</li>
				<li><a href="../RP/CreateRecurringPaymentsProfile">CreateRecurringPaymentsProfile</a>
				</li>
			</ul>
		</div>
	</div>
	<script language="javascript">
		generateCC();
	</script>
</body>
</html>