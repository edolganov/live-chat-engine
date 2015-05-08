<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>PayPal Invoicing SDK - RequestPermissions</title>
	<link rel="stylesheet" type="text/css" href="sdk.css"/> 
	<script type="text/javascript" src="sdk.js"></script>
<%
	StringBuilder url = new StringBuilder();
	url.append("http://");
	url.append(request.getServerName());
	url.append(":");
	url.append(request.getServerPort());
	url.append(request.getContextPath());
	String returnURL = url.toString() + "/GenerateAccessToken.jsp";
%>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<form method="post">
		<h4>PayPal Permissions</h4>
		<ul class="wizard">
			<li class="current">Step 1: Request Permissions</li>
			<li>Step 2: Generate Access Token</li>
		</ul>
		<div class="request_form">
			<div class="params">
				<input type="text" id="callback" name="callback" size="60"
					value="<%=returnURL%>" /><br /> 
<input
					type="checkbox" name="api" value="MASS_PAY" checked="checked"/>MASS_PAY<br />			</div>

			<input type="submit" class="submit" name="PermissionBtn" value="RequestPermissions" />
			<br /> <a href="index.html">Home</a>
		</div>
	</form>
</body>
</html>