<%@page import="och.api.model.PropKey"%>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>
<%@ taglib tagdir="/WEB-INF/tags/front/cabinet" prefix="cabinet" %>

<%@ taglib tagdir="/WEB-INF/tags/front/tmpl/admin" prefix="tmpl_admin" %>
<%@ taglib tagdir="/WEB-INF/tags/front/tmpl/cabinet" prefix="tmpl_cabinet" %>

<utils:setupRoles/>


<layout:base>
	<jsp:attribute name="title">${i18n.label('cabinet.title')}</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:cssBundle id="cabinet">
			<utils:css href="/css/front/cabinet.css"/>
		</utils:cssBundle>
	
		<utils:scriptBundle id="cabinet">
			<utils:script src="/js/api/model/chat/accPrivileges.js"/>
			<utils:script src="/js/comp/errLabels.js"/>
			<utils:script src="/js/comp/ui/tariffSelect.js"/>
			<utils:script src="/js/comp/ui/price.js"/>
			<utils:script src="/js/service/tariffs.js"/>
			<utils:script src="/js/front/frontApp.js"/>
			<utils:script src="/js/front/cabinet/cabLabels.js"/>
			<utils:script src="/js/front/cabinet/cabinetPage.js"/>
			<utils:script src="/js/front/cabinet/welcomePage.js"/>
			<utils:script src="/js/front/cabinet/chats/accs.js"/>
			<utils:script src="/js/front/cabinet/chats/newAcc.js"/>
			<utils:script src="/js/front/cabinet/chats/addReqs.js"/>
			<utils:script src="/js/front/cabinet/chats/accTab.js"/>
			<utils:script src="/js/front/cabinet/chats/active/root.js"/>
			<utils:script src="/js/front/cabinet/chats/active/list.js"/>
			<utils:script src="/js/front/cabinet/chats/active/buttons.js"/>
			<utils:script src="/js/front/cabinet/chats/active/log.js"/>
			<utils:script src="/js/front/cabinet/chats/active/info.js"/>
			<utils:script src="/js/front/cabinet/chats/history/root.js"/>
			<utils:script src="/js/front/cabinet/chats/history/buttons.js"/>
			<utils:script src="/js/front/cabinet/chats/feedbacks/root.js"/>
			<utils:script src="/js/front/cabinet/chats/feedbacks/buttons.js"/>
			<utils:script src="/js/front/cabinet/chats/info/root.js"/>
			<utils:script src="/js/front/cabinet/chats/info/updateTariff.js"/>
			<utils:script src="/js/front/cabinet/chats/users/root.js"/>
			<utils:script src="/js/front/cabinet/chats/users/addUser.js"/>
			<utils:script src="/js/front/cabinet/chats/users/addReqs.js"/>
			<utils:script src="/js/front/cabinet/chats/users/nickCell.js"/>
			<utils:script src="/js/front/cabinet/chats/users/privsCell.js"/>
			<utils:script src="/js/front/cabinet/chats/users/actionsCell.js"/>
			<utils:script src="/js/front/cabinet/user/root.js"/>
			<utils:script src="/js/front/cabinet/billing/root.js"/>
			<utils:script src="/js/front/cabinet/billing/paymentForm.js"/>
			<utils:script src="/js/front/cabinet/billing/confirmPayForm.js"/>
		</utils:scriptBundle>
		
		<c:if test="${Role_ADMIN}">
			<utils:scriptBundle id="admin">
				<utils:script src="/js/front/admin/root.js"/>
				<utils:script src="/js/front/admin/reloadModels.js"/>
				<utils:script src="/js/front/admin/syncPayments.js"/>
				<utils:script src="/js/front/admin/syncAccBlocked.js"/>
				<utils:script src="/js/front/admin/syncAccPaused.js"/>
			</utils:scriptBundle>
		</c:if>
		
	</jsp:attribute>
	<jsp:attribute name="content">

		<div id="userHeader">
			<div class="infoBlock">
				<span bind="name">${user.login}</span>
			</div>
			<a bind="logout" href="javascript:">${i18n.label('cabinet.logout')}</a>
		</div>

		<div id="reloadPageMsg" class="alert alert-block" style="display: none;">
			${i18n.label('cabinet.msgConnProblem')}
			<br>
			<a href="javascript:" class="pseudo" onclick="window.location.reload();">${i18n.label('cabinet.reload')}</a>
		</div>
	
		<div id="sections">
			<ul id="mainNav" class="nav nav-pills">
				<li bind="welcome"></li>
				<c:choose>
					<c:when test="${!empty chatAccs}">
						<li bind="chats">
							<a href="javascript:">
								<span class="chatsLabel">${i18n.label('cabinet.tab.accs')}</span>
								<i class="new-msg icon-envelope blink" title="${i18n.label('cabinet.newMessages')}"></i>
							</a>
							
						</li>
						<c:if test="${isOwnerOfAnyAcc}">
							<li bind="billing">
								<a href="javascript:">${i18n.label('cabinet.tab.billing')}</a>
							</li>
						</c:if>
						<li bind="profile">
							<a href="javascript:">${i18n.label('cabinet.tab.userSettings')}</a>
						</li>
						<li bind="createAcc">
							<a href="javascript:">${i18n.label('cabinet.tab.addAcc')}</a>
						</li>
						<li bind="addReqs">
							<a href="javascript:">${i18n.label('cabinet.tab.addUser')}</a>
						</li>
						<%--
						<li bind="other">
							<a href="javascript:">Other</a>
						</li>
						--%>
					</c:when>
					<%-- Приветственный экран --%>
					<c:otherwise>
						<li bind="createAcc">
							<a href="javascript:">${i18n.label('cabinet.tab.createAcc')}</a>
						</li>
						<li bind="addReqs">
							<a href="javascript:">${i18n.label('cabinet.tab.addUser')}</a>
						</li>
						<li bind="profile">
							<a href="javascript:">${i18n.label('cabinet.tab.userSettings')}</a>
						</li>
					</c:otherwise>
				</c:choose>
				
				<%-- Admin --%>
				<c:if test="${Role_ADMIN}">
					<li bind="system">
						<a href="javascript:">System</a>
					</li>
				</c:if>
			</ul>

			<div bind="content" class="sectionContent"></div>
		</div>

		<front:copyright />


		
	</jsp:attribute>
	<jsp:attribute name="footer">
	
		<front:baseFooterData/>

		<div id="adminTemplates" style="display: none;">
			
			<comp:tariffSelect/>
			<comp:price/>
			
			<tmpl_cabinet:welcomePage/>		
			<tmpl_cabinet:accs/>
			<tmpl_cabinet:userProfile/>
			<tmpl_cabinet:billing/>
			
			<c:if test="${Role_ADMIN}">
				<tmpl_admin:system/>
			</c:if>
		</div>
		
		<cabinet:chatServersData/>

		
	</jsp:attribute>
	

</layout:base>
