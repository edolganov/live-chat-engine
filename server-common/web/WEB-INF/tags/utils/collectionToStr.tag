<%@tag import="och.util.StringUtil"%>
<%@ tag trimDirectiveWhitespaces="true"%>
<%@ attribute name="collection" required="true" type="java.util.Collection"%>
<%@ attribute name="separator" type="java.lang.Character"%>
<%@ attribute name="beginBlock" type="java.lang.Character"%>
<%@ attribute name="endBlock" type="java.lang.Character"%>
<%@ attribute name="useConvert" type="java.lang.Boolean"%>
<%@attribute name="var" %>
<%
	String str = StringUtil.collectionToStr(collection, separator, beginBlock, endBlock, Boolean.TRUE.equals(useConvert));
	if(var != null) request.setAttribute(var, str);
	else out.print(str);
%>