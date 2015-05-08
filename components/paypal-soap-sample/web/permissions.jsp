<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	String checkboxAttrib = "";
	String divAttrib = "";
	if (request.getParameter("accessToken") != null) {
		checkboxAttrib = " checked ";
		divAttrib = "class='open' ";
	} else {
		divAttrib = " class='closed' ";
	}
%>
<br />
<input type="checkbox" name="authentication" id="authentication"
	<%=checkboxAttrib%> onclick="toggleDisplay('permissions');" />
<label for="authentication">Use Permission Credentials</label>
<a href='RequestPermissions?page=<%=request.getParameter("source")%>'>Get
	permissions credentials here</a>
<br />
<div id="permissions" <%=divAttrib%>>
	<div class="overview">The PayPal Permissions API allows you to
		request and obtain permissions to execute one or more APIs on behalf
		of your customers (third party). The granted permission is represented
		by a access token and token secret pair that you must store securely.
		Here we use it for MASSPAY API</div>
	<div class="param_name">Access Token</div>
	<div class="param_value">
		<input type="text" name="accessToken"
			<%if (request.getParameter("accessToken") == null) {%> value=""
			<%} else {%> value="<%=request.getParameter("accessToken")%>" <%}%>
			size="50" maxlength="260" />
	</div>
	<div class="param_name">Token Secret</div>
	<div class="param_value">
		<input type="text" name="tokenSecret"
			<%if (request.getParameter("tokenSecret") == null) {%> value=""
			<%} else {%> value="<%=request.getParameter("tokenSecret")%>" <%}%>
			size="50" maxlength="260" />
	</div>
</div>
<br />
