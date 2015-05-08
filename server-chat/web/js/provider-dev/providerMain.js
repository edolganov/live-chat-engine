
$(document).ready(function() {
	new ProviderMain().init();
});

function ProviderMain(){

	var parent =  window.parent;

	this.init = function(){

		if( ! parent) return;

		initParentListener();
		sendInitedToParent();
	};

	function initParentListener(){
		//messages from parent
		window.addEventListener('message', function(e){

			var origin = e.origin;
			//TODO check access;

			var msg = e.data;
			if(BrowserUtil.isIE_8_9()){
				msg = JSON.parse(msg);
			}
			
			if(Util.isEmpty(msg)) return;

			var data = msg.data;
			if(Util.isEmpty(data)) return;
			
			if(msg.type === "ajaxReq"){
				ajaxReq(data);
			}
		});
	}

	function ajaxReq(data){

		var ajaxOpt = data.req;
		var reqRef = data.reqRef;

		if(reqRef && ajaxOpt.data){
			ajaxOpt.data.origRef = reqRef;
		}
		
		var resp = {};
		resp.id = ajaxOpt.id;

		ajaxOpt.success = function(serverResp){
			resp.data = serverResp;
		};
		ajaxOpt.error = function(req, status, msg){
			resp.error = {status:status, msg:msg};
		};
		ajaxOpt.complete = function(req, status){
			resp.status = status;
			sendAjaxResp(resp);
		};
		$.ajax(ajaxOpt);
	}


	function sendInitedToParent(){
		postMessge({
			type: "inited"
		});
	}

	function sendAjaxResp(resp){
		postMessge({
			type: "ajaxResp",
			data: resp
		});
	}

	function postMessge(msg){
		if(BrowserUtil.isIE_8_9()){
			msg = JSON.stringify(msg);
		}
		parent.postMessage(msg, '*');
	}

}