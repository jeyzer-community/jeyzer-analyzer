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

public abstract class ConfigJiraAction {

	protected static final String JEYZER_MONITOR_JIRA_VALUE = "value";

	public ConfigJiraAction(Element node, String actionName) throws JzrInitializationException {
		if (node == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA " + actionName + " action configuration is missing.");
	}

	protected ConfigTemplate loadtemplate(Element node, String templateName) throws JzrInitializationException {
		Element templateNode = ConfigUtil.getFirstChildNode(node, templateName);
		if (templateNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA " + templateName+ " is missing.");
		
		return new ConfigTemplate(templateNode);
	}
	
	protected boolean hasOptionalParam(Element node, String param) {
		Element paramNode = ConfigUtil.getFirstChildNode(node, param);
		return paramNode != null;
	}
	
	protected String loadOptionalParam(Element node, String param) throws JzrInitializationException {
		String value = null;
		Element paramNode = ConfigUtil.getFirstChildNode(node, param);
		if (paramNode != null) {
			value = ConfigUtil.getAttributeValue(paramNode, JEYZER_MONITOR_JIRA_VALUE);
			if (value.isEmpty())
				throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA " + param + " is empty.");			
		}
		return value;
	}
	
	protected String loadMandatoryParam(Element node, String param) throws JzrInitializationException {
		Element paramNode = ConfigUtil.getFirstChildNode(node, param);
		if (paramNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA " + param + " is missing.");
		String value = ConfigUtil.getAttributeValue(paramNode, JEYZER_MONITOR_JIRA_VALUE);
		if (value.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA " + param + " is empty.");
		return value;
	}
}
