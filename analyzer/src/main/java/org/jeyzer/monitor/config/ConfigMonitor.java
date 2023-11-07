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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.ConfigTemplate;
import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.graph.ConfigContentionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigGraphPlayer;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.config.engine.ConfigMonitorRules;
import org.jeyzer.monitor.config.publisher.ConfigMailPublisher;
import org.jeyzer.monitor.config.publisher.ConfigPublisher;
import org.jeyzer.monitor.config.publisher.ConfigSoundPublisher;
import org.jeyzer.monitor.config.publisher.ConfigWebPublisher;
import org.jeyzer.monitor.config.publisher.jira.ConfigJiraPublisher;
import org.jeyzer.monitor.config.publisher.zabbix.ConfigZabbixPublisher;
import org.jeyzer.monitor.config.sticker.ConfigStickers;
import org.jeyzer.monitor.report.HTMLLogger.HTMLLoggerDefinition;
import org.jeyzer.monitor.report.MonitorLogger.MonitorLoggerDefinition;
import org.jeyzer.monitor.util.MonitorHelper;
import org.jeyzer.service.JzrServiceManager;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigMonitor {

	private static final Logger logger = LoggerFactory.getLogger(ConfigMonitor.class);
	
	// property
	public static final String MONITOR_CONFIG_FILE = "jeyzer.monitor.config";
		
	private static final String JZRM_MONITOR = "monitor";
	public static final String JZRM_NODE = "node";
	public static final String JZRM_OUTPUT_DIR = "output_directory";
	public static final String JZRM_SESSION_DIR = "session_directory";
	
	private static final String JZRM_ANALYSIS = "analysis";
	public static final String JZRM_ANALYSIS_REPORT_ENABLED = "analysis_report_enabled";
	public static final String JZRM_ANALYSIS_REPORT_PUBLISHERS = "report_publishers";
	public static final String JZRM_ANALYSIS_REPORT_THRESHOLD = "threshold";
		
	private static final String JZRM_SCANNING = "scanning";
	public static final String JZRM_MONITOR_SCAN_PERIOD = "scanning_period";
	public static final String JZRM_STARTUP_POINT_PERSISTED = "startup_point_persisted";
	public static final String JZRM_CLEAN_DUPLICATE_EVENTS = "clean_duplicate_events";
		
	private static final String JZRM_DISPLAY = "display";
	private static final String JZRM_DATE_FORMAT = "date_format";
	private static final String JZRM_DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
		
	public static final String JZRM_PUBLISHERS = "publishers";
	
	private static final String JZRM_GRAPH_SNAPSHOT = "graph_snapshot";
	public static final String JZRM_GRAPH_SNAPSHOT_ENABLED = "snapshot_enabled";
	public static final String JZRM_GRAPH_SNAPSHOT_PUBLISHERS = "snapshot_publishers";
	
	public static final String JZRM_LOGGERS = "loggers";
	private static final String JZRM_LOGGER = "logger";
	private static final String JZRM_LOGGER_ENABLED = "logger_enabled";
	private static final String JZRM_LOGGER_FORMAT = "format";
	private static final String JZRM_LOGGER_FILE = "file";
	
	public static final String JZRM_SOUND = "sound";	
	
	private Map<String, Object> config = new HashMap<>();
	
	private static String dateFormat;

	/*
	 * Jeyzer Monitor constructor
	 */
	public ConfigMonitor(ConfigAnalyzer analyzerConfig, JzrServiceManager serviceMgr) throws JzrInitializationException{
		String monitorFilePath = ConfigThreadLocal.get(MONITOR_CONFIG_FILE);
		if (monitorFilePath == null)
			monitorFilePath = System.getProperty(MONITOR_CONFIG_FILE);
		
		if (monitorFilePath==null || monitorFilePath.isEmpty()){
			throw new JzrInitializationException("Monitor configuration file path \"-D" +MONITOR_CONFIG_FILE+ "\" property missing on the command line. Please set it.");
		}
		
		// resolve any variable or system property (ex: ${user.dir})
		monitorFilePath = ConfigUtil.resolveValue(monitorFilePath);
		logger.info("Loading Monitor config file : " + SystemHelper.sanitizePathSeparators(monitorFilePath));
		
		// test that file exists
		File monitorFile = new File(monitorFilePath);
		if (!monitorFile.exists()){
			throw new JzrInitializationException("Monitor configuration file \"" +monitorFilePath.replace('\\', '/') + "\" not found. Please fix it.");
		}
		
		loadConfiguration(monitorFile, serviceMgr);
	}

	public Object getValue(String field){
		return this.config.get(field);
	}
		
	private void loadConfiguration(File file, JzrServiceManager serviceMgr) throws JzrInitializationException{
		
		try {
			// load the XML file
			Document doc = ConfigUtil.loadDOM(file);
			
			// monitor
			NodeList nodes = doc.getElementsByTagName(JZRM_MONITOR);
			Element monitorNode = (Element)nodes.item(0);
			
			// application
			config.put(JZRM_NODE,ConfigUtil.getAttributeValue(monitorNode, JZRM_NODE));
			
			// output directory
			config.put(JZRM_OUTPUT_DIR,ConfigUtil.getAttributeValue(monitorNode, JZRM_OUTPUT_DIR));

			// session directory
			config.put(JZRM_SESSION_DIR,ConfigUtil.getAttributeValue(monitorNode, JZRM_SESSION_DIR));
			
			// analysis
			Element analysisNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_ANALYSIS);
			
			// JZR report enabled
			config.put(JZRM_ANALYSIS_REPORT_ENABLED,ConfigUtil.getAttributeValue(analysisNode,JZRM_ANALYSIS_REPORT_ENABLED));
			
			// JZR report publishers
			config.put(JZRM_ANALYSIS_REPORT_PUBLISHERS, MonitorHelper.parseStrings(ConfigUtil.getAttributeValue(analysisNode,JZRM_PUBLISHERS)));
			
			// JZR report event threshold
			config.put(JZRM_ANALYSIS_REPORT_THRESHOLD,ConfigUtil.getAttributeValue(analysisNode,JZRM_ANALYSIS_REPORT_THRESHOLD));
			
			// scanning
			Element scanNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_SCANNING);
			
			// thread period
			config.put(JZRM_MONITOR_SCAN_PERIOD,ConfigUtil.getAttributeValue(scanNode,JZRM_MONITOR_SCAN_PERIOD));
			
			// startup point persisted
			config.put(JZRM_STARTUP_POINT_PERSISTED,ConfigUtil.getAttributeValue(scanNode,JZRM_STARTUP_POINT_PERSISTED));

			// graph player 
			setGraphPlayer(monitorNode);
			
			// display
			setDisplay(monitorNode);
			
			// rules
			setRules(monitorNode, serviceMgr.getResourcePathResolver());
			
			// stickers
			setStickers(monitorNode, serviceMgr.getResourcePathResolver());
		
			// loggers
			setLoggerDefs(monitorNode);
			
			// publishers
			setPublishers(monitorNode);
			
		} catch (Exception e) {
			throw new JzrInitializationException("Failed to load the monitor configuration : \"" +file.getAbsolutePath() + "\". Error is : " + e.getMessage(), e);
		}
		
	}

	private void setGraphPlayer(Element monitorNode) throws JzrInitializationException {
		Element graphSnapshotNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_GRAPH_SNAPSHOT);
		config.put(JZRM_GRAPH_SNAPSHOT_ENABLED, ConfigUtil.getAttributeValue(graphSnapshotNode,JZRM_GRAPH_SNAPSHOT_ENABLED));
		config.put(JZRM_GRAPH_SNAPSHOT_PUBLISHERS,MonitorHelper.parseStrings(ConfigUtil.getAttributeValue(graphSnapshotNode,JZRM_PUBLISHERS))); 
		
		Element functionGraphPlayerNode = ConfigUtil.getFirstChildNode(graphSnapshotNode, ConfigGraphPlayer.JZRA_FUNCTION_GRAPH_PLAYER);
		if (functionGraphPlayerNode != null){
			ConfigFunctionGraphPlayer functionGraphPlayer = new ConfigFunctionGraphPlayer(functionGraphPlayerNode, (String)config.get(JZRM_OUTPUT_DIR)); 
			this.config.put(ConfigGraphPlayer.JZRA_FUNCTION_GRAPH_PLAYER, functionGraphPlayer);
		}
		
		Element contentionGraphPlayerNode = ConfigUtil.getFirstChildNode(graphSnapshotNode, ConfigGraphPlayer.JZRA_CONTENTION_GRAPH_PLAYER);
		if (contentionGraphPlayerNode != null){
			ConfigContentionGraphPlayer contentionGraphPlayer = new ConfigContentionGraphPlayer(contentionGraphPlayerNode, (String)config.get(JZRM_OUTPUT_DIR)); 
			this.config.put(ConfigGraphPlayer.JZRA_CONTENTION_GRAPH_PLAYER, contentionGraphPlayer);
		}
	}

	private void setRules(Element monitorNode, JzrLocationResolver pathResolver) throws JzrInitializationException {
		Element rulesNode = ConfigUtil.getFirstChildNode(monitorNode, ConfigMonitorRules.JZRM_RULES);
		ConfigMonitorRules rules = new ConfigMonitorRules(rulesNode,pathResolver);
		this.config.put(ConfigMonitorRules.JZRM_RULES, rules);
	}
	
	private void setStickers(Element monitorNode, JzrLocationResolver pathResolver) throws JzrInitializationException {
		Element stickersNode = ConfigUtil.getFirstChildNode(monitorNode, ConfigStickers.JZRM_STICKERS);
		ConfigStickers stickers = new ConfigStickers(stickersNode,pathResolver);
		this.config.put(ConfigStickers.JZRM_STICKERS, stickers);
	}
	
	private void setDisplay(Element monitorNode) {
		Element displayNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_DISPLAY);
		ConfigMonitor.dateFormat = ConfigUtil.getAttributeValue(displayNode,JZRM_DATE_FORMAT);
	}
	
	private void setLoggerDefs(Element monitorNode){
		Element loggersNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_LOGGERS);
		
		NodeList loggerNodes = loggersNode.getElementsByTagName(JZRM_LOGGER);
		
		List<MonitorLoggerDefinition> loggerDefs = new ArrayList<>();
		config.put(JZRM_LOGGERS, loggerDefs);

		for (int i=0; i<loggerNodes.getLength(); i++){
			Element loggerNode = (Element)loggerNodes.item(i);
			
			boolean enabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(loggerNode,JZRM_LOGGER_ENABLED));
			if (!enabled)
				continue;
			
			String file = ConfigUtil.getAttributeValue(loggerNode,JZRM_LOGGER_FILE);
			String format = ConfigUtil.getAttributeValue(loggerNode,JZRM_LOGGER_FORMAT);
			List<String> publishers = MonitorHelper.parseStrings(ConfigUtil.getAttributeValue(loggerNode,JZRM_PUBLISHERS));
			String outputDir = (String)this.config.get(JZRM_OUTPUT_DIR);
			
			MonitorLoggerDefinition def;
			if (!MonitorLoggerDefinition.FORMAT_HTML.equals(format)){
				def = new MonitorLoggerDefinition(format, outputDir, file, publishers);
			}
			else{
				// HTML one
				Element templateNode = ConfigUtil.getFirstChildNode(loggerNode, ConfigTemplate.JZRA_CONTENT_TEMPLATE);
				def = new HTMLLoggerDefinition(format, outputDir, file, publishers, new ConfigTemplate(templateNode));
			}
			
			loggerDefs.add(def);
		}
	}

	private void setPublishers(Element monitorNode) throws JzrInitializationException, Exception {
		Element publishersNode = ConfigUtil.getFirstChildNode(monitorNode, JZRM_PUBLISHERS);
		
		NodeList publisherNodes = publishersNode.getChildNodes();
		
		List<ConfigPublisher> publisherDefs = new ArrayList<>();
		config.put(JZRM_PUBLISHERS, publisherDefs);

		for (int i=0; i<publisherNodes.getLength(); i++){
			if (!(publisherNodes.item(i) instanceof Element))
				continue;
			
			Element publisherNode = (Element)publisherNodes.item(i);
			ConfigPublisher publisherCfg = null;
			
			if (ConfigMailPublisher.NAME.equals(publisherNode.getNodeName()))
				publisherCfg = new ConfigMailPublisher(publisherNode);
			else if (ConfigSoundPublisher.NAME.equals(publisherNode.getNodeName()))
				publisherCfg = new ConfigSoundPublisher(publisherNode);
			else if (ConfigWebPublisher.NAME.equals(publisherNode.getNodeName()))
				publisherCfg = new ConfigWebPublisher(publisherNode);
			else if (ConfigJiraPublisher.NAME.equals(publisherNode.getNodeName()))
				publisherCfg = new ConfigJiraPublisher(publisherNode);
			else if (ConfigZabbixPublisher.NAME.equals(publisherNode.getNodeName()))
				publisherCfg = new ConfigZabbixPublisher(publisherNode);
			
			if (publisherCfg != null && publisherCfg.isEnabled())
				publisherDefs.add(publisherCfg);
		}
	}

	public static String getDateFormat(){
		if (dateFormat != null)
			return dateFormat;
		else
			return JZRM_DEFAULT_DATE_FORMAT;
	}

	public static void setDateFormat(String dateFormat){
		ConfigMonitor.dateFormat = dateFormat;
	}
	
}
