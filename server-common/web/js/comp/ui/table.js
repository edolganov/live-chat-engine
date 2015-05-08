

TablePanelDefaults = {

	tableClass: null

};


TablePanel = function(colCount, labels, opt){

	opt = Util.extend({
		root: null,
		tableClass: TablePanelDefaults.tableClass,
		prepend: false,
		rowClass: null,
		useThead: true
	}, opt);

	var table = $("<table></table>");
	
	if(opt.tableClass){
		table.addClass(opt.tableClass);
	}


	var header = null;
	var rowsCount = 0;

	function init(){

		if(opt.root) {
			if(opt.prepend) opt.root.prepend(table);
			else opt.root.append(table);
		}

		if( ! Util.isEmptyArray(labels)){
			
			var headerParent = opt.useThead? $("<thead></thead>").appendTo(table) : table;
			header = $("<tr></tr>").appendTo(headerParent);

			header.addClass("header");
			for (var i = 0; i < colCount; i++) {
				var td = $("<th></th>").appendTo(header);
				td.addClass("th-"+i);
				if(labels.length > i) td.text(labels[i]);
			}
		}

	}

	this.getUI = function(){
		return table;
	}

	this.addElemsRow = function(elems){
		
		var tr = $("<tr></tr>").appendTo(table);
		tr.addClass("t-row");
		tr.addClass("tr-"+rowsCount);
		if(opt.rowClass) tr.addClass(opt.rowClass);
		rowsCount++;

		for (var i = 0; i < colCount; i++) {
			var td = $("<td></td>").appendTo(tr);
			td.addClass("td-"+i);
			if(elems.length > i) {
				var elem = elems[i];
				if(elem.content && ! Util.isEmpty(elem.html)){
					if(elem.html) td.html(elem.content);
					else td.text(elem.content);
				}
				else if(Util.isString(elem)
					|| Util.isNum(elem)){
					td.text(elem);
				}
				else {
					elem.detach();
					elem.appendTo(td);

					var cellController = TablePanelUtil.getCellController(elem);
					if(cellController && cellController.onAppended) cellController.onAppended(tr, td);
				}

			}
		}

		return tr;
	};

	this.empty = function(){
		table.find("tr.t-row").remove();
		rowsCount = 0;
	};
	

	init();
};


TablePanelUtil = {

	putCellController: function(cellElem, controller){
		cellElem.data("cellController", controller);
	},

	getCellController: function(cellElem){
		return cellElem.data("cellController");
	},

	/**
	 * Ячейка действий у таблицы:
	 * - появляется при наводке мыши, исчезает при ее убирании
	 * - остается во время запроса
	 */
	initActionsCell: function(ui, opt){
		
		opt = Util.extend({
			root: "root",
			reqElem: "root",
			wrapper: "wrapper"
		}, opt);

		TablePanelUtil.putCellController(ui[opt.root], {
			onAppended: function(tr){
				tr.mouseover(function(){
					ui[opt.wrapper].show();
				});
				tr.mouseout(function(){
					if(Ajax.isInRequest(ui[opt.reqElem])) return;
					ui[opt.wrapper].hide();
				});
			}
		});
		ui[opt.wrapper].hide();
	}

};