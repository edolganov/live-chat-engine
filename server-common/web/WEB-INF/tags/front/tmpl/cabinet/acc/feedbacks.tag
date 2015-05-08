<%@tag pageEncoding="UTF-8" %>



<div id="feedbacks-template">

	<div bind="topControl" class="topControl bordered"></div>

	<div bind="list" class="feedbacksList"></div>

</div>


<div id="feedback-elem-template" class="feedbackElem bordered">
	
	<span bind="num"></span>

    <dl class="dl-horizontal">
		<dt i18n="feed.from">From</dt>
		<dd bind="ref" class="text-info"></dd>
		<dt i18n="feed.name">Name</dt>
		<dd bind="name"></dd>
		<dt i18n="feed.email">Email</dt>
		<dd bind="email"></dd>
		<dt i18n="feed.created">Created</dt>
		<dd bind="created"></dd>
		<dt i18n="feed.info">Info</dt>
		<dd bind="info"></dd>
		<dt class="feedbackMsgBlock" i18n="feed.msg">Message</dt>
		<dd bind="text" class="feedbackMsgBlock msgContent"></dd>
    </dl>
</div>


<div id="feedbacksButtons-template">

	<input bind="date" class="input-small btnPanelInput" type="text" autocomplete="off">
	
	<button bind="load" class="btn btn-default" i18n="feed.show">Show feedbacks</button>
	
</div>


