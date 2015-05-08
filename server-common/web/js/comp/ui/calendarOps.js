
var CalendarOps = {

	init: function(elem, ops){

		ops = Util.extend({
			date: new Date(),
			format  : 'd.m.Y'
		}, ops);

		elem.pickmeup_twitter_bootstrap(ops);
	},

	getDate: function(elem){
		return elem.pickmeup('get_date', false);
	}

}