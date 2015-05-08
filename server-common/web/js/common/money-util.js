

MoneyUtil = {

    /**
     * Вернуть цену в формате ### ###,##
     * @param str строка с ценой
	 * @param opt доп параметры - {roundUp: true}
     */
    formatPrice: function(str, opt){
		opt = Util.extend({
			roundUp: false,
			separator: "."}
		, opt);

        var val = parseFloat(str);
		if(opt.roundUp) {
			val = Math.ceil(val);
		}
        var fixedVal = (""+val.toFixed(2));
        var parts = fixedVal.split(".");

        var firstPart = parts[0].split("").reverse().reduce(function(prevVal, curVal, i, array){
            return  curVal + (i && !(i % 3) ? " " : "") + prevVal;
        }, "");

		var hasFractionDigits = Util.parseInt(parts[1], 0) > 0;
        var formatted = firstPart + (hasFractionDigits? opt.separator+parts[1]: (opt.roundUp? '' : opt.separator+'00'));
        return formatted;
    }

};