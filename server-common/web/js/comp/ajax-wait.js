
//if has wait option - replace $.ajax
(function ajaxWaitFunc(){

	var urlParams = UrlUtil.parseDocumentUrlParams();
	if( ! urlParams.ajaxWait) return;

	var ajaxWait = parseInt(urlParams.ajaxWait);
	var log = LogFactory.getLog("AjaxWait");
	log.warn("Setup ajax wait time for "+ajaxWait+"ms");

	function initWaitProxy(opt, name){
		var realFunc = opt[name];
		if(!realFunc) return;
		opt[name] = function(){
			var args = arguments;
			setTimeout(function(){
				realFunc.apply(this, args);
			}, ajaxWait);
		}
	}

	var realAjaxFunc = $.ajax;
	$.ajax = function(opt){
		
		initWaitProxy(opt, "success");
		initWaitProxy(opt, "error");
		initWaitProxy(opt, "complete");
		
		realAjaxFunc(opt);
	};

}());