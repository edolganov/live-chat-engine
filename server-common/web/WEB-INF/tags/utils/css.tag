<%@tag import="java.util.Collection"%>
<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="href" required="true"%>
<%@ attribute name="skipBuildParam" type="java.lang.Boolean" required="false"%>
<%@ attribute name="media" required="false"%>
<%
	Object bundleFlag = request.getAttribute("inCssBundle");
	if(bundleFlag == null){
%>
<c:choose>
	<c:when test="${skipBuildParam}">
		<c:set var="urlValue" value="${href}"/>
	</c:when>
	<c:otherwise>
		<%-- проставляем после css номер ревизии, чтобы браузер точно не кешировал обновленный css --%>
		<c:set var="urlValue" value="${href}?__r=${appBuild}"/>
	</c:otherwise>
</c:choose>
<c:set var="mediaBlock" value="media='all'"/>
<c:if test="${!empty media}">
	<c:set var="mediaBlock" value="media='${media}'"/>
</c:if>
<link rel="stylesheet" type="text/css" href="${urlValue}" ${mediaBlock}/>
<%
	} else {
		Collection<String> urls = (Collection<String>)request.getAttribute("cssBundleUrls");
		if(urls != null) urls.add(href);
	}
%>