<%@tag import="och.util.DateUtil"%>
<%@tag import="och.api.model.Const"%>
<%@tag import="och.api.model.PropKey"%>
<%@tag pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>


<utils:propToJs key="<%= PropKey.frontApp_cabinet_useHttpUrlsForChats %>"/>
<utils:propToJs key="<%= PropKey.chats_maxMsgSize %>"/>
<utils:propToJs key="payConfirmVal" customVal="${payConfirmVal}"/>
<utils:propToJs key="userBalanceVal" customVal="${balance}"/>


<%-- Список доступных тарифов --%>
<span id="tariffsData" style="display: none;">
	<c:forEach var="t" items="${publicTariffs}">
		<span
			p-id="${t.id}"
			p-price="${t.price}"
			p-maxOperators="${t.maxOperators}"
		></span>
	</c:forEach>
</span>
<span id="pausedTariffsData"
	  p-id="0"
	  p-price="0"
	  style="display: none;"></span>

<%-- Список акков --%>
<span id="chatAccs" style="display: none;">
	<c:forEach var="acc" items="${chatAccs}">
		<span
			p-uid="${acc.uid}"
			p-name='<c:out value="${acc.name}"/>'
			p-tariffId="${acc.tariffId}"
			p-httpUrl="${acc.server.httpUrl}"
			p-httpsUrl="${acc.server.httpsUrl}"
			p-created="<fmt:formatDate pattern="<%=DateUtil.FULL_DATE_FORMAT%>" value="${acc.created}"/>"
			p-blocked="${acc.blocked}"
			p-feedback_notifyOpsByEmail="${acc.feedback_notifyOpsByEmail}"
			p-serverId="${acc.server.id}"
			></span>
	</c:forEach>
</span>


<%-- Привилегии по аккаунтам --%>
<span id="privsByAcc" style="display: none;">
	<c:forEach items="${privsByAcc}" var="entry">
		<span p-uid="${entry.key}">
			<c:forEach items="${entry.value}" var="priv">
				<span p-type="${priv}"></span>
			</c:forEach>
		</span>
	</c:forEach>
</span>
