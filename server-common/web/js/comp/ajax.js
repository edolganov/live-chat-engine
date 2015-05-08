

var AjaxSetup = {

	error: function(reqObj, status, errorThrown){
		//LogFactory.getLog("Ajax").error("ajax failed: status='"+status+"', responseText='"+(reqObj? reqObj.responseText : "")+"'");
	},

	customJsonError: null,
	customError: null,

	alwaysXhrFields: false
};

$.ajaxSetup({
	error: AjaxSetup.error
});



var Ajax = {

    log: LogFactory.getLog("Ajax"),

	CSRF_ProtectToken: null,
	init_CSRF_ProtectTokenFromDOM: function(){
		Ajax.set_CSRF_ProtectToken($("#CSRF_ProtectToken").attr("p-val"));
	},
	set_CSRF_ProtectToken: function(token){
		if( ! Util.isEmpty(token)) {
			Ajax.CSRF_ProtectToken = token;
		}
	},

    /**
	 * <pre>
	 * 	Ajax.proxyJsonPost(reqElem, {
	 *		url: '/some',
	 *		data: {},
	 *		beforeSend: function(){},
	 *		success: function(data){},
	 *		anyError: function({ajax, [req, status, text] | [resp]}),
	 *		complete: function(req, status){},
	 *		jsonError: function(resp),
	 *		error: function(req, status, error){}
	 *		
	 *		//extra
	 *		name: null,
	 *		singleReqDelay: 0,
	 *		singleReq: false
	 *		extraData: null,
	 *		secure: false,
	 *		useCustomErrors: false,
	 *	});
	 * </pre>
     * @param reqOwnerElem - jQuery элемент для хранения текущей версии запроса
     * @param ajaxOpt - опции запроса для $.ajax + доп. опции:
	 * <br>{url*, data, beforeSend, success, complete, anyError, jsonError, error, parseRespStatus, singleReq}
     */
	proxyJsonPost: function(reqOwnerElem, ajaxOpt){

		if(!ajaxOpt) {
			ajaxOpt = reqOwnerElem;
			reqOwnerElem = null;
		}
		ajaxOpt = Util.extend({
			secure: false,
			type: "POST",
			dataType : 'json',
			data: null,
			extraData: {}
		}, ajaxOpt);

		ajaxOpt.data = {data: JSON.stringify(ajaxOpt.data)};
		ajaxOpt.data = Util.extend(ajaxOpt.data, ajaxOpt.extraData);

		if( ! Util.isEmpty(Ajax.CSRF_ProtectToken)){
			ajaxOpt.data.token = Ajax.CSRF_ProtectToken;
		}

		if(ajaxOpt.secure){
			var httpsHost = Global.httpsUrl;
			if( ! Util.isEmpty(httpsHost)) {
				ajaxOpt.url = httpsHost + ajaxOpt.url;
				ajaxOpt.xhrFields = {withCredentials: true};
			}
		}

		Ajax.proxyAjax(reqOwnerElem, ajaxOpt);
	},

    /**
     * Ajax запрос с проверкой просроченности результата,
     * если был последующий запрос. Если ответ просрочен,
     * то не будут вызваны методы success или error
	 *
	 * <pre>
	 * 	Ajax.proxyAjax(reqElem, {
	 *		url: '/some',
	 *		data: {},
	 *		beforeSend: function(){},
	 *		success: function(data){},
	 *		anyError: function({ajax, [req, status, text] | [resp]}),
	 *		complete: function(req, status){},
	 *		jsonError: function(resp),
	 *		error: function(req, status, error){}
	 *
	 *		//extra
	 *		name: null,
	 *		singleReqDelay: 0,
	 *		singleReq: false,
	 *		parseRespStatus: true,
	 *		type: "POST",
	 *		dataType : 'json',
	 *		useCustomErrors: false,
	 *	});
	 * </pre>
	 *
     * @param reqOwnerElem - jQuery элемент для хранения текущей версии запроса
     * @param ajaxOpt - опции запроса для $.ajax + доп. опции:
	 * <br>{url*, type, data, dataType,
	 * <br> beforeSend, success, complete, anyError, jsonError, error, parseRespStatus, singleReq}
     */
    proxyAjax: function(reqOwnerElem, ajaxOpt){

		if(!ajaxOpt) {
			ajaxOpt = reqOwnerElem;
			reqOwnerElem = $("body");
		}
		if(!reqOwnerElem){
			reqOwnerElem = $("body");
		}

		ajaxOpt = Util.extend({
			name: null,
			parseRespStatus: true,
			jsonError: null,
			anyError: null,
			singleReqDelay: 0,
			singleReq: false,
			useCustomErrors: false
		}, ajaxOpt);

		if(AjaxSetup.alwaysXhrFields){
			ajaxOpt.xhrFields = {withCredentials: true};
		}
		ajaxOpt.getLogName = function(){
			return ajaxOpt.name? " ("+ajaxOpt.name+")" : "";
		};



		var callFunc = function(){
			//check if single req
			var curReqId = reqOwnerElem.data("reqKey");
			if(ajaxOpt.singleReq && ! Util.isEmpty(curReqId)){
				Ajax.log.warn("Cancel other req while exists single req "+curReqId + ajaxOpt.getLogName()+" for "+ajaxOpt.url);
				return;
			}

			var reqId = Util.generateUid();
			reqOwnerElem.data("reqKey", reqId);

			function notOverdueResp(){
				var curReqId = reqOwnerElem.data("reqKey");
				return curReqId == reqId;
			}

			function clearReq(){
				reqOwnerElem.data("reqKey", null);
			}

			var canCallComplete = false;

			var originalSuccess = ajaxOpt.success;
			ajaxOpt.success = function(resp){
				if(notOverdueResp()){
					clearReq();
					canCallComplete = true;
					//Ajax.log.debug("Call success resp "+reqId);

					var isJson = resp && ! Util.isString(resp);

					if(isJson && ajaxOpt.parseRespStatus && resp.status){
						if(resp.status == "error"){

							if(ajaxOpt.jsonError) ajaxOpt.jsonError(resp);
							else if(ajaxOpt.useCustomErrors && AjaxSetup.customJsonError) AjaxSetup.customJsonError(resp);
							else Global.logServerError(resp);

							if(ajaxOpt.anyError) ajaxOpt.anyError({ajax:true, resp:resp});

							return;
						}
					}
					if(originalSuccess) originalSuccess(isJson? resp.data : resp);

				} else {
					Ajax.log.warn("Cancel overdue success resp "+reqId + ajaxOpt.getLogName()+" for "+ajaxOpt.url);
				}
			};

			var originalError = ajaxOpt.error;
			ajaxOpt.error = function(request, status, error){
				if(notOverdueResp()){
					clearReq();
					canCallComplete = true;
					//Ajax.log.debug("Call error resp "+reqId);
					var defErrHandler = function(){
						AjaxSetup.error(request, status, error);
					};
					if(originalError) originalError(request, status, error, defErrHandler);
					else if(ajaxOpt.useCustomErrors && AjaxSetup.customError) AjaxSetup.customError(request, status, error, defErrHandler);
					else defErrHandler();

					if(ajaxOpt.anyError) ajaxOpt.anyError({ajax:false, req:request, status:status, text:error});
				} else {
					Ajax.log.warn("Cancel overdue error resp "+reqId + ajaxOpt.getLogName()+" for "+ajaxOpt.url);
				}
			}
			var originalComplete = ajaxOpt.complete;
			ajaxOpt.complete = function(req, status){
				Global.trigger(Msg.ajax_After, null, true);
				if(canCallComplete && originalComplete){
					originalComplete(req, status);
				}
			}

			//Ajax.log.debug("Start req "+reqId);
			Global.trigger(Msg.ajax_Before, null, true);
			$.ajax(ajaxOpt);
		};


		if(ajaxOpt.singleReqDelay > 0){

			var delayId = "delay-"+Util.generateUid();
			reqOwnerElem.data("delayId", delayId);
			setTimeout(function(){
				var curDelayId = reqOwnerElem.data("delayId");
				if(curDelayId == delayId){
					callFunc();
				} else {
					Ajax.log.warn("Cancel invalid delayed ajax call" + ajaxOpt.getLogName()+" for "+ajaxOpt.url);
				}
			}, ajaxOpt.singleReqDelay);

		} else {
			callFunc();
		}



    },

    cancelAjax: function(reqOwnerElem){

        if(Util.isEmpty(reqOwnerElem)){
            reqOwnerElem = $("body");
        }

        var curReqId = reqOwnerElem.data("reqKey");
        if( ! Util.isEmpty(curReqId)){
            Ajax.log.warn("Cancel cur req "+curReqId);
            reqOwnerElem.data("reqKey", null);
        }
    },

    isInRequest: function(reqOwnerElem){
        return ! Util.isEmpty(reqOwnerElem.data("reqKey"));
    }

};