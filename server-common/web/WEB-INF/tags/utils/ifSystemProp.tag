<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ attribute name="name" required="true"%>
<%@ attribute name="expectedVal"%>
<%
	String val = System.getProperty(name);
	if((expectedVal == null && val != null) || (expectedVal != null && expectedVal.equals(val))){
%>
<jsp:doBody/>
<%	}%>