package org.jeyzer.monitor.config.publisher.zabbix;

import org.jeyzer.analyzer.config.ConfigTemplate;

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

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.publisher.ConfigPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigZabbixPublisher extends ConfigPublisher {
	
	public static final Logger logger = LoggerFactory.getLogger(ConfigZabbixPublisher.class);
	
	public static final String NAME = "zabbix";
	
	private static final String JEYZER_MONITOR_ZABBIX_CONFIG_FILE = "zabbix_config_file";
	
	private static final String JEYZER_MONITOR_ZABBIX_ENABLED = "zabbix_enabled";
	
	private static final String JEYZER_MONITOR_ZABBIX_SETUP = "zabbix_setup";
	private static final String JEYZER_MONITOR_ZABBIX_SETUP_CONFIG_FILE = "setup_config_file";
	
	private static final String JEYZER_MONITOR_ZABBIX_INPUT_TEMPLATE = "zabbix_input_template";
		
	private boolean zabbixEnabled;
	private ConfigZabbixSetup setupCfg;

	private ConfigTemplate inputTemplate;
	
	public ConfigZabbixPublisher(Element zabbixNode) throws JzrInitializationException {
		super(NAME, zabbixNode);
		
		if (zabbixNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix publisher : Zabbix configuration is missing.");
		
		String path = ConfigUtil.getAttributeValue(zabbixNode, JEYZER_MONITOR_ZABBIX_CONFIG_FILE);
		if (path!= null && !path.isEmpty())
			// load the Zabbix configuration file
			zabbixNode = loadConfigurationFile(path, NAME);
		
		this.zabbixEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(zabbixNode, JEYZER_MONITOR_ZABBIX_ENABLED));
		if (!this.zabbixEnabled)
			return;
		
		Element zabbixSetupNode = ConfigUtil.getFirstChildNode(zabbixNode, JEYZER_MONITOR_ZABBIX_SETUP);
		path = ConfigUtil.getAttributeValue(zabbixSetupNode, JEYZER_MONITOR_ZABBIX_SETUP_CONFIG_FILE);
		if (path!= null && !path.isEmpty())
			zabbixSetupNode = loadConfigurationFile(path, JEYZER_MONITOR_ZABBIX_SETUP);

		setupCfg = new ConfigZabbixSetup(zabbixSetupNode);
		
		inputTemplate = loadtemplate(zabbixNode, JEYZER_MONITOR_ZABBIX_INPUT_TEMPLATE);
	}

	@Override
	public boolean isEnabled() {
		return zabbixEnabled;
	}
	
	public ConfigZabbixSetup getSetupCfg() {
		return setupCfg;
	}

	public ConfigTemplate getInputTemplateCfg() {
		return inputTemplate;
	}
	
	private Element loadConfigurationFile(String path, String nodeName) throws JzrInitializationException {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the Zabbix configuration resource using path : {}", path);
				throw new JzrInitializationException("Failed to open the Zabbix configuration resource using path : " + path);
			}
		} catch (JzrInitializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Failed to open the Zabbix configuration resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the Zabbix configuration resource using path : " + path, e);
		}
		
		NodeList nodes = doc.getElementsByTagName(nodeName);
		Element zabbixNode = (Element)nodes.item(0);
		if (zabbixNode == null){
			logger.error("Zabbix configuration {} is invalid.", path);
			throw new JzrInitializationException("Zabbix configuration " + path + " is invalid.");
		}
		
		return zabbixNode;
	}
	
	protected ConfigTemplate loadtemplate(Element node, String templateName) throws JzrInitializationException {
		Element templateNode = ConfigUtil.getFirstChildNode(node, templateName);
		if (templateNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor Zabbix publisher : Zabbix " + templateName+ " template is missing.");
		
		return new ConfigTemplate(templateNode);
	}
}
