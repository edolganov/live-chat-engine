

function ChatProvider(chatUrl, reqRef){

	var provider = new FrameReqProvider(chatUrl, "/chat/provider.html?ajaxWait=500");

	this.init = function(){
		provider.init();
		provider.setReqRef(reqRef);
	};
}