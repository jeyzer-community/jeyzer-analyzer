package org.jeyzer.analyzer.config.setup;

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
import org.jeyzer.analyzer.config.analysis.ConfigStackSorting;
import org.jeyzer.analyzer.config.analysis.ConfigStackSorting.StackSortingKey;
import org.jeyzer.analyzer.config.report.security.ConfigSecurity;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.primitives.Ints;

public class ConfigSetupManager {

	private static final Logger logger = LoggerFactory.getLogger(ConfigSetupManager.class);	
	
	public static final int HEADERS_UNFREEZE_THRESHOLD_DEFAULT_VALUE = 8;
	public static final int ROW_HEADERS_UNFREEZE_THRESHOLD_DEFAULT_VALUE = 5;
	
	private static final String JZRA_DEFAULT_SETUP = "default_setup";
	private static final String JZRA_REPOSITORY_SETUPS = "repository_setups";
	private static final String JZRA_ADVANCED_JMX = "jmx_advanced";
	private static final String JZRA_GARBAGE_COLLECTORS = "garbage_collectors";
	private static final String JZRA_MEMORY_POOLS = "memory_pools";
	
	private static final String JZRA_THREAD_STACK = "thread_stack";
	private static final String JZRA_CPU_RUNNABLE = "cpu_runnable";

	private static final String JZRA_MONITOR = "monitor";
	
	private static final String JZRA_DISPLAY = "display";
	private static final String JZRA_THEME = "theme";
	
	private static final String JZRA_LOGO = "logo";
	private static final String JZRA_PICTURE = "picture";
	private static final String JZRA_ICON = "icon";
	private static final String JZRA_FILE = "file";
	
	private static final String JZRA_REPORTS = "reports";
	
	private static final String JZRA_XLSX_REPORT = "xlsx_report";
	private static final String JZRA_CHARTS = "charts";
	private static final String JZRA_SOFT_LINE = "soft_line";
	private static final String JZRA_DOT_STYLE = "dot_style";
	private static final String JZRA_COLUMNS = "columns";
	private static final String JZRA_GROUPING = "grouping";
	private static final String JZRA_HEADERS = "headers";
	private static final String JZRA_ROW_HEADERS = "row_headers";
	private static final String JZRA_HEADERS_UNFREEZE_THRESHOLD = "unfreeze_pane_threshold";
	private static final String JZRA_CELLS = "cells";
	private static final String JZRA_ACTION_LINK = "action_link";
	private static final String JZRA_OPTIMIZE_STACKS_THRESHOLD = "optimize_stacks_threshold";
	private static final String JZRA_DATE_LINK = "date_link";
	private static final String JZRA_HIATUS_OR_RESTART_LINK = "hiatus_or_restart_link";
	private static final String JZRA_ACTION_HIGHLIGHT = "action_highlight";
	
	private static final String JZRA_GRAPH = "graph";
	
	private ConfigRepositorySetup repoSetupCfg;
	private ConfigMonitorSetup monitorSetupCfg;
	private ConfigGarbageCollectorSetup gcSetupCfg;
	private ConfigPoolMemorySetup memPoolSetupCfg;
	private ConfigGraphSetup graphSetupCfg;
	private ConfigCPURunnableContentionTypesSetup cpuRunnableContentionTypesSetup;
	
	private ConfigSecurity reportSecurity;

	private String logoFilePath;
	private String iconFilePath;
	private String theme;
	
	private boolean softLineEnabled;
	private String dotStyle;
	private boolean columnGroupingEnabled;
	private int headerUnfreezePaneThreshold;
	private int rowHeaderUnfreezePaneThreshold;
	private boolean actionLinkEnabled;
	private boolean headerDateLinkEnabled;
	private boolean headerHiatusOrRestartLinkEnabled;
	private boolean headerActionLinkEnabled;
	private boolean headerActionHighlightEnabled;
	private int optimizeStacksThreshold;
	
	private StackSortingKey stackSortingKey;
	
	public ConfigSetupManager(File setupConfigFile) throws JzrInitializationException {
		if (!setupConfigFile.exists())
			throw new JzrInitializationException("File " + setupConfigFile.getPath() + " not found.");
		loadConfiguration(setupConfigFile);
	}		
	
	private void loadConfiguration(File file) throws JzrInitializationException{
		
		try {
			Document doc = ConfigUtil.loadDOM(file);
			
			// default setup
			NodeList nodes = doc.getElementsByTagName(JZRA_DEFAULT_SETUP);
			Element setupNode = (Element)nodes.item(0);
			
			Element repositorySetupsNode = ConfigUtil.getFirstChildNode(setupNode, JZRA_REPOSITORY_SETUPS);
			this.repoSetupCfg = new ConfigRepositorySetup(repositorySetupsNode);

			Element monitorSetupsNode = ConfigUtil.getFirstChildNode(setupNode, JZRA_MONITOR);
			this.monitorSetupCfg = new ConfigMonitorSetup(monitorSetupsNode);
						
			Element advancedJmxNode = ConfigUtil.getFirstChildNode(setupNode, JZRA_ADVANCED_JMX);
			
			Element gcNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZRA_GARBAGE_COLLECTORS);
			this.gcSetupCfg = new ConfigGarbageCollectorSetup(gcNode);
			
			Element memoryPoolNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZRA_MEMORY_POOLS);
			this.memPoolSetupCfg = new ConfigPoolMemorySetup(memoryPoolNode);
			
			Element displayNode = ConfigUtil.getFirstChildNode(setupNode, JZRA_DISPLAY);
			
			Element logoNode = ConfigUtil.getFirstChildNode(displayNode, JZRA_LOGO);
			Element pictureNode = ConfigUtil.getFirstChildNode(logoNode, JZRA_PICTURE);
			logoFilePath = ConfigUtil.getAttributeValue(pictureNode, JZRA_FILE);
			logoFilePath = ConfigUtil.resolveValue(logoFilePath); // resolve any variable
			
			Element iconNode = ConfigUtil.getFirstChildNode(logoNode, JZRA_ICON);
			iconFilePath = ConfigUtil.getAttributeValue(iconNode, JZRA_FILE);
			iconFilePath = ConfigUtil.resolveValue(iconFilePath); // resolve any variable
			
			theme = ConfigUtil.getAttributeValue(displayNode, JZRA_THEME);
			theme = ConfigUtil.resolveValue(theme); // resolve any variable
			
			Element reportsNode = ConfigUtil.getFirstChildNode(displayNode, JZRA_REPORTS);
			
			Element xlsxReportsNode = ConfigUtil.getFirstChildNode(reportsNode, JZRA_XLSX_REPORT);
			reportSecurity = new ConfigSecurity(xlsxReportsNode);
			Element chartsNode = ConfigUtil.getFirstChildNode(xlsxReportsNode, JZRA_CHARTS);
			softLineEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(chartsNode, JZRA_SOFT_LINE));
			dotStyle = ConfigUtil.getAttributeValue(chartsNode, JZRA_DOT_STYLE);
			dotStyle = ConfigUtil.resolveValue(dotStyle); 
			headerUnfreezePaneThreshold = loadHeaderUnfreezeThreshold(xlsxReportsNode, JZRA_HEADERS, HEADERS_UNFREEZE_THRESHOLD_DEFAULT_VALUE);
			rowHeaderUnfreezePaneThreshold = loadHeaderUnfreezeThreshold(xlsxReportsNode, JZRA_ROW_HEADERS, ROW_HEADERS_UNFREEZE_THRESHOLD_DEFAULT_VALUE);
			Element headersNode = ConfigUtil.getFirstChildNode(xlsxReportsNode, JZRA_HEADERS);
			headerDateLinkEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(headersNode, JZRA_DATE_LINK));
			headerHiatusOrRestartLinkEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(headersNode, JZRA_HIATUS_OR_RESTART_LINK));
			headerActionLinkEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(headersNode, JZRA_ACTION_LINK));
			headerActionHighlightEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(headersNode, JZRA_ACTION_HIGHLIGHT));
			
			Element columnsNode = ConfigUtil.getFirstChildNode(xlsxReportsNode, JZRA_COLUMNS);
			columnGroupingEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(columnsNode, JZRA_GROUPING));

			Element cellsNode = ConfigUtil.getFirstChildNode(xlsxReportsNode, JZRA_CELLS);
			actionLinkEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(cellsNode, JZRA_ACTION_LINK));
			optimizeStacksThreshold = loadOptimizeStacksThreshold(cellsNode, -1);
			
			Element graphNode = ConfigUtil.getFirstChildNode(reportsNode, JZRA_GRAPH);
			this.graphSetupCfg = new ConfigGraphSetup(graphNode);
			
			Element threadStackNode = ConfigUtil.getFirstChildNode(setupNode, JZRA_THREAD_STACK);
			Element cpuRunnableNode = ConfigUtil.getFirstChildNode(threadStackNode, JZRA_CPU_RUNNABLE);
			cpuRunnableContentionTypesSetup = new ConfigCPURunnableContentionTypesSetup(cpuRunnableNode);
			stackSortingKey = ConfigStackSorting.loadStackSortingKey(threadStackNode);
		} catch (Exception e) {
			logger.error("Failed to load the setup configuration.", e);
			throw new JzrInitializationException("Failed to load the setup configuration.", e);
		}
		
	}

	public ConfigMonitorSetup getMonitorSetupConfig() {
		return this.monitorSetupCfg;
	}	
	
	public ConfigRepositorySetup getRepositorySetupConfig() {
		return repoSetupCfg;
	}
	
	public ConfigGarbageCollectorSetup getGarbageCollectorSetupConfig() {
		return gcSetupCfg;
	}

	public ConfigPoolMemorySetup getMemoryPoolSetupConfig() {
		return memPoolSetupCfg;
	}

	public ConfigSecurity getReportSecurity() {
		return reportSecurity;
	}

	public String getLogoFilePath() {
		return logoFilePath;
	}
	
	public String getIconFilePath() {
		return iconFilePath;
	}

	public String getDefaultTheme() {
		return theme;
	}
	
	public boolean isGraphSoftLine(){
		return softLineEnabled;
	}
	
	public String getGraphDotStyle() {
		return dotStyle;
	}
	
	public int getHeaderUnfreezePaneThreshold() {
		return headerUnfreezePaneThreshold;
	}

	public int getRowHeaderUnfreezePaneThreshold() {
		return rowHeaderUnfreezePaneThreshold;
	}

	public boolean isColumnGroupingEnabled(){
		return columnGroupingEnabled;
	}
	
	public boolean isActionLinkEnabled(){
		return actionLinkEnabled;
	}
	
	public boolean isHeaderDateLinkEnabled(){
		return headerDateLinkEnabled;
	}
	
	public boolean isHeaderHiatusOrRestartLinkEnabled(){
		return 	headerHiatusOrRestartLinkEnabled;
	}
	
	public boolean isHeaderActionLinkEnabled(){
		return headerActionLinkEnabled;
	}
	
	public boolean isHeaderActionHighlightEnabled(){
		return headerActionHighlightEnabled;
	}
	
	public int getReportOptimizeStacksThreshold(){
		return optimizeStacksThreshold;
	}	

	public ConfigGraphSetup getGraphSetupCfg() {
		return graphSetupCfg;
	}
	
	public ConfigCPURunnableContentionTypesSetup getConfigCPURunnableContentionTypesSetup(){
		return cpuRunnableContentionTypesSetup;
	}
	
	private int loadHeaderUnfreezeThreshold(Element xlsxReportsNode, String rowHeaderType, int defaultValue) {
		Element rowHeaderNode = ConfigUtil.getFirstChildNode(xlsxReportsNode, rowHeaderType);
		if (rowHeaderNode == null)
			return defaultValue;
		
		String thresholdValue = ConfigUtil.getAttributeValue(rowHeaderNode, JZRA_HEADERS_UNFREEZE_THRESHOLD);
		
		if (thresholdValue != null && !thresholdValue.isEmpty()){
			try{
				return Integer.parseInt(thresholdValue);
			}catch(java.lang.NumberFormatException e){
				return defaultValue;
			}
		}
		
		return defaultValue;
	}
	
	private int loadOptimizeStacksThreshold(Element xlsxCellsNode, int defaultValue) {
		String thresholdValue = ConfigUtil.getAttributeValue(xlsxCellsNode, JZRA_OPTIMIZE_STACKS_THRESHOLD);
		Integer value;
		
		if (thresholdValue != null && !thresholdValue.isEmpty()){
			value = Ints.tryParse(thresholdValue);
			if (value == null){
				logger.error("Failed to parse the " + JZRA_OPTIMIZE_STACKS_THRESHOLD + " configuration parameter value : " + thresholdValue +  ". Optimization will be disabled");
				return defaultValue;
			}

			return value < -1 ? defaultValue : value;
		}
		
		return defaultValue;
	}

	public StackSortingKey getDefaultStackSortingKey() {
		return stackSortingKey;
	}
}
