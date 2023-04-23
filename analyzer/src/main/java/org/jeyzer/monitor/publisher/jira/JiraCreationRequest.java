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

import org.jeyzer.analyzer.config.ConfigTemplate;
import org.jeyzer.analyzer.output.template.TemplateEngine;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.PropertyHelper;
import org.jeyzer.monitor.config.publisher.jira.ConfigJiraCreateAction;
import org.jeyzer.monitor.engine.event.MonitorEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraCreationRequest {
	
	public static final Logger logger = LoggerFactory.getLogger(JiraCreationRequest.class);
	
	public static final String JIRA_PROJECT_FIELD = "project";
	public static final String JIRA_ISSUE_TYPE_FIELD = "issuetype";
	public static final String JIRA_SUMMARY_FIELD = "summary";
	public static final String JIRA_DESCRIPTION_FIELD = "description";
	public static final String JIRA_ASSIGNEE_FIELD = "assignee";
	public static final String JIRA_COMPONENTS_FIELD = "components";
	public static final String JIRA_VERSIONS_FIELD = "versions";
	public static final String JIRA_ENVIRONMENT_FIELD = "environment";
	public static final String JIRA_PRIORITY_FIELD = "priority";
	private MonitorEvent event;
	private String summary;
	private String description;
	
	private String assignee;	// optional
	private String component;	// optional
	private String version;     // optional
	private String environment; // optional
	private boolean priorityPublished; // optional
	
	public JiraCreationRequest(List<MonitorEvent> events, JzrSession session, ConfigJiraCreateAction createCfg) {
		this.event = events.get(0);
		this.assignee = createCfg.getAssignee();
		this.description = PropertyHelper.expandRecordingProperties(buildText(events, session, createCfg.getDescriptionTemplate()), JIRA_DESCRIPTION_FIELD, session);
		this.summary = PropertyHelper.expandRecordingProperties(buildText(events, session, createCfg.getSummaryTemplate()), JIRA_SUMMARY_FIELD, session);
		this.component = PropertyHelper.expandRecordingProperties(createCfg.getComponent(), JIRA_COMPONENTS_FIELD, session);
		this.version = PropertyHelper.expandRecordingProperties(createCfg.getAffectVersion(), JIRA_VERSIONS_FIELD, session);
		this.environment = PropertyHelper.expandRecordingProperties(createCfg.getEnvironment(), JIRA_ENVIRONMENT_FIELD, session);
		this.priorityPublished = createCfg.hasPriorityPublished();
	}

	public MonitorEvent getEvent() {
		return event;
	}

	public String getSummary() {
		return this.summary;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getComponent() {
		return this.component; // can be null
	}
	
	public boolean hasComponent() {
		return this.component != null;
	}
	
	public String getAssignee() {
		return this.assignee; // can be null
	}
	
	public boolean hasAssignee() {
		return assignee != null;
	}
	
	public boolean hasAssigneeAccountId() {
		return assignee != null && assignee.indexOf('@') == -1; // not email
	}
	
	public String getVersion() {
		return this.version; // can be null
	}
	
	public boolean hasVersion() {
		return this.version != null;
	}
	
	public String getEnvironment() {
		return this.environment; // can be null
	}
	
	public boolean hasEnvironment() {
		return this.environment != null;
	}
	
	public boolean hasPriorityPublished() {
		return this.priorityPublished;
	}
	
	private String buildText(List<MonitorEvent> events, JzrSession session, ConfigTemplate templateCfg) {
		TemplateEngine templateEngine = new TemplateEngine(templateCfg);

		templateEngine.addContextEntry(TemplateEngine.EVENT_KEY, event);
		templateEngine.addContextEntry(TemplateEngine.LAST_EVENT_KEY, events.get(events.size()-1));
		templateEngine.addContextEntry(TemplateEngine.EVENTS_LIST_KEY, events);
		templateEngine.addContextEntry(
				TemplateEngine.APPLICATION_ID_KEY, 
				session.getApplicationId());
        templateEngine.addContextEntry(
        		TemplateEngine.APPLICATION_TYPE_KEY, 
        		session.getApplicationId());
        
        return templateEngine.generate();
	}
}
