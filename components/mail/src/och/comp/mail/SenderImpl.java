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

import static och.api.model.PropKey.*;
import static och.util.Util.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import och.comp.mail.SendReq.MailMsg;
import och.comp.mail.common.SendTask;
import och.service.props.Props;


public class SenderImpl implements Sender {

	@Override
	public void send(SendTask task, Props props) throws Exception {
		
		Properties sessionProps = new Properties();
		sessionProps.put("mail.smtp.auth", props.getStrVal(mail_smtp_auth));
		sessionProps.put("mail.smtp.starttls.enable", props.getStrVal(mail_smtp_starttls_enable));
		sessionProps.put("mail.smtp.host", props.getStrVal(mail_smtp_host));
		sessionProps.put("mail.smtp.port", props.getStrVal(mail_smtp_port));
		sessionProps.put("mail.debug", props.getStrVal(mail_debug));
		
		if(props.getBoolVal(mail_skipSslCertCheck)){
			sessionProps.put("mail.smtp.ssl.checkserveridentity", "false");
			sessionProps.put("mail.smtp.ssl.trust", "*");
		}

		Session session = Session.getInstance(sessionProps, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(props.getStrVal(mail_username), props.getStrVal(mail_password));
			}
			
		});
		
		MailMsg msg = task.msg;

		MimeMessage message = new MimeMessage(session);
		message.setFrom(msg.fromEmail);
		message.setSubject(msg.subject);
		message.setText(msg.text, msg.charset, msg.subtype);
		
		Map<RecipientType, List<InternetAddress>> recipients = task.recipientGroup.byType();
		for (Entry<RecipientType, List<InternetAddress>> entry : recipients.entrySet()) {
			message.setRecipients(entry.getKey(), array(entry.getValue(), Address.class));
		}		
		
		if( ! isEmpty(msg.replyTo)){
			message.setReplyTo(array(msg.replyTo, Address.class));
		}

		Transport.send(message);

	}

}
