<%@ tag  pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>

<utils:ifSystemProp name="och.front.analytics">

	<utils:propToJs key="front.analytics.uga.account"/>
	<utils:propToJs key="front.analytics.uga.domain"/>
	<utils:script src="/js/analytics.js"/>
	
</utils:ifSystemProp>