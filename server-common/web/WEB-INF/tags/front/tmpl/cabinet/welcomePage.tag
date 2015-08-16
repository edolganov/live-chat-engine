<%@tag import="och.api.model.PropKey"%>
<%@tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>

<utils:prop var="toolMode" key="<%= PropKey.toolMode %>"/>

<div id="welcomePage-template">
	
	<span i18n="welcome.title">Welcome to Live Chats!</span>
	<br>
	<span i18n="welcome.info" i18n-html>
		<c:if test="${toolMode != 'true'}">
			<b>Create your own chat account</b>
			or
		</c:if>
		<b>send request to exists owner</b>.
	</span>
	
</div>


