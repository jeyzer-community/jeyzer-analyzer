package org.jeyzer.monitor.publisher.jira;

import java.util.List;

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

import org.jeyzer.analyzer.output.template.TemplateEngine;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.PropertyHelper;
import org.jeyzer.monitor.config.publisher.jira.ConfigJiraUpdateAction;

import org.jeyzer.monitor.engine.event.MonitorEvent;

public class JiraCommentRequest {

	public static final String REPORT_ATTACHMENT_ACTIVE = "report_attachment_active";
	
	private String ticketId;
	private String comment;
	
	public JiraCommentRequest(List<MonitorEvent> events, JzrMonitorSession session, ConfigJiraUpdateAction updateCfg) {
		this.ticketId = events.get(0).getTicket();
		this.comment = PropertyHelper.expandRecordingProperties(buildComment(events, session, updateCfg), "comment", (JzrSession)session);
	}

	public JiraCommentRequest(List<MonitorEvent> events, String ticketId, JzrMonitorSession session, ConfigJiraUpdateAction updateCfg) {
		this.ticketId = ticketId;
		this.comment = PropertyHelper.expandRecordingProperties(buildComment(events, session, updateCfg), "comment", (JzrSession)session);
	}
	
	public String getTicketId() {
		return this.ticketId;
	}

	public Object getComment() {
		return comment;
	}
	
	private String buildComment(List<MonitorEvent> events, JzrMonitorSession session, ConfigJiraUpdateAction updateCfg) {
		TemplateEngine templateEngine = new TemplateEngine(updateCfg.getCommentTemplate());

		templateEngine.addContextEntry(TemplateEngine.EVENT_KEY, events.get(0));
		templateEngine.addContextEntry(TemplateEngine.LAST_EVENT_KEY, events.get(events.size()-1));
		templateEngine.addContextEntry(TemplateEngine.EVENTS_LIST_KEY, events);
		templateEngine.addContextEntry(
				TemplateEngine.APPLICATION_ID_KEY, 
				session instanceof JzrSession?((JzrSession)session).getApplicationId():null);
        templateEngine.addContextEntry(
        		TemplateEngine.APPLICATION_TYPE_KEY, 
        		session instanceof JzrSession?((JzrSession)session).getApplicationType():null);
        templateEngine.addContextEntry(REPORT_ATTACHMENT_ACTIVE, updateCfg.isAttachmentEnabled());

        return templateEngine.generate();
	}
}
