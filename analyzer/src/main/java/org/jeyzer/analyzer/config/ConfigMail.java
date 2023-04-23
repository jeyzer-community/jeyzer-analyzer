package org.jeyzer.analyzer.config;

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



import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigMail {

	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String MAIL_SMTP_PORT = "mail.smtp.port";
	public static final String MAIL_FROM = "mail.from";
	public static final String MAIL_USERNAME = "mail.username";
	public static final String MAIL_PASSWORD = "mail.password";
	
	private static final String JZRA_MAIL_NODE = "mail";
	private static final String JZRA_MAIL_SETUP = "setup";
	private static final String JZRA_MAIL_RECIPIENTS = "recipients";
	private static final String JZRA_MAIL_ENABLED = "mail_enabled";
	private static final String JZRA_MAIL_SUBJECT = "subject";
	private static final String JZRA_DUMP_EVENTS = "dump_events";
	
	private String setup;
	private String subject;
	private List<InternetAddress> recipients;
	private String recipientsEmails;
	private Properties props;
	private boolean enabled;
	private boolean dumpEvents;
	
	private ConfigTemplate templateCfg;
	
	public ConfigMail(Element node) throws AddressException, JzrInitializationException {
		Element mailNode = ConfigUtil.getFirstChildNode(node, JZRA_MAIL_NODE);
		if (mailNode == null){
			this.enabled = false;
			return;
		}
		
		this.enabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(mailNode,JZRA_MAIL_ENABLED));
		if (!enabled)
			return;
		
		this.setup = ConfigUtil.getAttributeValue(mailNode,JZRA_MAIL_SETUP);
		this.subject = ConfigUtil.getAttributeValue(mailNode,JZRA_MAIL_SUBJECT);
		this.recipients = buildRecipients(ConfigUtil.getAttributeValue(mailNode,JZRA_MAIL_RECIPIENTS));
		this.recipientsEmails = ConfigUtil.getAttributeValue(mailNode,JZRA_MAIL_RECIPIENTS);
		
		this.dumpEvents = Boolean.parseBoolean(ConfigUtil.getAttributeValue(mailNode,JZRA_DUMP_EVENTS));
		
		this.templateCfg = new ConfigTemplate(ConfigUtil.getFirstChildNode(mailNode, ConfigTemplate.JZRA_CONTENT_TEMPLATE));
		
		initMailProperties();
	}
	
	public String getProperty(String key){
		return props.getProperty(key);
	}

	public Properties getProperties(){
		return props;
	}	
	
	public List<InternetAddress> getRecipientAdresses() {
		return recipients;
	}
	
	public String getRecipientEmails() {
		return recipientsEmails;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean areEventsDumpable() {
		return dumpEvents;
	}
	
	public String getSubject(){
		return subject;
	}
	
	public ConfigTemplate getTemplateConfiguration() {
		return templateCfg;
	}

	private List<InternetAddress> buildRecipients(String attributeValue) throws AddressException {
		List<InternetAddress> emailAdresses = new ArrayList<>();
		StringTokenizer stringTokenizer = new StringTokenizer(attributeValue, ";");
		while (stringTokenizer.hasMoreElements()) 
			emailAdresses.add(new InternetAddress(stringTokenizer.nextElement().toString()));
		return emailAdresses;
	}
	
	private void initMailProperties() throws JzrInitializationException{
		File file = new File(this.setup);
		this.props = ConfigUtil.loadPropertyFile(file);
		if (props == null || props.isEmpty())
			throw new JzrInitializationException("Mail profile not found : " + this.setup);
	}
	
}
