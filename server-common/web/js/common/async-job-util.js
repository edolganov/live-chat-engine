

AsyncJob = {

	/**
	 * invoke async job.
	 * @param opt - {name, blockSize, waitTime, isAllDone(index), doJob(index), onDone, isCancel(index), onCancel(index)}
	 */
	invoke: function(opt){

		opt = Util.extend({
			name:"unknown",
			blockSize: 20,
			waitTime: 20,
			
			isAllDone: function(index){
				return true;
			},
			doJob: function(index){},
			onDone: function(){},
			
			isCancel: function(index){
				return false;
			},
			onCancel: function(index){}
		}, opt);

		var index = 0;
		var jobFunc = function(){

			if(opt.isCancel(index)){
				LogFactory.getLogger("AsyncJob").info("Cancel async job with name: "+opt.name);
				opt.onCancel(index);
				return;
			}
			
			var curDone = 0;
			while( ! opt.isAllDone(index) && curDone <= opt.blockSize){
				opt.doJob(index);
				index++;
				curDone++;
			}
			if( ! opt.isAllDone(index)) setTimeout(jobFunc, opt.waitTime);
			else opt.onDone();
		};
		jobFunc();
	}

}