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

import static java.util.Collections.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

public class SendReq {
	
	public static class MailMsg {
		
		public String subject; 
		public String text; 
		public String charset = "UTF-8"; 
		public String subtype = "html";
		public InternetAddress fromEmail;
		public List<InternetAddress> replyTo;
		
		public void addReplyTo(InternetAddress address){
			if(replyTo == null) replyTo = new ArrayList<InternetAddress>();
			replyTo.add(address);
		}

		@Override
		public String toString() {
			return "MailMsg [fromEmail=" + fromEmail + ", replyTo=" + replyTo
					+ ", subject=" + subject + ", charset=" + charset
					+ ", subtype=" + subtype + ", text=" + text + "]";
		}
		
		
		
	}
	
	public static final class RecipientGroup {
		
		private final Map<RecipientType, List<InternetAddress>> byType = new HashMap<>();
		
		public RecipientGroup() {}
		
		public RecipientGroup tryAddTo(String toEmail){
			
			if( ! hasText(toEmail)) return this;
			if( ! toEmail.contains("@")) return this;
			
			toEmail = toEmail.trim();
			
			try {
				putToListMap(byType, RecipientType.TO, new InternetAddress(toEmail));
			}catch(Exception e){
				//ok
			}
			return this;
		}

		public boolean isEmpty(){
			return byType.isEmpty();
		}
		
		public Map<RecipientType, List<InternetAddress>> byType(){
			return unmodifiableMap(byType);
		}
		
		public Set<InternetAddress> getAll(){
			HashSet<InternetAddress> out = new HashSet<>();
			for (List<InternetAddress> list : byType.values()) {
				out.addAll(list);
			}
			return out;
		}
		
		public Set<String> getAllByStrings(){
			HashSet<String> out = new HashSet<>();
			for (List<InternetAddress> list : byType.values()) {
				for (InternetAddress address : list) {
					out.add(address.toString());
				}
			}
			return out;
		}


		@Override
		public String toString() {
			return byType.toString();
		}
		
	}
	
	public final MailMsg msg = new MailMsg();
	private final List<RecipientGroup> recipientGroups = new ArrayList<>();
	
	public SendReq() {}
	
	public SendReq(String toEmails, String subject, String text){
		to(toEmails).subject(subject).text(text);
	}
	
	public SendReq(List<String> toEmails, String subject, String text){
		to(toEmails).subject(subject).text(text);
	}

	@Override
	public String toString() {
		return "SendReq [recipientGroups=" + recipientGroups + ", msg=" + msg
				+ "]";
	}

	public SendReq subject(String subject) {
		msg.subject = subject;
		return this;
	}

	public SendReq text(String text) {
		msg.text = text;
		return this;
	}

	public SendReq to(String toEmails) {
		if( ! hasText(toEmails)) return this;
		return to(strToList(toEmails, " "));
	}
	
	
	public SendReq to(List<String> toEmails){
		
		if( isEmpty(toEmails)) return this;
		
		for (String email : toEmails) {
			RecipientGroup group = new RecipientGroup().tryAddTo(email);
			if( ! group.isEmpty()) recipientGroups.add(group);
		}
		
		return this;
	}
	
	public SendReq from(String email) {
		try {
			msg.fromEmail = new InternetAddress(email);
		}catch(Exception e){
			//ok
		}
		return this;
	}
	
	public SendReq replyTo(String email){
		try {
			msg.addReplyTo(new InternetAddress(email));
		}catch(Exception e){
			//ok
		}
		return this;
	}
	
	public List<RecipientGroup> getRecipientGroups(){
		if(isEmpty(recipientGroups)) return emptyList();
		return unmodifiableList(recipientGroups);
	}
	
	
	
	

}
