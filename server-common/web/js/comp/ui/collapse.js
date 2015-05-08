
/**
 * For accordion
 * @param opt - {[root || title, content], fadeIn}
 */
CollapseBlock = function(opt){

	opt = Util.extend({
		fadeIn: false,
		onClick: null
	}, opt);

	if(opt.root){
		initElems($("[collapseTitle]", opt.root), $("[collapseContent]", opt.root), opt);
	}
	else if(opt.title && opt.content){
		initElems(opt.title, opt.content, opt);
	}

	function initElems(title, content, param){
		title.click(function(){

			if(opt.onClick) opt.onClick();
			
			if(content.isVisible()){
				content.hide();
			} else {
				if(param.fadeIn){
					content.fadeIn();
				} else {
					content.show();
				}
			}
		});
	}

};