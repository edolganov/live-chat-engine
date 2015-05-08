<%@page import="och.api.model.PropKey"%>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>

<utils:setupRoles/>


<layout:base useAnalytics="true">
	<jsp:attribute name="title">Live Chat — open source live chats for support and sales!</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:css href="/css/front/front.css"/>
		
		<front:chatScript/>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<front:enterTopBlock/>
	
		<div id="title">
			<%--<img class="logo" src="/i/icon48.png"> --%>
			<h1>
				<span id="titleCheap">Live</span> Chat
			</h1>
		</div>

		<h2 id="mainDesc">
			Open source live chats engine.
			<br>
			More info on <a href='https://github.com/edolganov/live-chat-engine' target="_blank">GitHub page</a>.
		</h2>

		<div id="shortAboutInfo">
			
			<%-- 
			<h3>Links</h3>
			<hr class="title">
			<p></p>
			--%>

		</div>

		
		<div id="mainBlockMini">
		</div>
		
		
		<front:copyright />
		
		
	</jsp:attribute>
	
</layout:base>