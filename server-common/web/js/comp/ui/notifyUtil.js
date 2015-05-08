
var NotifyUtil = {

	defaultIcon: "",

	permitNotifications: function(title, text, iconSrc){
		if( ! window.Notification) return;
		var needAsk = ! Notification.permission || Notification.permission.toLowerCase() != "granted";
		if( ! needAsk) return;
		Notification.requestPermission( function() {
			new Notification(title, {
				body : text? text : "",
				icon : iconSrc? iconSrc : NotifyUtil.defaultIcon
			});
		});
	},

	sendNotification: function(title, text, iconSrc){
		
		if( ! window.Notification) return;
		Notification.requestPermission( function() {
			new Notification(title, {
				body : text? text : "",
				icon : iconSrc? iconSrc : NotifyUtil.defaultIcon
				//tag : (tag) ? tag : ""
			});
		});

	}

};