
DateUtil = {

    ONE_DAY_MS : (1000 * 60 * 60 * 24),

	dateFrom: function(val){
		if(Util.isString(val)){
			return DateUtil.parseUTCStr(val);
		}
		return new Date(val);

	},

    daysBetween: function(date1, date2) {
        var date1Ms = date1.getTime();
        var date2Ms = date2.getTime();
        var differenceMs = Math.abs(date1Ms - date2Ms);
        return Math.round(differenceMs / DateUtil.ONE_DAY_MS);
    },

    daysBetweenNotAbs: function(date1, date2) {
        var date1Ms = date1.getTime();
        var date2Ms = date2.getTime();
        var differenceMs = date1Ms - date2Ms;
        return Math.round(differenceMs / DateUtil.ONE_DAY_MS);
    },

    getAge: function(birthDate, curDate){
        birthDate = birthDate || new Date();
        curDate = curDate || new Date();
        var diff = curDate.getTime() - birthDate.getTime();
        return Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25));
    },

    /**
     * Получить дату из строки форматов dd.mm.yyyy, d.mm.yyyy, dd.m.yyyy
     * @param str - строка
     * @return дату, либо null, если строка не корректна
     */
    parseDigitsStr: function(str){
        var dateParts = str.split(".");

        //проверяем строку на соотвествие форматам dd.mm.yyyy, d.mm.yyyy, dd.m.yyyy
        if( dateParts.length != 3){
            return null;
        }
        if(dateParts[0].length < 1 || dateParts[0].length > 2 || dateParts[0]=="0"){
            return null;
        }
        if(dateParts[1].length < 1 || dateParts[1].length > 2 || dateParts[1]=="0"){
            return null;
        }
        if(dateParts[2].length != 4){
            return null;
        }
        // проверяем, что значения месяца не больще 12, а значение дня не больше 31
        if (dateParts[0]>31 || dateParts[1]>12) {
            return null;
        }

        var d = new Date(dateParts[2], (dateParts[1] - 1), dateParts[0]);
        if( ! isNaN(d.getTime())){
            return d;
        } else {
            return null;
        }
    },

    getDayStart: function(date){
        var out = new Date(date);
        out.setHours(0,0,0,0);
        return out;
    },

    addDays: function(date, count){
        var time = date.getTime();
        var newTime = time + count*24*60*60*1000;
        var out = new Date();
        out.setTime(newTime);
        return out;
    },

    getHoursAndMinutes: function(date){
        if(typeof date == "number"){
            date = new Date(date);
        }
        var h = DateUtil.zeroPrefix(date.getHours());
        var m = DateUtil.zeroPrefix(date.getMinutes());
        return h+":"+m;
    },

    zeroPrefix: function(num) {
        return (num >= 0 && num < 10) ? "0" + num : ""+ num;
    },

	format_DD_MM_YYYY_HH_mm_SS: function(date){
		return new DateFmt("%d.%m.%y %H:%M:%S").format(date);
	},

	format_DD_MM_YYYY: function(date){
		return new DateFmt("%d.%m.%y").format(date);
	},

	format_HH_mm_SS: function(date){
		return new DateFmt("%H:%M:%S").format(date);
	},

	format_HH_mm: function(date){
		return new DateFmt("%H:%M").format(date);
	},


	/**
	 * from: http://stackoverflow.com/questions/5802461
	 * примеры: '2014-09-16T18:32:05+0400'
	 */
	parseUTCStr: function(dateStr){

		//valid example: '2014-09-16T18:32:05.000+04:00'

		//add ms if need
		if(dateStr.length > 19 && dateStr.indexOf('.') < 0){
			dateStr = dateStr.substring(0, 19) + ".000"+dateStr.substring(19);
		}
		//add ':' to timezone if need
		if(dateStr.length > 19 && (dateStr.indexOf('+', 19) > 0 || dateStr.indexOf('-', 19) > 0)){
			dateStr = dateStr.substring(0, 26) + ":"+dateStr.substring(26);
		}

		dateStr = dateStr.trim();
		//alert("2011-11-29T15:52:18.867+03:30"+" --- "+dateStr);

        var regexIso8601 = /^(\d{4}|\+\d{6})(?:-(\d{2})(?:-(\d{2})(?:T(\d{2}):(\d{2}):(\d{2})\.(\d{1,})(Z|([\-+])(\d{2}):(\d{2}))?)?)?)?$/;
        var lOff = -(new Date().getTimezoneOffset());
		var lHrs = Math.floor(lOff / 60);
		var lMin = lOff % 60;
		var m = regexIso8601.exec(dateStr);
        if (m) {
			var timestamp = Date.UTC(
				m[1],
				(m[2] || 1) - 1,
				m[3] || 1,
				m[4] - (m[8] ? m[9] ? m[9] + m[10] : 0 : lHrs) || 0,
				m[5] - (m[8] ? m[9] ? m[9] + m[11] : 0 : lMin) || 0,
				m[6] || 0,
				((m[7] || 0) + '00').substr(0, 3)
			);
			return new Date(timestamp);
        }
		return new Date(dateStr);
    }

};


/**
 * <pre>
 * fmt = new DateFmt("%w %d:%n:%y - %H:%M:%S  %i")
 * v = fmt.format(new Date())
 * </pre>
 */
function DateFmt(fstr) {
	
  this.formatString = fstr

  var mthNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
  var dayNames = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
  
  var zeroPad = function(number) {
     return ("0"+number).substr(-2,2);
  }

  var dateMarkers = {
    d:['getDate',function(v) {return zeroPad(v)}],
    m:['getMonth',function(v) {return zeroPad(v+1)}],
    n:['getMonth',function(v) {return mthNames[v];}],
    w:['getDay',function(v) {return dayNames[v];}],
    y:['getFullYear'],
    H:['getHours',function(v) {return zeroPad(v)}],
    M:['getMinutes',function(v) {return zeroPad(v)}],
    S:['getSeconds',function(v) {return zeroPad(v)}],
    i:['toISOString']
  };

  this.format = function(date) {
    var dateTxt = this.formatString.replace(/%(.)/g, function(m, p) {
      var rv = date[(dateMarkers[p])[0]]()

      if ( dateMarkers[p][1] != null ) rv = dateMarkers[p][1](rv)

      return rv

    });

    return dateTxt
  }

}