<%@tag pageEncoding="UTF-8"%>

<div id="accUsers-template" class="accUsers">
	
	<div bind="topControl" class="topControl bordered">
		<button bind="reload" class="btn btn-default" i18n="common.reload">Reload</button>
		<sep></sep>
		<a bind="addUser" class="pseudo" href="javascript:" i18n="users.add">Add User</a>
		<sep></sep>
		<a bind="addReqs" class="pseudo" href="javascript:" i18n="users.reqs">Requests to Add</a>
	</div>

	<div bind="addUserForm" class="dialogForm bordered"></div>

	<div bind="addReqsForm" class="dialogForm bordered"></div>
	
	<div bind="users" class="users"></div>

</div>

<div id="nicks-template" class="nicksCell">
	<div bind="viewMode" class="viewMode">
		<span bind="nickLabel"></span>
		<i bind="edit" class="icon-pencil selectImg" 
		   title-i18n="common.edit"
		   title="Edit"></i>
	</div>
	<div bind="editMode" style="display: none;">
		<input bind="nick" formData class="block" type="text"
			   placeholder-i18n="users.enterNickname"
			   placeholder="Enter nickname"
			   autocomplete="off">
		<button bind="update" class="btn btn-default btn-small" i18n="common.update">Update</button>
		<sep></sep>
		<a bind="cancel" href="javascript:" class="pseudo pseudo-small" i18n="common.cancel">Cancel</a>
	</div>
</div>


<div id="privs-template" class="privsCell">
	<div bind="viewMode" class="viewMode">
		<span bind="ownerPriv" class="label label-important" i18n="users.priv.owner">Owner</span>
		<span bind="moderPriv" class="label label-info" i18n="users.priv.moder">Moderator</span>
		<span bind="opPriv" class="label label-info" i18n="users.priv.op">Operator</span>
		<i bind="edit" class="icon-pencil selectImg" 
		   title-i18n="common.edit"
		   title="Edit"></i>
	</div>
	<div bind="editMode" style="display: none;">
		<span bind="isOwnerLabel" class="label label-important" i18n="users.priv.owner">Owner</span>
		<label bind="isModeratorLabel" class="label label-info">
			<input bind="isModerator" formData type="checkbox"> <span i18n="users.priv.moder">Moderator</span>
		</label>
		<label bind="isOperatorLabel" class="label label-info">
			<input bind="isOperator" formData type="checkbox"> <span i18n="users.priv.op">Operator</span>
		</label>
		<br>
		<button bind="update" class="btn btn-default btn-small" i18n="common.update">Update</button>
		<sep></sep>
		<a bind="cancel" href="javascript:" class="pseudo pseudo-small" i18n="common.cancel">Cancel</a>
	</div>
</div>


<div id="addOp-template">

	<legend i18n="users.add">Add User</legend>
	
	<input bind="login" formData type="text" class="form-control"
		autocomplete="off"
		placeholder-i18n="users.enterLoginOrEmail"
		placeholder="Enter login or email"
		title-i18n="users.loginOrEmail"
		title="Login or email">

	<br>
	<label bind="isOperatorLabel" class="label label-info">
		<input bind="isOperator" formData type="checkbox"> <span i18n="users.priv.op">Operator</span>
	</label>
	<sep></sep>
	<label bind="isModeratorLabel" class="label label-info">
		<input bind="isModerator" formData type="checkbox"> <span i18n="users.priv.moder">Moderator</span>
	</label>

	<br>
	<button bind="add" class="btn btn-default" i18n="common.add">Add</button>
	<sep></sep>
	<a bind="cancel" href="javascript:" class="pseudo" i18n="common.hide">Hide</a>
</div>


<div id="accUserActions-template" class="accUserActions">
	<div bind="wrapper" class="wrapper">

		<i bind="deleteUser" class="imgBtn imgRemove" 
		   title-i18n="common.delete"
		   title="Delete"></i>
	</div>
</div>



<div id="addAccReqs-template">

		<legend>
			<span i18n="users.reqs">Requests to Add</span>
			<button bind="reload" class="btn btn-default" i18n="common.update">Update</button>
		</legend>

		<div bind="reqs" class="accAddReqs"></div>

		<a bind="cancel" href="javascript:" class="pseudo" i18n="common.hide">Hide</a>
</div>


<div id="accAddReqsActions-template" class="accAddReqsActions">
	<div bind="wrapper" class="wrapper">
		<i bind="confirm" class="imgBtn imgOk" 
		   title-i18n="common.confirmReq"
		   title="Confirm request"></i>
		<sep></sep>
		<i bind="deleteReq" class="imgBtn imgRemove" 
		   title-i18n="common.delReq"
		   title="Delete request"></i>
	</div>
</div>