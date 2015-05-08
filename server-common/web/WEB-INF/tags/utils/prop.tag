<%@tag import="och.api.model.PropKey"%>
<%@tag import="och.util.Util"%>
<%@tag import="och.service.props.Props"%>
<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="key" required="true" type="java.lang.Object"%>
<%@ attribute name="defaultVal" description="Значение по умолчанию, если проперти не найдено по ключу"%>
<%@ attribute name="defaultValFromProp" type="java.lang.Boolean"%>
<%@ attribute name="var" %>
<%
	String val = null;
	Props props = (Props) request.getAttribute("props");
	if(props != null) val = props.getVal(key);
	if(val == null && Boolean.TRUE.equals(defaultValFromProp)){
		PropKey propKey = Util.tryParseEnum(key, PropKey.class);
		if(propKey != null) val = propKey.strDefVal();
	}
	if(val == null) val = defaultVal;
	
	if(var != null) request.setAttribute(var, val);
	else out.print(val);
%>