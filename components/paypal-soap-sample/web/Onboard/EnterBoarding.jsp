<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PayPal SDK - EnterBoarding</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<div id="wrapper">
		<div id="header">
			<h3>EnterBoarding</h3>
			<div id="apidetails">EnterBoarding establishes an integration
				program with a merchant, specifies a merchant information</div>
		</div>
		<form method="POST">
			<div id="request_form">
				<div class="params">
					<div class="param_name">Program Code*(which is given by
						PayPal Integration Specialist)</div>
					<div class="param_value">
						<input type="text" name="programCode" value="" maxlength="260" />
					</div>
				</div>

				<div class="params">
					<div class="param_name">Product List*(indicates the PayPal
						products you are implementing for this merchant)</div>
					<div class="param_value">
						<input type="text" name="prodList"
							value="ec,dp,auth_settle,admin_api" maxlength="260" /><br />ec-Express
						Checkout, dp-Direct Payment, auth_settle-Authorization and
						Settlement, admin_api-Admin APIs
					</div>
				</div>
				<div class="section_header">Bank Account Details</div>
				<table class="params">
					<tr>
						<th class="param_name">Account Number</th>
						<th class="param_name">Account Name</th>
						<th class="param_name">Account Type</th>
					</tr>
					<tr>
						<td class="param_value"><input type="text" name="accNum"
							value="" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="accName"
							value="" maxlength="260" />
						</td>
						<td class="param_value"><select name="accType">
								<option value="Checking">Checking</option>
								<option value="Savings">Savings</option>
						</select>
						</td>
					</tr>
				</table>

				<div class="section_header">Business Information</div>
				<table class="params">
					<tr>
						<th class="param_name">Business Name</th>
						<th class="param_name">Business Type</th>
						<th class="param_name">Business Category</th>
						<th class="param_name">Average Monthly Volume</th>
						<th class="param_name">Average Transaction Price</th>
						<th class="param_name">Revenue Percentage for online service</th>
					</tr>
					<tr>
						<td class="param_value"><input type="text"
							name="businessName" value="Grocery" maxlength="260" />
						</td>
						<td class="param_value"><select name="businessType">
								<option value="Unknown">Unknown</option>
								<option value="Individual">Individual</option>
								<option value="Proprietorship">Proprietorship</option>
								<option value="Partnership">Partnership</option>
								<option value="Corporation">Corporation</option>
								<option value="Nonprofit">Nonprofit</option>
								<option value="Government">Government</option>
						</select></td>
						<td class="param_value"><select name="businessCategory">
								<option value="Category-Unspecified">Category-Unspecified</option>
								<option value="Antiques">Antiques</option>
								<option value="Arts">Arts</option>
								<option value="Automotive">Automotive</option>
								<option value="Beauty">Beauty</option>
								<option value="Books">Books</option>
								<option value="Business">Business</option>
								<option value="Cameras-and-Photography">Cameras-and-Photography</option>
								<option value="Clothing">Clothing</option>
								<option value="Collectibles">Collectibles</option>
								<option value="Computer-Hardware-and-Software">Computer-Hardware-and-Software</option>
								<option value="Culture-and-Religion">Culture-and-Religion</option>
								<option value="Electronics-and-Telecom">Electronics-and-Telecom</option>
								<option value="Entertainment-Memorabilia">Entertainment-Memorabilia</option>
								<option value="Entertainment">Entertainment</option>
								<option value="Food-Drink-and-Nutrition">Food-Drink-and-Nutrition</option>
								<option value="Gifts-and-Flowers">Gifts-and-Flowers</option>
								<option value="Hobbies-Toys-and-Games">Hobbies-Toys-and-Games</option>
								<option value="Home-and-Garden">Home-and-Garden</option>
								<option value="Internet-and-Network-Services">Internet-and-Network-Services</option>
								<option value="Media-and-Entertainment">Media-and-Entertainment</option>
								<option value="Medical-and-Pharmaceutical">Medical-and-Pharmaceutical</option>
								<option value="Money-Service-Businesses">Money-Service-Businesses</option>
								<option value="Non-Profit-Political-and-Religion">Non-Profit-Political-and-Religion</option>
								<option value="Not-Elsewhere-Classified">Not-Elsewhere-Classified</option>
								<option value="Pets-and-Animals">Pets-and-Animals</option>
								<option value="Real-Estate">Real-Estate</option>
								<option value="Services">Services</option>
								<option value="Sports-and-Recreation">Sports-and-Recreation</option>
								<option value="Travel">Travel</option>
								<option value="Other-Categories">Other-Categories</option>

						</select></td>
						<td class="param_value"><select name="averageMonthlyVolume">
								<option value="AverageMonthlyVolume-Not-Applicable">AverageMonthlyVolume-Not-Applicable</option>
								<option value="AverageMonthlyVolume-Range1">AverageMonthlyVolume-Range1</option>
								<option value="AverageMonthlyVolume-Range2">AverageMonthlyVolume-Range2</option>
								<option value="AverageMonthlyVolume-Range3">AverageMonthlyVolume-Range3</option>
								<option value="AverageMonthlyVolume-Range4">AverageMonthlyVolume-Range4</option>
								<option value="AverageMonthlyVolume-Range5">AverageMonthlyVolume-Range5</option>
								<option value="AverageMonthlyVolume-Range6">AverageMonthlyVolume-Range6</option>
						</select></td>
						<td class="param_value"><select name="averageTransPrice">
								<option value="AverageTransactionPrice-Not-Applicable">AverageTransactionPrice-Not-Applicable</option>
								<option value="AverageTransactionPrice-Range1">AverageTransactionPrice-Range1</option>
								<option value="AverageTransactionPrice-Range2">AverageTransactionPrice-Range2</option>
								<option value="AverageTransactionPrice-Range3">AverageTransactionPrice-Range3</option>
								<option value="AverageTransactionPrice-Range4">AverageTransactionPrice-Range4</option>
								<option value="AverageTransactionPrice-Range5">AverageTransactionPrice-Range5</option>
								<option value="AverageTransactionPrice-Range6">AverageTransactionPrice-Range6</option>
								<option value="AverageTransactionPrice-Range7">AverageTransactionPrice-Range7</option>
								<option value="AverageTransactionPrice-Range8">AverageTransactionPrice-Range8</option>
								<option value="AverageTransactionPrice-Range9">AverageTransactionPrice-Range9</option>
								<option value="AverageTransactionPrice-Range10">AverageTransactionPrice-Range10</option>
						</select></td>
						<td class="param_value"><select name="revenuePercentage">
								<option value="PercentageRevenueFromOnlineSales-Not-Applicable">PercentageRevenueFromOnlineSales-Not-Applicable</option>
								<option value="PercentageRevenueFromOnlineSales-Range1">PercentageRevenueFromOnlineSales-Range1</option>
								<option value="PercentageRevenueFromOnlineSales-Range2">PercentageRevenueFromOnlineSales-Range2</option>
								<option value="PercentageRevenueFromOnlineSales-Range3">PercentageRevenueFromOnlineSales-Range3</option>
								<option value="PercentageRevenueFromOnlineSales-Range4">PercentageRevenueFromOnlineSales-Range4</option>
						</select></td>
					</tr>
				</table>
				<div class="section_header">Address</div>
				<table class="params">
					<tr>
						<th class="param_name">Name</th>
						<th class="param_name">Address Line1</th>
						<th class="param_name">City</th>
						<th class="param_name">State</th>
						<th class="param_name">PostalCode</th>
						<th class="param_name">CountryCode</th>
					</tr>
					<tr>
						<td class="param_value"><input type="text" name="name"
							value="John" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="street"
							value="1,Main St" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="city"
							value="Austin" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="state"
							value="TX" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="postalCode"
							value="78750" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="countryCode"
							value="US" maxlength="260" />
						</td>
					</tr>
				</table>
				<div class="params">
					<div class="param_name">Marketing Category</div>
					<div class="param_value">
						<select name="marketingCategory">
							<option value="Marketing-Category-Default">Marketing-Category-Default</option>
							<option value="Marketing-Category1">Marketing-Category1</option>
							<option value="Marketing-Category2">Marketing-Category2</option>
							<option value="Marketing-Category3">Marketing-Category3</option>
							<option value="Marketing-Category4">Marketing-Category4</option>
							<option value="Marketing-Category5">Marketing-Category5</option>
							<option value="Marketing-Category6">Marketing-Category6</option>
							<option value="Marketing-Category7">Marketing-Category7</option>
							<option value="Marketing-Category8">Marketing-Category8</option>
							<option value="Marketing-Category9">Marketing-Category9</option>
							<option value="Marketing-Category10">Marketing-Category10</option>
							<option value="Marketing-Category11">Marketing-Category11</option>
							<option value="Marketing-Category12">Marketing-Category12</option>
							<option value="Marketing-Category13">Marketing-Category13</option>
							<option value="Marketing-Category14">Marketing-Category14</option>
							<option value="Marketing-Category15">Marketing-Category15</option>
							<option value="Marketing-Category16">Marketing-Category16</option>
							<option value="Marketing-Category17">Marketing-Category17</option>
							<option value="Marketing-Category18">Marketing-Category18</option>
							<option value="Marketing-Category19">Marketing-Category19</option>
							<option value="Marketing-Category20">Marketing-Category20</option>
						</select>
					</div>
				</div>
				<div class="section_header">Owner Info</div>
				<table class="params">
					<tr>
						<th class="param_name">First Name</th>
						<th class="param_name">Last Name</th>
						<th class="param_name">Phone</th>
						<th class="param_name">Mail</th>
						<th class="param_name">Social Security Number</th>
					</tr>
					<tr>
						<td class="param_value"><input type="text" name="firstName"
							value="John" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="lastName"
							value="Doe" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="ownerPhone"
							value="247365935" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="ownerMail"
							value="xyz@paypal.com" maxlength="260" />
						</td>
						<td class="param_value"><input type="text" name="SSN"
							value="" maxlength="260" />
						</td>
					</tr>
				</table>
				<div class="submit">
					<input type="submit" name="EnterBoardingBtn" value="EnterBoarding" />
					<br />
				</div>
				<a href="../index.html">Home</a>
			</div>
		</form>
		<div id="relatedcalls">
			See also
			<ul>
				<li><a href="GetBoardingDetails">GetBoardingDetails</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>