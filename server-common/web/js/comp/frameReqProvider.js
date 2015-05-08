

var FrameReqProvidersMap = {};

function FrameReqProvider(serverUrl, frameUri){

	var self = this;
	var log = LogFactory.getLog("FrameReqProvider["+serverUrl+"]@"+Util.generateUid());

	var oldAjaxFunc = $.ajax;
	var inited = false;
	var shutdown = false;

	var frame;
	var frameWin;
	var waitInitReqs = [];
	var reqsInProcess = {};
	var reqRef;

	this.init = function(){

		//shutdown old proxy
		var oldProvider = FrameReqProvidersMap[serverUrl];
		if( ! Util.isEmpty(oldProvider)){
			oldProvider.shutdown();
		}
		FrameReqProvidersMap[serverUrl] = self;


		//init proxy
		$.ajax = ajaxProxy;

		initFrameListener();
		initFrame();

	};


	this.shutdown = function(){
		shutdown = true;
		if(frame) {
			frame.remove();
		}
	};

	this.setReqRef = function(ref){
		reqRef = ref;
	};

	function initFrameListener(){

		//messages from frame
		window.addEventListener('message', function(e){
			if(shutdown) {
				return;
			}
			if ( ! Util.startsWith(e.origin, serverUrl)) {
				return;
			}
			var msg = e.data;
			if(BrowserUtil.isIE_8_9()){
				msg = JSON.parse(msg);
			}

			if(msg.type === "inited"){
				initProxy();
			}
			else if(msg.type === "ajaxResp"){
				processResp(msg.data);
			}
		});

		//check for error
		setTimeout(function(){
			if(!inited){
				self.shutdown();
				errorConnectToWaitReqs();
			}
		}, 5000);
	}

	function initFrame(){
		frame = $("<iframe></iframe>");
		frame.attr("src", serverUrl + frameUri);
		frame.hide();
		$("body").append(frame);
	}


	var ajaxProxy = function(ajaxOpt){

		if(shutdown) {
			oldAjaxFunc(ajaxOpt);
			return;
		}

		var url = ajaxOpt.url;
		if( ! Util.startsWith(url, serverUrl)){
			oldAjaxFunc(ajaxOpt);
			return;
		}

		if(!inited){
			waitInitReqs.push(ajaxOpt);
			return;
		}

		//send to frame
		if(ajaxOpt.beforeSend) ajaxOpt.beforeSend();
		sendReqToFrame(ajaxOpt);

	};


	function initProxy(){
		if(inited) return;

		frameWin = frame[0].contentWindow;
		inited = true;
		log.info("inited");

		flushWaitReqs();
	}

	function errorConnectToWaitReqs(){
		log.info("cancel reqs with error connect");
		$.each(waitInitReqs, function(i, ajaxOpt){
			try {
				var status = "error connect";
				if(ajaxOpt.error) ajaxOpt.error(null, status, null);
				if(ajaxOpt.complete) ajaxOpt.complete(null, status);
			}catch(e){
				log.error("can't errorConnectToWaitReqs", e);
			}
		});
	}


	function flushWaitReqs(){
		$.each(waitInitReqs, function(i, ajaxOpt){
			try {
				ajaxProxy(ajaxOpt);
			}catch(e){
				log.error("can't flushWaitReq", e);
			}
		});
	}


	function sendReqToFrame(ajaxOpt){

		var id = Util.generateUid();
		reqsInProcess[id] = ajaxOpt;

		var sendReq = {};
		sendReq.id = id;
		if(ajaxOpt.url) sendReq.url = ajaxOpt.url;
		if(ajaxOpt.data) sendReq.data = ajaxOpt.data;
		if(ajaxOpt.type) sendReq.type = ajaxOpt.type;
		if(ajaxOpt.dataType) sendReq.dataType = ajaxOpt.dataType;

		var msg = {
			type: "ajaxReq",
			data: {req: sendReq, reqRef:reqRef}
		};
		if(BrowserUtil.isIE_8_9()){
			msg = JSON.stringify(msg);
		}
		frameWin.postMessage(msg, serverUrl);
	}

	function processResp(resp){
		var id = resp.id;
		var ajaxOpt = reqsInProcess[id];
		delete reqsInProcess[id];

		if(Util.isEmpty(ajaxOpt)) return;

		if( ! Util.isEmpty(resp.error)){
			if(ajaxOpt.error) ajaxOpt.error(null, resp.error.status, resp.error.msg);
		} else {
			if(ajaxOpt.success) ajaxOpt.success(resp.data);
		}
		if(ajaxOpt.complete) ajaxOpt.complete(null, resp.status);

	}


}