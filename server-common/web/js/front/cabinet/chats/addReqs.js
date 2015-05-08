
function AddReqsController(){
	
	var ui = Util.cloneById("addReqs-template").createUI();
	var isFirst = true;
	var labels = ["#",
		Labels.get("col.accountId"),
		Labels.get("col.created"),
		Labels.get("col.actions")];
	var curReqs = [];

	this.init = function(){

		ui.reload.click(function(){
			reloadReq();
		});

		ui.send.click(function(){
			sendReq();
		});

	};

	this.getUI = function(){
		if(isFirst){
			isFirst = false;
			reloadReq();
		}
		return ui.root;
	};

	function sendReq(){

		var data = ui.root.getFormData();

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/createAddReq",
			data: data,
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.send);
			},
			success: function(){
				ui.accId.val("");
				reloadReq();
			},
			complete: function(){
				BtnOps.enableBtn(ui.send);
			}
		});
	}

	function reloadReq(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/addReqsList",
			data: {},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.reload);
			},
			success: function(reqs){
				showReq(reqs);
			},
			complete: function(){
				BtnOps.enableBtn(ui.reload);
			}
		});
	}

	function showReq(reqs){
		curReqs = reqs;
		renderTable();
		
	}

	function renderTable(){
		
		ui.reqs.empty();

		if(Util.isEmptyArray(curReqs)){
			ui.reqs.text(Labels.get("common.noReq"));
			return;
		}

		var table = new TablePanel(4, labels, {root: ui.reqs});

		$.each(curReqs, function(i, item){
			var date = item.params? DateUtil.dateFrom(item.params.reqDate) : null;
			table.addElemsRow([
				i+1,
				createNameCell(item),
				date? DateUtil.format_DD_MM_YYYY(date) : "",
				createActionsCell(item)
			]);
		});

	}

	function createNameCell(item){
		var elem = $("<span></span>");
		elem.text(item.uid);
		if(item.name){
			$("<b></b>").text(" ("+item.name+")").appendTo(elem);
		}
		return elem;
	}

	function createActionsCell(item){

		var cellUI = Util.cloneById("addReqsActions-template").createUI();

		TablePanelUtil.initActionsCell(cellUI);

		cellUI.deleteReq.click(function(){
			if(HtmlUtil.isDisabled(cellUI.deleteReq)) return;
			deleteReq(item, cellUI);
		});

		return cellUI.root;
	}

	function deleteReq(item, cellUI){

		Ajax.proxyJsonPost(cellUI.root, {
			url: "/system-api/chat/deleteAddReq",
			data: {accId: item.uid},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableImgWait(cellUI.deleteReq);
			},
			success: function(){
				reloadReq();
			},
			complete: function(){
				BtnOps.enableImg(cellUI.deleteReq);
			}
		});

	}

}