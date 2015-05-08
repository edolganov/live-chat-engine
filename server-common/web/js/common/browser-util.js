

var BrowserUtil = {

	isTouchDevice: function(){
        try{
            document.createEvent("TouchEvent");
            return true;
        }catch(e){
            return false;
        }
    },

	getCurrentOS: function(){
		var appVersion = navigator.appVersion;
		if (appVersion.indexOf("Win")!=-1) return "windows";
		if (appVersion.indexOf("Mac")!=-1) return "mac";
		if (appVersion.indexOf("X11")!=-1) return "unix";
		if (appVersion.indexOf("Android")!=-1) return "android";
		if (appVersion.indexOf("Linux")!=-1) return "linux";

		return "unknown"
	},

	isNixOS: function(){
		var os = BrowserUtil.getCurrentOS();
		return os === "unix" || os === "linux";
	},

	isMac: function(){
		var os = BrowserUtil.getCurrentOS();
		return os === "mac";
	},

	isAndroid: function(){
		var os = BrowserUtil.getCurrentOS();
		return os === "android";
	},

	isIE: function(){
		var userAgent = navigator.userAgent;
		return $.browser.msie || (userAgent.indexOf("like Gecko") != -1 && userAgent.indexOf("rv") != -1);
	},

	isIE8: function(){
		return $.browser.msie && $.browser.version == 8;
	},

	isIE9: function(){
		return $.browser.msie && $.browser.version == 9;
	},

	isIE_8_9: function(){
		return BrowserUtil.isIE8() || BrowserUtil.isIE9();
	},

    isIE10: function(){
        return $.browser.msie && $.browser.version == 10;
    },

	isIPad: function(){
		return navigator.userAgent.match(/iPad/i) != null;
	},

	isIPhone: function (){
		return (
			//Detect iPhone
			(navigator.platform.indexOf("iPhone") != -1) ||
			//Detect iPod
			(navigator.platform.indexOf("iPod") != -1)
		);
	},

	isChrome: function(){
		return /chrome/.test(navigator.userAgent.toLowerCase());
	},

	isFirefox: function(){
		return /firefox/.test(navigator.userAgent.toLowerCase());
	},

	isOpera: function(){
		return $.browser.opera;
	},

	isOpera_12_less: function(){
		return BrowserUtil.isOpera() && BrowserUtil.getBrowserVersion() < 13;
	},

	isSafari: function(){
		return navigator.userAgent.indexOf("Safari") > -1;
	},

	isWebkit: function(){
		return $.browser.webkit;
	},

	isIOS: function(){
		var nP = navigator.platform;
		return nP == "iPad" || nP == "iPhone" || nP == "iPod" || nP == "iPhone Simulator" || nP == "iPad Simulator";
	},

	getBrowserVersion: function(){
		return $.browser.version;
	},

	reloadPage: function(){
		window.location.reload();
	},

	loadPage: function(url){
		window.open(url,"_self");
	},

	initIsWindowActiveListener: function(){

		window.isActive = true;
		
		window.onfocus = function () {
			window.isActive = true;
		};

		window.onblur = function () {
			window.isActive = false;
		};
	}

};