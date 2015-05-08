
function TariffsService(){

	var self = this;
	var map = {};
	var paused;

	this.init = function(){

		var data = $("#tariffsData");
		if(data.length > 0){
			data.children().each(function(){
				var item = $(this);
				var id = item.attr("p-id");
				map[item.attr("p-id")] = {
					id: id,
					price: item.attr("p-price"),
					maxOperators: item.attr("p-maxOperators"),
					name: Labels.get("tariffName."+id, "Unknown"),
					desc: Labels.get("tariffDesc."+id, "")
				}
			});

			var pausedData = $("#pausedTariffsData");
			var pausedId = pausedData.attr("p-id");
			paused = {
				id: pausedId,
				price: pausedData.attr("p-price"),
				name: Labels.get("tariffName."+pausedId, "Freezed"),
				desc: Labels.get("tariffDesc."+pausedId, "Current account is frozen")
			}
		}

	};

	this.isPausedTariff = function(id){
		return paused? paused.id == id : false;
	};

	this.getPausedTariff = function(){
		return paused;
	};

	this.getTariff = function(id){
		return map[id];
	};

	this.getAll = function(){
		return Util.mapToList(map);
	};


}