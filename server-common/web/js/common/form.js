
$.fn.getFormData = function(){
	return FormUtil.getFormData(this);
}

$.fn.clearFormData = function(){
	return FormUtil.clearFormData(this);
}

FormUtil = {
	
	getFormData: function(html){
		var out = {};

		var elems = $("[formData]", html);
		$.each(elems, function(i, elem){
			elem = $(elem);
			var name = elem.attr("formData");
			if(!name) name = elem.attr("name");
			if(!name) name = elem.attr("bind");

			if( ! Util.isEmpty(out[name])){
				throw new Error("Can't create FormData: duplicated name: "+name);
			}
			var type = elem.attr("type");
			if(type == "text") out[name] = elem.val();
			else if(type == "checkbox") out[name] = HtmlUtil.isChecked(elem);
			else out[name] = elem.val();
		});

		return out;
	},

	clearFormData: function(html){
		var elems = $("[formData]", html);
		$.each(elems, function(i, elem){
			elem = $(elem);
			elem.val("");
		});
	},

	/**
	 * Динамически создаем форму и отправляем ее
	 */
	submitDynamicForm: function(actionUrl, params){

        var form = $("body").append("<form></form>").children().last();
        form.attr("action", actionUrl);
        form.attr("method", "post");

        $.each(params, function(key, value){
            form.append("<input type='hidden' name='"+key+"' value='"+value+"'/>");
        });

        form.submit();

	}
}

