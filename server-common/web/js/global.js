


var Global = {

	log: LogFactory.getLog("Global"),

	logServerError: function(resp, log){

		if(!log) log = Global.log;
		
		if(resp.code == ServerErrorCode.VALIDATION) {
			log.warn("Server Validation Error:\n"+resp.msg);
		} else {
			log.error("error get data from server: "+resp.msg);
		}
	},

	triggerGroup: function(type, groupId, attrs, skipLog){
		Global.trigger(type+"-"+groupId, attrs, skipLog);
	},

	trigger: function(type, attrs, skipLog){
		GlobalBindModel.trigger(type, attrs, skipLog);
	},

	bindGroup: function(checkStateElem, type, groupId, fn){
		Global.bind(checkStateElem, type+"-"+groupId, fn);
	},
	
	bind: function(checkStateElem, type, fn){
		GlobalBindModel.bind(checkStateElem, type, fn);
	}

};


var GlobalBindModel = new GlobalBindModelImpl();

function GlobalBindModelImpl(){

	var log = LogFactory.getLog("GlobalBindModelImpl");
	var byType = {};

	this.bind = function(checkStateElem, type, fn){

		if(!checkStateElem) checkStateElem = $("body");

		var listeners = byType[type];
		if(!listeners) {
			listeners = [];
			byType[type] = listeners;
			$("body").bind(type, createBindFn(type));
		}
		checkStateElem.data("bind-check", true);
		listeners.push({elem: checkStateElem, fn: fn});
	};

	this.trigger = function(type, attrs, skipLog){

		cleanDeadListeners(type);

		var listeners = byType[type];
		if(!listeners) return;

		if(!skipLog) Global.log.info("EVENT ["+type
			+", attrs="+Util.toString(attrs)
			+", listenersCount="+(listeners? listeners.length : 0)
			+"]");

		$("body").trigger(type, attrs);
		
	};

	function createBindFn(type){
		return function(){
			var listeners = byType[type];
			if(!listeners) return;
			var callThis = this;
			var args = arguments;
			$.each(listeners, function(i, curListener){
				curListener.fn.apply(callThis, args);
			});
		};
	}

	function cleanDeadListeners(type){

		var listeners = byType[type];
		if(!listeners) return;

		var filteredListeners = $.grep(listeners, function(curListener){
			var elem = curListener.elem;
			var valid = ! Util.isEmpty(elem) && elem.length > 0 && elem.data("bind-check") == true;
			if(!valid){
				log.warn("remove invalid listener for type="+type);
			}
			return valid;
		});
		byType[type] = filteredListeners;

	}

}