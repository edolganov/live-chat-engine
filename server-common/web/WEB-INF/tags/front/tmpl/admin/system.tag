<%@tag pageEncoding="UTF-8" %>

<div id="system-template">
	

</div>

<div id="systemReloadModels-template" class="dialogForm bordered">
	
	<legend>Reload Models</legend>
	<ul>
		<li>servers</li>
		<li>accounts</li>
		<li>privileges</li>
		<li>tariffs</li>
		<li>user balances</li>
	</ul>
	
	<button bind="reload" class="btn btn-default">Reload</button>
	
</div>


<div id="systemSyncPayments-tempalte" class="dialogForm bordered">

	<legend>Payments sync</legend>

	<div class="alert">
		<b>It's money - be careful!</b>
		<br>
		Call if month timer failed.
	</div>

	<div>
		<b>Last sync info</b>
		<div>Date: <span bind="lastSyncDate">-</span></div>
		<div>Updated accs count: <span bind="lastSyncCount">-</span></div>
	</div>

	<br>

	<button bind="sync" class="btn btn-default btn-warning">New sync</button>

	<br>
	<br>
	<label>Results</label>
	<pre bind="result" class="pre-scrollable height-150">
		To see results press 'Refresh' btn
	</pre>
	<button bind="refresh" class="btn btn-default">Refresh</button>

</div>


<div id="systemSyncAccBlocked-tempalte" class="dialogForm bordered">

	<legend>Acc blocked sync</legend>

	<div>
		<div>Unblocked: <span bind="unblocked">-</span></div>
		<div>Blocked: <span bind="blocked">-</span></div>
	</div>

	<button bind="sync" class="btn btn-default">Sync blocked</button>
</div>


<div id="systemSyncAccPaused-tempalte" class="dialogForm bordered">

	<legend>Acc paused sync</legend>

	<div>
		<div>Unpaused: <span bind="unpaused">-</span></div>
		<div>Paused <span bind="paused">-</span></div>
	</div>

	<button bind="sync" class="btn btn-default">Sync paused</button>
</div>


