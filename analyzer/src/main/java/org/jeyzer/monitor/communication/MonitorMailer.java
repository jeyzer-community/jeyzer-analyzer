package org.jeyzer.monitor.communication;

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

import org.jeyzer.analyzer.communication.Mailer;
import org.jeyzer.analyzer.config.ConfigMail;
import org.jeyzer.analyzer.output.template.TemplateEngine;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorMailer extends Mailer {

	private static final Logger logger = LoggerFactory.getLogger(MonitorMailer.class);
	
	private static final String EVENTS_DUMPABLE_KEY = "events_dumpable";
	
	public MonitorMailer(ConfigMail cfg){
		super(cfg);
	}
	
	public void sendAlert(JzrMonitorSession session, List<MonitorEvent> events, List<String> attachmentPaths) {
		
		if (events.isEmpty()){
			logger.info("No event to send by email");
			return;
		}
		
		if (!this.cfg.areEventsDumpable() && attachmentPaths.isEmpty()){
			logger.info("No attachments to be sent by email.");
			return;
		}

		// Add data to the context
		templateEngine.addContextEntry(TemplateEngine.EVENTS_LIST_KEY, events);
		templateEngine.addContextEntry(EVENTS_DUMPABLE_KEY, Boolean.valueOf(this.cfg.areEventsDumpable()));
		templateEngine.addContextEntry(
				TemplateEngine.APPLICATION_ID_KEY, 
				session instanceof JzrSession?((JzrSession)session).getApplicationId():null);
        templateEngine.addContextEntry(
        		TemplateEngine.APPLICATION_TYPE_KEY, 
        		session instanceof JzrSession?((JzrSession)session).getApplicationType():null);

		// prepare content
        String text = templateEngine.generate();
		
		try {
			sendMail(text.toString(), attachmentPaths);
		} catch (Exception ex) {
			logger.error("Failed to send alert email.", ex);
			// may work on the next iteration
		}
	}

	@Override
	protected boolean isHtmlFormat() {
		return true;
	}
}
