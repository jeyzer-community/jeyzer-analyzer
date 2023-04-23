package org.jeyzer.analyzer.communication;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jeyzer.analyzer.config.ConfigMail;
import org.jeyzer.analyzer.output.template.TemplateEngine;

public abstract class Mailer {
	
	protected ConfigMail cfg;
	protected TemplateEngine templateEngine;
	
	public Mailer(ConfigMail cfg){
		this.cfg = cfg;
		templateEngine = new TemplateEngine(this.cfg.getTemplateConfiguration());
	}
	
	public void sendMail(String content, List<String> attachmentPaths) throws MessagingException{

		// Use session.setDebug(true) to enable debugging
		Session session = Session.getInstance(
				this.cfg.getProperties(), 
				new javax.mail.Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						cfg.getProperty(ConfigMail.MAIL_USERNAME),
						cfg.getProperty(ConfigMail.MAIL_PASSWORD));
			}
		});
		
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(cfg.getProperty(ConfigMail.MAIL_FROM)));
		
		for (InternetAddress recipient : cfg.getRecipientAdresses()){
			message.addRecipient(Message.RecipientType.TO, recipient);
		}

		message.setSubject(cfg.getSubject());

		// create the message part 
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		
		//fill message
		if (isHtmlFormat())
			messageBodyPart.setContent(content, "text/html; charset=utf-8");
		else
			messageBodyPart.setText(content);
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		
		// Part two is attachment
		for (String path : attachmentPaths){
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(path);
			messageBodyPart.setDataHandler(new DataHandler(source));
			
			String fileName = path.substring(path.lastIndexOf('/') + 1);
			messageBodyPart.setFileName(fileName);
			
			multipart.addBodyPart(messageBodyPart);
		}

		// Put parts in message
		message.setContent(multipart);

		Transport.send(message);
	}

	protected abstract boolean isHtmlFormat();
}
