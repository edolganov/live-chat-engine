<%@tag import="och.api.model.PropKey"%>
<%@tag import="och.service.props.Props"%>
<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="key" required="true" type="java.lang.Object"%>
<%@ attribute name="defaultVal" description="Значение по умолчанию, если проперти не найдено по ключу"%>
<%@ attribute name="customVal" description="Явное значение, которое нужно проставить по ключу"%>
<c:choose>
	<c:when test="${empty customVal}">
		<%
			Props props = (Props) request.getAttribute("props");
			if(props == null) {
				request.setAttribute("valToJs", defaultVal);
			}
			else {
				if(key instanceof PropKey){
					request.setAttribute("valToJs", props.getStrVal((PropKey)key));
				} else {
					request.setAttribute("valToJs", props.getVal(key, defaultVal));
				}
			}
		%>
	</c:when>
	<c:otherwise>
		<c:set var="valToJs" value="${customVal}"/>
	</c:otherwise>
</c:choose>
<script type="text/javascript">
	var Props = window.Props? Props : {};
	Props['${key}'] = '${!empty valToJs? valToJs : ""}';
</script>