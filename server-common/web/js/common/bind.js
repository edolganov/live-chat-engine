

$.fn.initUI = function(){
	this.ui = Bind.createUI(this);
	return this;
};

$.fn.createUI = function(){
	this.ui = Bind.createUI(this);
	return this.ui;
};

var Bind = {

	useI18n: true,

	createUI: function(html){
		
		var out = {};

		out.root = html;

		var elems = $("[bind]", html);
		$.each(elems, function(i, elem){
			var name = $(elem).attr("bind");

			var field = out[name];
			if(Util.isEmpty(field)){
				field = $(elem);
				out[name] = field;
			} else {
				throw new Error("Can't create UI: duplicated name: "+name);
			}

		});

		try {
			if(Bind.useI18n && I18n) I18n.initLabels(html);
		}catch(e){}

		return out;
	}

};