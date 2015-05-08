

var Util = {

    isEmpty: function(ob){
        return ob === null? true : (ob === undefined? true: false);
    },

    isEmptyString: function(str){
        return Util.isEmpty(str) || str.length === 0 || str.trim().length === 0;
    },

    isEmptyArray: function(ob){
        return Util.isEmpty(ob) || ob.length === 0;
    },

    checkArgumentForEmpty: function(arg, errorMsg){
        if(Util.isEmpty(arg)){
            throw new Error("Invalid argument: "+errorMsg);
        }
    },

    checkArgument: function(condition, errorMsg){
        if(!condition){
            throw new Error("Invalid argument: "+errorMsg);
        }
    },

    /**
     * Convert Firefox stacktrace object to String
     * @param {FF stacktrace object} stack
     */
    stackToString: function(stack){
        var out = "";
        var lines = stack.split('@');
        jQuery.each(lines, function(index){
            var line = lines[index];
            out = out+"\n"+line;
        });
        return out;
    },

    /**
     * @param {object} ex
     */
    exceptionToString: function(ex){
        if(Util.isEmpty(ex)){
            return "Undefined exception";
        }

        var msg = "";
        if(ex.javaClassName){
            msg += "\nExceptionClass: "+ex.javaClassName;
        }

        if(ex.message){
            msg += "\nMessage: "+ex.message;
        }

        if(ex.stack){
            msg += "\nStack: "+Util.stackToString(ex.stack);
        }

        if(msg.length === 0){
            msg = ""+ex;
        }

        return "Exception: "+msg;
    },

    extend: function(target, otherObject){
        if(Util.isEmpty(otherObject)){
            otherObject = {};
        }

        return $.extend(target, otherObject);
    },

    generateUid: function(){
        var randomSuffix = "";
        for(var i = 1; i < 5; i++){
            randomSuffix = randomSuffix + (Math.floor(Math.random()*10));
        }
        var timestamp = ""+new Date().getTime();
        var firstPart = timestamp.substring(0, timestamp.length-3);
        var secondPart = timestamp.substring(timestamp.length-3, timestamp.length);
        return firstPart+"-"+secondPart+"-"+randomSuffix;
    },

    /**
     * @param source
     * @param deep - optional (default - true)
     */
    clone: function(source, deep){
        if(Util.isEmpty(source)){
            return null;
        }

        if(Util.isEmpty(deep)){
            deep = true;
        }

        if($.isArray(source)){
            var result = [];
            $.each(source, function(i, item){
                result.push(Util.clone(item, deep));
            });
            return result;
        }else{
            return $.extend(deep,{},source);
        }
    },

    inherit: function(ChildType, ParentType) {
		var F = function() { };
		F.prototype = ParentType.prototype;
		ChildType.prototype = new F();
		ChildType.prototype.constructor = ChildType;
		ChildType.superclass = ParentType.prototype;
    },

    substr: function(src, maxSize){
      src = ""+src;
      var out = src.substr(0, maxSize);
      var isShort = false;
      if(out.length < src.length){
          out = out + "...";
          isShort = true;
      }

	  var res = {val:out, isShort:isShort};
	  res.toString = function(){
		  return this.val;
	  }
      return res;
    },


    getValue: function(obj, exp){

        if(Util.isEmpty(obj)){
            return null;
        }

        var fields = exp.split(".");
        var curObj = obj;
        for(var i=0; i<fields.length; i++){
            var field = fields[i];
            var curValue = curObj[field];
            if(Util.isEmpty(curValue)){
                return null;
            } else {
                curObj = curValue;
            }
        }
        return curObj;

    },

    deleteField: function(obj, fieldName){
        var value = obj[fieldName];
        delete obj[fieldName];
        return value;
    },


    toString: function(obj, deep){
        if(deep){
            if(JSON && JSON.stringify){
                return JSON.stringify(obj);
            } else {
                return ""+obj; //dummy impl
            }
        }


        if(typeof obj == 'number'){
            return ""+obj;
        }

        //onde level string
        var outStr = "";
        if( ! Util.isEmpty(obj)){
            outStr = "{";
            var first = true;
            $.each(obj, function(name, value){
                if(first){
                    first = false;
                } else {
                    outStr += ", ";
                }
                outStr += name+":"+value;
            });
            outStr += "}";
        } else {
            outStr = "null";
        }
        return outStr;
    },

    removeTags: function(str) {
        str = str.replace(/["\'][\s]*javascript:(.*)["\']/gi, "");
        str = str.replace(/script(.*)/gi, "");
        str = str.replace(/eval\((.*)\)/gi, "");
        return str;
    },

    cloneById: function(id, opt){

		opt = Util.extend({
			root: null
		}, opt);

		var elem = opt.root ? $("#"+id, opt.root) : $("#"+id);
		if(elem.length === 0) throw new Error("can't find template for clone by id='"+id+"'");
        return Util.cloneByElem(elem, opt);
    },

    cloneByElem: function(elem, opt){

        opt = Util.extend({
            hide:false,
            id:null
        }, opt);

        if(Util.isEmpty(elem) || elem.length === 0){
            throw new Error("can't find template for clone: "+elem);
        }

        var cloneElem = elem.clone();
        if(opt.id) cloneElem.attr("id",opt.id);
		else cloneElem.removeAttr("id");

        if( ! opt.hide){
            cloneElem.show();
        } else {
            cloneElem.hide();
        }

        cloneElem.bindedUI = Bind.createUI(cloneElem);
        return cloneElem;

    },

	append: function(elem, content){
		return elem.append(content).children().last();
	},

	_escapeHtmlMap: {
		"&": "&amp;",
		"<": "&lt;",
		">": "&gt;",
		'"': '&quot;',
		"'": '&#39;',
		"/": '&#x2F;'
	},

	escapeHtml: function(str){
		return String(str).replace(/[&<>"'\/]/g, function (s) {
			return Util._escapeHtmlMap[s];
		});
	},

	unescapeHtml: function(str, ignoreKeys){
		if(Util.isEmptyString(str)) return str;

		var useIgnore = ! Util.isEmptyArray(ignoreKeys);
		var ignoreMap = {};
		$.each(ignoreKeys, function(i, elem){
			ignoreMap[elem] = true;
		});

		$.each(Util._escapeHtmlMap, function(key, value){
			if(!useIgnore || Util.isEmpty(ignoreMap[key])) str = str.replaceAll(value, key);
		})
		return str;
	},

	/**
	 * Returns a random DOUBLE number between min and max
	 */
	getRandomArbitary: function(min, max) {
	  return Math.random() * (max - min) + min;
	},

	/**
	 * Returns a random integer between min and max
	 */
	getRandomInt: function(min, max) {
		return Math.floor(Math.random() * (max - min + 1)) + min;
	},

	isHttps: function(){
		return 'https:' == document.location.protocol;
	},


	trim: function(str) {
		return str.replace(/^\s+|\s+$/g, '');
	},

	contains: function(str, it) {
		return str.indexOf(it) != -1;
	},

	startsWith: function (str, subStr){
		return str.indexOf(subStr) === 0;
	},

	endsWith: function (str, subStr){
		return this.slice(-subStr.length) == subStr;
	},

	//пример: "okay.this.is.a.string".replaceAll('.', ' ')
	replaceAll: function(str, token, newToken, ignoreCase) {
		var i = -1, _token;
		if(typeof token === "string") {
			_token = ignoreCase === true? token.toLowerCase() : undefined;
			while((i = (
				_token !== undefined?
					str.toLowerCase().indexOf(
								_token,
								i >= 0? i + newToken.length : 0
					) : str.indexOf(
								token,
								i >= 0? i + newToken.length : 0
					)
			)) !== -1 ) {
				str = str.substring(0, i)
						.concat(newToken)
						.concat(str.substring(i + token.length));
			}
		}
		return str;
	},

	ascSort : function(a, b){
        return a < b? -1 : (a > b ? 1 : 0);
    },

    descSort : function(a, b){
        return -1 * Util.ascSort(a, b);
    },

	parseInt: function(obj, defaultVal){
		try {
			return window.parseInt(obj, 10);
		}catch(e){
			return defaultVal;
		}
	},

	parseFloat: function(obj, defaultVal){
		try {
			return window.parseFloat(obj);
		}catch(e){
			return defaultVal;
		}
	},

	listToMap: function(list){
		var out = {};
		$.each(list, function(i, key){
			out[key] = true;
		});
		return out;
	},

	mapToList: function(map){
		var out = [];
		$.each(map, function(key, val){
			out.push(val);
		});
		return out;
	},

	mapSize: function(map){
		var count = 0;
		$.each(map, function(i, key){
			count++;
		});
		return count;
	},

	isString: function(val){
		if(typeof val == 'string' || val instanceof String) return true;
		return false;
	},

	isNum: function(val){
		return $.isNumeric(val);
	},

	getProp: function(key, defVal){
		if(!window.Props) return defVal;
		return window.Props[key];
	},


	initAndAppend: function(controllerType, parent){
		var c = new window[controllerType]();
		c.init();
		parent.append(c.getUI());
	}

};