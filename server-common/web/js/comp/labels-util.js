

function LabelsImpl(){

	this.initGlobalLabels = function(labelsTypeList){
		var props = window.Props || {};
		var lang = props["lang"] || "en";
		var allLabels = {};
		$.each(labelsTypeList, function(i, labelsType){
			if(window[labelsType]) {
				allLabels = Util.extend(allLabels, window[labelsType][lang] || {});
			}
		});
		Global.labels = allLabels;
	};

	this.get = function(code, defVal){
		var label = Global.labels[code];
		if(Util.isEmpty(label)) label = defVal;
		if(Util.isEmpty(label) && defVal != null) label = "'"+code+"'";
		return label;
	};

}

var Labels = new LabelsImpl();