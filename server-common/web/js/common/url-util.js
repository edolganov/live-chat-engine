
var UrlUtil = {

	parseDocumentUrlParams: function(){
		return UrlUtil.parseUrlParams(document.URL);
	},

    /**
     * Переводит строку вида "url?param1=val1&param2=val2"
     * в объект {param1:val1, param2:val2}
     */
    parseUrlParams: function(str, opt){

        if(Util.isEmptyString(str)){
            return {};
        }

		opt = Util.extend({
			decode: true
		}, opt);

        var urlAndParams = str.split("?");
        var paramsStr = urlAndParams.length > 1? urlAndParams[1] : urlAndParams[0];

        var map = {};
        var params = paramsStr.split('&');
        for (var i = 0; i < params.length; ++i){
            var pair = params[i].split('=');
            var key = opt.decode ? decodeURIComponent(pair[0].replace(/\+/g, " ")) : pair[0];
            var value = pair.length > 1? (opt.decode ? decodeURIComponent(pair[1].replace(/\+/g, " ")) : pair[1]) : true;
            map[key] = value;
        }
        return map;

    },

	getUrlWithoutQuery: function(){
		return window.location.href.split('?')[0];
	}

};