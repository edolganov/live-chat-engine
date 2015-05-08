
$(function(){
	var app = new DemoLook();
	app.init();
	window.demoApp = app;
});

var oChatProps = window.oChatProps? window.oChatProps : {};
oChatProps.onLoaded = function(chat){
	setTimeout(function(){
		if(window.demoApp) window.demoApp.initChat(chat);
	}, 0);
};


function DemoLook(){

	var frame = $("#siteFrame");
	var input = $("#selectSite");
	var btn = $("#showSite");

	var defaultUrl = "wikipedia.org";

	this.init = function(){

		Labels.initGlobalLabels(["DemoLabels"]);

		var params = UrlUtil.parseDocumentUrlParams();
		var initUrl = params.url? params.url : defaultUrl;

		input.val(initUrl);
		updateFrame();

		KeyUtil.onEnter(input, updateFrame);
		btn.click(updateFrame);
	};

	function updateFrame(){

		var url = input.val();
		if(!url) url = defaultUrl;

		var frameUrl = url;
		if( ! frameUrl.startsWith("http://") || frameUrl.startsWith("https://")){
			frameUrl = "http://" + frameUrl;
		}
		
		frame.attr("src", frameUrl);

		try {
			var newUrl = UrlUtil.getUrlWithoutQuery() + "?url="+url;
			window.history.replaceState('newPage', document.title, newUrl);
		}catch(e){}
	}


	this.initChat = function(chat){
		chat.show();

		var opName = Labels.get("opName");

		chat.appendLocalMsg(Labels.get("msg1.text"), {isUser: true});
		chat.appendLocalMsg(Labels.get("msg2.text"), {operatorNick:opName});

		chat.beforeSend(function(text){
			chat.appendLocalMsg(text, {isUser: true, noScroll: false});
			setTimeout(function(){
				chat.appendLocalMsg(Labels.get("msg3.text"), {operatorNick:opName, noScroll: false});
			}, 1000);
		});

	};

}