<%@tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>


<c:if test="${user != null}">
	<span id="initData" style="display: none;"
		p-userId="${user.id}"
		p-userLogin="${user.login}"
		p-userEmail="${user.email}"></span>
</c:if>


<div id="templates" style="display: none;">

	<comp:labelDetails/>
	<comp:warningMsgTemplate/>
	<comp:paggingPanel/>
	
</div>