<%@tag pageEncoding="UTF-8" %>


<%-- панель активного чата --%>
<div id="acviteChats-template">

	<div bind="topControl" class="topControl bordered"></div>
	
	<div class="leftWrap">
		<div bind="chats" class="chatsPanel bordered"></div>
		<div bind="log" class="logPanel bordered"></div>
	</div>
	
	<div bind="chatInfo" class="chatInfoPanel bordered"></div>
	
</div>


<div id="acviteChatsButtons-template">

	<div bind="opsOnlineRoot" class="opsOnlineRoot btnLeftPanel" style="display: none;">
		<span i18n="onlineOps">Operators online:</span> <span bind="opsOnline"></span>
	</div>

	<%-- онлайн статус --%>
    <div bind="changeOnlineStatus" class="btn-group btnLeftPanel">
		<%-- btn-success btn-danger --%>
		<a bind="statusLabelRoot" class="btn dropdown-toggle changeStatusLink" data-toggle="dropdown" href="javascript:">
			<span bind="statusLabelWait"><span i18n="status.wait">Wait...</span>&nbsp;&nbsp;&nbsp;</span>
			<span bind="statusLabelOnline" style="display: none;" i18n="status.youOnline">You ONLINE</span>
			<span bind="statusLabelOffline" style="display: none;" i18n="status.youOffline">You OFFLINE</span>
			<span class="caret"></span>
		</a>
		<ul class="dropdown-menu">
			<li>
				<a bind="statusSetOnline" tabindex="-1" href="javascript:">
					<span i18n="status.change">Change to</span> <span class="badge badge-success" i18n="status.online">ONLINE</span>
				</a>
			</li>
			<li>
				<a bind="statusSetOffline" tabindex="-1" href="javascript:">
					<span i18n="status.change">Change to</span> <span class="badge badge-important" i18n="status.offline">OFFLINE</span>
				</a>
			</li>
		</ul>
    </div>

	<%-- кнопки нового чата --%>
	<div bind="noOperatorControl" class="btnLeftPanel" style="display: none;">
		<button bind="takeChat" class="btn btn-default" i18n="chat.take">Take</button>
	</div>
	
	<%-- сортировка списка --%>
	<div bind="sortControl" class="btnLeftPanel" style="display: none;">
		
	</div>
</div>



<div id="chatLogPanel-template">
	
	<div bind="msgs" class="logMsgs"></div>
	
	<div bind="footer" class="logMsgsFooter">
		
		<div bind="takeChatPanel" class="takeChatPanel">
			<span bind="takeFirstMsg" style="display: none;" i18n="chat.mustTake">
				Before send msg you must take the chat.
			</span>
			<span bind="takeNextMsg" style="display: none;" i18n="chat.alreadyHasOp">
				Chat already has operator. To send msg you must take the chat too.
			</span>
			
			<br>
			<button bind="takeChat" class="btn btn-default" i18n="chat.take">Take</button>
		</div>
		
		<div bind="closedInfo" class="closedInfo" i18n="chat.closedInfo">
			Chat closed. You can't add new comments.
		</div>
		
		<div bind="addMsgPanel" class="addMsgPanel">
			<textarea
				bind="text"
				autocomplete="off"
				cols="30" rows="5"
				placeholder-i18n="chat.sendInfo"
				placeholder="Text and press Send button or&nbsp;&lt;Ctrl>&nbsp;+&nbsp;&lt;Enter>"
					  ></textarea>
			<button bind="send" class="btn btn-default" i18n="common.send">Send</button>
		</div>
	</div>
	
</div>


<div id="chatItem-template" class="chatItem">
	<div class="wrap">
		<div class="chatIcon"></div>
		<div class="chatLabel">
			<span bind="ip"></span>
			<br>
			<span bind="time"></span>
			<div bind="closed" class="closed" style="display: none;">
				<span i18n="chat.closed">CLOSED</span>
			</div>
			<i class="new-msg icon-envelope" title-i18n="chats.newMessages" title="New messages!"></i>
		</div>
	</div>
</div>


<div id="userMsg-template" class="msg userMsg">
	<div class="user">
		<span bind="nick" i18n="common.client">Client</span>
		<span bind="time"></span>
	</div>
	<div bind="content" class="msgContent"></div>
</div>

<div id="operatorMsg-template" class="msg operatorMsg">
	<div class="user">
		<span bind="nick" i18n="common.op">Operator</span>
		<span bind="time"></span>
	</div>
	<div bind="content" class="msgContent"></div>
</div>

<div id="clientRef-template" class="clientRef text-info">
</div>


<div id="chatInfo-template">
	
	<div bind="users">
	</div>
	
</div>



<li id="changeGlobalActiveStatus-template" class="changeGlobalActive">
	<a bind="changeBtn" class="btn dropdown-toggle" data-toggle="dropdown" href="javascript:">
		<span i18n="status.global">Global status</span>
		<span class="caret"></span>
	</a>
	<ul class="dropdown-menu">
		<li>
			<a bind="online" tabindex="-1" href="javascript:">
				<span i18n="status.changeGlobal">Global</span> 
				<span class="badge badge-success" i18n="status.online">ONLINE</span>
			</a>
		</li>
		<li>
			<a bind="offline" tabindex="-1" href="javascript:">
				<span i18n="status.changeGlobal">Global</span>
				<span class="badge badge-important" i18n="status.offline">OFFLINE</span>
			</a>
		</li>
	</ul>
</li>

<i id="newMsgIcon-template" class="new-msg icon-envelope" title-i18n="chats.newMessages" title="New messages!"></i>


