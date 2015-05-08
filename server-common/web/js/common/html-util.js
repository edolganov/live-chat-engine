

$.fn.insertAt = function(index, $parent) {
    return this.each(function() {
        if (index === 0) {
            $parent.prepend(this);
        } else {
            $parent.children().eq(index - 1).after(this);
        }
    });
}

$.fn.disable = function(){
	HtmlUtil.disable(this);
	return this;
}

$.fn.enable = function(){
	HtmlUtil.enable(this);
	return this;
};

$.fn.setEnable = function(val){
	HtmlUtil.setEnable(this, val);
	return this;
};

$.fn.isEnabled = function(){
	return HtmlUtil.isEnabled(this);
};

$.fn.isDisabled = function(){
	return HtmlUtil.isDisabled(this);
};

$.fn.isVisible = function(){
	return HtmlUtil.isVisible(this);
};

$.fn.setVisible = function(val){
	if(val) this.show();
	else this.hide();
};

$.fn.selectOptionByIndex = function(index){
	HtmlUtil.selectOptionByIndex(this, index);
	return this;
};

$.fn.selectOptionByVal = function(val){
	HtmlUtil.selectOptionByVal(this, val);
	return this;
};


var HtmlUtil = {

    isVisible: function(elem){
        return elem.is(":visible");
    },

	isVisibleNotRecursive: function(elem){
		return ! elem.is(':hidden') && ! elem.parents(':hidden').length;
	},

    isInFocus: function(elem){
        return elem.is(":focus");
    },

    /**
     * @param {jQuery elem} elem
     * @retrun {top, left}
     */
    getOffsetRect: function(elem) {
        //from http://javascript.ru/ui/offset
        if( ! Util.isEmpty(elem[0])){
            elem = elem[0];
        }

        var box = elem.getBoundingClientRect();

        var body = document.body;
        var docElem = document.documentElement;

        var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
        var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;

        var clientTop = docElem.clientTop || body.clientTop || 0;
        var clientLeft = docElem.clientLeft || body.clientLeft || 0;

        var top  = box.top +  scrollTop - clientTop;
        var left = box.left + scrollLeft - clientLeft;

        return {top: Math.round(top), left: Math.round(left)};
    },

    disable: function(elem){
        elem.attr("disabled", "disabled");
        elem.addClass("disabled");

    },

    enable: function(elem){
        elem.attr("disabled", false);
        elem.removeClass("disabled");
    },

    setEnable: function(elem, value){
        if(value){
            HtmlUtil.enable(elem);
        } else {
            HtmlUtil.disable(elem);
        }
    },

    isEnabled: function(elem){
        return elem.attr("disabled") != "disabled";
    },

    isDisabled: function(elem){
        return ! HtmlUtil.isEnabled(elem);
    },

	isChecked: function(elem){
		return elem.is(':checked');
	},

	setChecked: function(elem, val){
		elem.prop('checked', val);
	},
	

    //from http://stackoverflow.com/questions/8840580/force-dom-redraw-refresh-on-chrome-mac
    forceRedraw: function(elem){

        var parent = elem.parent();
        var classId = "stub-elem-for-refresh-DOM";
        var stubElem = $("."+classId, parent);
        if(stubElem.length == 0){
            parent.append("<div class='"+classId+"'style='height:0px; width:0px;'></div>");
            stubElem = $("."+classId, parent);
        }

        stubElem.hide();

        var timeout = 20; // you can play with this timeout to make it as short as possible
        setTimeout(function(){
            stubElem.show();
        },timeout);
    },

    setHideImportantStyle: function(elem, val){
        if(val){
            elem.attr("style", "display:none !important");
        } else {
			HtmlUtil.removeStyle(elem, "display");
        }
    },

	setVisibleByOpacityStyle: function(elem, val){
		if(val){
			HtmlUtil.removeStyle(elem, "opacity");
		} else {
			elem.css("opacity", "0");
		}
	},

	removeStyle: function(elem, styleName){
		elem.each(function(){
			this.style.removeProperty(styleName);
		});
	},

   /**
     * Оповещать, если произошло какое-либо событие с инпут полем
     * @param inputElem
     * @param onChangeFunc
     */
    onAllInputChanges: function(inputElem, onChangeFunc){

        var dataKey = "old-input-val";
        var proxyKey = "onAllInputChangesProxy";

        var initVal = inputElem.val();
        inputElem.data(dataKey, initVal);

        var changeProxy = inputElem.data(proxyKey);
        if( ! changeProxy){

            //не оповещяем лишний раз, если значение не изменилось
            changeProxy = function(){
                var newVal = inputElem.val();
                var oldVal = inputElem.data(dataKey);
                if(newVal != oldVal){
                    //LogFactory.getLog("onAllInputChanges").debug("update data. oldVal="+oldVal+", newVal="+newVal);
                    inputElem.data(dataKey, newVal);
                    $.each(changeProxy._realFunc, function(i, fn){
                       fn(oldVal, newVal);
                    });
                }
            }
            changeProxy._realFunc = [];
            inputElem.data(proxyKey, changeProxy);

            inputElem.keypress(changeProxy);
            inputElem.change(changeProxy);
            inputElem.blur(changeProxy);
            inputElem.bind('input', changeProxy); //для автоподстановки значения браузером
            inputElem.bind('paste', function() {
                setTimeout(changeProxy, 100);
            });
        }
        changeProxy._realFunc.push(onChangeFunc);
    },

    onEnterPress: function(elem, listenerFunc){
        elem.keypress(function(e){
            if(e.keyCode == 13){
                listenerFunc(e);
            }
        });
    },

    preventPostFormByEnter: function(inputElem){
        HtmlUtil.onEnterPress(inputElem, function(e){
            e.preventDefault();
        });
    },

	resizeTextArea: function(textArea){
		var elem = textArea[0];
		elem.style.height = "1px";
		elem.style.height = (25+elem.scrollHeight)+"px";
	},

	resizeTextAreaOnChanges: function(textArea){
		HtmlUtil.onAllInputChanges(textArea, function(){
			HtmlUtil.resizeTextArea(textArea);
		});
	},

	scrollOnPageTop: function(slow){
		var scrollAnimation = slow? 'slow' : 0;
		$('html, body').animate({ scrollTop: 0 }, scrollAnimation);
	},

	scrollToBottom: function(elem){
		elem.scrollTop(elem[0].scrollHeight);
	},

	scrollOnTop: function(elem){
		elem.scrollTop(0);
	},

	scrollOn: function(elem, val){
		elem.scrollTop(elem.scrollTop()+val);
	},

	scrollIntoView: function(elem, toTop){
		if(Util.isEmpty(toTop)) toTop = true;
		elem[0].scrollIntoView(toTop);
	},

	getScrollHeight: function(elem){
		return elem[0].scrollHeight;
	},

	showAndHide: function(elem, showTime, hideTime){
		if(Util.isEmpty(hideTime)) hideTime = showTime;
		elem.show(showTime, function(){
			elem.hide(hideTime);
		});
	},

	fadeInAndOut: function(elem, showTime, hideTime){
		if(Util.isEmpty(hideTime)) hideTime = showTime;
		elem.fadeIn(showTime, function(){
			elem.fadeOut(hideTime);
		});
	},

	isAttached: function(elem){
		return $.contains(document, elem[0]);
	},


	//
	//select ops
	//
	selectOptionByIndex: function(selectElem, index){
		selectElem.find("option").each(function(i, option){
			option = $(option);
			if(i != index) option.removeAttr("selected");
			else option.attr("selected", "selected");
		});
	},

	selectOptionByVal: function(selectElem, val){
		var selected = false;
		selectElem.find("option").each(function(i, option){
			option = $(option);
			
			if(selected) {
				option.removeAttr("selected");
				return;
			}

			var curVal = option.attr("value");
			if(curVal == val){
				option.attr("selected", "selected");
				selected = true;
				return;
			}
			
			option.removeAttr("selected");
		});
	},

	getSelectedOption: function(selectElem){
		return $("option:selected", selectElem);
	},

	getSelectedOptionVal: function(selectElem){
		return HtmlUtil.getSelectedOption(selectElem).val();
	},

	deselectAllOptions: function(selectElem){
		HtmlUtil.getSelectedOption(selectElem).each(function(){
			$(this).removeAttr("selected");
		});
	},

	removeOptionByVal: function(selectElem, val){
		var option = selectElem.find("option[value="+val+"]");
		option.remove();
	},

	getOptionByVal: function(selectElem, val){
		return selectElem.find("option[value="+val+"]");
	},

	getOptions: function(selectElem){
		return selectElem.find("option");
	},

	sortOptions: function(selectElem, sortFunc){
		var options = HtmlUtil.getOptions(selectElem);
		options.sort(function(a, b){
			return sortFunc($(a), $(b));
		});
		selectElem.append(options);
	},




	showTooltip: function(elem, title, opt){
		
		opt = Util.extend({
			placement: "bottom",
			showTime: 1500
		}, opt);

		elem.tooltip({
			title: title,
			placement: opt.placement,
			trigger: "manual"
		});
		elem.tooltip('show');
		setTimeout(function(){
			elem.tooltip('hide');
		}, opt.showTime);
	},


	createRootReqElem: function(name, nameIsId){

		var id = nameIsId? name : name+"-"+Util.generateUid();

		var oldElem = $("#"+id);
		if(oldElem.length > 0) return oldElem;

		var elem = $("<span id='"+id+"'></span>").appendTo($("body"));
		elem.hide();
		return elem;
	},


	switchTexts: function(elemA, elemB){
		var oldText = elemA.text();
		var newText = elemB.text();
		elemA.text(newText);
		elemB.text(oldText);
	},

	getRadioCheckedVal: function(root, radioName){
		return $('input[name='+radioName+']:checked', root).val();
	}

};