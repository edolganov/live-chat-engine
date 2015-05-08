
var KeyUtil = {

	onCtrlEnter: function(input, fn){
		input.keydown(function(e){
			if(e.ctrlKey && e.keyCode == 13){
				fn();
			}
		});
	},

	onEnter: function(input, fn){
		input.keydown(function(e){
			if(e.keyCode == 13){
				fn();
			}
		});
	},

	onKeyDown: function(elem, key, fn){
		elem.keydown(function(e){
			if (e.keyCode == key) return fn();
			return true;
		});
	},

	onKeyUp: function(elem, key, fn){
		elem.keydown(function(e){
			if (e.keyCode == key) return fn();
			return true;
		});
	},

	onKeyPress: function(elem, key, fn){
		elem.keypress(function(e){
			if (e.keyCode == key) return fn();
			return true;
		});
	},

	onArrowUp: function(elem, fn){
		KeyUtil.onKeyPress(elem, 38, fn);
	},

	onArrowDown: function(elem, fn){
		KeyUtil.onKeyPress(elem, 40, fn);
	},

	onArrowLeft: function(elem, fn){
		KeyUtil.onKeyPress(elem, 37, fn);
	},

	onArrowRight: function(elem, fn){
		KeyUtil.onKeyUp(elem, 39, fn);
	}



};