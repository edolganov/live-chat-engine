
function TariffSelect(parent){

	var ui = Util.cloneById("tariffSelect-template").createUI();

	function init(){

		var tariffs = FrontApp.tariffs.getAll();
		if(Util.isEmptyArray(tariffs)){
			return;
		}
		

		$.each(tariffs, function(i, tariff){
			var opt = $("<option></option>");
			opt.text(tariff.name+" — $"+tariff.price);
			opt.attr("value", tariff.id);
			opt.attr("p-desc", tariff.desc);
			ui.tariff.append(opt);
		});
		
		if(tariffs.length == 1 || Props["toolMode"] == 'true'){
			ui.tariff.disable();
		}

		ui.tariff.change(onTariffChanged);
		ui.tariff.change();
		
		if(Props["toolMode"] == 'true'){
			ui.root.setVisible(false);
		}
		parent.append(ui.root);

	}

	var onTariffChanged = function(){
		var opt = HtmlUtil.getSelectedOption(ui.tariff);
		ui.tariffDesc.html(opt.attr("p-desc"));
	};



	init();

}