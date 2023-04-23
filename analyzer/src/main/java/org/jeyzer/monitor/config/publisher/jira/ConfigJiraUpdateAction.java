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
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigJiraUpdateAction extends ConfigJiraAction {
	
	public static final String JEYZER_MONITOR_JIRA_ACTION_UPDATE = "update";
	
	private static final String JEYZER_MONITOR_JIRA_ATTACHMENT_ENABLED = "attachment_enabled";
	private static final String JEYZER_MONITOR_JIRA_COMMENT_TEMPLATE = "comment_template";
	
	private boolean attachmentEnabled;
	private ConfigTemplate commentTemplate;
	
	public ConfigJiraUpdateAction(Element node) throws JzrInitializationException {
		super(node, JEYZER_MONITOR_JIRA_ACTION_UPDATE);
		
		this.attachmentEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(node, JEYZER_MONITOR_JIRA_ATTACHMENT_ENABLED));
		this.commentTemplate = loadtemplate(node, JEYZER_MONITOR_JIRA_COMMENT_TEMPLATE);
	}

	public boolean isAttachmentEnabled() {
		return attachmentEnabled;
	}

	public ConfigTemplate getCommentTemplate() {
		return commentTemplate;
	}

}
