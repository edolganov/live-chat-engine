
var TabOps = {

	showOnlineAccTab: function(elem){
		elem.addClass("operator");
		TabOps.getOrCreateOnlineStatusFlagElem(elem).addClass("online").removeClass("offline");
	},

	 showOfflineAccTab: function(elem){
		elem.addClass("operator");
		TabOps.getOrCreateOnlineStatusFlagElem(elem).addClass("offline").removeClass("online");
	},

	getOrCreateOnlineStatusFlagElem: function(root){
		var flag = $(".onlineStatus", root);
		if(flag.length == 0){
			flag = $("<div></div>");
			flag.addClass("onlineStatus").appendTo(root);
		}
		return flag;
	}

};