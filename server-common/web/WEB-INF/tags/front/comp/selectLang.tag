<%@tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
	<c:when test="${lang == 'ru'}">
		<a href="?lang=en">English Version</a>
	</c:when>
	<c:otherwise>
		<a href="?lang=ru">По-русски</a>
	</c:otherwise>
</c:choose>
