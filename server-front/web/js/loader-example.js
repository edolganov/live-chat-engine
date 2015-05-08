

(function() {

	window.oChatProps = window.oChatProps? window.oChatProps : {};

    var s = document.createElement('script');
    s.type = 'text/javascript';
    s.async = true;
	var srcVal = "";
	if('https:' == document.location.protocol) srcVal = "https://127.0.0.1:10643";
	else srcVal = "http://127.0.0.1:10280";
	s.src = srcVal + "/chat/app.js"
    var o = document.getElementsByTagName('script')[0];
    o.parentNode.insertBefore(s, o);
})();

oChatProps["id"] = "123";