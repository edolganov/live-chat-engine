
//Добавляем флаг браузера
if(window.BrowserUtil){

    function initHtmlClasses(){
        var html = $("html");
        if(BrowserUtil.isIE()) html.addClass("ie");
        if(BrowserUtil.isIE9()) html.addClass("ie9");
        if(BrowserUtil.isIE10()) html.addClass("ie10");
        if(BrowserUtil.isChrome()) html.addClass("chrome");
        if(BrowserUtil.isFirefox()) html.addClass("firefox");
        if(BrowserUtil.isOpera()) html.addClass("opera");
        if(BrowserUtil.isSafari()) html.addClass("safari");
        if(BrowserUtil.isMac()) {
            html.addClass("mac-os");
            if(BrowserUtil.isIPad()) {
                html.addClass("ipad");
                detectIpadVersionAsync(html);
            }
            else if(BrowserUtil.isIPhone()) html.addClass("iphone");
            else html.addClass("mac-pc");
        }
        if(BrowserUtil.isAndroid()){
            html.addClass("android");
        }

        var version = BrowserUtil.getBrowserVersion();
        if(version){
            var shortVersion = version;
            var splitsByDots = version.split(".");
            if(splitsByDots.length > 0) shortVersion = splitsByDots[0];
            html.addClass("ver-"+shortVersion);
        }

    }

    //from http://stackoverflow.com/questions/7400489
    //result will be set after initHtmlClasses() method end
    function detectIpadVersionAsync(html){
        window.ondevicemotion = function(event) {
            if (navigator.platform.indexOf("iPad") != -1) {
                var version = 1;
                if (event.acceleration) version += window.devicePixelRatio;
                html.addClass("ipad-"+version);
            }
            window.ondevicemotion = null;
        }
    }

    initHtmlClasses();

}



