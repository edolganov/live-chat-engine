<%@page import="och.api.model.PropKey"%>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>


<layout:base useAnalytics="true">
	<jsp:attribute name="title">${i18n.label('demoLook.title')}</jsp:attribute>
	<jsp:attribute name="head">
	
		<link rel="stylesheet" type="text/css" href="/css/front/demo-look.css" />
		
		<utils:scriptBundle id="demoLook">
			<utils:script src="/js/front/demo/demoLabels.js"/>
			<utils:script src="/js/front/demo/demoLook.js"/>
		</utils:scriptBundle>
		
		<front:chatScript id="demoMode"/>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<div id="selectSiteBlockWrapper">
			<div id="selectSiteBlock">
				${i18n.label('demoLook.label')}
				<input id="selectSite" type="text"/>
				<button id="showSite" class="btn btn-mini">${i18n.label('demoLook.btn')}</button>
			</div>
		</div>
		
		<iframe id="siteFrame" src="" width="100%" height="100%" frameBorder="0" sandbox="allow-scripts">Browser not compatible.</iframe>
		
		
	</jsp:attribute>
	
</layout:base>