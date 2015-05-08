<%@tag pageEncoding="UTF-8" %>

<div id="newAcc-template">
	
	<label i18n="newAcc.title">Account name (example: my-site.com)</label>
	<input bind="name" formData type="text" class="form-control" 
		autocomplete="off"
		placeholder-i18n="newAcc.name.placeholder"
		placeholder="Enter name"
		title-i18n="newAcc.name.title"
		title="Account name">
	
	<br>
	<label>
		<input bind="isOperator" formData type="checkbox"> 
		<span i18n="newAcc.checkbox">I will be Operator too (not only Owner)</span>
	</label>

	<div bind="tariffSelect"></div>
	
	<br>
	<button bind="create" class="btn btn-default" i18n="newAcc.create">Create</button>
	
</div>


