
var CookieUtil = {

	getCookieVal: function(key){
		return $.cookie(key);
	},

	/**
	 * <pre>
	 *	opt.expires - Date
	 *	opt.path - Str
	 *	opt.domain - Str
	 *	opt.secure - Bool
	 * </pre>
	 */
	addCookie: function(key, val, opt){
		$.cookie(key, val, opt);
	}

};