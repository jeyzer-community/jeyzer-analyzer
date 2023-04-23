package org.jeyzer.monitor.config.multimonitor;

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







import java.util.Properties;

import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.monitor.config.ConfigMonitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigMultiMonitorEntry {

	private static final String JZRM_ANALYSIS = "analysis";
	private static final String JZRM_FILE = "file";
	private static final String JZRM_ENVIRONMENT = "environment";
	private static final String JZRM_PARAM = "param";
	private static final String JZRM_NAME = "name";
	private static final String JZRM_VALUE = "value";
	
	private Properties params = new Properties();

	public ConfigMultiMonitorEntry(Element monitorNode) {
		
		String monitorPath = ConfigUtil.getAttributeValue(monitorNode,JZRM_FILE);
		params.put(ConfigMonitor.MONITOR_CONFIG_FILE, monitorPath);
		
		Element analysisNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_ANALYSIS);
		String analysisPath = ConfigUtil.getAttributeValue(analysisNode,JZRM_FILE);
		params.put(ConfigAnalyzer.ANALYSIS_CONFIG_FILE, analysisPath);
		
		loadEnvironment(monitorNode);
	}

	public Properties getParams() {
		return params;
	}

	private void loadEnvironment(Element monitorNode) {
		Element envNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_ENVIRONMENT);
		
		NodeList paramNodes = envNode.getElementsByTagName(JZRM_PARAM);
		for (int i=0; i<paramNodes.getLength(); i++){
			Element paramNode = (Element)paramNodes.item(i);
			String name = ConfigUtil.getAttributeValue(paramNode,JZRM_NAME);
			String value = ConfigUtil.getAttributeValue(paramNode,JZRM_VALUE);

			if (!name.isEmpty())
				params.put(name, value);
		}
	}
	
}
