<%@tag pageEncoding="UTF-8" %>

<div id="addReqs-template">

	<label i18n="addOp.title">Send a request to an account's owner</label>
	<input bind="accId"
		formData
		type="text"
		class="form-control input-xlarge"
		autocomplete="off" 
		placeholder-i18n="addOp.accId"
		placeholder="Enter account id">
	<button bind="send" class="btn btn-default" i18n="addOp.btn">Send</button>

	<br>
	<br>
	<legend><span i18n="addOp.sended.title">Sended Requests</span>
		<button bind="reload" 
			class="btn btn-default"
			i18n="addOp.update">Update</button>
	</legend>
	<div bind="reqs" class="accReqs"></div>
	
</div>

<div id="addReqsActions-template" class="addReqsActions">
	<div bind="wrapper" class="wrapper">
		<i bind="deleteReq" class="imgBtn imgRemove" 
		   title-i18n="addOp.deleteReq"
		   title="Delete request"></i>
	</div>
</div>


