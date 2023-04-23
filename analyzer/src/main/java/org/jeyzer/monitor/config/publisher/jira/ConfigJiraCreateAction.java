package org.jeyzer.monitor.config.publisher.jira;

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
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigJiraCreateAction extends ConfigJiraAction {
	
	public static final String JEYZER_MONITOR_JIRA_ACTION_CREATE = "create";
	
	private static final String JEYZER_MONITOR_JIRA_AFFECT_VERSION = "affect_version";
	private static final String JEYZER_MONITOR_JIRA_ASSIGNEE = "assignee";
	private static final String JEYZER_MONITOR_JIRA_PRIORITY = "priority";
	private static final String JEYZER_MONITOR_JIRA_ENVIRONMENT = "environment";
	private static final String JEYZER_MONITOR_JIRA_SUMMARY_TEMPLATE = "summary_template";
	private static final String JEYZER_MONITOR_JIRA_DESCRIPTION_TEMPLATE = "description_template";
	private static final String JEYZER_MONITOR_JIRA_COMPONENT = "component";
	
	private String affectVersion;
	private String assignee;
	private String component;    // optional
	private boolean priorityPublished;    // optional
	private String environment;  // optional
	private ConfigTemplate summaryTemplate;
	private ConfigTemplate descriptionTemplate;
	
	public ConfigJiraCreateAction(Element node) throws JzrInitializationException {
		super(node, JEYZER_MONITOR_JIRA_ACTION_CREATE);

		affectVersion = loadMandatoryParam(node, JEYZER_MONITOR_JIRA_AFFECT_VERSION);
		assignee = loadOptionalParam(node, JEYZER_MONITOR_JIRA_ASSIGNEE);
		component = loadOptionalParam(node, JEYZER_MONITOR_JIRA_COMPONENT);
		priorityPublished = hasOptionalParam(node, JEYZER_MONITOR_JIRA_PRIORITY);
		environment = loadOptionalParam(node, JEYZER_MONITOR_JIRA_ENVIRONMENT);
		
		summaryTemplate = loadtemplate(node, JEYZER_MONITOR_JIRA_SUMMARY_TEMPLATE);
		descriptionTemplate = loadtemplate(node, JEYZER_MONITOR_JIRA_DESCRIPTION_TEMPLATE);
	}

	public String getAffectVersion() {
		return affectVersion;
	}

	public String getAssignee() {
		return assignee;
	}
	
	public String getComponent() {
		return component;
	}

	public boolean hasPriorityPublished() {
		return priorityPublished;
	}

	public String getEnvironment() {
		return environment;
	}

	public ConfigTemplate getSummaryTemplate() {
		return summaryTemplate;
	}

	public ConfigTemplate getDescriptionTemplate() {
		return descriptionTemplate;
	}
}
