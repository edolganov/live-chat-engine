
var AddAccUserReqsController = function(owner, accId){

	var ui = Util.cloneById("addAccReqs-template").createUI();
	var labels = ["#", 
		Labels.get("col.login"),
		Labels.get("col.created"),
		Labels.get("col.actions")];

	this.init = function(){

		ui.cancel.click(function(){
			owner.hideAddReqsForm();
		});

		ui.reload.click(function(){
			reloadReqs();
		});

	};

	this.getUI = function(){
		return ui.root;
	};


	this.reinit = function(){
		reloadReqs();
	};


	function reloadReqs(){
		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/addReqsListForAcc",
			data: {accId: accId},
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
		ui.reqs.empty();

		if(Util.isEmptyArray(reqs)){
			ui.reqs.text(Labels.get("common.noReq"));
			return;
		}

		var table = new TablePanel(4, labels, {root: ui.reqs});

		$.each(reqs, function(i, user){
			var date = user.params? DateUtil.dateFrom(user.params.reqDate) : null;
			table.addElemsRow([
				i+1,
				user.login,
				date? DateUtil.format_DD_MM_YYYY(date) : "",
				createActionsCell(user)
			]);
		});
	}

	function createActionsCell(user){

		var cellUI = Util.cloneById("accAddReqsActions-template").createUI();

		TablePanelUtil.initActionsCell(cellUI);

		cellUI.deleteReq.click(function(){
			if(HtmlUtil.isDisabled(cellUI.deleteReq)) return;
			deleteReq(user, cellUI);
		});

		cellUI.confirm.click(function(){
			owner.openAddUserForm(user.login, true);
		});

		return cellUI.root;
	}



	function deleteReq(user, cellUI){

		Ajax.proxyJsonPost(cellUI.root, {
			url: "/system-api/chat/deleteAddReqForAcc",
			data: {accId: accId, userId: user.id},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableImgWait(cellUI.deleteReq);
			},
			success: function(){
				reloadReqs();
			},
			complete: function(){
				BtnOps.enableImg(cellUI.deleteReq);
			}
		});

	}

};