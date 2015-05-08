<%@tag pageEncoding="UTF-8" %>

<div id="userProfile-template">

	<div class="form-group">
	  <input bind="email" formData type="text" class="form-control" autocomplete="off" 
			 placeholder-i18n="user.newEmail"
			 placeholder="New email">
	  <br>
	  <input bind="login" formData type="text" class="form-control" autocomplete="off"
			 placeholder-i18n="user.newLogin"
			 placeholder="New login">
	  <br>
	  <input bind="psw" formData type="password" class="form-control" autocomplete="off" 
			 placeholder-i18n="user.newPsw"
			 placeholder="New Password">
	</div>

	<br>
	<b i18n="user.confirmChangesPsw">Current password to confirm changes</b>
	<div class="form-group">
	  <input bind="curPsw" formData type="password" class="form-control" autocomplete="off" 
			 placeholder-i18n="user.curPsw"
			 placeholder="Enter Current Password">
	</div>

	<button bind="edit" disabled class="btn btn-default" i18n="user.update">Update</button>
	<button bind="cancel" class="btn btn-default" i18n="user.reset">Reset</button>
	
</div>


