<%@tag pageEncoding="UTF-8" %>

<div id="paggingPanel-template" class="paggingPanel">
	<button bind="refresh" class="btn btn-small refresh" title="Refresh page" style="display: none;"><i class="icon-refresh"></i></button>
	<div class="float-right">
		<button bind="prev" class="btn btn-small prev disabled" title="Prev Page"><i class="icon-arrow-left"></i></button>
		<span bind="pageNum"></span>
		<button bind="next" class="btn btn-small next disabled" title="Next Page"><i class="icon-arrow-right"></i></button>
	</div>
</div>