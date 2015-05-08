/*
 * Copyright 2015 Evgeny Dolganov (evgenij.dolganov@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package och.comp.mail;

import static java.util.concurrent.TimeUnit.*;
import static och.api.model.PropKey.*;
import static och.util.DateUtil.*;
import static och.util.FileUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;
import static och.util.concurrent.AsyncListener.*;
import static och.util.concurrent.ExecutorsUtil.*;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.mail.internet.InternetAddress;

import och.comp.mail.SendReq.MailMsg;
import och.comp.mail.SendReq.RecipientGroup;
import och.comp.mail.common.SendTask;
import och.service.props.Props;
import och.util.concurrent.AsyncListener;

import org.apache.commons.logging.Log;


public class MailService {
	
	private static final AtomicLong storeCounter = new AtomicLong(0);
	
	private Log log = getLog(getClass());
	
	
	private ScheduledExecutorService workThreads = newScheduledThreadPool("MailService-send", 3);
	private ExecutorService answerThread = newSingleThreadExecutor("MailService-result");
	private AsyncListener asyncListener;

	final Sender sender;
	Props props;
	
	private long lastStoreClearDate;
	
	public MailService(Props props) {
		this(null, props, null);
	}
	
	public MailService(Sender sender, Props props) {
		this(sender, props, null);
	}
	
	public MailService(Sender sender, Props props, AsyncListener asyncListener) {
		this.sender = sender != null? sender : new SenderImpl();
		this.props = props;
		this.asyncListener = asyncListener;
	}
	
	public void sendAsyncWarnData(String title, Collection<String> coll){
		sendAsyncWarnData(title, collectionToStr(coll, '\n'));
	}
	
	public void sendAsyncWarnData(String title, String data){
		SendReq req = new SendReq();
		req.subject("Live Chat Warning: " + title);
		req.text("<html><body><pre>"+data+"</pre></body></html>");
		req.to(props.getStrVal(admin_Emails));
		sendAsync(req);
	}
	
	public void sendAsyncInfoData(String title, String data){
		SendReq req = new SendReq();
		req.subject("Live Chat Info: " + title);
		req.text("<html><body><pre>"+data+"</pre></body></html>");
		req.to(props.getStrVal(admin_Emails));
		sendAsync(req);
	}
	
	
	public void sendAsync(SendReq req) {
		sendAsync(req, null);
	}
	

	public void sendAsync(SendReq req, SendCallback callback) {
		
		List<RecipientGroup> recipientGroups = req.getRecipientGroups();
		
		if( recipientGroups.isEmpty()){
			if(callback != null) callback.onSuccess();
			return;
		}
		
		for (RecipientGroup recipientGroup : recipientGroups) {
			if(recipientGroup.isEmpty()) continue;
			SendTask task = createSendTask(req, recipientGroup, callback);
			processTaskReq(task);
		}
	}
	
	
	public void sendOnce(SendReq req) throws Exception{
		
		List<RecipientGroup> recipientGroups = req.getRecipientGroups();
		if(recipientGroups.isEmpty()) return;
		
		for (RecipientGroup recipientGroup : recipientGroups) {
			
			if(recipientGroup.isEmpty()) continue;
			
			SendTask task = createSendTask(req, recipientGroup, null);
			storeIfNeed(task);
			sender.send(task, props);
		}
	}
	
	private SendTask createSendTask(SendReq req, RecipientGroup recipientGroup, SendCallback callback) {
		
		MailMsg msg = req.msg;
		if(msg.fromEmail == null){
			try {
				msg.fromEmail = new InternetAddress(props.getStrVal(mail_fromMail));
			}catch(Exception e){
				throw new IllegalStateException("can't set default from email", e);
			}
		}
		
		return new SendTask(msg, recipientGroup, callback);
	}
	

	protected void processTaskReq(final SendTask task) {
		
		Future<?> f = null;
		
		if(task.failCount == 0){
			f = workThreads.submit(() 
					-> trySend(task));
		} else {
			long waitTime = props.getIntVal(mail_errorWaitDelta) * task.failCount;
			f = workThreads.schedule(() 
					-> trySend(task), waitTime, MILLISECONDS);
		}
		
		fireAsyncEvent(asyncListener, f);
	}

	protected void trySend(SendTask task) {
		
		storeIfNeed(task);
		
		try {
			sender.send(task, props);
			successAnswerReq(task);
		}catch (Throwable t) {
			
			task.failCount++;
			if(task.failCount < props.getIntVal(mail_trySendCount)){
				processTaskReq(task);
			} else {
				task.t = t;
				failAnswerReq(task);
			}
		}
		
	}

	private void storeIfNeed(SendTask task) {
		
		if( ! props.getBoolVal(mail_storeToDisc)) return;
		if( task.stored) return;
		
		try {
			
			File dir = new File(props.getStrVal(mail_storeDir));
			dir.mkdirs();
			
			clearOldEmailsIfNeed(dir);
			
			StringBuilder sb = new StringBuilder();
			sb.append("Recipients: ").append(task.recipientGroup).append('\n');
			sb.append("From: ").append(task.msg.fromEmail).append('\n');
			sb.append("Subject: ").append(task.msg.subject).append('\n');
			sb.append("---\n");
			sb.append(task.msg.text);
			
			long storeIndex = storeCounter.incrementAndGet();
			String fileName = "m-"+formatDate(new Date(), "yyyy-MM-dd_HH-mm-ss");
			fileName += "_"+storeIndex;
			fileName += ".txt";
			
			File file = new File(dir, fileName);
			file.createNewFile();
			writeFileUTF8(file, sb.toString());
			
			task.stored = true;
			
		}catch(Throwable t){
			log.error("can't store email: "+t);
		}
		
	}

	private void clearOldEmailsIfNeed(File dir) {
		
		if( ! props.getBoolVal(mail_storeClearOld)) return;
		
		//once at day
		long curDay = dateStart(System.currentTimeMillis());
		if(curDay == lastStoreClearDate) return;
		lastStoreClearDate = curDay;
		
		try {
			
			long lastDate = curDay - (props.getLongVal(mail_storeOldMaxDays)*24*60*60*1000);
			
			File[] files = dir.listFiles();
			if(isEmpty(files)) return;
			
			for(File f : files){
				if(f.isDirectory()) return;
				
				long lastModified = f.lastModified();
				if(lastModified < lastDate){
					f.delete();
				}
			}
			
		}catch(Throwable t){
			log.error("can't clearOldEmails: "+t);
		}
	}

	protected void failAnswerReq(final SendTask task) {
		
		if(task.callback == null) {
			log.error("can't send mail for: "+task.recipientGroup, task.t);
			return;
		}
		
		answerThread.execute(()-> {
			try {
				task.callback.onFailed(task.t);
			}catch (Throwable t) {
				log.error("error on callbak", t);
			}
		});
	}
	
	protected void successAnswerReq(final SendTask task) {
		
		if(task.callback == null) return;
		
		answerThread.execute(() -> {
			try {
				task.callback.onSuccess();
			}catch (Throwable t) {
				log.error("error on callbak", t);
			}
		});
	}

	public void shutdown() {
		workThreads.shutdown();
		answerThread.shutdown();
	}




}
