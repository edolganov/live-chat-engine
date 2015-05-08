<%@tag pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/front/tmpl/cabinet/acc" prefix="tmpl_acc" %>


<%-- Загрузка данных о всех доступных акках --%>
<div id="chatServersPanel">

	<div bind="initMsg" class="initMsg" style="display: none;">
		<span i18n="chats.conToChats">Connecting to chats...</span>
	</div>
	
	<div bind="errorInitMsg" class="errorInitMsg" style="display: none;">
		<span i18n="chats.cantConToChats">Can't connect to chats.</span>
		<br>
		<a bind="retryInit" href="javascript:" class="pseudo" i18n="chats.retryCon">Retry</a>
	</div>
	
	<div bind="content">
	</div>

</div>


<%-- Панель акка --%>
<div id="chatAccPanel-template" class="accsPanel">


	<div bind="blockedMsg" class="alert alert-block" style="display: none;">
		<h4 i18n="chats.blocked.title">Warning!</h4>
		<span i18n="chats.blocked.desc">The account is blocked. You can't work with it.
		May be the owner has negative balance.</span>
	</div>

	<div bind="pausedMsg" class="alert alert-info alert-block" style="display: none;">
		<span i18n="chats.paused">The account is frozen. You can't work with it.</span>
		<span bind="unpausedBlock">
			<button bind="unpause"
					class="btn btn-success btn-small"
					i18n="chats.unpaused.btn">Unfreeze account</button> 
			<span i18n="chats.unpaused.btnDesc">to start using it.</span>
		</span>
	</div>

	<div bind="initMsg" class="initMsg" style="display: none;">
		<span i18n="chats.acc.load">Getting account info...</span>
	</div>
	
	<div bind="errorInitMsg" class="errorInitMsg" style="display: none;">
		<span i18n="chats.acc.loadError">Can't get account info.</span>
		<br>
		<a href="javascript:" class="reconnectLink pseudo" i18n="chats.acc.retryCon">Retry</a>
	</div>

	<div bind="reconnectMsg" class="alert alert-block" style="display: none;">
		<span i18n="chats.conProblems">Connection problems. Need to reconnect.</span>
		<br>
		<a href="javascript:" class="reconnectLink pseudo" i18n="chats.reconnect">Reconnect</a>
	</div>
	
	<div bind="content" style="display: none;">
		<ul class="nav nav-pills accTabs">
			<li bind="activeChats">
				<a href="javascript:" i18n="chats.tab.active">Active chats</a>
			</li>
			<li bind="history">
				<a href="javascript:" i18n="chats.tab.hist">History</a>
			</li>
			<li bind="feedbacks">
				<a href="javascript:" i18n="chats.tab.feedback">Feedbacks</a>
			</li>
			<li bind="info">
				<a href="javascript:" i18n="chats.tab.accInfo">Account Info</a>
			</li>
			<li bind="users">
				<a href="javascript:" i18n="chats.tab.users">Users</a>
			</li>
		</ul>

		<div bind="accTabContent" class="accTabContent"></div>
	</div>
</div>


<tmpl_acc:active/>
<tmpl_acc:history/>
<tmpl_acc:newAcc/>
<tmpl_acc:addReqs/>
<tmpl_acc:accInfo/>
<tmpl_acc:accUsers/>
<tmpl_acc:feedbacks/>









