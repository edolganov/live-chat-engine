<%@tag import="java.util.Collection"%>
<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="src" required="true"%>
<%@ attribute name="skipBuildParam" type="java.lang.Boolean" required="false"%>
<%
	Object bundleFlag = request.getAttribute("inScriptBundle");
	if(bundleFlag == null){
%>
<c:choose>
	<c:when test="${skipBuildParam}">
		<c:set var="urlValue" value="${src}"/>
	</c:when>
	<c:otherwise>
		<%-- проставляем после урла номер ревизии, чтобы браузер точно не кешировал обновленный файл --%>
		<c:set var="urlValue" value="${src}?__r=${appBuild}"/>
	</c:otherwise>
</c:choose>
<script type="text/javascript" src="${urlValue}"></script>
<%
	} else {
		Collection<String> urls = (Collection<String>)request.getAttribute("scriptBundleUrls");
		if(urls != null) urls.add(src);
	}
%>