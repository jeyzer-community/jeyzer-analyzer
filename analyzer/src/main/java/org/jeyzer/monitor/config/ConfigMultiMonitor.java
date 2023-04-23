package org.jeyzer.monitor.config;

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

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.config.multimonitor.ConfigMultiMonitorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigMultiMonitor {

	private static final Logger logger = LoggerFactory.getLogger(ConfigMultiMonitor.class);
	
	// property
	public static final String MULTIMONITOR_CONFIG_FILE = "jeyzer.multimonitor.config";
	
	public static final String JEYZ_MM_DEV_CONFIG_FILE = "config-mm.properties";
	
	private static final String JZRM_MULTIMONITOR = "multi_monitor";
	private static final String JZRM_PARALLEL_MAX = "parallel_max";
	
	private static final String JZRM_MONITOR = "monitor";
	
	private List<ConfigMultiMonitorEntry> multiMonitors = new ArrayList<>();

	private int maxParallelExecutions;

	/*
	 * ConfigMultiMonitor constructor
	 */
	public ConfigMultiMonitor() throws JzrInitializationException{
		String monitorFilePath = System.getProperty(MULTIMONITOR_CONFIG_FILE);
		
		if (monitorFilePath==null || monitorFilePath.isEmpty()){
			// Load system properties in dev environment
			ConfigUtil.loadDevVariables(monitorFilePath, JEYZ_MM_DEV_CONFIG_FILE);
			monitorFilePath = System.getProperty(MULTIMONITOR_CONFIG_FILE);
		}
		
		if (monitorFilePath==null || monitorFilePath.isEmpty()){
			throw new JzrInitializationException("Multi monitor configuration file path \"-D" +MULTIMONITOR_CONFIG_FILE+ "\" property missing on the command line. Please set it.");
		}
		
		// resolve any variable or system property (ex: ${user.dir})
		monitorFilePath = ConfigUtil.resolveValue(monitorFilePath);
		logger.info("Loading Multi Monitor config file : " + SystemHelper.sanitizePathSeparators(monitorFilePath));
		
		// test that file exists
		File monitorFile = new File(monitorFilePath);
		if (!monitorFile.exists()){
			throw new JzrInitializationException("Multi monitor configuration file \"" +monitorFilePath.replace('\\', '/') + "\" not found. Please fix it.");
		}		
		
		loadConfiguration(monitorFile);
	}
		
	public List<ConfigMultiMonitorEntry> getMultiMonitors() {
		return multiMonitors;
	}

	public int getMaxParallelExecutions() {
		return maxParallelExecutions;
	}

	private void loadConfiguration(File file) throws JzrInitializationException{
		
		try {
			// load the XML file
			Document doc = ConfigUtil.loadDOM(file);
			
			// monitor
			NodeList nodes = doc.getElementsByTagName(JZRM_MULTIMONITOR);
			Element multiMonitorNode = (Element)nodes.item(0);
			
			maxParallelExecutions = Integer.parseInt(ConfigUtil.getAttributeValue(multiMonitorNode,JZRM_PARALLEL_MAX));
			if (!(maxParallelExecutions>0)){
				logger.warn("Invalid " + JZRM_PARALLEL_MAX + " parameter value : \"" + maxParallelExecutions + "\". Defaulting to 1");
				maxParallelExecutions = 1;
			}
			
			loadMonitorNodes(multiMonitorNode);
			
		} catch (Exception e) {
			throw new JzrInitializationException("Failed to load the Multi Monitor configuration : \"" +file.getAbsolutePath() + "\". Error is : " + e.getMessage(), e);
		}
	}

	private void loadMonitorNodes(Element multiMonitorNode) {
		NodeList monitorNodes = multiMonitorNode.getElementsByTagName(JZRM_MONITOR);

		for (int i=0; i<monitorNodes.getLength(); i++){
			Element monitorNode = (Element)monitorNodes.item(i);
			ConfigMultiMonitorEntry entry = new ConfigMultiMonitorEntry(monitorNode);
			multiMonitors.add(entry);
		}
	}
	
}
