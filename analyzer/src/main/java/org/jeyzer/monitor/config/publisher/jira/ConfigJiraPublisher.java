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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.publisher.ConfigPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigJiraPublisher extends ConfigPublisher {
	
	public static final Logger logger = LoggerFactory.getLogger(ConfigJiraPublisher.class);
	
	public static final String NAME = "jira";
	
	private static final String JEYZER_MONITOR_JIRA_CONFIG_FILE = "jira_config_file";
	
	private static final String JEYZER_MONITOR_JIRA_ENABLED = "jira_enabled";
	
	private static final String JEYZER_MONITOR_JIRA_SETUP = "jira_setup";
	private static final String JEYZER_MONITOR_JIRA_SETUP_CONFIG_FILE = "setup_config_file";
	
	public static final String JEYZER_MONITOR_JIRA_TARGET_PROJECT = "project";
	public static final String JEYZER_MONITOR_JIRA_TARGET_ISSUE_TYPE = "issue_type";
	
	private static final String JEYZER_MONITOR_JIRA_TARGET = "target";	
	private static final String JEYZER_MONITOR_JIRA_VALUE = "value";

	private static final String JEYZER_MONITOR_JIRA_ACTIONS = "actions";
	private static final String JEYZER_MONITOR_JIRA_EVENT_THRESHOLDS = "event_thresholds";

	private static final String JEYZER_MONITOR_JIRA_PRIORITY_MAPPINGS = "priority_mappings";
	private static final String JEYZER_MONITOR_JIRA_PRIORITY_MAPPING_CONFIG_FILE = "priority_mapping_config_file";
	private static final String JEYZER_MONITOR_JIRA_MAPPING = "mapping";
	private static final String JEYZER_MONITOR_EVENT_LEVEL = "event_level";
	private static final String JEYZER_MONITOR_JIRA_PRIORITY = "jira_priority";
	
	private boolean jiraEnabled;
	private ConfigJiraSetup setupCfg;

	private Map<String, String> targets = new HashMap<>();
	private ConfigJiraCreateAction createActionCfg; // optional
	private ConfigJiraUpdateAction updateActionCfg; // optional
	
	private List<String> thresholds = new ArrayList<>();
	private Map<String, String> priorityMappings = new HashMap<>();

	public ConfigJiraPublisher(Element jiraNode) throws JzrInitializationException {
		super(NAME, jiraNode);
		
		if (jiraNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA configuration is missing.");
		
		String path = ConfigUtil.getAttributeValue(jiraNode, JEYZER_MONITOR_JIRA_CONFIG_FILE);
		if (path!= null && !path.isEmpty())
			// load the Jira configuration file
			jiraNode = loadConfigurationFile(path, NAME);
		
		this.jiraEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(jiraNode, JEYZER_MONITOR_JIRA_ENABLED));
		if (!this.jiraEnabled)
			return;
		
		Element jiraSetupNode = ConfigUtil.getFirstChildNode(jiraNode, JEYZER_MONITOR_JIRA_SETUP);
		path = ConfigUtil.getAttributeValue(jiraSetupNode, JEYZER_MONITOR_JIRA_SETUP_CONFIG_FILE);
		if (path!= null && !path.isEmpty())
			jiraSetupNode = loadConfigurationFile(path, JEYZER_MONITOR_JIRA_SETUP);
		
		setupCfg = new ConfigJiraSetup(jiraSetupNode);
		
		loadTargets(jiraNode);
		loadActions(jiraNode);
		loaPriorityMappings(jiraNode);
	}

	private void loaPriorityMappings(Element jiraNode) throws JzrInitializationException {
		Element mappingsNode = ConfigUtil.getFirstChildNode(jiraNode, JEYZER_MONITOR_JIRA_PRIORITY_MAPPINGS);
		String path = ConfigUtil.getAttributeValue(mappingsNode, JEYZER_MONITOR_JIRA_PRIORITY_MAPPING_CONFIG_FILE);
		if (path!= null && !path.isEmpty())
			mappingsNode = loadConfigurationFile(path, JEYZER_MONITOR_JIRA_PRIORITY_MAPPINGS);
		
		NodeList mappingNodes = mappingsNode.getChildNodes();
		for (int i=0; i<mappingNodes.getLength(); i++){
			if (!(mappingNodes.item(i) instanceof Element))
				continue;
			Element mappingNode = (Element)mappingNodes.item(i);
			if (!JEYZER_MONITOR_JIRA_MAPPING.equalsIgnoreCase(mappingNode.getTagName()))
				continue;
			loadMapping(mappingNode);
		}
	}

	private void loadMapping(Element mappingNode) {
		// JIRA priority
		String priority = ConfigUtil.getAttributeValue(mappingNode, JEYZER_MONITOR_JIRA_PRIORITY);
		if (priority.isEmpty())
			return;
		
		// Jeyzer level and sub level
		String levels = ConfigUtil.getAttributeValue(mappingNode, JEYZER_MONITOR_EVENT_LEVEL);
		String[] eventLevels = levels.trim().split(",");
		for (int j=0; j<eventLevels.length; j++){
			String levelValue = eventLevels[j].trim();
			if (!levelValue.isEmpty())
				priorityMappings.put(levelValue, priority);
		}
	}

	@Override
	public boolean isEnabled() {
		return jiraEnabled;
	}
	
	public ConfigJiraSetup getSetupCfg() {
		return setupCfg;
	}

	public Map<String, String> getTargets() {
		return targets;
	}

	public ConfigJiraCreateAction getCreateActionCfg() {
		return createActionCfg;
	}
	
	public boolean isCreateActionActive() {
		return createActionCfg != null;
	}

	public ConfigJiraUpdateAction getUpdateActionCfg() {
		return updateActionCfg;
	}
	
	public boolean isUpdateActionActive() {
		return updateActionCfg != null;
	}
	
	public List<String> getThresholds() {
		return thresholds;
	}
	
	public String getPriority(String levelAndSubLevel) {
		return priorityMappings.get(levelAndSubLevel);
	}

	private void loadActions(Element node) throws JzrInitializationException {
		Element actionsNode = ConfigUtil.getFirstChildNode(node, JEYZER_MONITOR_JIRA_ACTIONS);
		if (actionsNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA actions are missing.");
		
		loadThresholds(actionsNode);
		
		Element createNode = ConfigUtil.getFirstChildNode(actionsNode, ConfigJiraCreateAction.JEYZER_MONITOR_JIRA_ACTION_CREATE);
		if (createNode != null)
			createActionCfg = new ConfigJiraCreateAction(createNode);

		Element updateNode = ConfigUtil.getFirstChildNode(actionsNode, ConfigJiraUpdateAction.JEYZER_MONITOR_JIRA_ACTION_UPDATE);
		if (updateNode != null)
			updateActionCfg = new ConfigJiraUpdateAction(updateNode);
		
		if (this.createActionCfg == null && this.updateActionCfg == null )
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : no JIRA actions specified.");
	}

	private void loadTargets(Element node) throws JzrInitializationException {
		Element targetNode = ConfigUtil.getFirstChildNode(node, JEYZER_MONITOR_JIRA_TARGET);
		NodeList targetNodes = targetNode.getChildNodes();
		for (int i=0; i<targetNodes.getLength(); i++){
			if (!(targetNodes.item(i) instanceof Element))
				continue;
			Element keyValuePairNode = (Element)targetNodes.item(i);
			String key = keyValuePairNode.getTagName();
			String value = ConfigUtil.getAttributeValue(keyValuePairNode, JEYZER_MONITOR_JIRA_VALUE);
			targets.put(key, value);
		}
		
		if (!this.targets.containsKey(JEYZER_MONITOR_JIRA_TARGET_PROJECT))
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA project is missing.");
		
		if (!this.targets.containsKey(JEYZER_MONITOR_JIRA_TARGET_ISSUE_TYPE))
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA issue type is missing.");
	}
	
	private void loadThresholds(Element node) throws JzrInitializationException {
		String value = ConfigUtil.getAttributeValue(node, JEYZER_MONITOR_JIRA_EVENT_THRESHOLDS);
		if (value.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : event thresholds are empty.");
		
		String[] parts = value.trim().split(",");
		for (int i=0; i<parts.length; i++){
			String part = parts[i].trim();
			if (!part.isEmpty())
				thresholds.add(part);			
		}
		
		if (thresholds.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : event thresholds are missing.");
	}
	
	private Element loadConfigurationFile(String path, String nodeName) throws JzrInitializationException {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the Jira configuration resource using path : " + path);
				throw new JzrInitializationException("Failed to open the Jira configuration resource using path : " + path);
			}
		} catch (JzrInitializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Failed to open the Jira configuration resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the Jira configuration resource using path : " + path, e);
		}
		
		NodeList nodes = doc.getElementsByTagName(nodeName);
		Element jiraNode = (Element)nodes.item(0);
		if (jiraNode == null){
			logger.error("Jira configuration " + path + " is invalid.");
			throw new JzrInitializationException("Jira configuration " + path + " is invalid.");
		}
		
		return jiraNode;
	}
}
