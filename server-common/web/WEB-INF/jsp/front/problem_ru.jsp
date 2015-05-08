<%@page import="och.api.model.PropKey"%>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>


<layout:base useAnalytics="true">
	<jsp:attribute name="title">Произошла ошибка — Live Chat</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:css href="/css/front/front.css"/>
		
		<front:chatScript/>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<front:enterTopBlock />
	
		<front:leftTitleLogo/>
		
		<h2 id="mainDesc">
			Извините, что-то пошло не так... :(
		</h2>
		
		<c:if test="${errorMsg != null}">
			<br>
			<br>
			<pre class="simple">${errorMsg}</pre>
		</c:if>
		
		
		<div id="mainBlockMini">
		</div>
		
		
		<front:copyright />
		
		
	</jsp:attribute>
	
</layout:base>