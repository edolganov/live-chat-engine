<%@tag import="och.api.model.PropKey"%>
<%@tag pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>

<div id="accInfo-template" class="accInfo">
	
	<fieldset>
		<legend i18n="accInfo.base">Base</legend>
		
		<label i18n="accInfo.id">Id</label>
		<div bind="id" class="input-xlarge uneditable-input"></div>
		
		<div bind="createdBlock">
			<label i18n="accInfo.created">Created</label>
			<div bind="created" class="input-small uneditable-input"></div>
		</div>
		
		<label i18n="accInfo.name">Name</label>
		<div bind="nameRead" class="input-xlarge uneditable-input block"></div>
		<input bind="name" class="input-xlarge block" type="text"
			   placeholder-i18n="accInfo.nameEnter"
			   placeholder="Enter name" autocomplete="off">
		
		<button bind="update" class="btn" i18n="accInfo.update">Update</button>
		
	</fieldset>


	
	<div bind="tariffBlock" class="tariffInfoBlock">
		<br>
		<br>
		<fieldset>
			<legend i18n="accInfo.tariff">Tariff</legend>

			<dl class="dl-horizontal">
				<dt i18n="accInfo.tariff.name">Tariff name</dt>
				<dd bind="tariffName"></dd>
				<dt i18n="accInfo.tariff.monthPrice">Monthly price</dt>
				<dd>
					<b>$<span bind="tariffPrice"></span></b>
					<sep></sep>
					<span i18n="accInfo.tariff.desc">For more info see</span> <a bind="tariffOpenBilling" href="javascript:" i18n="accInfo.tariff.billing">Billing Tab</a>
				</dd>
				<dt i18n="accInfo.tariff.maxOps">Max operators count</dt>
				<dd bind="tariffMaxOperators"></dd>
			</dl>

			 <a bind="changeNextTariffLink" href="javascript:" class="pseudo">
				 <span i18n="accInfo.tariff.change">Change tariff or freeze account</span>
			 </a>
			 <span bind="changeNextTariffLink_otherLabel" style="display: none;" i18n="accInfo.tariff.unpaused">Unfreeze account</span>

			 <div bind="changeNextTariffForm" class="dialogForm bordered"></div>

		</fieldset>
	</div>


	<div bind="emailNotifyBlock" class="emailNotifyBlock">
		<br>
		<br>
		<br>
		<br>
		<fieldset>
			<legend i18n="accInfo.emailNotify">Email notifications</legend>

			<label bind="feedback_notifyOpsByEmail_label" class="checkbox">
				<input bind="feedback_notifyOpsByEmail" type="checkbox"> <span i18n="accInfo.emailFeedForOps">New feedbacks for operators</span>
			</label>
		</fieldset>
	</div>
	
	
	<legend class="clientCodeLabel" i18n="accInfo.clientCode">Client code</legend>
	<div class="help-block" i18n="accInfo.clientCodeDesc" i18n-html>
		For load Cheap Chat to your site use this javascript code snippet.
		It starts with &lt;script> and ends with &lt;/script>. 
		<p>Don’t edit the snippet. Just copy it. And paste into every web page you want to show chats. 
		Paste it immediately before the closing &lt;/head> tag.
	</div>
	<pre bind="clientCode" class="codeBlock uneditable-input">
&lt;script&gt;
(function() {
	window.oChatProps = window.oChatProps? window.oChatProps : {};
	var s = document.createElement('script');
	s.type = 'text/javascript';
	s.async = true;
	var srcVal;
	if('https:' == document.location.protocol) srcVal = "${props.getVal('httpsServerUrl')}";
	else srcVal = "${props.getVal('httpServerUrl')}";
	s.src = srcVal + "/chat/app.js";
	var o = document.getElementsByTagName('script')[0];
	o.parentNode.insertBefore(s, o);
})();
oChatProps.id = "#{ACC_ID}";
&lt;/script&gt;</pre>
	
	<p>
	<span class="help-block" i18n="accInfo.clientCodeCheck">
		Check your updated pages in your browser. If you see "Ask a question" block - installation is OK! 
	</span>
	
	<p>
	<span class="help-block">
		<span i18n="accInfo.seeDocs">For more info see the</span>
		<a href='/docs' target="_blank" i18n="accInfo.docsLink">Documentaion</a>
	</span>

</div>


<div id="updateTariff-template" class="updateTariff">

	<div bind="tariffSelect"></div>
	
	<div class="alert alert-block">
		<h4 i18n="tariffChange.warning">Warning!</h4>
		<p>
			<span i18n="tariffChange.payInfo">After the change of tariff the System will be made payment
				for the use of the old tariff for the current month.</span>
			<utils:prop key="<%= PropKey.tariffs_maxChangedInDay %>" var="maxChangedInDay" defaultValFromProp="true"/>
			<c:if test="${maxChangedInDay != null}">
				<span i18n="tariffChange.dayLimitStart">You can change current tariff or freeze the account only</span>
				<b>
					${maxChangedInDay} <span i18n="tariffChange.dayLimitEnd">times per day</span></b>.
			</c:if>
		</p>
	</div>
	
	<button bind="update" class="btn" i18n="tariffChange.update">Update tariff</button>
	<sep></sep>

	<button bind="pause" class="btn btn-warning" i18n="tariffChange.pause">Freeze account</button>
	<span bind="pause_otherLabel" style="display: none;" i18n="tariffChange.unpause">Unfreeze account</span>

	<sep></sep>
	<a bind="cancel" href="javascript:" class="pseudo pseudo-small" i18n="common.cancel">Cancel</a>
</div>


