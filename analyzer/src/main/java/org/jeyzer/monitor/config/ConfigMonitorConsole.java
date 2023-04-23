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

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.graph.ConfigContentionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigGraphPlayer;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigMonitorConsole {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigMonitorConsole.class);	

	// property
	public static final String MONITOR_CONSOLE_CONFIG_FILE = "jeyzer.monitor.console.config";

	public static final String JZRM_MONITOR_CONSOLE = "monitor_console";
	public static final String JEYZER_OUTPUT_DIR = "output_directory";
	
	private ConfigFunctionGraphPlayer functionGraphPlayerCfg;
	private ConfigContentionGraphPlayer contentionGraphPlayerCfg;
	
	public ConfigMonitorConsole() throws JzrInitializationException{
		String monitorFilePath = System.getProperty(MONITOR_CONSOLE_CONFIG_FILE);
		
		if (monitorFilePath==null || monitorFilePath.isEmpty()){
			throw new JzrInitializationException("Monitor Console configuration file path \"-D" +MONITOR_CONSOLE_CONFIG_FILE+ "\" property missing on the command line. Please set it.");
		}
		
		// resolve any variable or system property (ex: ${user.dir})
		monitorFilePath = ConfigUtil.resolveValue(monitorFilePath);
		logger.info("Loading Console Monitor config file : " + SystemHelper.sanitizePathSeparators(monitorFilePath));
		
		// test that file exists
		File monitorFile = new File(monitorFilePath);
		if (!monitorFile.exists()){
			throw new JzrInitializationException("Monitor Console configuration file \"" +monitorFilePath.replace('\\', '/') + "\" not found. Please fix it.");
		}
		
		loadConfiguration(monitorFile);
	}

	private void loadConfiguration(File file) throws JzrInitializationException {
		
		// load the XML file
		Document doc = ConfigUtil.loadDOM(file);
		
		// monitor
		NodeList nodes = doc.getElementsByTagName(JZRM_MONITOR_CONSOLE);
		Element monitorConsoleNode = (Element)nodes.item(0);
		String outputDirectory = ConfigUtil.getAttributeValue(monitorConsoleNode, JEYZER_OUTPUT_DIR);
		
		Element functionGraphNode = ConfigUtil.getFirstChildNode(monitorConsoleNode, ConfigGraphPlayer.JZRA_FUNCTION_GRAPH_PLAYER);
		if (functionGraphNode != null)
			this.functionGraphPlayerCfg = new ConfigFunctionGraphPlayer(functionGraphNode, outputDirectory);
		
		Element contentionGraphNode = ConfigUtil.getFirstChildNode(monitorConsoleNode, ConfigGraphPlayer.JZRA_CONTENTION_GRAPH_PLAYER);
		if (contentionGraphNode != null)
			this.contentionGraphPlayerCfg = new ConfigContentionGraphPlayer(contentionGraphNode, outputDirectory);		
	}

	public ConfigFunctionGraphPlayer getFunctionGraphPlayerCfg() {
		return functionGraphPlayerCfg; // can be null
	}
	
	public ConfigContentionGraphPlayer getContentionGraphPlayerCfg() {
		return contentionGraphPlayerCfg; // can be null
	}
	
}
