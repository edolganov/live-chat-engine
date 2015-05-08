<%@tag import="och.api.model.PropKey"%>
<%@tag pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>

<%@ attribute name="id" %>

<utils:prop key="<%=PropKey.httpServerUrl%>" var="httpUrl"/>
<utils:prop key="<%=PropKey.httpsServerUrl%>" var="httpsUrl"/>
<script>
	(function() {

		window.oChatProps = window.oChatProps? window.oChatProps : {};

		var s = document.createElement('script');
		s.type = 'text/javascript';
		s.async = true;
		var srcVal = "";
		if('https:' == document.location.protocol) srcVal = "${httpsUrl}";
		else srcVal = "${httpUrl}";
		s.src = srcVal + "/chat/app.js"
		var o = document.getElementsByTagName('script')[0];
		o.parentNode.insertBefore(s, o);
	})();

	oChatProps.id = "${id == null? 'front' : id}";
	oChatProps.lang = "${lang}";
</script>
