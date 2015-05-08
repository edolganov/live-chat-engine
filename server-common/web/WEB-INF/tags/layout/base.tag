<%@tag import="java.util.Collections"%>
<%@ tag display-name="Base layout" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>

<%@ attribute name="title" fragment="true"%>
<%@ attribute name="head" fragment="true"%>
<%@ attribute name="content" fragment="true"%>
<%@ attribute name="footer" fragment="true"%>
<%@ attribute name="useAnalytics" type="java.lang.Boolean"%>

<c:set var="prodMode" value='<%= System.getProperty("prodMode") != null %>'/>

<!DOCTYPE html>
<html>
<head>
	<meta charset='utf-8'/>
	<%-- <meta name="viewport" content="width=device-width, target-densityDpi=device-dpi">--%>
	<meta name="viewport" content="width=device-width"/>
	
	<c:if test="${!empty title}">
       <title><jsp:invoke fragment="title"/></title>
    </c:if>
    
    <meta name="description" content="${i18n != null ? i18n.label('meta.description') : ''}"/>
    <meta name="keywords" content="${i18n != null ? i18n.label('meta.keywords') : ''}"/>
    
    <link rel="shortcut icon" href="/i/icon.png" />
    <link rel="image_src" href="/i/icon48.png" />
    <meta property="og:image" content="/i/logo-big.png"/>
    
    <utils:cssBundle id="base">
        <utils:css href="/css/bootstrap-custom.css"/>
        <utils:css href="/css/highlight.css"/>
    	<utils:css href="/css/common.css"/>
		<utils:css href="/css/comp.css"/>
    </utils:cssBundle>

	
	<utils:scriptBundle id="thirdparty" skipOptimization="${true}">
		<utils:script src="/js/thirdparty/jquery-1.9.1.min.js"/>
		<utils:script src="/js/thirdparty/jquery.scrollTo.min.js"/>
		<utils:script src="/js/thirdparty/jquery-browser.js"/>
		
		<c:if test="${ ! prodMode}">
			<utils:script src="/js/thirdparty/log4javascript.js"/>
		</c:if>
		
		
		<utils:script src="/js/thirdparty/bootstrap.min.js"/>
		<utils:script src="/js/thirdparty/bootstrap-tooltip.js"/>
		<utils:script src="/js/thirdparty/jquery.cookie.min.js"/>
		<utils:script src="/js/thirdparty/jquery.noty.packaged.min.js"/>
		<utils:script src="/js/thirdparty/jquery.pickmeup.min.js"/>
		<utils:script src="/js/thirdparty/jquery.pickmeup.twitter-bootstrap.min.js"/>
		<utils:script src="/js/thirdparty/highlight.pack.js"/>
	</utils:scriptBundle>
	
	<utils:scriptBundle id="base">
		<utils:script src="/js/lang.js"/>
		<utils:script src="/js/log.js"/>
		<utils:script src="/js/global.js"/>
		<utils:script src="/js/props.js"/>
		
		<utils:script src="/js/api/msgs.js"/>
		<utils:script src="/js/api/error-codes.js"/>
		<utils:script src="/js/api/const.js"/>
		<utils:script src="/js/common/util.js"/>
		<utils:script src="/js/common/html-util.js"/>
		<utils:script src="/js/common/bind.js"/>
		<utils:script src="/js/common/url-util.js"/>
		<utils:script src="/js/common/browser-util.js"/>
		<utils:script src="/js/common/setupBrowserFlag.js"/>
		<utils:script src="/js/common/async-job-util.js"/>
		<utils:script src="/js/common/date-util.js"/>
		<utils:script src="/js/common/form.js"/>
		<utils:script src="/js/common/key-util.js"/>
		<utils:script src="/js/common/string-util.js"/>
		<utils:script src="/js/common/money-util.js"/>
		<utils:script src="/js/common/cookie-util.js"/>
		<utils:script src="/js/comp/ajax.js"/>
		<utils:script src="/js/comp/ajax-xdr.js"/>
		<utils:script src="/js/comp/ajax-wait.js"/>
		<utils:script src="/js/comp/i18n.js"/>
		<utils:script src="/js/comp/labels-util.js"/>
		<utils:script src="/js/comp/frameReqProvider.js"/>
		

		<utils:script src="/js/comp/ui/captcha.js"/>
		<utils:script src="/js/comp/ui/collapse.js"/>
		<utils:script src="/js/comp/ui/tab-panel.js"/>
		<utils:script src="/js/comp/ui/table.js"/>
		<utils:script src="/js/comp/ui/warning-msg.js"/>
		<utils:script src="/js/comp/ui/globalMsgPopup.js"/>
		<utils:script src="/js/comp/ui/btnOps.js"/>
		<utils:script src="/js/comp/ui/tabOps.js"/>
		<utils:script src="/js/comp/ui/labelDetails.js"/>
		<utils:script src="/js/comp/ui/pagging.js"/>
		<utils:script src="/js/comp/ui/calendarOps.js"/>
		<utils:script src="/js/comp/ui/notifyUtil.js"/>
		
		<utils:script src="/js/comp/chat/chatProvider.js"/>
		<utils:script src="/js/comp/chat/chatUtil.js"/>
		
		<utils:script src="/js/service/security.js"/>
		
	</utils:scriptBundle>
	
	<c:if test="${!empty head}">
		<jsp:invoke fragment="head"/>
    </c:if>
</head>
<body>

	<div id="wrapper">
		
		<c:if test="${!empty content}">
			<jsp:invoke fragment="content"/>
	    </c:if>
		
	</div>
	
	<c:if test="${!empty footer}">
		<jsp:invoke fragment="footer"/>
    </c:if>
	
	<c:if test="${prodMode && useAnalytics}">
		<utils:analytics/>
	</c:if>
	
	<utils:CSRF_ProtectToken/>
	
	<utils:propToJs key="lang" customVal="${lang}"/>
    
</body>
</html>