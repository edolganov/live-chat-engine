<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>

<utils:setupRoles/>


<layout:base>
	<jsp:attribute name="title">Acc frame</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:cssBundle id="accframe">
			<utils:css href="/css/chat/admin/admin.css_"/>
		</utils:cssBundle>
	
		<utils:scriptBundle id="accframe">
			<utils:script src="/js/chat/frontApp.js_"/>
		</utils:scriptBundle>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<h2>Acc frame</h2>
	
		
	
		
	</jsp:attribute>
	<jsp:attribute name="footer">
		
		<div id="templates" style="display: none;">
		
			
		</div>
	</jsp:attribute>
	
</layout:base>