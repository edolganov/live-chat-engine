
var GlobalMsgPopup = {

	initDefaultTheme: function(){
		//http://ned.im/noty/#layouts
		if($.noty){
			$.noty.defaults = Util.extend($.noty.defaults, {
				theme: 'notyStdTheme',
				dismissQueue: true,
				layout: 'topRight',
				timeout: 10000
			});
		}
	},

	showInvalidReqMsgForAnyError: function(errorData){
		if(errorData 
			&& errorData.ajax
			&& errorData.resp) GlobalMsgPopup.showJsonError(errorData.resp);
		else GlobalMsgPopup.showInvalidReqMsg();
	},

	showInvalidReqMsg: function(opt){
		opt = Util.extend({text: Labels.get("invalidReq", "Invalid request")}, opt);
		GlobalMsgPopup.showErrorMsg(opt);
	},

	showJsonError: function(resp, opt){
		var errText = ServerErrorUtil.getJsonErrorMsg(resp);
		opt = Util.extend({text: Labels.get("invalidReq", "Invalid request")+": "+errText}, opt);
		GlobalMsgPopup.showErrorMsg(opt);
	},

	showErrorMsg: function(opt){

		if(Util.isString(opt)) opt = {text:opt};

		opt = Util.extend({
			text: 'Error',
            type: 'error'
		}, opt);

		noty(opt);
	},

	showWarningMsg: function(opt){

		if(Util.isString(opt)) opt = {text:opt};

		opt = Util.extend({
			text: Labels.get("warnTitle", 'Warning'),
            type: 'warning'
		}, opt);

		noty(opt);

	},

	showInfoMsg: function(opt){

		if(Util.isString(opt)) opt = {text:opt};

		opt = Util.extend({
			text:  Labels.get("infoTitle", 'Info'),
            type: 'information'
		}, opt);

		noty(opt);

	}

};