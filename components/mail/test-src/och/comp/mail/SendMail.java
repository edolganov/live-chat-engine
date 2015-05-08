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

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import och.service.props.WriteProps;
import och.service.props.impl.MapProps;

public class SendMail {
	
	
	
	public static void main(String[] args) throws Exception {
		
		//main_send_by_own_server();
		//main_send_by_mandrillapp();
		//main_send_by_google();
		//main_send_by_concept();
		
		System.out.println("ok");
	}
	
	public static void main_send_by_own_server() throws Exception{
		
		String username = "";
		String password = "";
		String fromMail = "";
		String toMail = "";
		String subject = "Тестовый заголовок";
		String text = "<html><body><h1>Тест</h1><p>Тест отправки письма</body></html>";
		String host = "";
		int port = 0;
		
		WriteProps props = new MapProps();
		props.putVal(mail_skipSslCertCheck, true);
		props.putVal(mail_debug, false);
		props.putVal(mail_fromMail, fromMail);
		props.putVal(mail_username, username);
		props.putVal(mail_password, password);
		props.putVal(mail_smtp_auth, true);
		props.putVal(mail_smtp_starttls_enable, true);
		props.putVal(mail_smtp_host, host);
		props.putVal(mail_smtp_port, port);
		
		MailService service = new MailService(props);
		try {
			service.sendOnce(new SendReq(toMail, subject, text));
		} finally {
			service.shutdown();
		}
		
		
	}
	
	public static void main_send_by_mandrillapp() throws Exception {
		
		String username = "";
		String password = "";
		String fromMail = "";
		String toMail = "";
		String subject = "Тестовый заголовок";
		String text = "<html><body><h1>Тест</h1><p>Тест отправки письма</body></html>";
		
		WriteProps props = new MapProps();
		props.putVal(mail_fromMail, fromMail);
		props.putVal(mail_username, username);
		props.putVal(mail_password, password);
		props.putVal(mail_smtp_auth, true);
		props.putVal(mail_smtp_starttls_enable, true);
		props.putVal(mail_smtp_host, "smtp.mandrillapp.com");
		props.putVal(mail_smtp_port, 587);
		
		MailService service = new MailService(props);
		try {
			service.sendOnce(new SendReq(toMail, subject, text));
		} finally {
			service.shutdown();
		}
	}
	
	public static void main_send_by_google() throws Exception {
		
		String username = "";
		String password = "";
		String fromMail = "";
		String toMail = "";
		String subject = "Тестовый заголовок";
		String text = "<html><body><h1>Тест</h1><p>Тест отправки письма</body></html>";
		
		WriteProps props = new MapProps();
		props.putVal(mail_fromMail, fromMail);
		props.putVal(mail_username, username);
		props.putVal(mail_password, password);
		props.putVal(mail_smtp_auth, true);
		props.putVal(mail_smtp_starttls_enable, true);
		props.putVal(mail_smtp_host, "smtp.gmail.com");
		props.putVal(mail_smtp_port, 587);
		
		MailService service = new MailService(props);
		try {
			service.sendOnce(new SendReq(toMail, subject, text));
		} finally {
			service.shutdown();
		}
		
	}
	
	public static void main_send_by_concept() {
		
		String fromMail = "";
		String toMail = "";
		String subject = "Тестовый заголовок";
		String text = "<html><body><h1>Тест</h1><p>Тест отправки письма</body></html>";
		String username = "";
		String password = "";
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
 
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromMail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toMail));
			message.setSubject(subject);
			message.setText(text, "UTF-8", "html");
 
			Transport.send(message);
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
		
	}

}
