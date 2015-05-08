<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>

<layout:base>
	<jsp:attribute name="title">User activated</jsp:attribute>
	<jsp:attribute name="content">
	
		<div class="alert alert-success">
		
			<h3>User activated!</h3>
			<p>Done for email: <big><c:out value="${userEmail}"/></big>
			<p>Now you can <a href="/enter">Sign In</a>.
			
		</div>

		
	</jsp:attribute>
</layout:base>