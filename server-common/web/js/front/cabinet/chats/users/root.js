
function AccUsersController(acc){

	var self = this;
	var accId = acc.uid;
	var ui = Util.cloneById("accUsers-template").createUI();
	var isFirst = true;
	var curUsers;
	var labels = ["#", 
		Labels.get("col.login"),
		Labels.get("col.chatName"),
		Labels.get("col.privileges"),
		Labels.get("col.actions")];
	var curUser = FrontApp.security.getUser();

	var addUserController;
	var addReqsController;

	this.init = function(){

		ui.reload.click(function(){
			reloadReq();
		});

		initAddUser();
		initReqsTable();

	};

	function initAddUser(){
		
		if( ! FrontApp.security.isAccOwnerOrModer(accId)){
			ui.addUser.hide();
			ui.addUserForm.hide();
			return;
		}

		addUserController = new AddAccUserController(self, accId);
		addUserController.init();
		ui.addUserForm.append(addUserController.getUI());

		ui.addUserForm.hide();
		ui.addUser.click(function(){
			if(ui.addUserForm.isVisible()){
				self.hideAddUserForm();
			} else {
				self.openAddUserForm();
			}
		});

	}

	this.hideAddUserForm = function(){
		ui.addUserForm.hide(500);
	};

	this.openAddUserForm = function(login, scrollToTop){
		addUserController.reinit(login);
		ui.addUserForm.show(500);

		if(scrollToTop) HtmlUtil.scrollOnPageTop(true);
	};

	function initReqsTable(){

		if( ! FrontApp.security.isAccOwnerOrModer(accId)){
			ui.addReqs.hide();
			ui.addReqsForm.hide();
			return;
		}

		addReqsController = new AddAccUserReqsController(self, accId);
		addReqsController.init();
		ui.addReqsForm.append(addReqsController.getUI());
		
		ui.addReqsForm.hide();
		ui.addReqs.click(function(){
			if(ui.addReqsForm.isVisible()){
				self.hideAddReqsForm();
			} else {
				addReqsController.reinit();
				ui.addReqsForm.show(500);
			}
		});

	}

	this.hideAddReqsForm = function(){
		ui.addReqsForm.hide(500);
	};


	this.getUI = function(){
		if(isFirst){
			isFirst = false;
			reloadReq();
		}
		return ui.root;
	};

	this.onUserAdded = function(){

		ui.addUserForm.hide();
		
		if(ui.addReqsForm.isVisible()){
			addReqsController.reinit();
		}

		reloadReq();
	};

	this.reloadUsersList = function(){
		reloadReq();
	};


	function reloadReq(){

		Ajax.proxyJsonPost(ui.root, {
			url: "/system-api/chat/users",
			data: {
				accId: accId
			},
			useCustomErrors: true,
			beforeSend: function(){
				BtnOps.disableBtnWait(ui.reload);
			},
			success: function(resp){
				showUsers(resp.users);
			},
			complete: function(){
				BtnOps.enableBtn(ui.reload);
			}
		});
	}

	function showUsers(list){
		curUsers = initUsers(list);
		curUsers.sort(function(a, b){
			return Util.ascSort(a.login, b.login);
		});
		renderCurUsers();
	}

	function initUsers(list){
		$.each(list, function(i, user){
			user.privs =  Util.listToMap(user.params.privs);
			user.nickname = user.params.nickname;
		});
		return list;
	}

	function renderCurUsers(){
		
		ui.users.empty();

		var table = new TablePanel(5, labels, {root: ui.users});

		if( ! FrontApp.security.isAccOwnerOrModer(accId)){
			table.getUI().addClass("operator");
		}

		$.each(curUsers, function(i, user){
			addUserRow(table, i, user);
		});
		

	}

	function addUserRow(table, i, user){
		
		var row = table.addElemsRow([
			i+1,
			user.login,
			createNickCell(user),
			createPrivsCell(user),
			createActionsCell(user)
		]);

		if(curUser.id == user.id){
			row.addClass("curUser");
		}

		row.attr("p-id", user.id);
	}

	function createPrivsCell(user){
		var cell = new AccUserPrivsCellController(self, accId, user);
		cell.init();
		return cell.getUI();
	}

	function createActionsCell(user){
		var cell = new AccUserActionsCellController(self, accId, user);
		cell.init();
		return cell.getUI();
	}

	function createNickCell(user){
		var cell = new AccUserNickCellController(self, accId, user);
		cell.init();
		return cell.getUI();
	}


}