
var I18n = {

	initLabels: function(root){
		var queue = [];
		queue.push(root);
		while(queue.length > 0){
			var elem = queue.shift();
			I18n.initLabelsForSingleElem(elem);
			elem.children().each(function(i, child){
				queue.push($(child));
			});
		}
	},

	initLabelsForSingleElem: function(elem){

		var labels = Global.labels? Global.labels : {};
		var key = null;
		var text = null;

		key = elem.attr("i18n");
		if( ! Util.isEmpty(key) && labels[key]){
			text = labels[key];
			if(Util.isEmpty(elem.attr("i18n-html"))) elem.text(text);
			else elem.html(text);
		}

		key = elem.attr("title-i18n");
		if( ! Util.isEmpty(key) && labels[key]){
			text = labels[key];
			elem.attr("title", text);
		}

		key = elem.attr("placeholder-i18n");
		if( ! Util.isEmpty(key) && labels[key]){
			text = labels[key];
			elem.attr("placeholder", text);
		}
		
	}

};