<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>

<layout:base>
	<jsp:attribute name="title">User activation failed</jsp:attribute>
	<jsp:attribute name="content">
		
		<div class="alert alert-block">
		
			<h3>Can't activate user</h3>
			<p>Failed for email: <big><c:out value="${userEmail}"/></big>
			<p>${errorMsg}</p>
			
		</div>
		
	</jsp:attribute>
</layout:base>