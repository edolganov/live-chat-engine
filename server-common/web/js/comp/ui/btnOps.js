
var BtnOps = {

	disableBtnWait: function(btn){

		if(btn.isDisabled()) return;

		var initHtml = btn.html();
		btn.data("initHtml", initHtml);
		btn.html(initHtml + " - "+Labels.get("common.inReq"));
		btn.disable();
	},

	enableBtn: function(btn){

		if(btn.isEnabled()) return;
		
		var initHtml = btn.data("initHtml");
		if(initHtml) btn.html(initHtml);
		btn.enable();
	},

	disableImgWait: function(img){
		if(img.isDisabled()) return;
		img.addClass("loading");
		img.disable();

		var loading = $("<div></div>").appendTo(img);
		loading.addClass("loadingMsg");
		loading.text(Labels.get("common.inReq"));
	},

	enableImg: function(img){
		if(img.isEnabled()) return;
		img.removeClass("loading");
		img.html("");
		img.enable();
	}

};