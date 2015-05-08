<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>PayPal Invoicing SDK - GetAccessToken</title>
	<link rel="stylesheet" type="text/css" href="sdk.css"/> 
	<script type="text/javascript" src="sdk.js"></script>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<h4>PayPal Permissions</h4>
	<ul class="wizard">
		<li class="complete">Step 1: Request Permissions</li>
		<li class="current">Step 2: Generate Access Token</li>
	</ul>
	<div class="request_form">
		<form method="post" action="GenerateAccessToken">
			<%
				String query = request.getQueryString();
				Map map = new HashMap();
				try {
					StringTokenizer stTok = new StringTokenizer(query, "&");
					while (stTok.hasMoreTokens()) {
						StringTokenizer stInternalTokenizer = new StringTokenizer(
								stTok.nextToken(), "=");
						if (stInternalTokenizer.countTokens() == 2) {
							map.put(stInternalTokenizer.nextToken(),
									stInternalTokenizer.nextToken());
						}
					}
				} catch (Exception e) {
					//left on purpose
				}
				if (map != null) {
					Iterator iterator = map.entrySet().iterator();

					while (iterator.hasNext()) {
						Map.Entry elem = (Map.Entry) iterator.next();
			%><table>
				<tr>
					<td><%=elem.getKey()%>:</td>
					<td align="left"><input type="text" name="txtbox"
						value=<%=elem.getValue()%>></td>
				</tr>
			</table>
			<%
				}
				}
			%>

			<input type="submit" name="GenerateBtn" value="GenerateAccessToken">			
		</form>
	</div>
	<br />
	<a href="index.html">Home</a>
</body>
</html>