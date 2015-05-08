

$(document).ready(function() {
	new AppMain().init();
});

function AppMain(){

	var oldChatObj;
	
	this.init = function(){

		Global.clientProps = Util.extend({
			showStartPanel: true,
			chatPosition: "bottom",
			chatPositionFixed: true
		}, window.oChatProps);
		
		if(Util.isEmpty(Global.clientProps.id))
			throw new Error("Empty id in oChatProps obj: Setup your oChat ID by code oChatProps['id']='YOUR_OCHAT_ID'");

		if(Global.clientProps.id === "demoMode"){
			Global.demoMode = true;
		}

		Global.url = Util.isHttps()? Global.httpsUrl : Global.httpUrl;

		var labels = {};
		try {
			var lang = Global.clientProps.lang;
			if(!lang) lang = "en";
			if(Labels && Labels[lang]) labels = Labels[lang];
		}catch(e){}
		
		Global.labels = Util.extend(labels, Global.clientProps.labels);


		var refMaxSize = 2000;
		var ref = null;
		if(window.document) ref = document.URL;
		if(!ref && window.location) ref = window.location.host;
		if(ref && ref.length > refMaxSize) ref = Util.substr(ref, refMaxSize).val;
		Global.ref = ref;



		//layout
		var layout = $("<div id='oChatTemplates' style='display:none;'></div>");
		layout.hide();
		layout.append(LayoutContent);
		Global.layout = layout;

		//css
		var cssName = "app-def.css";
		if("gray" === Global.clientProps.theme) cssName = "app-gray.css";
		else if("orange" === Global.clientProps.theme) cssName = "app-orange.css";
		else if("purple" === Global.clientProps.theme) cssName = "app-purple.css";

		$("<link>", {
			rel: "stylesheet",
			type: "text/css",
			href: Global.url + "/css/"+cssName
		}).appendTo('head');

		var customCss = Global.clientProps.css;
		if( ! Util.isEmptyString(customCss)){
			$("<link>", {
				rel: "stylesheet",
				type: "text/css",
				href: customCss
			}).appendTo('head');
		}


		//old chat obj
		oldChatObj = window.oChat;


		//start app
		var chat = new Chat();
		window.oChat = chat;

		if(Global.clientProps.showStartPanel){
			Global.startPanel = new StartPanel(chat);
			Global.startPanel.init();
		}

		
		if(Global.clientProps.onLoaded){
			try {
				Global.clientProps.onLoaded(chat);
			}catch(e){}
		}

	};
}

