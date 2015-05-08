

var ServerErrorCode = {
	UNKNOWN: "0",
	VALIDATION: "1",
	ACCESS_DENIED: "2"
};

var ServerErrorUtil = {

	getJsonErrorMsg: function(resp){
		var type = Util.isEmptyString(resp.type)? null : resp.type;
		var msg = Util.isEmptyString(resp.msg)? null : resp.msg;

		var errText = null;

		//label by type
		if( Util.isEmpty(errText) && type != null) errText = Labels.get(type, null);
		
		//label by msg
		if( Util.isEmpty(errText) && msg != null) {

			var prefix = "errorMsg: ";
			errText = Labels.get(prefix + msg, null);

			if(Util.isEmpty(errText)){
				var splits = msg.split(":");
				if(splits.length > 1) errText = Labels.get(prefix + splits[0], null);
				if(Util.isEmpty(errText) && splits.length > 1) errText = Labels.get(prefix + splits[0] + ":" + splits[1], null);
			}

			if(Util.isEmpty(errText) && resp.code == ServerErrorCode.VALIDATION) {
				errText = msg;
			}
		}

		//unknown exception
		if(Util.isEmpty(errText)){
			errText = "";
			if(type != null) errText += type +": ";
			if(msg != null) errText += msg;
		}

		return errText;
	},


	isNeedSignInError: function(errorData){
		if( ! errorData.ajax || ! errorData.resp) return false;
		return errorData.resp.code == ServerErrorCode.ACCESS_DENIED;
	}

};
