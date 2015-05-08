

var logType = "@logType@"; //may be replaced by ANT in build
if( logType.length === 0 || logType.indexOf("@logType") === 0){
    //from http://log4javascript.org/docs/manual.html#appenders
    logType = "BrowserConsoleAppender"; //PopUpAppender| InPageAppender | PopUpAppender | production
}




try {
    log4javascript.toString();
}catch(e){
    //no log4javascript
    log4javascript = null;
}

var LogFactory = {};

if(logType === "production" || ! log4javascript){

    var NullLoggerImpl = function(){
        this.debug = function(){};
        this.info = function(){};
        this.error = function(){};
        this.trace = function(){};
        this.warn = function(){};
        this.fatal = function(){};
    };

    var nullLogger = new NullLoggerImpl();

    LogFactory.getLog = function(name){
        return nullLogger;
    };

} else {

    if( ! $.isFunction(log4javascript[logType])){
        logType = "BrowserConsoleAppender";
    }

    //using log4javascript
    var layout = new log4javascript.PatternLayout("%d{HH:mm:ss} %p %c - %m%n");
    var appender = new log4javascript[logType]();
    appender.setLayout(layout);
    log4javascript.getRootLogger().addAppender(appender);
    log4javascript.getRootLogger().info("Logging system has been initialized");

    LogFactory.getLog = function(name){
        var logger = log4javascript.getLogger(name);
        var loggerErrorFunc = logger.error;

        //add exception to signature
        logger.error = function(msg, e){
			try {
				if( ! Util.isEmpty(e)){
					var stack = Util.exceptionToString(e);
					if( ! Util.isEmpty(stack)){
						msg = msg +": "+stack;
					}
				}
			}catch(e){}
            loggerErrorFunc.call(logger, msg);
        };


        return logger;
    };

}


