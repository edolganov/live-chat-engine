<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.io.*"%>
<%@ page import="urn.ebay.apis.eBLBaseComponents.ErrorType"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ErrorPage</title>
</head>
<body>
	<img src="https://devtools-paypal.com/image/bdg_payments_by_pp_2line.png" alt="PAYMENTS BY PayPal" />
	<h2>Error Page</h2>
	<%
		List<ErrorType> errorList = (List<ErrorType>) session
				.getAttribute("Error");

		for (ErrorType e : errorList) {
	%><table>
		<tr>
			<td>ShortMsg:</td>
			<td><%=e.getShortMessage()%>
		</tr>
		<tr>
			<td>LongMsg:</td>
			<td><%=e.getLongMessage()%>
		</tr>
	</table>
	<table>
		<tr>
			<td>Request:</td>
		</tr>
		<tr>
			<td><textarea rows="10" cols="100"><%=session.getAttribute("lastReq")%></textarea>
			</td>
		</tr>
		<tr>
			<td>Response:</td>
		</tr>
		<tr>
			<td><textarea rows="10" cols="100"><%=session.getAttribute("lastResp")%></textarea>
			</td>
		</tr>
	</table>


	<%
		}
	%>
	<a href="index.html">Home</a>
	<a href="<%=session.getAttribute("url")%>">Back</a>
	<div id="related_calls">
		See also
		<%=session.getAttribute("relatedUrl")%>
	</div>

</body>
</html>