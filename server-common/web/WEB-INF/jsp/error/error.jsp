<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset='utf-8'/>
	<title>Error</title>
</head>
<body>
<c:choose>
    <c:when test="${!empty requestScope.statusCode}">
        Error code: ${requestScope.statusCode}
    </c:when>
    <c:otherwise>
    	Some server error. Please try again.
    </c:otherwise>
</c:choose>
</body>
</html>